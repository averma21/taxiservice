package com.amrit.taxiservice.api;

import com.amrit.taxiservice.TaxiServiceThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@EnableScheduling
public class SsePushNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsePushNotificationService.class.getName());

    public static class Notification {
        private final String regNo;
        private final double latitude;
        private final double longitude;

        public Notification(String regNo, double latitude, double longitude) {
            this.regNo = regNo;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getRegNo() {
            return regNo;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
    
    private final Map<String, BlockingQueue<Notification>> notificationQueues = new ConcurrentHashMap<>();

    @Autowired
    public SsePushNotificationService(TaxiServiceThreadFactory threadFactory) {
        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    private final ExecutorService executorService;

    public void addNotification(Notification notification) {
        notificationQueues.putIfAbsent(notification.regNo, new LinkedBlockingQueue<>());
        notificationQueues.computeIfPresent(notification.regNo, (reg, queue) -> {
            try {
                queue.put(notification);
            } catch (InterruptedException e) {
                LOGGER.error("Could not send notification for vehicle {}", notification.regNo, e);
            }
            return queue;
        });
    }

    public void doNotify(String regNo, SseEmitter emitter) {
        executorService.submit(() -> {
            try {
                BlockingQueue<Notification> queue = notificationQueues.get(regNo);
                if (queue == null) {
                    LOGGER.error("No notification queue present for {}", regNo);
                    return;
                }
                while (!queue.isEmpty()) {
                    Notification notification = queue.take();
                    LOGGER.debug("Got notification for cab {}", notification.regNo);
                    try {
                        emitter.send(notification);
                    } catch (IOException e) {
                        LOGGER.error("Could not send notification for cab {}", notification.regNo, e);
                    }
                }
                LOGGER.debug("Sent all received notifications for {}", regNo);
            } catch (InterruptedException e) {
                LOGGER.error("Error receiving notification from queue", e);
            } finally {
                emitter.complete();
            }
        });

    }

}
