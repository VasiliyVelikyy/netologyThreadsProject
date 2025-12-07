package ru.moskalev.demo.repository;

import org.springframework.data.repository.CrudRepository;
import ru.moskalev.demo.domain.ClientBalanceDto;

public interface ClientBalanceRepository extends CrudRepository<ClientBalanceDto, String> {
}
