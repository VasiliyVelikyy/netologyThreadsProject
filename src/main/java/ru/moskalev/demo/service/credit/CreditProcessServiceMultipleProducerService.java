package ru.moskalev.demo.service.credit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.credit.CreditApplication;
import ru.moskalev.demo.domain.credit.CreditApplicationDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
public class CreditProcessServiceMultipleProducerService implements CreditProcessService {
    private final BlockingQueue<CreditApplication> queue = new LinkedBlockingQueue<>(80);
    private final ExecutorService producerExecutor = Executors.newFixedThreadPool(10);

    @Override
    public CreditApplication take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public String submitApplication(List<CreditApplicationDto> applicationsDto) {
        List<CreditApplication> applications = new ArrayList<>(applicationsDto.size());
        for (var dto : applicationsDto) {
            var app = new CreditApplication(dto.getAccountNumber(), dto.getAmount());
            applications.add(app);
        }

        int total = applications.size();
        int chunkSize = (int) Math.ceil((double) total / 10);

        for (int i = 0; i < chunkSize; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, total);
            if (start >= total) break;
            List<CreditApplication> chunk = applications.subList(start, end);
            producerExecutor.submit(() -> processApplication(chunk));
        }
        return "Submitted " + total + " app with 10 producers";
    }

    private void processApplication(List<CreditApplication> chunk) {
        for (var app : chunk) {
            try {
                boolean added = queue.offer(app, 5, TimeUnit.SECONDS);
                if (!added) {
                    log.warn("Queue full! Dropping application {}", app.getAccountNumber());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Producer interuppted", e);
                break;
            }
        }
    }
}
