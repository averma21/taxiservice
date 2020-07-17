package com.amrit.taxiservice.api;

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
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void addEmitter(final String regNo, final SseEmitter emitter) {
        emitters.putIfAbsent(regNo, emitter);
    }

    public void removeEmitter(final String regNo) {
        emitters.remove(regNo);
    }

    public void addNotification(Notification notification) {
        try {
            notificationQueue.put(notification);
        } catch (InterruptedException e) {
            e.printStackTrace(); // todo use logger
        }
    }

    public void doNotify() {
        boolean success = threadStarted.compareAndSet(false, true);
        if (success) {
            executorService.submit(() -> {
                try {
                    while (true) {
                        Notification notification = notificationQueue.take();
                        System.out.println("Got notification " + notification);
                        try {
                            emitters.get(notification.regNo).send(notification);
                        } catch (IOException e) {
                            emitters.remove(notification.regNo);
                        }
                        if (emitters.size() == 0) {
                            threadStarted.set(false);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    //todo log
                    e.printStackTrace();
                }
            });
        }
    }

}
