package ru.moskalev.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankAppApplication {

	 static void main(String[] args) {
		SpringApplication.run(BankAppApplication.class, args);
        System.out.println("ok");
	}

}
