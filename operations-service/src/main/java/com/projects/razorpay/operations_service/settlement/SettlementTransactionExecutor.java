package com.projects.razorpay.operations_service.settlement;

import com.projects.razorpay.common_lib.dto.PaymentSettlementView;
import com.projects.razorpay.common_lib.dto.SettlementBankDetails;
import com.projects.razorpay.common_lib.entity.Money;
import com.projects.razorpay.common_lib.enums.EventAggregateType;
import com.projects.razorpay.common_lib.enums.SettlementStatus;
import com.projects.razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.projects.razorpay.operations_service.entity.Settlement;
import com.projects.razorpay.operations_service.entity.SettlementPayment;
import com.projects.razorpay.operations_service.entity.SettlementPaymentId;
import com.projects.razorpay.operations_service.outbox.OutboxEventPublisher;
import com.projects.razorpay.operations_service.repository.SettlementPaymentRepository;
import com.projects.razorpay.operations_service.repository.SettlementRepository;
import com.projects.razorpay.operations_service.settlement.dto.BankTransferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementTransactionExecutor {
    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final BankTransferProcessor bankTransferProcessor;
    private final OutboxEventPublisher outboxEventPublisher;
    private final SettlementIntegrationGateway settlementIntegrationGateway;

    private static final double FEE_RATE = 0.02;
    private static final double GST_RATE = 0.18;

    @Transactional
    public void processForMerchant(UUID merchantId, LocalDate settlementDate) {

        // Finds all the unsettled payments
//        List<PaymentSettlementView> unsettledPayments = paymentServiceClient.findUnsettledCapturedPayments(merchantId);
        List<PaymentSettlementView> unsettledPayments = settlementIntegrationGateway.findUnsettledCapturedPayments(merchantId);

        if(unsettledPayments.isEmpty()) return;

        log.info("Processing {} unsettled payments for merchantId: {} on {}", unsettledPayments.size(), merchantId, settlementDate);

         Integer grossAmount = unsettledPayments.stream()
                .map(PaymentSettlementView::amountUnits)
                .reduce(Integer::sum)
                .orElse(0);

         Money gross = Money.of(grossAmount, unsettledPayments.getFirst().currency());

        int fee = Math.toIntExact(Math.round(gross.getAmountUnits() * FEE_RATE));
        int gst = Math.toIntExact(Math.round(gross.getAmountUnits() * GST_RATE));

        Money feeAmount = Money.of(fee, gross.getCurrency());
        Money gstAmount = Money.of(gst, gross.getCurrency());
        Money netAmount = gross.subtract(feeAmount).subtract(gstAmount);

        // Creates the settlement object for the day for this merchant
        Settlement settlement = Settlement.builder()
                .merchantId(merchantId)
                .grossAmount(gross)
                .gstAmount(gstAmount)
                .feeAmount(feeAmount)
                .netAmount(netAmount)
                .status(SettlementStatus.INITIATED)
                .processedAt(LocalDateTime.now())
                .build();

        settlementRepository.save(settlement);

        try {
            List<SettlementPayment> links = new ArrayList<>();
            for (PaymentSettlementView p : unsettledPayments) {
                // Goes over each unsettled payment and creates a list of SettlementPayment
                links.add(SettlementPayment
                                  .builder()
                                  .id(new SettlementPaymentId(settlement.getId(), p.paymentId()))
                                  .settlement(settlement)
                                  .build());
            }

            settlementPaymentRepository.saveAll(links);


            // Get the merchant's settlement bank details
//            SettlementBankDetails settlementBankDetails = merchantServiceClient.getSettlementBankDetails(merchantId);
            SettlementBankDetails settlementBankDetails = settlementIntegrationGateway.getSettlementBankDetails(merchantId);

            // Call the banktransferservice to transfer the netamount to merchant's settlement bank details
            BankTransferResult bankTransferResult = bankTransferProcessor.initiate(settlement.getId(),
                                                                                   merchantId,
                                                                                   netAmount,
                                                                                   settlementBankDetails.accountNumber(),
                                                                                   settlementBankDetails.ifsc());

            settlement.setStatus(SettlementStatus.TRANSFER_PENDING);
            settlement.setBankReference(bankTransferResult.registrationRef());
            settlementRepository.save(settlement);
        } catch (Exception e) {
            log.error("Settlement failed for settlementId:{} on {} date", settlement.getId(), settlementDate, e);
            settlement.setStatus(SettlementStatus.FAILED);
            settlementRepository.save(settlement);
        }
    }

    @Transactional
    public void resolveTransfer(UUID settlementId, String errorCode, String errorDescription) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", settlementId));

        if(settlement.getStatus() != SettlementStatus.TRANSFER_PENDING) {
            log.info("Settlement resolved for settlementId: {}, so skiping", settlement.getId());
            return;
        }

        if(errorCode == null) {
            settlement.setStatus(SettlementStatus.PROCESSED);
            settlement.setProcessedAt(LocalDateTime.now());
            settlementRepository.save(settlement);

            List<SettlementPayment> settlementPaymentList = settlementRepository.findBySettlement(settlement);
            List<UUID> paymentIds = settlementPaymentList.stream()
                    .map(SettlementPayment::getId)
                    .map(SettlementPaymentId::getPaymentId)
                    .toList();
            settlementIntegrationGateway.markSettled(paymentIds);


            log.info("Settlement processed successfully, settlementId: {}", settlement.getId());
            outboxEventPublisher.publish(EventAggregateType.SETTLEMENTS, settlement.getId(), "SETTLEMENT_PROCESSED",
                                         Map.of(
                                                 "settlementId", settlement.getId(),
                                                 "merchantId", settlement.getMerchantId(),
                                                 "status", settlement.getStatus().name(),
                                                 "settlementAmount", settlement.getNetAmount().getAmountUnits(),
                                                 "settlementCurrency", settlement.getNetAmount().getCurrency()
                                                 ));
        } else {
            settlement.setStatus(SettlementStatus.FAILED);
            settlement.setFailureReason(errorCode + " : " +errorDescription);
            settlementRepository.save(settlement);
            log.warn("Settlement failed, settlementId: {}", settlement.getId());
            outboxEventPublisher.publish(EventAggregateType.SETTLEMENTS, settlement.getId(), "SETTLEMENT_FAILED",
                                         Map.of(
                                                 "settlementId", settlement.getId(),
                                                 "merchantId", settlement.getMerchantId(),
                                                 "status", settlement.getStatus().name(),
                                                 "settlementAmount", settlement.getNetAmount().getAmountUnits(),
                                                 "settlementCurrency", settlement.getNetAmount().getCurrency()
                                         ));
        }
    }
}
