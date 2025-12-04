package ru.moskalev.demo.service.balance;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.domain.ClientBalanceDto;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.websockets.LiveBalanceWebSocketHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BalanceUpdater {
    private final LiveBalanceWebSocketHandler webSocketHandler;
    private final List<String> accountIds = Arrays.asList("ACC001", "ACC002", "ACC003");
    private final BankAccountRepository bankAccountRepository;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public BalanceUpdater(LiveBalanceWebSocketHandler webSocketHandler, BankAccountRepository bankAccountRepository) {
        this.webSocketHandler = webSocketHandler;
        this.bankAccountRepository = bankAccountRepository;
        startSimulate();
    }

    private void startSimulate() {
        scheduler.scheduleAtFixedRate(() -> {
            String acc = accountIds.get(new Random().nextInt(accountIds.size()));
            double newBalance = 10000 + new Random().nextDouble() * 90000;
            updateAcc(acc, newBalance);
            var update = new ClientBalanceDto(acc, newBalance);
            webSocketHandler.sendUpdate(update);
        }, 3, 3, TimeUnit.SECONDS);
    }

    private void updateAcc(String acc, double newBalance) {
        Optional<BankAccount> opt = bankAccountRepository.findById(acc);
        if (opt.isPresent()) {
            var account = opt.get();
            account.setBalance(newBalance);
            bankAccountRepository.save(account);
        } else {
            log.warn(STR."Acc not found\{acc} skiping update");
        }
    }

    @PreDestroy
    public void stopSimulate() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }
}
