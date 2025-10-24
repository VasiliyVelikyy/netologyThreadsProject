package ru.moskalev.demo.service.exproblem;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AtomicReferenceAccountService {
    private final BankAccountRepository bankAccountRepository;

    private final ConcurrentHashMap<String, AtomicReference<BankAccount>> accountCache = new ConcurrentHashMap<>();

    public AtomicReferenceAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }


    public void deposite(String accountNum, int amount) {
        AtomicReference<BankAccount> atomicReference = getAccount(accountNum);
        while (true) {
            BankAccount current = atomicReference.get();
            BankAccount update = new BankAccount(accountNum, current.getBalance() + amount);

            if (atomicReference.compareAndSet(current, update)) {
                System.out.println(Thread.currentThread().getName() + " Успешно обновил аккаунт " + accountNum + " updated= " + update.getBalance());
                return;
            }
            System.out.println(Thread.currentThread().getName() + " Конфликт " + accountNum + " c повтором");
        }
    }

    private AtomicReference<BankAccount> getAccount(String accountNum) {
        return accountCache.computeIfAbsent(accountNum, key -> {
            BankAccount fromDb = bankAccountRepository.getAccount(key);
            if (fromDb == null) {
                throw new IllegalArgumentException("Аккаунт не найден");
            }
            return new AtomicReference<>(fromDb);
        });
    }

    public ConcurrentHashMap<String, AtomicReference<BankAccount>> getAccountCache() {
        return accountCache;
    }
}
