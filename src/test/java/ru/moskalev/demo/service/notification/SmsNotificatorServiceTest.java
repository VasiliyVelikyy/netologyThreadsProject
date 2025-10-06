package ru.moskalev.demo.service.notification;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@SpringBootTest
class SmsNotificatorServiceTest {
    private SmsNotificatorService smsNotificatorService;

    @BeforeEach
    void setUp() {
        smsNotificatorService = new SmsNotificatorService();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        smsNotificatorService.awaitAllScheduledTask(10);
    }

    @Test
    public void fourAccount_smsNotify_threadExample() throws InterruptedException {
        String phoneNumber = "+79991112233";
        String message = "Тестовое сообщение";
        int threadCount = 4;


        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(4);

        for (int i = 1; i <= threadCount; i++) {
            final int msgId = i;
            executorService.submit(() -> {
                try {
                    smsNotificatorService.trySendSms(phoneNumber, message + " #" + msgId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);

        Thread.sleep(10000);

        smsNotificatorService.awaitAllScheduledTask(10);

    }

}