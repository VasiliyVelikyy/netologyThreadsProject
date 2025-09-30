package ru.moskalev.demo.service.exproblem;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static ru.moskalev.demo.Constants.ITERATIONS_FOR_SPEED_TEST;

@Service
public class SpeedTestSyncAndAtomicService {
    private int syncCounter = 0;
    private final AtomicLong atomicCounter = new AtomicLong(0);
    private Object lock = new Object();


    public String processSpeedTestAtomic() {
        // Количество инкрементов на поток

        StringBuilder result = new StringBuilder();
        result.append("=== Simple Speed Test with 2 Threads ===\n\n");

        // Тестирование synchronized
        long syncTime = testSyncronized(ITERATIONS_FOR_SPEED_TEST);
        result.append("Synchronized Test:\n")
                .append("Increments per thread: ").append(ITERATIONS_FOR_SPEED_TEST).append("\n")
                .append("Final counter value: ").append(syncCounter).append("\n")
                .append("Expected value: ").append(2 * ITERATIONS_FOR_SPEED_TEST).append("\n")
                .append("Time taken: ").append(syncTime).append(" ms\n\n");

        // Тестирование Atomic
        long atomicTime = testAtomic(ITERATIONS_FOR_SPEED_TEST);
        result.append("Atomic Test:\n")
                .append("Increments per thread: ").append(ITERATIONS_FOR_SPEED_TEST).append("\n")
                .append("Final counter value: ").append(atomicCounter.get()).append("\n")
                .append("Expected value: ").append(2 * ITERATIONS_FOR_SPEED_TEST).append("\n")
                .append("Time taken: ").append(atomicTime).append(" ms\n\n");

        // Сравнение результатов
        result.append("=== Comparison ===\n")
                .append("Synchronized: ").append(syncTime).append(" ms\n")
                .append("Atomic: ").append(atomicTime).append(" ms\n");

        if (atomicTime > 0) {
            result.append("Atomic is ").append(String.format("%.2f", (double) syncTime / atomicTime))
                    .append(" times faster");
        }

        // Сброс счетчиков для следующего теста
        syncCounter = 0;
        atomicCounter.set(0);

        System.out.println(result);
        return result.toString();


    }

    private long testAtomic(int incremetAndThreads) {
        long startTime = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < incremetAndThreads; i++) {
                atomicIncremet();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < incremetAndThreads; i++) {
                atomicIncremet();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis() - startTime;
    }

    private long testSyncronized(int incremetAndThreads) {
        long startTime = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < incremetAndThreads; i++) {
                syncIncremet();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < incremetAndThreads; i++) {
                syncIncremet();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis() - startTime;
    }


    private synchronized void syncIncremet() {
        synchronized (lock) {
            syncCounter++;
        }
    }

    private void atomicIncremet() {
        atomicCounter.incrementAndGet();
    }
}
