package ru.moskalev.demo.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.moskalev.demo.domain.documents.BankAccountDocument;

import java.util.Optional;

@Repository
public interface BankAccountDocumentRepository extends MongoRepository<BankAccountDocument, String> {

    Optional<BankAccountDocument> findByAccountNumber(String accountNumber);
}
