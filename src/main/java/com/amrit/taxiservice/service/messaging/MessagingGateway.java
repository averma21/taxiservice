package com.amrit.taxiservice.service.messaging;

import com.amrit.taxiserviceapi.messaging.MessageRecord;
import com.amrit.taxiserviceapi.messaging.MessagingService;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.Queue;

@Service
public class MessagingGateway {
    
    private final MessagingService messagingService;

    public MessagingGateway() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.setProperty("group.id", this.getClass().getName());
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        this.messagingService = new MessagingService(props);
    }

    public void sendMessage(String topic, String key, byte[] value) {
        messagingService.sendMessage(topic, key, value);
    }

    public void subscribe(String topic, Queue<MessageRecord> queue) {
        messagingService.subscribe(topic, queue);
    }

}
