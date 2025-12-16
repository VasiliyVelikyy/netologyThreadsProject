package ru.moskalev.demo.service.aggrigation;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.moskalev.demo.domain.entity.BankAccount;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfo;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfoWithEmail;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfoWithEmailVerify;
import ru.moskalev.demo.integration.client.PhoneNumberClient;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.email.EmailService;
import ru.moskalev.demo.service.email.VerificationEmailService;
import ru.moskalev.demo.utils.MaskUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.moskalev.demo.Constants.*;
import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Service

public class ClientAggregationService {
    private static final Logger log = LoggerFactory.getLogger(ClientAggregationService.class);

    private final PhoneNumberClient phoneNumberClient;
    private final WebClient webClient;

    private final BankAccountRepository accountRepository;
    private final EmailService emailService;
    private final VerificationEmailService verificationEmailService;
    private final ExecutorService executorFullInfo = Executors.newFixedThreadPool(10);
    private final ExecutorService emailFetchExecutor = createEmailExecutor();
    private final Semaphore webClientSemaphore = new Semaphore(200);
    private final Counter clientInfoAsyncCounter;
    private final Timer clientInfoAsyncTimer;


    public ClientAggregationService(WebClient webClient,
                                    BankAccountRepository accountRepository,
                                    EmailService emailService,
                                    VerificationEmailService verificationEmailService,
                                    MeterRegistry meterRegistry,
                                    PhoneNumberClient phoneNumberClient) {
        this.webClient = webClient;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.verificationEmailService = verificationEmailService;
        this.clientInfoAsyncCounter = Counter.builder("client.info.async.requests.total")
                .description("Total number of calls info clients")
                .register(meterRegistry);
        this.clientInfoAsyncTimer = Timer.builder("client.info.async.requests")
                .description("Times of duration method info clients")
                .register(meterRegistry);
        this.phoneNumberClient = phoneNumberClient;

    }

    public List<ClientFullInfoWithEmail> getFullClientInfoWithEmailAsync() {
        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам через CompletableFuture...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());

        List<CompletableFuture<ClientFullInfoWithEmail>> futures = accounts.stream()
                .map(acc -> {
                    String accountNumber = acc.getAccountNumber();
                    double balance = acc.getBalance();

                    CompletableFuture<String> phoneFuture = phoneNumberClient.getPhoneNumberAsyncWithTelemetry(accountNumber)
                            .handle(this::handleFetchPhoneNumber)
                            .thenApply(MaskUtils::maskPhone);

                    CompletableFuture<String> emailFuture = CompletableFuture.supplyAsync(
                            () -> emailService.findEmail(accountNumber),
                            emailFetchExecutor);

                    return phoneFuture.thenCombine(emailFuture, (phone, email) -> {
                                if (phone.contains(UKNOWN) || email.contains(UKNOWN)) {
                                    return null;
                                }
                                return new ClientFullInfoWithEmail(accountNumber, balance, phone, email);
                            }
                    );
                }).toList();

        List<ClientFullInfoWithEmail> result = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
        evaluateExecutionTime(startTime);
        return result;
    }

    private ExecutorService createEmailExecutor() {
        return Executors.newFixedThreadPool(5, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "emailFetcher" + counter.getAndIncrement());
            }
        });
    }

    public List<ClientFullInfoWithEmail> getClientsFullWithEmailWithTimeout() {
        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам через CompletableFuture...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());

        List<CompletableFuture<ClientFullInfoWithEmail>> futures = accounts.stream()
                .map(acc -> {
                    String accountNumber = acc.getAccountNumber();
                    double balance = acc.getBalance();

                    CompletableFuture<String> phoneFuture = phoneNumberClient.getPhoneNumberAsyncWithTelemetry(accountNumber)
                            .orTimeout(1, TimeUnit.SECONDS)
                            .handle(this::handleFetchPhoneNumberWithTimeout)
                            .thenApply(MaskUtils::maskPhone)
                            .whenComplete(this::checkCompleteFetchPhoneNum);

                    CompletableFuture<String> emailFuture = getEmailFuture(accountNumber);

                    return phoneFuture.thenCombine(emailFuture, (phone, email) -> {
                                if (phone.contains(UKNOWN) || email.contains(UKNOWN)) {
                                    return null;
                                }
                                return new ClientFullInfoWithEmail(accountNumber, balance, phone, email);
                            }
                    );
                }).toList();

        List<ClientFullInfoWithEmail> result = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
        evaluateExecutionTime(startTime);
        return result;
    }

    private CompletableFuture<String> getEmailFuture(String accountNumber) {
        return CompletableFuture.supplyAsync(
                        () -> emailService.findEmail(accountNumber),
                        emailFetchExecutor)
                .orTimeout(1, TimeUnit.SECONDS)
                .handle((email, ex) -> handleFetchEmailExWithTimeout(email, ex, accountNumber));
    }

    private String handleFetchEmailExWithTimeout(String email, Throwable throwable, String accountNumber) {
        if (throwable != null) {
            if (throwable instanceof TimeoutException) {
                log.warn("Таймаут при получении email  accnum={}", accountNumber);
            } else {
                log.warn("Ошибка при получении email  accnum={}", accountNumber);
            }
            return UKNOWN;
        }
        return email;
    }

    private String handleFetchPhoneNumberWithTimeout(String phone, Throwable throwable) {
        if (throwable != null) {
            if (throwable instanceof TimeoutException) {
                log.warn("Таймаут при получении номера телефона");
            } else {
                log.warn("Ошибка при получении номера телефона {}", throwable.getMessage());
            }
            return UKNOWN;
        }
        return phone;
    }

    public List<ClientFullInfoWithEmail> getFullClientInfoWithEmailAsyncWithLogs() {
        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам через CompletableFuture...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());

        List<CompletableFuture<ClientFullInfoWithEmail>> futures = accounts.stream()
                .map(acc -> {
                    String accountNumber = acc.getAccountNumber();
                    double balance = acc.getBalance();

                    CompletableFuture<String> phoneFuture = getPhoneFuture(acc, accountNumber);

                    CompletableFuture<String> emailFuture = CompletableFuture.supplyAsync(
                            () -> emailService.findEmail(accountNumber),
                            emailFetchExecutor);

                    return phoneFuture.thenCombine(emailFuture, (phone, email) -> {
                                if (phone.contains(UKNOWN) || email.contains(UKNOWN)) {
                                    return null;
                                }
                                return new ClientFullInfoWithEmail(accountNumber, balance, phone, email);
                            }
                    );
                }).toList();

        List<ClientFullInfoWithEmail> result = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
        evaluateExecutionTime(startTime);
        return result;
    }


    public List<ClientFullInfoWithEmail> getFullClientInfoWithEmailVT() throws Exception {
        return clientInfoAsyncTimer.recordCallable(() -> {
            clientInfoAsyncCounter.increment();
            long startTime = System.nanoTime();
            log.info("Виртуальные потоки. Начинаю асинхронную агрегацию данных по всем счетам...");
            List<BankAccount> accounts = accountRepository.findAll();

            log.info("Найдено {} счетов для обработки", accounts.size());

            if (accounts.isEmpty()) {
                evaluateExecutionTime(startTime);
                return List.of();
            }

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                List<StructuredTaskScope.Subtask<ClientFullInfoWithEmail>> tasks = accounts.stream().map(
                                acc -> scope.fork(() -> proccessAccountSequentialy(acc)))
                        .toList();


                //основной поток будет ждать завершения всех запущенных подзадач (subtasks) в рамках StructuredTaskScope не более 50 секунд.
                scope.joinUntil(Instant.now().plusMillis(500000));
                //ожидание всех задач
                scope.throwIfFailed();

                List<ClientFullInfoWithEmail> results = tasks
                        .stream()
                        .map(StructuredTaskScope.Subtask::get)
                        .toList();

                evaluateExecutionTime(startTime);
                log.debug("Успешно обработно записей ={}", results.size());
                return results;
            }
        });
    }

    private ClientFullInfoWithEmail proccessAccountSequentialy(BankAccount acc) {
        try {
            String accountNumber = acc.getAccountNumber();
            double balance = acc.getBalance();

            String phone = getPhoneNumberForVTWithSemaphore(accountNumber);
            String email = emailService.findEmail(accountNumber);

            String maskedPhone = MaskUtils.maskPhone(phone);
            checkCompleteFetchPhoneNum(maskedPhone, null);
            return new ClientFullInfoWithEmail(accountNumber, balance, maskedPhone, email);
        } catch (Exception e) {
            log.error("Ошибка при обработке счёта {}: {}",
                    acc != null ? acc.getAccountNumber() : "null",
                    e,
                    e);
            throw e;
        }
    }


    private CompletableFuture<String> getPhoneFuture(BankAccount acc, String accountNumber) {
        return CompletableFuture
                .supplyAsync(() -> {
                    log.info("Step 1: starting initial date supply accnum={}", accountNumber);
                    return "data";

                }).thenCompose(ignored -> {
                    log.info(ignored);
                    log.info("Step 2 fetch phonenumber for account ={}", accountNumber);
                    return phoneNumberClient.getPhoneNumberAsyncWithTelemetry(accountNumber);
                }).handle((phone, ex) -> {
                    if (ex != null) {
                        log.error("Step 3 failed to fetch phonenumber acc={} errormessage= {}", acc, ex.getMessage());
                        return handleFetchPhoneNumber(phone, ex);
                    } else {
                        log.info("Step 3 :phone number fetched successfuly: phone={} , accnum={}", phone, accountNumber);
                        return phone;
                    }
                }).thenApply(phone -> {
                    String maskPhone = MaskUtils.maskPhone(phone);
                    log.info("Step 4:masking phone number {} , accNum={}", maskPhone, accountNumber);
                    return maskPhone;
                });
    }


    private void checkCompleteFetchPhoneNum(String s,
                                            Throwable throwable) {
        if (throwable != null) {
            log.error("Failed " + throwable.getMessage());
        } else {
            log.info("Success {}", s);
        }
    }

    private String handleFetchPhoneNumber(String result, Throwable throwable) {
        if (throwable != null) {
            return UKNOWN + " phoneNumber";
        }
        return result;
    }

    public List<ClientFullInfoWithEmailVerify> getFullClientInfoWithEmailVerifyAsync() {
        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам через CompletableFuture...");

        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());

        List<CompletableFuture<ClientFullInfoWithEmailVerify>> futures = accounts.stream()
                .map(acc -> {
                    String accountNumber = acc.getAccountNumber();
                    double balance = acc.getBalance();

                    return CompletableFuture.supplyAsync(() -> emailService.findEmail(accountNumber))
                            .thenCompose(email -> {
                                CompletableFuture<String> phoneFuture = phoneNumberClient.getPhoneNumberAsyncWithTelemetry(accountNumber)
                                        .thenApply(MaskUtils::maskPhone);
                                CompletableFuture<Boolean> verifyFuture =
                                        verificationEmailService.isEmailVeifiedAsync(email);

                                return phoneFuture.thenCombine(verifyFuture, (phone, verify) ->
                                        new ClientFullInfoWithEmailVerify(accountNumber,
                                                balance,
                                                phone,
                                                email,
                                                verify));
                            });
                }).toList();

        List<ClientFullInfoWithEmailVerify> result = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        evaluateExecutionTime(startTime);
        return result;
    }


    public List<ClientFullInfo> getFullClientInfoWithFuture() {
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
            futures = executorFullInfo.invokeAll(tasks);
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

    public List<ClientFullInfo> getFullClientInfoWithVirtualThreads() {
        long startTime = System.nanoTime();
        log.info("Виртуальные потоки. Начинаю асинхронную агрегацию данных по всем счетам...");
        List<BankAccount> accounts = accountRepository.findAll();

        log.info("Найдено {} счетов для обработки", accounts.size());

        List<ClientFullInfo> results = new ArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

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
            Future<ClientFullInfo> future = executorFullInfo.submit(() -> fetchFullInfo(acc.getAccountNumber(),
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

        futures = executorFullInfo.invokeAll(tasks, timeout, TimeUnit.MILLISECONDS);

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

    //    public CompletableFuture<String> getPhoneNumberAsync(String accountNumber) {
//        String url = LOCAL_HOST + URL_PHONE_BY_BAD_ACCOUNT;
//
//        return webClient.get()
//                .uri(url, accountNumber)
//                .retrieve()
//                .bodyToMono(String.class)
//                .toFuture();
//    }


    private String getPhoneNumberSync(String accountNumber) {
        String url = LOCAL_HOST + URL_PHONE_BY_GOOD_ACCOUNT;
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

    private String getPhoneNumberForVTWithSemaphore(String accountNumber) {
        String url = LOCAL_HOST + URL_PHONE_BY_GOOD_ACCOUNT;

        try {
            webClientSemaphore.acquire();
            try {
                return webClient.get()
                        .uri(url, accountNumber)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(); // блокирующий вызов для совместимости с синхронным кодом
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            } finally {
                webClientSemaphore.release();
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }

    }

    //все потоки в нём non-daemon (обычные).
    //
    // JVM завершается только тогда, когда все non-daemon потоки завершены.
    //
    //Если вы не вызовете shutdown(), пул потоков останется живым, и JVM не завершится, даже если Spring Boot "остановил" веб-контейнер.
    @PreDestroy
    public void shutdown() {
        executorFullInfo.shutdown();
    }


}