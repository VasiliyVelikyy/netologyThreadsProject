package ru.moskalev.demo.service.balance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.ClientBalanceDto;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientBalanceService {
    private final BankAccountRepository bankAccountRepository;

    public List<ClientBalanceDto> getClientBalances() {
        var accounts = bankAccountRepository.findAll();
        return accounts.stream()
                .map(acc -> new ClientBalanceDto(
                        acc.getAccountNumber(),
                        acc.getBalance()))
                .toList();
    }
}
