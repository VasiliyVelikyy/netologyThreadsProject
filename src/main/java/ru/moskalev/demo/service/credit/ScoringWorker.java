package ru.moskalev.demo.service.credit;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class ScoringWorker {
    private final CreditProcessService creditProcessServiceOneProducerService;
    private final ExecutorService executorService;

    public ScoringWorker(CreditProcessService creditProcessServiceOneProducerService) {
        this.creditProcessServiceOneProducerService = creditProcessServiceOneProducerService;
        executorService = Executors.newFixedThreadPool(3);
    }

    @PostConstruct
    public void startWorker() {
        for (int i = 0; i < 3; i++) {
            //executorService.submit(this::processApplication);
        }
    }

    private void processApplication() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                log.info("{}, Waiting application", Thread.currentThread().getName());
                var app = creditProcessServiceOneProducerService.take();
                log.info("{}, Application was received, accNum={}", Thread.currentThread().getName(), app.getAccountNumber());

                double riskScore = app.getAmount() / 10_000.0;
                boolean approved = riskScore < 5.0;
                Thread.sleep(3000);
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
}
