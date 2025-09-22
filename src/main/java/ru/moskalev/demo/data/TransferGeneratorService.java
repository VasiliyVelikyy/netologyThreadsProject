package ru.moskalev.demo.data;


import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TransferGeneratorService {

    private final BankAccountRepository bankAccountRepository;

    public TransferGeneratorService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

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

    // Класс для хранения операции перевода
    public record TransferOperation(String from, String to, double amount) {
    }
}
