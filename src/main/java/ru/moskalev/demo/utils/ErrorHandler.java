package ru.moskalev.demo.utils;

public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println("Поток -" + t.getName() + " завершился с ошибкой -" + e.getMessage());
    }
}
