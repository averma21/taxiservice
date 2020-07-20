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
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

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

    final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final BlockingQueue<Notification> notificationQueue = new LinkedBlockingDeque<>();
    private final AtomicBoolean threadStarted = new AtomicBoolean(false);

    @Autowired
    public SsePushNotificationService(TaxiServiceThreadFactory threadFactory) {
        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    private final ExecutorService executorService;

    public void addEmitter(final String regNo, final SseEmitter emitter) {
        emitters.put(regNo, emitter);
    }

    public void removeEmitter(final String regNo) {
        emitters.remove(regNo);
    }

    public void addNotification(Notification notification) {
        try {
            notificationQueue.put(notification);
        } catch (InterruptedException e) {
            LOGGER.error("Could not send notification for vehicle {}", notification.regNo, e);
        }
    }

    public void doNotify() {
        boolean success = threadStarted.compareAndSet(false, true);
        if (success) {
            executorService.submit(() -> {
                try {
                    while (true) {
                        Notification notification = notificationQueue.take();
                        LOGGER.debug("Got notification for cab {}", notification.regNo);
                        try {
                            SseEmitter emitter = emitters.get(notification.regNo);
                            emitter.send(notification);
                            emitter.complete();
                        } catch (IOException e) {
                            LOGGER.error("Could not send notification for cab {}", notification.regNo, e);
                        } finally {
                            emitters.remove(notification.regNo);
                        }
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Error receiving notification from queue", e);
                }
            });
        }
    }

}
