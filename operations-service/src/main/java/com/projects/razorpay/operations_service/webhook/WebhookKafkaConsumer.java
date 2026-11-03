package com.projects.razorpay.operations_service.webhook;

import com.projects.razorpay.common_lib.dto.WebhookTarget;
import com.projects.razorpay.common_lib.enums.WebHookEventStatus;
import com.projects.razorpay.common_lib.util.SignerUtil;
import com.projects.razorpay.operations_service.client.MerchantServiceClient;
import com.projects.razorpay.operations_service.entity.WebhookEvent;
import com.projects.razorpay.operations_service.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookKafkaConsumer {
    private final MerchantServiceClient merchantServiceClient;
    private final ObjectMapper objectMapper;
    private final SignerUtil signerUtil;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue retryQueue;
    private final WebhookDlqRecorder dlqRecorder;

    @KafkaListener(topics = {
            "${app.kafka.topics.payments:payments.events}",
            "${app.kafka.topics.orders:orders.events}",
            "${app.kafka.topics.refunds:refunds.events}",
            "${app.kafka.topics.settlements:settlements.events}",
    })
    public void onWebhookEvent(ConsumerRecord<String, Map<String,Object>> record,
                               Acknowledgment acknowledgement) {

        try {
            Map<String, Object> envelop = record.value();
            Map<String, Object> data = (Map<String, Object>) envelop.get("data");
            String eventType = (String) envelop.get("eventType");
            String aggregateType = (String) envelop.get("aggregateType");
            String aggregateId = (String) envelop.get("aggregateId");

            Object merchantIdRaw = data.get("merchantId");
            if(merchantIdRaw == null) {
                log.warn("No merchant id was found, skipping the event: {}", eventType);
                acknowledgement.acknowledge();
                return;
            }

            UUID merchantId = UUID.fromString(merchantIdRaw.toString());

            List<WebhookTarget> targets = merchantServiceClient.getActiveConfigsForEvent(merchantId, eventType);

            if(targets.isEmpty()) {
                acknowledgement.acknowledge();
                log.debug("No webhook target was found, skipping the event: {}", eventType);
                return;
            }

            Map<String, Object> signatureData = Map.of(
                    "event", eventType,
                    "payload", data
            );

            String signatureJson = objectMapper.writeValueAsString(signatureData);

            for(WebhookTarget target: targets) {
                String signature = signerUtil.sign(signatureJson, target.webhookSecret());

                WebhookEvent webhookEvent = WebhookEvent.builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(target.targetUrl())
                        .signature(signature)
                        .status(WebHookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();

                webhookEventRepository.save(webhookEvent);

                retryQueue.enqueue(webhookEvent.getId(), webhookEvent.getNextRetryAt());
                log.info("Created a webhook event with id: {}", webhookEvent.getId());
            }

            acknowledgement.acknowledge();
        } catch (DataAccessException  | CannotCreateTransactionException dbDown) {
            log.error("Webhook consumer failed due to db down, " +
                              "Could now process the record, offset:{}",  record.offset(), dbDown);

        } catch (Exception logicalError) {
            dlqRecorder.recordConsumerFailed(record, logicalError.getMessage());
            log.error("Logical error in the event, check dlq, offset: {}", record.offset(), logicalError);
            acknowledgement.acknowledge();
        }
    }
}
