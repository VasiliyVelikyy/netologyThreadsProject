package ru.moskalev.demo.data;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.moskalev.demo.Constants.ACCOUNT_COUNT;
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
                .mapToObj(i -> new BankAccount("GEN_ACC_-" + i, 90.0))
                .collect(Collectors.toList());

        bankAccountRepository.saveAll(accounts);

        System.out.println(" Инициализировано " + ACCOUNT_COUNT + " счетов.");
        evaluateExecutionTime(startTime);
    }
}