package com.projects.razorpay.vault_service.service.impl;

import com.projects.razorpay.common_lib.dto.PaymentProcessRequest;
import com.projects.razorpay.common_lib.dto.PaymentProcessResponse;
import com.projects.razorpay.common_lib.entity.Money;
import com.projects.razorpay.common_lib.enums.CardBrand;
import com.projects.razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.projects.razorpay.common_lib.util.RandomizerUtil;
import com.projects.razorpay.vault_service.config.VaultEncryptionConfig;
import com.projects.razorpay.vault_service.dto.request.TokenizeRequest;
import com.projects.razorpay.vault_service.dto.response.TokenizeResponse;
import com.projects.razorpay.vault_service.entity.CardToken;
import com.projects.razorpay.vault_service.entity.VaultCard;
import com.projects.razorpay.vault_service.processor.CardPaymentProcessor;
import com.projects.razorpay.vault_service.repository.CardTokenRepository;
import com.projects.razorpay.vault_service.repository.VaultCardRepository;
import com.projects.razorpay.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {
    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor bytesEncryptor;
    private final CardPaymentProcessor paymentProcessor;

    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId) {
        String lastFour = request.pan().substring(request.pan().length() - 4);
        String bin = request.pan().substring(0,6);
        CardBrand cardBrand = detectBrand(request.pan());

        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptionConfig.panEncrypter(dek)
                .encrypt(request.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek = bytesEncryptor.encrypt(dek);

        VaultCard vaultCard = vaultCardRepository.save(VaultCard.builder()
                .brand(cardBrand)
                .bin(bin)
                .encryptedDek(encryptedDek)
                .encryptedPan(encryptedPan)
                .lastFour(lastFour)
                .expiryYear(request.expiryYear().toString())
                .expiryMonth(request.expiryMonth().toString())
                .cardHolderName(request.cardHolderName())
                .build());

        String token = "tok_" + RandomizerUtil.randomBase64(32);

        cardTokenRepository.save(
                CardToken.builder()
                        .vaultCard(vaultCard)
                        .customer(request.customerId())
                        .merchant(merchantId)
                        .token(token)
                        .build());

        return new TokenizeResponse(token, lastFour, cardBrand, request.expiryMonth(), request.expiryYear());
    }

    @Override
    public PaymentProcessResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        byte[] panBytes = null;
        try {
            CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                    .orElseThrow(() -> new ResourceNotFoundException("cardToken", token));

            VaultCard vaultCard = cardToken.getVaultCard();
            byte[] dekBytes = bytesEncryptor.decrypt(vaultCard.getEncryptedDek());
            panBytes = VaultEncryptionConfig.panEncrypter(dekBytes).decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessRequest paymentProcessRequest = PaymentProcessRequest.card(
                    paymentId, pan, expiry, amount, methodDetails
            );

            PaymentProcessResponse response = paymentProcessor.charge(paymentProcessRequest).get(5, TimeUnit.SECONDS);

            log.info("Vault charge registered, token: {}****", token.substring(0, 4));
            return response;
        } catch (Exception e) {
            log.warn("Vault charge failed, token: {}****", token.substring(0, 4));
            return new PaymentProcessResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        } finally {
            if(panBytes != null) Arrays.fill(panBytes, (byte) 0);
        }
    }

    private CardBrand detectBrand(String pan) {
        if(pan.startsWith("4")) return CardBrand.VISA;
        if(pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if(pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
