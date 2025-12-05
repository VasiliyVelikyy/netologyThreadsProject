package ru.moskalev.demo.service.exproblem;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.account.BankAccountServiceLock;
import ru.moskalev.demo.service.notification.BalanceNotificationWithVolatileService;

@Service
public class ProcessVolatileService {
    private final BankAccountServiceLock bankAccountServiceLock;
    private final BalanceNotificationWithVolatileService balanceNotificationWithVolatileService;

    public ProcessVolatileService(BankAccountServiceLock bankAccountServiceLock, BalanceNotificationWithVolatileService balanceNotificationWithVolatileService) {
        this.bankAccountServiceLock = bankAccountServiceLock;
        this.balanceNotificationWithVolatileService = balanceNotificationWithVolatileService;
    }

    public String processWriteAndReadVolatile() throws InterruptedException {
        int steps = 100;

        Runnable write = () -> {
            for (int i = 0; i < steps; i++) {
                bankAccountServiceLock.depositWithNotification("ACC001", 1);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };


        Runnable reader = () -> {
            for (int i = 0; i < steps; i++) {
                System.out.println(Thread.currentThread().getName() + " последние значение баланса-> "
                        + balanceNotificationWithVolatileService.getLastKnowBalance());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };

        Thread t1 = new Thread(write, "Writer");
        Thread t2 = new Thread(reader, "Reader");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("ИТОГ");
        System.out.println("Ожидаемый баланс " + steps);
        System.out.println("последние значение баланса-> "
                + balanceNotificationWithVolatileService.getLastKnowBalance());
        return "ok";


    }

    public String processRaceCondition() throws InterruptedException {
        int steps = 100;

        Runnable writer1 = () -> {
            for (int i = 0; i < steps; i++) {
                bankAccountServiceLock.depositWithNotification("ACC001", 1);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };

        Runnable writer2 = () -> {
            for (int i = 0; i < steps; i++) {
                bankAccountServiceLock.depositWithNotification("ACC001", 1);
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
        System.out.println("Ожидаемый баланс " + 2*steps);
        System.out.println("последние значение баланса-> "
                + balanceNotificationWithVolatileService.getLastKnowBalance());
        return "ok";


    }
}
