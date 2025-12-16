package ru.moskalev.demo.data;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.entity.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.CacheableService;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TransferGeneratorService {

    private final BankAccountRepository bankAccountRepository;
    private final CacheableService cacheableService;


    // Получить все номера счетов
    private List<String> getAllAccountNumbers() {
        return bankAccountRepository.findAll().stream()
                .map(BankAccount::getAccountNumber)
                .collect(Collectors.toList());
    }

    // Создать случайные переводы: n операций между случайными счетами
    public List<TransferOperation> generateTransfers(int count) {
        List<String> accounts = getAllAccountNumbers();
        Random rand = new Random();

        return IntStream.range(0, count)
                .mapToObj(i -> {
                    String from = accounts.get(rand.nextInt(accounts.size()));
                    String to;
                    do {
                        to = accounts.get(rand.nextInt(accounts.size()));
                    } while (from.equals(to));
                    // Не переводить самому себе
                    double amount = 10 + rand.nextDouble() * 90; // 10–100 руб.
                    return new TransferOperation(from, to, amount);
                })
                .collect(Collectors.toList());
    }

    public List<TransferOperation> generateHotAccTransfers(int count) {

        Random rand = new Random();

        List<String> otherAccount = IntStream.range(0, 100)
                .mapToObj(i -> "ACC" + String.format("%03d", i)).toList();

        return IntStream.range(0, count)
                .mapToObj(i -> {
                    String other = otherAccount.get(rand.nextInt(otherAccount.size()));
                    TransferOperation operation;

                    if (rand.nextBoolean()) {
                        operation = new TransferOperation("ACC001", other, 1.0);
                    } else {
                        operation = new TransferOperation(other, "ACC001", 1.0);
                    }
                    cacheableService.executeTransfer(operation);
                    return operation;

                })
                .collect(Collectors.toList());
    }

    // Класс для хранения операции перевода
    public record TransferOperation(String from, String to, double amount) {
    }
}
