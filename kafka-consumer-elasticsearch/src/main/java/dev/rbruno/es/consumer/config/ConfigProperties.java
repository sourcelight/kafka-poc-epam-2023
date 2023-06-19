package dev.rbruno.es.consumer.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "kafka")
public record ConfigProperties(@NotEmpty String topic, @NotEmpty String groupId) {
        }

