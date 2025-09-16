package ru.moskalev.demo.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService {

    @Autowired
    private BankAccountRepository repository;

    Object monitor = new Object();

    // Получить все счета
    public List<BankAccount> getAllAccounts() {
        return repository.findAll();
    }

    // Получить счёт по номеру
    public BankAccount getAccount(String accountNumber) {
        Optional<BankAccount> account = repository.findById(accountNumber);
        return account.orElseThrow(() ->
                new RuntimeException("Счёт не найден: " + accountNumber));
    }

    // Сохранить или обновить счёт
    public BankAccount saveAccount(BankAccount account) {
        return repository.save(account);
    }


    //Поток 1: переводит с A на B
//Поток 2: переводит с B на A
    public void transferForStream(String from, String to, double amount) {
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

    public void transferForStreamBlockOneMonitor(String from, String to, double amount) {
        synchronized (monitor) {


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
                " на счёт " + toNum + " выполнен.");
    }

}
