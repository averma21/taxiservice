package com.amrit.taxiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Service
public class TaxiServiceThreadFactory implements ThreadFactory {

    private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
    private static final UncaughtExceptionHandler handler = new UncaughtExceptionHandler();

    private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(UncaughtExceptionHandler.class.getName());

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.error("Uncaught exception in thread {}", t.getId() + "=" + t.getName(),  e);
        }
    }

    @Override
    public Thread newThread(Runnable run) {
        Thread thread = defaultFactory.newThread(run);
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }
}
