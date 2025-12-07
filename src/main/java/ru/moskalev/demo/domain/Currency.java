package ru.moskalev.demo.domain;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Currency {
    private String code;
    private double rate;
    private long timestamp;

    public Currency(String code, double rate) {
        this.code = code;
        this.rate = rate;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("%s=%.2f @ %d", code, rate, timestamp);
    }
}
