package ru.moskalev.demo.service.account;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.notification.BalanceNotificationWithVolatileService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class BankAccountServiceLock {

    private final BankAccountRepository repository;
    private final BankAccountService bankAccountService;
    private final BalanceNotificationWithVolatileService balanceNotificationWithVolatileService;

    private final Lock reentrantLock = new ReentrantLock();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public BankAccountServiceLock(BankAccountRepository repository,
                                  BalanceNotificationWithVolatileService balanceNotificationWithVolatileService,
                                  BankAccountService bankAccountService) {
        this.repository = repository;
        this.balanceNotificationWithVolatileService = balanceNotificationWithVolatileService;
        this.bankAccountService = bankAccountService;
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

        BankAccount fromAcc = bankAccountService.getAccount(from);
        BankAccount toAcc = bankAccountService.getAccount(to);
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
        BankAccount acc = bankAccountService.getAccount(accNum);
        var newBalance = acc.getBalance() + amount;
        acc.setBalance(newBalance);
        System.out.println(Thread.currentThread().getName() + " Баланс обновлен " + newBalance);
        repository.save(acc);
        balanceNotificationWithVolatileService.onBalanceChanged(Math.round(newBalance), "someNumber");
    }

    public void deposit(String accNum, int amount) {
        readWriteLock.writeLock().lock();
        BankAccount acc = bankAccountService.getAccount(accNum);
        try {
            System.out.println(Thread.currentThread().getName() + " захватил writeLock для пополнения" + amount);
            acc.setBalance(acc.getBalance() + amount);
            System.out.println(Thread.currentThread().getName() + " баланс обновлен" + acc.getBalance());
            repository.save(acc);
            Thread.sleep(800);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            readWriteLock.writeLock().unlock();
            System.out.println(Thread.currentThread().getName() + " освободил writeLock для пополнения" + amount);
        }
    }

    public void withdraw(String accNum, int amount) {
        readWriteLock.writeLock().lock();
        BankAccount acc = bankAccountService.getAccount(accNum);
        try {
            System.out.println(Thread.currentThread().getName() + " захватил writeLock для снятия" + amount);
            acc.setBalance(acc.getBalance() - amount);
            System.out.println(Thread.currentThread().getName() + " баланс обновлен" + acc.getBalance());
            repository.save(acc);
            Thread.sleep(800);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            readWriteLock.writeLock().unlock();
            System.out.println(Thread.currentThread().getName() + " освободил writeLock для пополнения" + amount);
        }

    }

    public void printBalance(String accNum) {
        readWriteLock.readLock().lock();
        BankAccount acc = bankAccountService.getAccount(accNum);
        try {
            System.out.println(Thread.currentThread().getName() + " захватил readLock баланс" + acc.getBalance());
            Thread.sleep(10000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            readWriteLock.readLock().unlock();
            System.out.println(Thread.currentThread().getName() + " освободил readLock для пополнения");
        }

    }

    public double depositWithDowngrade(String accNum, double amount) {
        readWriteLock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " ➤ Захватил WRITE-LOCK для пополнения на " + amount);

            BankAccount acc = bankAccountService.getAccount(accNum);
            acc.setBalance(acc.getBalance() + amount);
            repository.save(acc);

            System.out.println(Thread.currentThread().getName() + " ➤ Баланс обновлён: " + acc.getBalance());

            readWriteLock.readLock().lock();
            try {

                readWriteLock.writeLock().unlock(); // write снят, но read остаётся!
                System.out.println(Thread.currentThread().getName() + " ➤ WRITE-LOCK снят, остался READ-LOCK");

                Thread.sleep(2000);
                double currentBalance = acc.getBalance();

                System.out.println(Thread.currentThread().getName() + " ➤ После downgrade, баланс для отчёта: " + currentBalance);
                return currentBalance;

            } finally {

                readWriteLock.readLock().unlock();
                System.out.println(Thread.currentThread().getName() + " ➤ READ-LOCK освобождён");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    public List<BankAccount> getAllAcc() {
        return repository.findAll();
    }

    public BankAccount save(BankAccount account) {
        return repository.save(account);
    }
}

