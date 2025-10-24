package ru.moskalev.demo.service.credit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ScoringMultipleWorker {
    private final CreditProcessService creditProcessServiceMultipleProducerService;
    private final ExecutorService executorService;

    public ScoringMultipleWorker(CreditProcessService creditProcessServiceMultipleProducerService) {
        this.creditProcessServiceMultipleProducerService = creditProcessServiceMultipleProducerService;
        executorService = Executors.newFixedThreadPool(10);
    }

    @PostConstruct
    public void startWorker() {
        for (int i = 0; i < 10; i++) {
            executorService.submit(this::processApplication);
        }
    }

    private void processApplication() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                log.info("{}, Waiting application", Thread.currentThread().getName());
                var app = creditProcessServiceMultipleProducerService.take();
                log.info("{}, Application was received, accNum={}", Thread.currentThread().getName(), app.getAccountNumber());

                double riskScore = app.getAmount() / 10_000.0;
                boolean approved = riskScore < 5.0;

                log.info("{}, Last decision {} -> {}", Thread.currentThread().getName(),
                        app.getAccountNumber(),
                        approved ? "APPROVED" : "REJECTED");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error is scoring {}", e.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            throw new RuntimeException(e);
        }
    }
}
