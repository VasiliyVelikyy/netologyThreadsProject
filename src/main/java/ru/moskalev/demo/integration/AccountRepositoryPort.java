package ru.moskalev.demo.integration;

import ru.moskalev.demo.domain.account.BankAccount;

import java.util.List;

public interface AccountRepositoryPort {
    List<BankAccount> findAllAccounts();
}
