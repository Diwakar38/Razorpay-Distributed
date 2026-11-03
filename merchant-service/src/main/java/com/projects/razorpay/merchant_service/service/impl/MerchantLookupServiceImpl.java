package com.projects.razorpay.merchant_service.service.impl;

import com.projects.razorpay.common_lib.dto.SettlementBankDetails;
import com.projects.razorpay.common_lib.dto.WebhookTarget;
import com.projects.razorpay.common_lib.enums.MerchantStatus;
import com.projects.razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.projects.razorpay.merchant_service.api.MerchantLookupService;
import com.projects.razorpay.merchant_service.entity.Merchant;
import com.projects.razorpay.merchant_service.mapper.WebhookConfigMapper;
import com.projects.razorpay.merchant_service.repository.MerchantRepository;
import com.projects.razorpay.merchant_service.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantLookupServiceImpl implements MerchantLookupService {
    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor bytesEncryptor;
    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    public List<WebhookTarget> getActiveConfigForEvent(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream().filter(config -> config.isSubscibedTo(eventType)).map(config -> {
            byte[] cipherBytes = Base64.getDecoder().decode(config.getWebhookSecret());
            byte[] decryptedSecretBytes = bytesEncryptor.decrypt(cipherBytes);
            return new WebhookTarget(config.getId(), config.getTargetUrl(), new String(decryptedSecretBytes, StandardCharsets.UTF_8));
        }).toList();
    }

    @Override
    public List<UUID> listActiveMerchantIds() {
        return merchantRepository.findByMerchantStatus(MerchantStatus.ACTIVE).stream().map(Merchant::getId).toList();
    }

    @Override
    public SettlementBankDetails getSettlementBankDetails(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        return new SettlementBankDetails(
                merchant.getSettlementBankAccount(),
                merchant.getSettlementBankIfsc(),
                merchant.getSettlementBankAccountHolderName()
        );
    }
}
