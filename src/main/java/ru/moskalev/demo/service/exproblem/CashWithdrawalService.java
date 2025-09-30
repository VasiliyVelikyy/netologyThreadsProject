package ru.moskalev.demo.service.exproblem;

import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class CashWithdrawalService {
    private final Semaphore cahsierSemaphore = new Semaphore(3);

    public void withdrawalCash(String clientName, int amount) {
        boolean permitAcquired = false;

        try {
            permitAcquired = cahsierSemaphore.tryAcquire(5, TimeUnit.SECONDS);
            if (permitAcquired) {
                try {
                    System.out.println(clientName + "  Сумма " + amount + " обслуживается на кассе.");
                    Thread.sleep(3000);
                } finally {
                    cahsierSemaphore.release();
                    System.out.println(clientName + "  освободил кассу");
                }

            } else {
                System.out.println(clientName + " устал ждать, ухожу");
            }

        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            if (permitAcquired) {
                cahsierSemaphore.release();
            }
        }
    }
}
