package com.major.project.crypto.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.major.project.crypto.module.VideoUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    private final String bootStrapServer;

    private static final String VID_CRYPTO_TOPIC = "vid-crypto";
    private final String VID_CRPTO_GRP;


    public KafkaConfig(@Value("${kafka.bootstrap.server:}") String bootStrapServer) {
        this.bootStrapServer = bootStrapServer;
        this.VID_CRPTO_GRP = "vid-crypto-grp" +  UUID.randomUUID();
    }

    @Bean(value = "kafkaTemplate")
    public KafkaTemplate<String, VideoUtils> kafkaTemplate() {
        return new KafkaTemplate<>(customProducerFactory());
    }

    private ProducerFactory<String, VideoUtils> customProducerFactory() {
        final Map<String, Object> producerConfigProps = new HashMap<>();
        producerConfigProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);

        producerConfigProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfigProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerConfigProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 20971520); // Max file sized use

        return new DefaultKafkaProducerFactory<>(producerConfigProps);
    }

    @Bean
    public KafkaConsumer<String, VideoUtils> kafkaConsumer() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, VID_CRPTO_GRP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, VideoUtils.class.getName());
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 20971520);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 20971520);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.major.project.crypto");

        KafkaConsumer<String, VideoUtils> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(VID_CRYPTO_TOPIC));

        return consumer;
    }
}
