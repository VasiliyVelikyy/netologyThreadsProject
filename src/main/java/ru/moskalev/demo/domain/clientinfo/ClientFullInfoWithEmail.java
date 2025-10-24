package ru.moskalev.demo.domain.clientinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClientFullInfoWithEmail {
    private String account;
    private double balance;
    private String phoneNumber;
    private String email;
}
