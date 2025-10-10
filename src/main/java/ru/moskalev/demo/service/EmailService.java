package ru.moskalev.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.repository.EmailRepository;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;

    public String findEmail(String accNum) {
        return emailRepository.findEmail(accNum);
    }
}
