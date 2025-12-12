package ru.moskalev.demo.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.integration.AccountRepositoryPort;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JpaAccountRepositoryAdapter implements AccountRepositoryPort {
    private final BankAccountRepository jpaRepository;

    @Override
    public List<BankAccount> findAllAccounts() {
        return jpaRepository.findAll();
    }
}
