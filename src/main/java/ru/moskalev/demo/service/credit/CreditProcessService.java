package ru.moskalev.demo.service.credit;

import ru.moskalev.demo.domain.credit.CreditApplication;
import ru.moskalev.demo.domain.credit.CreditApplicationDto;

import java.util.List;

public interface CreditProcessService {

    CreditApplication take() throws InterruptedException;

    String submitApplication(List<CreditApplicationDto> application);

}
