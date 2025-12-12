package ru.moskalev.demo.service.aggrigation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfoWithEmail;
import ru.moskalev.demo.integration.AccountRepositoryPort;
import ru.moskalev.demo.integration.EmailPort;
import ru.moskalev.demo.integration.PhoneNumberPort;
import ru.moskalev.demo.utils.MaskUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static ru.moskalev.demo.Constants.UKNOWN;
import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@RequiredArgsConstructor
@Slf4j
public class ClientAggregationCoreService {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PhoneNumberPort phoneNumberPort;
    private final EmailPort emailPort;

    public List<ClientFullInfoWithEmail> getFullClientInfoWithEmailAsync() {
        long startTime = System.nanoTime();

        log.info("Начинаю асинхронную агрегацию данных по всем счетам через CompletableFuture...");

        List<BankAccount> accounts = accountRepositoryPort.findAllAccounts();

        log.info("Найдено {} счетов для обработки", accounts.size());

        List<CompletableFuture<ClientFullInfoWithEmail>> futures = accounts.stream()
                .map(acc -> {
                    String accountNumber = acc.getAccountNumber();
                    double balance = acc.getBalance();

                    CompletableFuture<String> phoneFuture = phoneNumberPort.getPhoneNumberAsync(accountNumber)
                            .handle((phone, ex)->ex!=null ?"UNKNOWN":phone)
                            .thenApply(MaskUtils::maskPhone);

                    CompletableFuture<String> emailFuture = CompletableFuture.supplyAsync(
                            () -> emailPort.findEmail(accountNumber));

                    return phoneFuture.thenCombine(emailFuture, (phone, email) -> new ClientFullInfoWithEmail(accountNumber, balance, phone, email)
                    );
                }).toList();

        List<ClientFullInfoWithEmail> result = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
        evaluateExecutionTime(startTime);
        return result;
    }
}
