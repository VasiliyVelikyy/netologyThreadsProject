package ru.moskalev.demo.utils;

public class TimeUtils {

    public static String evaluateExecutionTime(long startTime) {
        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        double durationSeconds = durationNanos / 1_000_000_000.0;

        String message = String.format("Время выполнения: %.3f секунд (%d наносекунд)",
                durationSeconds, durationNanos);
        printExecutionTime(message);

        return message;
    }

    public static void printExecutionTime(String time) {
        System.out.println(time);
    }
}
