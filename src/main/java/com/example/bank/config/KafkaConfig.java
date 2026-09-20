package com.example.bank.config;

import com.example.bank.event.*;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.core.KafkaAdmin;


import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Создаём топики при старте приложения
    @Bean
    public NewTopic transferCompletedTopic() {
        return TopicBuilder.name("bank.transfer.completed")
                .partitions(3)    // 3 партиции для параллельности
                .replicas(1)      // 1 реплика (достаточно для dev)
                .build();
    }

    @Bean
    public NewTopic accountCreatedTopic() {
        return TopicBuilder.name("bank.account.created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic accountBlockedTopic() {
        return TopicBuilder.name("bank.account.blocked")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // ConsumerFactory для TransferCompletedEvent
    @Bean
    public ConsumerFactory<String, TransferCompletedEvent>
    transferConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bank-notification-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<TransferCompletedEvent> deserializer =
                new JsonDeserializer<>(TransferCompletedEvent.class);
        deserializer.addTrustedPackages("com.example.bank.event");

        return new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransferCompletedEvent>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TransferCompletedEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transferConsumerFactory());
        factory.setConcurrency(3); // 3 треда = 3 партиции обрабатываются параллельно
        return factory;
    }
}