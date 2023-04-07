package com.ontop.balance.infrastructure.configs;

import com.ontop.kernels.ChargebackMessage;
import com.ontop.kernels.ParentMessage;
import com.ontop.kernels.PaymentMessage;
import com.ontop.kernels.WalletMessage;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    private Map<String, Object> producerConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return configProps;
    }

    @Bean("walletProducer")
    public KafkaTemplate<String, WalletMessage> walletProducer() {
        DefaultKafkaProducerFactory<String, WalletMessage> factory = new DefaultKafkaProducerFactory<>(
                producerConfig(), new StringSerializer(), new JsonSerializer<>());
        return new KafkaTemplate<>(factory);
    }

    @Bean("paymentProducer")
    public KafkaTemplate<String, PaymentMessage> paymentProducer() {
        DefaultKafkaProducerFactory<String, PaymentMessage> factory = new DefaultKafkaProducerFactory<>(
                producerConfig(), new StringSerializer(), new JsonSerializer<>());
        return new KafkaTemplate<>(factory);
    }

    @Bean("chargebackProducer")
    public KafkaTemplate<String, ChargebackMessage> chargebackProducer() {
        DefaultKafkaProducerFactory<String, ChargebackMessage> factory = new DefaultKafkaProducerFactory<>(
                producerConfig(), new StringSerializer(), new JsonSerializer<>());
        return new KafkaTemplate<>(factory);
    }

    private ConsumerFactory<String, ParentMessage> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(),
                new JsonDeserializer<>(ParentMessage.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ParentMessage> listenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ParentMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
