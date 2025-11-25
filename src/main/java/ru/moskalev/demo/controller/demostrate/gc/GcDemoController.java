package ru.moskalev.demo.controller.demostrate.gc;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.IntStream;

@RestController
public class GcDemoController {
    private static final List<byte[]> MEMORY_LEAK = new ArrayList<>();

    ThreadPoolTaskExecutor workExecutor;

    public GcDemoController() {
        workExecutor = new ThreadPoolTaskExecutor();
        workExecutor.setCorePoolSize(50);
        workExecutor.setMaxPoolSize(200);
        workExecutor.setQueueCapacity(1000);
        workExecutor.setThreadNamePrefix("work-pool-");
        workExecutor.initialize();

    }

    @GetMapping("/pressure")
    public String createTemporaryObjects() {
        // Создаём 100 МБ временных объектов → быстро умрут, но вызовут Minor GC
        List<byte[]> temp = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            temp.add(new byte[1024 * 1024]); // 1 MB каждый
        }
        return "Created 100 MB of short-lived objects";
    }

    @GetMapping("/leak")
    public String causeMemoryLeak() {
        for (int i = 0; i < 100; i++) {
            MEMORY_LEAK.add(new byte[10 * 1024 * 1024]); // 10 MB

        }
        return "Added 1 MB to static list. Total: " + MEMORY_LEAK.size() + "0 MB";
    }

    @GetMapping("/force-full-gc")
    public String forceFullGC() {
        System.gc(); // Подсказка JVM: "пора сделать GC"

        // Чтобы увеличить вероятность Full GC, вызовем несколько раз
        // и добавим аллокацию "тяжёлых" объектов
        for (int i = 0; i < 5; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return "Full GC requested. Check GC logs.";
    }

    @GetMapping("/work")
    public String doWork() {
        long start = System.nanoTime();
        double sum = 0;
        for (int i = 0; i < 1_000_000; i++) {
            sum += Math.sqrt(i);
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Work took: " + durationMs + " ms");
        return "Work done: " + sum;
    }
//
//    @GetMapping("/work")
//    public String doWork() {
//        long start = System.nanoTime();
//        double sum = IntStream.range(0, 1_000_000)
//                .parallel()
//                .mapToDouble(Math::sqrt)
//                .sum();
//
//        long durationMs = (System.nanoTime() - start) / 1_000_000;
//        System.out.println("Work took: " + durationMs + " ms");
//        return "Work done: " + sum;
//    }

//    @GetMapping("/work")
//    public CompletableFuture<String> doWork() {
//        return CompletableFuture.supplyAsync(() -> {
//            long start = System.nanoTime();
//            double sum = 0;
//            for (int i = 0; i < 1_000_000; i++) {
//                sum += Math.sqrt(i);
//            }
//
//            long durationMs = (System.nanoTime() - start) / 1_000_000;
//            System.out.println("Work took: " + durationMs + " ms");
//            return "Work done: " + sum;
//        }, workExecutor);
//    }
}