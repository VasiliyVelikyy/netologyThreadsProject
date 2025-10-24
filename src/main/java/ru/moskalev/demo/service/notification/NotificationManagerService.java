package ru.moskalev.demo.service.notification;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.task.LowBalanceCheckTask;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.moskalev.demo.Constants.BALANCE_THRESHOLD;


@Service
public class NotificationManagerService {
    private static final Logger log = LoggerFactory.getLogger(NotificationManagerService.class);
    private final BankAccountRepository bankAccountRepository;

    private final ExecutorService executorService;

    public NotificationManagerService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
        executorService = createExecutor();
    }

    private ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(5, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "notifyThread-" + count.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void checkAndNotifyLowBalanceAsync() {
        List<BankAccount> accounts = bankAccountRepository.findAll();
        log.info("Запуск аснихронной проверки низкого баланса для {} счетов", accounts.size());

        for (var acc : accounts) {
            executorService.submit(new LowBalanceCheckTask(acc, this));
        }

        log.info("Все задачи отправлены в пул потоков. Проверка выполняется в фоне");
    }

    public void processAccount(BankAccount account) {
        if (account.getBalance() < BALANCE_THRESHOLD) {
            String message = "Внимание! На счете " + account.getAccountNumber() +
                    " низкий баланс: " + account.getBalance() + " руб";

            log.warn(message);

            //todo  SmsNotificatorService
        } else {
            log.debug("Счет {} в порядке (баланс {})", account.getAccountNumber(),
                    account.getBalance());
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Завершил выполнение ExecutorService");
        executorService.shutdown();
    }
}
