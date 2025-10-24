package ru.moskalev.demo.domain.clientinfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientFullInfoWithEmailVerify extends ClientFullInfoWithEmail{
    private final boolean isVerify;

    public ClientFullInfoWithEmailVerify(String account, double balance, String phoneNumber, String email, boolean isVerify) {
        super(account, balance, phoneNumber, email);
        this.isVerify = isVerify;
    }
}
