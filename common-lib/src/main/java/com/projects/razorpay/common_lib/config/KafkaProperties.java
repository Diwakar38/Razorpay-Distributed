package com.projects.razorpay.common_lib.config;

import com.projects.razorpay.common_lib.enums.EventAggregateType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaProperties {

    private Map<String, String> topics;

    public String topicFor(EventAggregateType type) {
        String topic = topics.get(type.name().toLowerCase());

        if (topic == null) {
            throw new IllegalStateException(String.format("No topic found for type %s", type.name().toLowerCase()));
        }
        return topic;
    }
}
