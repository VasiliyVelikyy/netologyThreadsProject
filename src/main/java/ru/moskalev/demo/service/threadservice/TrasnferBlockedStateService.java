package ru.moskalev.demo.service.threadservice;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.BankAccountService;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TrasnferBlockedStateService {

    private final BankAccountService bankAccountService;

    public TrasnferBlockedStateService(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    public String processBlocked() {
        Object monitor = new Object();

        Thread threadBlocker = new Thread(() -> {
            try {
                System.out.println("[" + Thread.currentThread().getName() + "] Стартовал.");
                bankAccountService.transferWithBlock("ACC002", "ACC003", monitor, 20.0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }

        }, "Transfer-blocker");

        Thread threadBlockee = new Thread(() -> {
            try {
                System.out.println("[" + Thread.currentThread().getName() + "] Стартовал.");
                bankAccountService.transferWithBlock("ACC002", "ACC003", monitor, 20.0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }

        }, "Transfer-Blockee");

        threadBlocker.start();
        threadBlockee.start();

        return "ok";
    }


    public String processPark() {
        Lock lock = new ReentrantLock();

        Thread threadParkHolder = new Thread(() -> {
            try {
                System.out.println("[" + Thread.currentThread().getName() + "] Стартовал.");
                bankAccountService.transferWithPark("ACC002", "ACC003", lock, 20.0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }

        }, "Transfer-threadParkHolder");


        Thread threadParkWaiter = new Thread(() -> {
            try {
                System.out.println("[" + Thread.currentThread().getName() + "] Стартовал.");
                Thread.sleep(5000);
                bankAccountService.transferWithPark("ACC002", "ACC003", lock, 20.0);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }

        }, "Transfer-Waiter");


        threadParkHolder.start();
        threadParkWaiter.start();
        return "ok";
    }
}
