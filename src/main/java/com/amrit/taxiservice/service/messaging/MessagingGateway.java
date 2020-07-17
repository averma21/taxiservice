package com.amrit.taxiservice.service.messaging;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

@Service
public class MessagingGateway implements Closeable {

    Producer<String, byte[]> producer;

    public MessagingGateway() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String key, byte[] value) {
        producer.send(new ProducerRecord<String, byte[]>(topic, key, value));
    }

    @Override
    public void close() throws IOException {
        producer.close();
    }
}
