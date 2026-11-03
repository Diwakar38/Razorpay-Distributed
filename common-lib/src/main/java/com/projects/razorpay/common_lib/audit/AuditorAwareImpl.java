package com.projects.razorpay.common_lib.audit;

import com.projects.razorpay.common_lib.context.MerchantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    private final MerchantContext merchantContext;

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            String keyId = merchantContext.getKeyId();
            if (keyId != null && !keyId.isBlank())
                return Optional.of(keyId);

            if (merchantContext.getMerchantId() != null)
                return Optional.of("merchant_id" + merchantContext.getMerchantId());

        } catch (Exception ignore) {

        }
        return Optional.of("SYSTEM");
    }
}
