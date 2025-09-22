package ru.moskalev.demo.service;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class WithdrawalVerificationService {
    private final BankAccountRepository repository;

    private final ReentrantLock lock = new ReentrantLock();

    // Condition: клиенты ждут, пока менеджер не одобрит снятие
    private final Condition withdrawalApproved = lock.newCondition();

    // Condition (опционально): менеджер ждёт, пока не появятся заявки
    private final Condition newRequestsArrived = lock.newCondition();

    // Очередь ожидающих снятий (можно хранить объект WithdrawalRequest)
    private final Queue<WithdrawalRequest> pendingRequests = new ConcurrentLinkedQueue<>();

    public WithdrawalVerificationService(BankAccountRepository repository) {
        this.repository = repository;
    }


    // КЛИЕНТ: запрашивает снятие крупной суммы ===
    public void requestLargeWithdrawal(String accountNum, double amount) throws InterruptedException {
        if (amount <= 1000) {
            // Маленькие суммы — пропускаем без проверки
            executeWithdrawal(accountNum, amount);
            return;
        }

        WithdrawalRequest request = new WithdrawalRequest(accountNum, amount);

        lock.lock();
        try {
            System.out.println("Клиент " + accountNum + " запросил снятие " + amount + ". Ожидает подтверждения менеджера...");

            // Добавляем заявку в очередь
            pendingRequests.add(request);

            // Уведомляем менеджера (если используется второй Condition)
            newRequestsArrived.signal(); // ← опционально, если менеджер "спит", пока нет заявок

            // Ждём, пока менеджер не одобрит
            withdrawalApproved.await(); // ← вот где Condition реально используется!

            // Если дождались — выполняем снятие
            System.out.println("✅ Запрос клиента " + accountNum + " одобрен. Выполняем снятие...");
            executeWithdrawal(accountNum, amount);
        } finally {
            lock.unlock();
        }
    }

    // === МЕНЕДЖЕР: подтверждает следующую заявку ===
    public void approveNextWithdrawal() throws InterruptedException {
        lock.lock();
        try {
            WithdrawalRequest next = pendingRequests.poll();
            if (next != null) {
                System.out.println(Thread.currentThread().getName() + ":🔍 Проверяет заявку...");
                Thread.sleep(500);
                System.out.println(Thread.currentThread().getName() + ":👨‍💼  одобрил снятие " + next.amount + " со счёта " + next.accountNum);
                withdrawalApproved.signal(); // ← будим одного клиента!
            } else {
                System.out.println("📭 Нет ожидающих заявок на снятие.");
            }
        } finally {
            lock.unlock();
        }
    }

    // === (Опционально) МЕНЕДЖЕР: ждёт, пока появятся заявки ===
    public void waitForRequests() throws InterruptedException {
        lock.lock();
        try {
            while (pendingRequests.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + ": 👨‍💼 ожидает поступления заявок...");
                newRequestsArrived.await(); // ← ждёт, пока клиенты не добавят заявки
            }
            System.out.println(" Поступили новые заявки на снятие!");
        } finally {
            lock.unlock();
        }
    }

    // Вспомогательный метод — выполняет снятие (уже после подтверждения)
    private void executeWithdrawal(String accountNum, double amount) {
        BankAccount account = repository.getAccount(accountNum);
        if (account.getBalance() < amount) {
            throw new RuntimeException("Недостаточно средств на счёте " + accountNum);
        }
        account.setBalance(account.getBalance() - amount);
        repository.save(account);
        System.out.println(" Снято " + amount + " со счёта " + accountNum + ". Текущий баланс: " + account.getBalance());
    }

    // Вспомогательный класс для заявки
    private static class WithdrawalRequest {
        String accountNum;
        double amount;

        WithdrawalRequest(String accountNum, double amount) {
            this.accountNum = accountNum;
            this.amount = amount;
        }
    }
}
