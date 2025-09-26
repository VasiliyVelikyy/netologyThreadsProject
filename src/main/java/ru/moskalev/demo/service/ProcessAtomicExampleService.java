package ru.moskalev.demo.service;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.notification.BalanceNotificationWithAtomicService;

@Service
public class ProcessAtomicExampleService {
    private final BalanceNotificationWithAtomicService balanceNotificationWithAtomicService;
    private final BankAccountRepository bankAccountRepository;
    private final AtomicReferenceAccountService atomicReferenceAccountService;

    public ProcessAtomicExampleService(BalanceNotificationWithAtomicService balanceNotificationWithAtomicService, BankAccountRepository bankAccountRepository, AtomicReferenceAccountService atomicReferenceAccountService) {
        this.balanceNotificationWithAtomicService = balanceNotificationWithAtomicService;
        this.bankAccountRepository = bankAccountRepository;
        this.atomicReferenceAccountService = atomicReferenceAccountService;
    }

    public String processAtomic() throws InterruptedException {
        int steps = 100;

        Runnable writer1 = () -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    public String processAtomicReference() throws InterruptedException {
        int steps = 100;
        var accountNum = "ACC001";
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < steps; i++) {
                atomicReferenceAccountService.deposite(accountNum, 1);

            }
        }, "Potok-1");

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < steps; i++) {
                atomicReferenceAccountService.deposite(accountNum, 1);

            }
        }, "Potok-2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();


        System.out.println("ИТОГ");
        System.out.println("Ожидаемый баланс " + 2 * steps);

        System.out.println("последние значение баланса-> "
                + atomicReferenceAccountService.getAccountCache()
                .get(accountNum).get().getBalance());
        return "ok";

    }
}
