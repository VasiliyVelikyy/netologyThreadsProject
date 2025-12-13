package ru.moskalev.demo.domain.clientinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@RedisHash("client-view")
public class ClientViewUpdate {

    @Id
    private String accountNumber;
    private double balance;
    private String phoneNumber;
    private Instant lastUpdate;
}
