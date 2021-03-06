package com.ynthm.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Author : Ynthm
 * 配置生产者
 * producerConfigs
 * producerFactory
 * KafkaTemplate
 * 配置消费者
 * consumerConfigs
 * consumerFactory
 * kafkaListenerContainerFactory
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${kafka.producer.bootstrapServers}")
    private String producerBootstrapServers; //生产者连接Server地址

    @Value("${kafka.producer.retries}")
    private String producerRetries; //生产者重试次数

    @Value("${kafka.producer.batchSize}")
    private String producerBatchSize;

    @Value("${kafka.producer.lingerMs}")
    private String producerLingerMs;

    @Value("${kafka.producer.bufferMemory}")
    private String producerBufferMemory;


    @Value("${kafka.consumer.bootstrapServers}")
    private String consumerBootstrapServers;

    @Value("${kafka.consumer.groupId}")
    private String consumerGroupId;

    @Value("${kafka.consumer.enableAutoCommit}")
    private String consumerEnableAutoCommit;

    @Value("${kafka.consumer.autoCommitIntervalMs}")
    private String consumerAutoCommitIntervalMs;

    @Value("${kafka.consumer.sessionTimeoutMs}")
    private String consumerSessionTimeoutMs;

    @Value("${kafka.consumer.maxPollRecords}")
    private String consumerMaxPollRecords;

    @Value("${kafka.consumer.autoOffsetReset}")
    private String consumerAutoOffsetReset;

    @Bean
    public ObjectMapper ObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 4096);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 40960);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    /**
     * ConsumerFactory
     *
     * @return
     */
    @Bean
    public ConsumerFactory<Object, Object> consumerFactory() {
        Map<String, Object> configs = new HashMap<>(); //参数
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerEnableAutoCommit);
        configs.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, consumerAutoCommitIntervalMs);
        configs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, consumerSessionTimeoutMs);
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerMaxPollRecords); //批量消费数量
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * 添加KafkaListenerContainerFactory，用于批量消费消息
     *
     * @return
     */
    @Bean
    public KafkaListenerContainerFactory<?> batchContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory());
        // 多线程消费
        containerFactory.setConcurrency(4);
        containerFactory.setBatchListener(true); //批量消费
        // 手工管理 Offset  ConsumerConfig  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return containerFactory;
    }


    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    @Qualifier("customKafkaTemplate")
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}