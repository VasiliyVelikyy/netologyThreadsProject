package ru.moskalev.demo.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class SmsNotificatorService {
    private final ScheduledExecutorService scheduler = createSchedulerExecutor();

    private final Map<String, Queue<String>> pendingSmsMap = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> clientLock = new ConcurrentHashMap<>();
    private final Set<String> scheduledNumbers = ConcurrentHashMap.newKeySet();


    private static final long RETRY_DELAY_SECONDS = 2;
    private static final long LOCK_TIMEOUT_MS = 500;

    private volatile long simulatedSendDelayMs = 0;


    public void trySendSms(String phoneNumber, String message) {
        Objects.requireNonNull(phoneNumber, "phoneNumber must not be null");
        Objects.requireNonNull(message, "message must not be null");

        var smsLock = clientLock.computeIfAbsent(phoneNumber, k -> new ReentrantLock());
        boolean lockAcquired = false;

        try {
            lockAcquired = smsLock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (lockAcquired) {
                sendSmsNow(phoneNumber, message);
                drainPendingQueue(phoneNumber);
            } else {
                addInQueue(phoneNumber, message);

                if (scheduledNumbers.add(phoneNumber)) {
                    scheduler.schedule(() -> {
                        scheduledNumbers.remove(phoneNumber);
                        processPendingSms(phoneNumber);
                    }, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
                }
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

    private void addInQueue(String phoneNumber, String message) {
        ReentrantLock lock = clientLock.computeIfAbsent(phoneNumber, k -> new ReentrantLock());
        lock.lock();
        try {
            Queue<String> queue = pendingSmsMap.computeIfAbsent(phoneNumber, k -> new ConcurrentLinkedQueue<>());
            if (!queue.contains(message)) {
                queue.add(message);
                log.info("[SMS] Отложено message {} для номера {}", message, phoneNumber);
            } else {
                log.info("[SMS] Пропущен дубликат message {} для номера {}", message, phoneNumber);
            }
        } finally {
            lock.unlock();
        }

    }

    private void processPendingSms(String phoneNumber) {
        ReentrantLock lock = clientLock.get(phoneNumber);
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!locked) {
                if (scheduledNumbers.add(phoneNumber)) {
                    scheduler.schedule(() -> {
                        scheduledNumbers.remove(phoneNumber);
                        processPendingSms(phoneNumber);
                    }, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
                }
                return;
            }
            Queue<String> queue = pendingSmsMap.get(phoneNumber);
            if (queue != null) {
                while (!queue.isEmpty()) {
                    String msg = queue.poll();
                    if (msg != null) {
                        sendSmsNow(phoneNumber, msg);
                    }
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    private void drainPendingQueue(String phoneNumber) {
        Queue<String> queue = pendingSmsMap.get(phoneNumber);
        if (queue != null) {
            while (!queue.isEmpty()) {
                String msg = queue.poll();
                if (msg != null) {
                    sendSmsNow(phoneNumber, msg);
                }
            }
        }
    }

    private void sendSmsNow(String phoneNumber, String message) {
        if (simulatedSendDelayMs > 0) {
            try {
                Thread.sleep(simulatedSendDelayMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("[SMS] Успешно отправлено на номер={} сообщение ={}", phoneNumber, message);
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

    private ScheduledExecutorService createSchedulerExecutor() {
        return Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "smsSender-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void setSimulatedSendDelay(int i) {
        this.simulatedSendDelayMs = i;
    }
}
