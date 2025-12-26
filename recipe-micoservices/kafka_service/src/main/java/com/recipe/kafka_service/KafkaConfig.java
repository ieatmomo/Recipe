package com.recipe.kafka_service;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.web.client.RestTemplate;


// Marks this class as a configuration class — Spring will automatically load it at startup


@Configuration

// Enables Kafka support in Spring (so you can use @KafkaListener, etc.)
@EnableKafka
public class KafkaConfig {

    // read from spring property (falls back to kafka:9092)
    @Value("${spring.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;

    // ---------------- PRODUCER CONFIGURATION ----------------

    // Defines how Kafka producers (message senders) should be created and configured
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // use injected bootstrap servers instead of hard-coded localhost
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serializer for the key — converts String keys into bytes before sending
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Serializer for the value — converts your Java objects (e.g., Recipe) into JSON
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Returns a DefaultKafkaProducerFactory with the above configurations
        return new DefaultKafkaProducerFactory<>(config);
    }

    // Creates a KafkaTemplate — this is what you’ll inject into your services to send messages
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    // ---------------- CONSUMER CONFIGURATION ----------------

    // Defines how Kafka consumers (message receivers) should be created and configured
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // use injected bootstrap servers instead of hard-coded localhost
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // The group ID — consumers in the same group share messages (like load balancing)
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "recipe-group");

        // Deserializer for the key — converts bytes back into String keys
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Deserializer for the value — converts JSON back into your Java object
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Allows deserializing any class (for safety you can restrict it later)
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Returns a DefaultKafkaConsumerFactory with the above configurations
        return new DefaultKafkaConsumerFactory<>(config);
    }

    // Creates the Kafka listener container — needed for @KafkaListener to work
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        // The container factory manages listener threads and message consumption
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // Assign the consumer factory so the listener knows how to consume messages
        factory.setConsumerFactory(consumerFactory());

        // Returns the fully configured listener factory
        return factory;
    }
}
