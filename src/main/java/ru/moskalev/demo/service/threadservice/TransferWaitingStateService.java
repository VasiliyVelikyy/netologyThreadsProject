package ru.moskalev.demo.service.threadservice;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.ProfilingExampleService;

@Service
public class TransferWaitingStateService {

    private final ProfilingExampleService profilingExampleService;

    public TransferWaitingStateService(ProfilingExampleService profilingExampleService) {
        this.profilingExampleService = profilingExampleService;
    }

    public String processWaiting() {

        Object monitor = new Object();

        Thread waitingThread = new Thread(() -> {
            try {
                System.out.println("[" + Thread.currentThread().getName() + "] Стартовал.");
                profilingExampleService.transferWithWait("ACC005", "ACC006", monitor, 20.0, true); // shouldWait = true
            } catch (Exception e) {
                System.err.println("Ошибка в thread1: " + e.getMessage());
            }
        }, "Transfer-Waiter");


        Thread notifierThread = new Thread(() -> {
            try {
                // Даём waitingThread время начать и войти в wait()
                System.out.println("[" + Thread.currentThread().getName() + "] Стартовал.");
                Thread.sleep(3000);
                profilingExampleService.transferWithWait("ACC007", "ACC005", monitor, 30.0, false); // shouldWait = false → вызовет notify()
            } catch (Exception e) {
                System.err.println("Ошибка в thread2: " + e.getMessage());
            }
        }, "Transfer-Notifier");

        Thread observer = new Thread(() -> {
            try {


                while (true) {
                    Thread.State state = waitingThread.getState();
                    System.out.println("Наблюдатель, состояние потока " + waitingThread.getName() + "= " + state);

                    if (state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING) {
                        System.out.println("Ура увидели поток в состоянии WAITING");
                    }
                    if (state == Thread.State.TERMINATED) {
                        System.out.println("Наблюдаемый поток остановил свое выполнение. Поток наблюдатель завершается");
                        break;
                    }
                    Thread.sleep(2000);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("error");
            }
        }, "Transfer-Observer");

        observer.setDaemon(true);

        observer.start();
        waitingThread.start();
        notifierThread.start();

        return "ok!";
    }
}