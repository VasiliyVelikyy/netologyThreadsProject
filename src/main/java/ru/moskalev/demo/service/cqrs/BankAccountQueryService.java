package ru.moskalev.demo.service.cqrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.clientinfo.ClientFullInfo;
import ru.moskalev.demo.repository.ClientViewRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountQueryService {
    private final ClientViewRepository clientViewRepository;

    public ClientFullInfo handleClientQuery(String accountNumber) {
        var view = clientViewRepository.findById(accountNumber).orElseThrow(() -> new NoSuchElementException("Not found element =" + accountNumber));
        return new ClientFullInfo(view.getAccountNumber(),
                view.getBalance(),
                view.getPhoneNumber());
    }
}
