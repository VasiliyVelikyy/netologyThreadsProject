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
import java.util.concurrent.*;

import static ru.moskalev.demo.Constants.*;
import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Service
@RequiredArgsConstructor
public class ClientAggregationService {
    private static final Logger log = LoggerFactory.getLogger(ClientAggregationService.class);

    private final WebClient webClient;
    private final BankAccountRepository accountRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);


    public List<ClientFullInfo> getFullClientInfoAsync() {
        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());
        List<Future<ClientFullInfo>> futures;


        List<Callable<ClientFullInfo>> tasks = accounts.stream()
                .map(acc -> (Callable<ClientFullInfo>) () -> fetchFullInfo(acc.getAccountNumber(),
                        acc.getBalance()))
                .toList();
        try {
            futures = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
                Throwable cause = e.getCause();
                log.error("Не удалось получить данные от = {}", cause.getMessage());
            }
        }

        evaluateExecutionTime(startTime);
        return results;
    }

    public List<ClientFullInfo> getFullClientInfoAsyncWithCancel() {
        log.info("Начинаю асинхронную агрегацию данных по всем счетам...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());
        List<Future<ClientFullInfo>> futures = new ArrayList<>();

        for (BankAccount acc : accounts) {
            Future<ClientFullInfo> future = executor.submit(() -> fetchFullInfo(acc.getAccountNumber(),
                    acc.getBalance()));
            futures.add(future);
        }

        timeOut();
        List<ClientFullInfo> results = new ArrayList<>();

        for (int i = 0; i < futures.size(); i++) {
            Future<ClientFullInfo> future = futures.get(i);
            String accountNumber = accounts.get(i).getAccountNumber();
            if (future.isDone()) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    log.error("Аккаунт ={},Ошибка ={}", accountNumber, e.getMessage());
                }
            } else {
                boolean cancelled = future.cancel(true);
                log.info("Отмена задачи {}: {}", accountNumber, cancelled ? "успешна" : "не удалась");
            }
        }

        return results;
    }

    private void timeOut() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ClientFullInfo> getFullClientInfoAsyncWithTimeout() throws InterruptedException {
        long timeout = 1000;

        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());
        List<Future<ClientFullInfo>> futures;


        List<Callable<ClientFullInfo>> tasks = accounts.stream()
                .map(acc -> (Callable<ClientFullInfo>) () -> fetchFullInfo(acc.getAccountNumber(),
                        acc.getBalance()))
                .toList();

        futures = executor.invokeAll(tasks, timeout, TimeUnit.MILLISECONDS);

        List<ClientFullInfo> results = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            Future<ClientFullInfo> future = futures.get(i);
            String accountNumber = accounts.get(i).getAccountNumber();

            if (future.isCancelled()) {
                log.warn("Задача для счета {} была отменена", accountNumber);
            } else if (future.isDone()) {
                try {
                    var clientInfo = future.get();
                    results.add(clientInfo);
                    log.info("Успешно получены данные для счета {}", accountNumber);
                } catch (ExecutionException e) {
                    log.error("Ошибка при получении данных со счета {}, errormessage={}",
                            accountNumber,
                            e.getMessage());
                }
            }
        }

        evaluateExecutionTime(startTime);
        return results;
    }


    private ClientFullInfo fetchFullInfo(String accountNumber, double balance) {
        String phone = getPhoneNumberSync(accountNumber);
        return new ClientFullInfo(accountNumber, balance, phone);
    }

    private CompletableFuture<String> getPhoneNumberAsync(String accountNumber) {
        String url = LOCAL_HOST + URL_PHONE_BY_GOOD_ACCOUNT;

        return webClient.get()
                .uri(url, accountNumber)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture(); // блокирующий вызов для совместимости с синхронным кодом
    }

    private String getPhoneNumberSync(String accountNumber) {
        String url = LOCAL_HOST + URL_PHONE_BY_BAD_ACCOUNT;
        try {
            return webClient.get()
                    .uri(url, accountNumber)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // блокирующий вызов для совместимости с синхронным кодом
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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