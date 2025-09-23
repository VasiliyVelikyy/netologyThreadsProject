package ru.moskalev.demo.service;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.notification.BalanceNotificationWithAtomicService;

@Service
public class ProcessAtomicExampleService {
    private final BalanceNotificationWithAtomicService balanceNotificationWithAtomicService;

    public ProcessAtomicExampleService(BalanceNotificationWithAtomicService balanceNotificationWithAtomicService) {
        this.balanceNotificationWithAtomicService = balanceNotificationWithAtomicService;
    }

    public String processAtomic() throws InterruptedException {
        int steps = 100;

        Runnable writer1 = () -> {
            for (int i = 0; i < steps; i++) {
                balanceNotificationWithAtomicService.onBalanceChanged(1, "someNumber");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Runnable writer2 = () -> {
            for (int i = 0; i < steps; i++) {
                balanceNotificationWithAtomicService.onBalanceChanged(1, "someNumber");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };


        Thread t1 = new Thread(writer1, "Writer1");
        Thread t2 = new Thread(writer2, "Writer2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("ИТОГ");
        System.out.println("Ожидаемый баланс " + 2 * steps);
        System.out.println("последние значение баланса-> "
                + balanceNotificationWithAtomicService.getLastKnowBalance());
        return "ok";
    }
}
