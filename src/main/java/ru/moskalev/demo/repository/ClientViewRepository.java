package ru.moskalev.demo.repository;

import org.springframework.data.repository.CrudRepository;
import ru.moskalev.demo.domain.clientinfo.ClientViewUpdate;

public interface ClientViewRepository extends CrudRepository<ClientViewUpdate, String> {
}
