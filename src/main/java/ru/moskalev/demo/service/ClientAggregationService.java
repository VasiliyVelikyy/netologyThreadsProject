package ru.moskalev.demo.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.domain.ClientFullInfo;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Service
@RequiredArgsConstructor
public class ClientAggregationService {
    private static final Logger log = LoggerFactory.getLogger(ClientAggregationService.class);

    private final WebClient webClient;
    private final BankAccountRepository accountRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(100);

    public List<ClientFullInfo> getFullClientInfoAsync() {
        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());
        List<Future<ClientFullInfo>> futures = new ArrayList<>();

        for (BankAccount acc : accounts) {
            Future<ClientFullInfo> future = executor.submit(() -> fetchFullInfo(acc.getAccountNumber(),
                    acc.getBalance()));
            futures.add(future);
        }

        List<ClientFullInfo> results = new ArrayList<>();
        for (Future<ClientFullInfo> future : futures) {
            try {
                ClientFullInfo info = future.get();
                log.debug("Получен результат для счёта {}: баланс={}, телефон={}",
                        info.account(), info.balance(), info.phoneNumber());
                results.add(info);
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Ошибка выполнения задачи", e);
            }
        }

        evaluateExecutionTime(startTime);
        return results;
    }

    private ClientFullInfo fetchFullInfo(String accountNumber, double balance) {

        String phone = getPhoneNumber(accountNumber);
        return new ClientFullInfo(accountNumber, balance, phone);
    }


    private String getPhoneNumber(String accountNumber) {
        String url = "http://localhost:8080/api/account/" + accountNumber + "/phone";

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // блокирующий вызов для совместимости с синхронным кодом
        } catch (Exception e) {

            return "UNKNOWN";
        }
    }

    //все потоки в нём non-daemon (обычные).
    //
    // JVM завершается только тогда, когда все non-daemon потоки завершены.
    //
    //Если вы не вызовете shutdown(), пул потоков останется живым, и JVM не завершится, даже если Spring Boot "остановил" веб-контейнер.
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}