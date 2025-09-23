package ru.moskalev.demo.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.notification.BalanceNotificationWithVolatileService;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BankAccountService {

    private final BankAccountRepository repository;
    private final BalanceNotificationWithVolatileService balanceNotificationWithVolatileService;

    private final Lock reentrantLock = new ReentrantLock();

    public BankAccountService(BankAccountRepository repository, BalanceNotificationWithVolatileService balanceNotificationWithVolatileService) {
        this.repository = repository;
        this.balanceNotificationWithVolatileService = balanceNotificationWithVolatileService;
    }

    // Получить счёт по номеру
    public BankAccount getAccount(String accountNumber) {
        Optional<BankAccount> account = repository.findById(accountNumber);
        return account.orElseThrow(() ->
                new RuntimeException("Счёт не найден: " + accountNumber));
    }


    //Поток 1: переводит с A на B
//Поток 2: переводит с B на A
    public void transferWithDoubleSync(String from, String to, double amount) {
        // 👇 КЛЮЧЕВАЯ ОПТИМИЗАЦИЯ: блокируем только два счета
        //Чтобы избежать deadlock, все потоки должны захватывать блокировки в одном и том же порядке.
        //
        String first = from.compareTo(to) < 0 ? from : to;
        String second = from.compareTo(to) < 0 ? to : from;

        synchronized (first.intern()) { //кладем в пулл строк


            synchronized (second.intern()) {
                BankAccount fromAcc = getAccount(from);
                BankAccount toAcc = getAccount(to);

                if (fromAcc.getBalance() < amount) {
                    throw new RuntimeException("Недостаточно средств: " + from);
                }

                fromAcc.setBalance(fromAcc.getBalance() - amount);
                toAcc.setBalance(toAcc.getBalance() + amount);

                repository.save(fromAcc);
                repository.save(toAcc);

                System.out.println(Thread.currentThread().getName() + ": Перевод " + amount + " с " + from + " на " + to + " выполнен.");
            }
        }
    }

    public void transferBlockOneMonitor(String from, String to, double amount) {
        reentrantLock.lock();
        try {

            BankAccount fromAcc = getAccount(from);
            BankAccount toAcc = getAccount(to);

            if (fromAcc.getBalance() < amount) {
                throw new RuntimeException("Недостаточно средств: " + from);
            }

            fromAcc.setBalance(fromAcc.getBalance() - amount);
            toAcc.setBalance(toAcc.getBalance() + amount);

            repository.save(fromAcc);
            repository.save(toAcc);

            System.out.println(Thread.currentThread().getName() + ": Перевод " + amount + " с " + from + " на " + to + " выполнен.");
        } finally {
            reentrantLock.unlock();
        }
    }


    // Перевод между счетами (в одной транзакции!)
    @Transactional
    public void transfer(String fromNum, String toNum, double amount) {
        BankAccount fromAcc = getAccount(fromNum);
        BankAccount toAcc = getAccount(toNum);

        if (fromAcc.getBalance() < amount) {
            throw new RuntimeException("Недостаточно средств: " + fromNum);
        }

        fromAcc.setBalance(fromAcc.getBalance() - amount);
        toAcc.setBalance(toAcc.getBalance() + amount);

        // Сохранение изменений (управляется транзакцией)
        repository.save(fromAcc);
        repository.save(toAcc);

        System.out.println("Перевод " + amount + " со счёта " + fromNum +
                " на счёт " + toNum + " выполнен. " + Thread.currentThread().getName());
    }

    public void transferDeadLock(String from,
                                 String to,
                                 double amount) throws InterruptedException {

        BankAccount fromAcc = repository.getAccount(from);
        BankAccount toAcc = repository.getAccount(to);
        Thread.sleep(10);
        synchronized (from.intern()) {

            System.out.println(Thread.currentThread().getName() + " захватил " + from);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(e.getMessage());
            }
            System.out.println(Thread.currentThread().getName() + " пытается захватить " + to);

            synchronized (to.intern()) {

                if (fromAcc.getBalance() < amount) {
                    throw new RuntimeException("Недостаточно средств: " + from);
                }

                fromAcc.setBalance(fromAcc.getBalance() - amount);
                toAcc.setBalance(toAcc.getBalance() + amount);

                repository.save(fromAcc);
                repository.save(toAcc);

                System.out.println(Thread.currentThread().getName() + ": Перевод " + amount + " с " + from + " на " + to + " выполнен.");
            }
        }
    }

    public void depositWithNotification(String accNum, int amount) {
        BankAccount acc = repository.getAccount(accNum);
        var newBalance = acc.getBalance() + amount;
        acc.setBalance(newBalance);
        System.out.println(Thread.currentThread().getName() + " Баланс обновлен " + newBalance);
        repository.save(acc);
        balanceNotificationWithVolatileService.onBalanceChanged(Math.round(newBalance), "someNumber");
    }
}

