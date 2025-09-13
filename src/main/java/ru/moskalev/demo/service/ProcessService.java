package ru.moskalev.demo.service;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.threadservice.TransferRunnableStateService;
import ru.moskalev.demo.service.threadservice.TransferWaitingStateService;
import ru.moskalev.demo.service.threadservice.TrasnferBlockedStateService;
import ru.moskalev.demo.task.LoggerTask;

@Service
public class ProcessService {

    private final BankAccountRepository bankAccountRepository;
    private final ConfigurableApplicationContext context;

    private final TransferWaitingStateService transferWaitingStateService;
    private final TransferRunnableStateService transferRunnableStateService;
    private final TrasnferBlockedStateService trasnferBlockedStateService;

    int demonCount = 0;

    public ProcessService(BankAccountRepository bankAccountRepository,
                          ConfigurableApplicationContext context,
                          TransferWaitingStateService transferWaitingStateService,
                          TransferRunnableStateService transferRunnableStateService,
                          TrasnferBlockedStateService trasnferBlockedStateService) {
        this.bankAccountRepository = bankAccountRepository;
        this.context = context;
        this.transferWaitingStateService = transferWaitingStateService;
        this.transferRunnableStateService = transferRunnableStateService;
        this.trasnferBlockedStateService = trasnferBlockedStateService;
    }

    public String processStartDemon() {
        // 3. Устанавливаем глобальный обработчик для всех потоков
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("🚨 Неперехваченная ошибка в потоке '" + thread.getName() + "': " + throwable.getMessage());
            System.err.println("🛑 Завершаем всё приложение...");
            context.close();
        });

        // === 1. Демон-поток: логирование ===
        LoggerTask loggerTask = new LoggerTask(bankAccountRepository);
        Thread loggerThread = new Thread(loggerTask, "LoggerTask-Daemon:" + demonCount++);
        loggerThread.setDaemon(true);
        loggerThread.start();

        return "ok!";
    }

    public String processRunnable() {
        return transferRunnableStateService.processRunnable();
    }

    public String processWaiting() {
        return transferWaitingStateService.processWaiting();
    }

    public String processBlocked() {
        return trasnferBlockedStateService.processBlocked();
    }

    public String processPark() {
        return trasnferBlockedStateService.processPark();
    }
}
