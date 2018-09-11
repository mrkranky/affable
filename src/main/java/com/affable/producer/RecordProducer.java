package com.affable.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class RecordProducer {
    private static final String TOPIC = "influencers-analytics";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private Producer<String, String> producer;

    public RecordProducer() {
        this.producer = createProducer();
    }

    private Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384 * 4);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "affable-analytics-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    public void sendMessages(Map<String, String> messages) {
        try {
            messages.forEach((pk, message) -> {
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, pk, message);
                producer.send(record, (metadata, exception) -> {
                    if (metadata == null) {
                        exception.printStackTrace();
                    }
                });
            });
        } finally {
            producer.flush();
        }
    }
}
