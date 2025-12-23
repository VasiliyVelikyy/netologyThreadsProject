package ru.moskalev.demo.service.jpaproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.documents.BankAccountDocument;
import ru.moskalev.demo.repository.mongo.BankAccountDocumentRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MongoDataService {
    private final BankAccountDocumentRepository bankAccountDocumentRepository;

    public String initMongoDb(boolean clear) {
        if (clear) {
            bankAccountDocumentRepository.deleteAll();
        }
        List<BankAccountDocument> accounts = Arrays.asList(
                new BankAccountDocument("ACC001", 10000.0, "USD", "ACTIVE", 20),
                new BankAccountDocument("ACC002", 10000.0, "USD", "ACTIVE", 20),
                new BankAccountDocument("ACC003", 10000.0, "USD", "ACTIVE", 20),
                new BankAccountDocument("ACC004", 10000.0, "USD", "ACTIVE", 20),
                new BankAccountDocument("ACC005", 10000.0, "USD", "ACTIVE", 20),
                new BankAccountDocument("ACC006", 10000.0, "USD", "ACTIVE", 20),
                new BankAccountDocument("ACC007", 10000.0, "USD", "ACTIVE", 20)
        );
        bankAccountDocumentRepository.saveAll(accounts);
        return "Download " + accounts.size() + " records";
    }

    public BankAccountDocument getMongoAccount(String id) {
        return bankAccountDocumentRepository.findByAccountNumber(id)
                .orElseThrow(() -> new RuntimeException("Acc not found " + id));
    }
}
