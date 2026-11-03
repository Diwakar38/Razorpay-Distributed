package com.projects.razorpay.merchant_service.api;


import com.projects.razorpay.common_lib.dto.SettlementBankDetails;
import com.projects.razorpay.common_lib.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {
    List<WebhookTarget> getActiveConfigForEvent(UUID merchantId, String eventType);

    List<UUID> listActiveMerchantIds();

    SettlementBankDetails getSettlementBankDetails(UUID merchantId);
}
