package com.affable.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.List;
import java.util.Properties;

public class RecordProducer {
    private static final String TOPIC = "influencers-analytics";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private Producer<Long, String> producer;

    public RecordProducer() {
        this.producer = createProducer();
    }

    private Producer<Long, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384 * 4);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "affable-analytics-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    public void sendMessages(List<String> messages) {
        try {
            messages.forEach(message -> {
                ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, System.currentTimeMillis(), message);
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
