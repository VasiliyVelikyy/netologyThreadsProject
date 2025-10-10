package ru.moskalev.demo.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.notification.SmsNotificatorService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.moskalev.demo.Constants.BALANCE_THRESHOLD;

@Service
@Slf4j
public class BalanceMonitor {
    private final ScheduledExecutorService scheduler = createScheduler();
    private final BankAccountRepository accountRepository;
    private final SmsNotificatorService smsNotificatorService;
    private final ClientAggregationService aggregationService;

    public BalanceMonitor(BankAccountRepository accountRepository,
                          SmsNotificatorService smsNotificatorService,
                          ClientAggregationService aggregationService) {
        this.accountRepository = accountRepository;
        this.smsNotificatorService = smsNotificatorService;
        this.aggregationService = aggregationService;

//        scheduler.scheduleAtFixedRate(
//                this::checkForLowBalance,
//                0,
//                10,
//                TimeUnit.SECONDS
//        );

//        scheduler.scheduleAtFixedRate(
//                this::checkForHighBalance,
//                0,
//                30,
//                TimeUnit.SECONDS
//        );
    }

    private void checkForHighBalance() {
        try {
            log.info("Запуск проверки достаточных балансов......");
            List<BankAccount> lowBalances = accountRepository.findByBalanceGreaterThan(BALANCE_THRESHOLD);

            for (BankAccount account : lowBalances) {
                String message = "Внимание на счете " + account.getAccountNumber() +
                        " достаточный баланс: " + account.getBalance() + " Руб";
                log.warn(message);

                // smsNotificatorService.trySendSms();
            }
            log.info("Проверка завершена. Найдено {} счетов с достаточным балансом", lowBalances.size());
        } catch (Exception e) {
            log.error("Ошибка в фоновой задаче проверки достаточного баланса", e);
        }
    }

    private void checkForLowBalance() {
        try {
            log.info("Запуск проверки недостаточных балансов......");
            List<BankAccount> lowBalances = accountRepository.findByBalanceLessThan(BALANCE_THRESHOLD);

            for (BankAccount account : lowBalances) {
                String message = "Внимание на счете " + account.getAccountNumber() +
                        " низкий баланс: " + account.getBalance() + " Руб";
                log.warn(message);
                fetchPhoneNumberAndSendSms(account, message);
            }
            log.info("Проверка завершена. Найдено {} счетов с низким балансом", lowBalances.size());
        } catch (Exception e) {
            log.error("Ошибка в фоновой задаче проверки низкого баланса", e);
        }
    }

    private void fetchPhoneNumberAndSendSms(BankAccount account, String message) {
        aggregationService.getPhoneNumberAsync(account.getAccountNumber())
                .thenAcceptAsync(phoneNumber -> {
                    if (phoneNumber != null) {
                        smsNotificatorService.trySendSms(phoneNumber, message);
                    } else {
                        log.warn("Не удалось получить номер счета для акканута ={}",
                                account.getAccountNumber());
                    }
                });
    }

    private ScheduledExecutorService createScheduler() {
        return Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "balancerSch-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}
