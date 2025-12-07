package ru.moskalev.demo.service.exproblem;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.account.BankAccountService;

import java.util.concurrent.locks.Lock;

import static ru.moskalev.demo.utils.TaskSimulateWork.simulateCpuWork;

@Service
@RequiredArgsConstructor
public class ProfilingExampleService {
    private final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;


    @Transactional
    public void transferWithWait(String fromNum, String toNum, Object monitor, double amount, boolean shouldWait) {

        synchronized (monitor) {
            BankAccount fromAcc = bankAccountService.getAccount(fromNum);
            BankAccount toAcc = bankAccountService.getAccount(toNum);

            if (fromAcc.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств: " + fromNum);
            }

            if (shouldWait) {
                System.out.println(Thread.currentThread().getName() + ": захватил монитор, теперь жду через wait()...");
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Операция прервана", e);
                }
                System.out.println(Thread.currentThread().getName() + ": проснулся после notify!");
            } else {
                System.out.println(Thread.currentThread().getName() + ": захватил монитор, вызываю notify() и ухожу...");
                monitor.notify(); // Пробуждаем ожидающий поток
            }

            simulateCpuWork(Thread.currentThread().getName(), 5000);
            // Выполняем перевод
            fromAcc.setBalance(fromAcc.getBalance() - amount);
            toAcc.setBalance(toAcc.getBalance() + amount);

            bankAccountRepository.save(fromAcc);
            bankAccountRepository.save(toAcc);

            System.out.println(Thread.currentThread().getName() + ": перевод " + amount + " с " + fromNum + " на " + toNum + " выполнен.");
        }
    }


    public void transferWithBlock(String fromNum, String toNum, Object monitor, double amount) {
        System.out.println(Thread.currentThread().getName() + ": пытается захватить монитор...");

        synchronized (monitor) {
            System.out.println(Thread.currentThread().getName() + ":  захватил монитор...");

            BankAccount fromAcc = bankAccountService.getAccount(fromNum);
            BankAccount toAcc = bankAccountService.getAccount(toNum);

            if (fromAcc.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств: " + fromNum);
            }


            // Выполняем перевод
            fromAcc.setBalance(fromAcc.getBalance() - amount);
            toAcc.setBalance(toAcc.getBalance() + amount);

            bankAccountRepository.save(fromAcc);
            bankAccountRepository.save(toAcc);

            System.out.println(Thread.currentThread().getName() + ": перевод " + amount + " с " + fromNum + " на " + toNum + " выполнен.");
        }
    }

    public void transferWithPark(String fromNum, String toNum, Lock lock, double amount) {
        System.out.println(Thread.currentThread().getName() + ": пытается захватить монитор...");

        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + ":  захватил монитор...");

            BankAccount fromAcc = bankAccountService.getAccount(fromNum);
            BankAccount toAcc = bankAccountService.getAccount(toNum);

            if (fromAcc.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств: " + fromNum);
            }


            simulateCpuWork(Thread.currentThread().getName(), 20000);
            // Выполняем перевод
            fromAcc.setBalance(fromAcc.getBalance() - amount);
            toAcc.setBalance(toAcc.getBalance() + amount);

            bankAccountRepository.save(fromAcc);
            bankAccountRepository.save(toAcc);

            System.out.println(Thread.currentThread().getName() + ": перевод " + amount + " с " + fromNum + " на " + toNum + " выполнен.");
        } finally {
            lock.unlock();
        }

    }
}
