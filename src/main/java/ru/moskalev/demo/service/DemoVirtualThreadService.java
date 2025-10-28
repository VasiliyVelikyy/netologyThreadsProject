package ru.moskalev.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;

import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Service
@Slf4j
public class DemoVirtualThreadService {
    private final int TASKS = 10_000;

    public String demo() {
//        log.info("Запуск с обычными потоками (ограниченный пул)");
//        startCommonThreads();

        log.info("Запуск виртуальных потоков");
        startVirtualThreads();
        return "ok";
    }


    private void startCommonThreads() {
        long start = System.nanoTime();
        try (var executor = Executors.newFixedThreadPool(200)) {
            for (int i = 0; i < TASKS; i++) {
                final int id = i;
                executor.submit(() -> simulateWork(id));
            }
        }
        log.info("Обычные потоки завершили выполнение");
        evaluateExecutionTime(start);
    }

    private void startVirtualThreads() {
        long start = System.nanoTime();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < TASKS; i++) {
                final int id = i;
                executor.submit(() -> simulateWork(id));
            }
        }

        log.info("Виртуальные потоки завершили выполнение");
        evaluateExecutionTime(start);
    }

    private static void simulateWork(int id) {
        Thread current = Thread.currentThread();
        String threadName = current.getName();

        boolean isVirtual = current.isVirtual();

        String carrierThreadNane = Thread.currentThread().toString();

        log.info("Задача №{} , Поток ={}, isVirtual={}, carrierThreadName={}",
                id, threadName, isVirtual, carrierThreadNane);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
