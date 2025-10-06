package ru.moskalev.demo.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class SmsNotificatorService {
    private final Map<String, ReentrantLock> clientLock = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, Queue<String>> pendingSmsMap = new ConcurrentHashMap<>();

    private static final long RETRY_DELAY_SECONDS = 2;
    private static final long LOCK_TIMEOUT_MS = 500;


    public void trySendSms(String phoneNumber, String message) {
        Objects.requireNonNull(phoneNumber, "phoneNumber must not be null");
        Objects.requireNonNull(message, "message must not be null");

        var smsLock = clientLock.computeIfAbsent(phoneNumber, k -> new ReentrantLock());
        boolean lockAcquired = false;

        try {
            lockAcquired = smsLock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (lockAcquired) {
                sendSmsNow(phoneNumber, message);
                scheduledPendingSmsIfAny(phoneNumber);
            } else {
                pendingSmsMap.computeIfAbsent(phoneNumber, k -> new ConcurrentLinkedQueue<>()).add(message);
                log.info("[SMS] Отложено message {} для номера {}", message, phoneNumber);

                scheduler.schedule(() -> processPendingSms(phoneNumber), RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        } finally {
            if (lockAcquired) {
                smsLock.unlock();
            }
        }
    }

    private void processPendingSms(String phoneNumber) {
        Queue<String> queue = pendingSmsMap.get(phoneNumber);
        if (queue == null && queue.isEmpty()) {
            return;
        }
        trySendSms(phoneNumber, queue.poll());
    }

    private void scheduledPendingSmsIfAny(String phoneNumber) {
        Queue<String> queue = pendingSmsMap.get(phoneNumber);
        if (queue != null && !queue.isEmpty()) {
            scheduler.schedule(() -> processPendingSms(phoneNumber), RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void sendSmsNow(String phoneNumber, String message) throws InterruptedException {
        log.info("SMS {} Отправляем на {}", message, phoneNumber);
        Thread.sleep(2000);
        System.out.println("ok");
    }


    public void trySendSms(long newBalance, long lastBalanceKnow) {
        System.out.println("Баланс изменился newBalance= " + newBalance + " lastBalanceKnow= " + lastBalanceKnow);
    }

    public void trySendSms(long newBalance) {
        System.out.println("Баланс изменился newBalance= " + newBalance);
    }

    public void awaitAllScheduledTask(long timeOutSecond) throws InterruptedException {
        scheduler.shutdown();
        if (!scheduler.awaitTermination(timeOutSecond, TimeUnit.SECONDS)) {
            scheduler.shutdownNow();
        }
    }
}
