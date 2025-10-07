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
        String phoneNumber1 = "+79991112233";
        String phoneNumber2 = "+78009998811";
        String message = "Тестовое сообщение";
        int threadCount = 4;

        smsNotificatorService.setSimulatedSendDelay(2000);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(8);

        for (int i = 1; i <= threadCount; i++) {
            final int msgId = i;
            executorService.submit(() -> {
                try {
                    smsNotificatorService.trySendSms(phoneNumber1, message + " #" + msgId);
                } finally {
                    latch.countDown();
                }
            });
        }

        for (int i = 1; i <= threadCount; i++) {
            final int msgId = i;
            executorService.submit(() -> {
                try {
                    smsNotificatorService.trySendSms(phoneNumber2, message + " #" + msgId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(8, TimeUnit.SECONDS);

        Thread.sleep(10000);

        smsNotificatorService.awaitAllScheduledTask(10);

    }

}