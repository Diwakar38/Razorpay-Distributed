package com.projects.razorpay.common_lib.context;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class MerchantContext {
    private UUID merchantId;
    private String keyId;
}
