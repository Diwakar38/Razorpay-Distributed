package com.projects.razorpay.operations_service.settlement;

import com.projects.razorpay.common_lib.entity.Money;
import com.projects.razorpay.common_lib.util.RandomizerUtil;
import com.projects.razorpay.operations_service.settlement.dto.BankTransferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class BankTransferProcessorImpl implements BankTransferProcessor{

    @Override
    public BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc) {
        // Call the bank api

        String registrationRef = "TXN_" + RandomizerUtil.randomBase64(12);

        log.debug("Bank transfer call completed for settlementId: {}, registrationRef: {}", settlementId, registrationRef);

        return new BankTransferResult(registrationRef);
    }
}
