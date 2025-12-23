package ru.moskalev.demo.service.jpaproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.documents.BankAccountDocument;
import ru.moskalev.demo.domain.dto.BankAccountDto;

@Service
@RequiredArgsConstructor
public class ComparisonSqlAndNoSqlService {
    private final JpaProblemsService jpaProblemsService;
    private final MongoDataService mongoDataService;

    public BankAccountDto getSqlAccount(String id) {
        return jpaProblemsService.getAccountWithNPlusOneByAcNum(id);
    }

    //todo BankAccountDocument->BankAccountDto mapper
    public BankAccountDocument getMongoAccount(String id) {
        return mongoDataService.getMongoAccount(id);
    }
}
