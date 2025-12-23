package ru.moskalev.demo.controller.jpaproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.domain.documents.BankAccountDocument;
import ru.moskalev.demo.domain.dto.BankAccountDto;
import ru.moskalev.demo.service.jpaproblem.ComparisonSqlAndNoSqlService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comparison")
public class ComparisonSqlAndNoSqlController {
    private final ComparisonSqlAndNoSqlService comparisonSqlAndNoSqlService;

    @GetMapping("/sql/account/{id}")
    public BankAccountDto getSqlAccount(@PathVariable String id) {
        return comparisonSqlAndNoSqlService.getSqlAccount(id);
    }

    @GetMapping("/mongo/account/{id}")
    public BankAccountDocument getMongoAccount(@PathVariable String id) {
        return comparisonSqlAndNoSqlService.getMongoAccount(id);
    }
}
