package ru.moskalev.demo.service.credit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.credit.CreditApplication;
import ru.moskalev.demo.domain.credit.CreditApplicationDto;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class CreditProcessServiceOneProducerService implements CreditProcessService {
    //private final BlockingQueue<CreditApplication> queue = new SynchronousQueue<>();
    private final Queue<CreditApplication> queue = new ConcurrentLinkedQueue<>();

    @Override
    public CreditApplication take() throws InterruptedException {
        //return queue.take();//<- блокирует
       return queue.poll();
    }

    @Override
    public String submitApplication(List<CreditApplicationDto> applications) {
        try {
            for (var dto : applications) {
                var app = new CreditApplication(dto.getAccountNumber(), dto.getAmount());
                submit(app);
            }
            return "Submition " + applications.size() + " applications";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Submission interrupted";
        }
    }

    private void submit(CreditApplication app) throws InterruptedException {
        log.info("put credit application in queue accNum={}", app.getAccountNumber());
        //queue.put(app);
        queue.offer(app);
       // log.info("put credit application in queue accNum={}", app.getAccountNumber());
    }
}
