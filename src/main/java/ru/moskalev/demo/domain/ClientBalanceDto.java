package ru.moskalev.demo.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Setter

@NoArgsConstructor
@RedisHash("client-accounts")
public class ClientBalanceDto {

    @Id
    private String accountNumber;

    private double balance;

    public ClientBalanceDto(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    @TimeToLive
    private Long ttl =30L;

}
