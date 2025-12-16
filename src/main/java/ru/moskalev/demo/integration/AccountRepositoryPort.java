package ru.moskalev.demo.integration;

import ru.moskalev.demo.domain.entity.BankAccount;

import java.util.List;

public interface AccountRepositoryPort {
    List<BankAccount> findAllAccounts();
}
