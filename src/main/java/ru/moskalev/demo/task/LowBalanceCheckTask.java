package ru.moskalev.demo.task;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.service.notification.AsyncNotificationService;

@RequiredArgsConstructor
public class LowBalanceCheckTask implements Runnable {
    private final BankAccount account;
    private final AsyncNotificationService asyncNotificationService;

    @Override
    public void run() {
        try {
            Thread.sleep(50);
            asyncNotificationService.processAccount(account);
        } catch (Exception e) {
            LoggerFactory.getLogger(LowBalanceCheckTask.class)
                    .error("Задача прервана по счету {} , errormessage= {}", account.getAccountNumber(),
                            e.getMessage());
            throw new RuntimeException(e);
        }


    }
}
