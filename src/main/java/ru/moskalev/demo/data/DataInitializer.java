package ru.moskalev.demo.data;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.moskalev.demo.Constants.*;
import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Component
public class DataInitializer {

    private final BankAccountRepository bankAccountRepository;

    public DataInitializer(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @PostConstruct
    public void init() {
        long startTime = System.nanoTime();

        List<BankAccount> accounts = IntStream.rangeClosed(9, ACCOUNT_COUNT)
                .mapToObj(i -> new BankAccount(ACCOUNT_GENERATE_PREFIX + i, 90.0))
                .collect(Collectors.toList());


        var errorAccounts = IntStream.rangeClosed(0, ACCOUNT_COUNT_WITH_PROBLEM)
                .mapToObj(i -> new BankAccount(ACCOUNT_ERROR_PREFIX + i, 90.0))
                .toList();

        accounts.addAll(errorAccounts);

        bankAccountRepository.saveAll(accounts);

        System.out.println(" Инициализировано " + (ACCOUNT_COUNT + ACCOUNT_COUNT_WITH_PROBLEM) + " счетов.");
        evaluateExecutionTime(startTime);
    }
}