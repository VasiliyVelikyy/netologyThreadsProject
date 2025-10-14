package ru.moskalev.demo;

public class Constants {
    public static final double BALANCE_THRESHOLD = 100.0;
    public static final int ITERATIONS_FOR_SPEED_TEST = 1_000_000;
    public final static int ACCOUNT_COUNT = 10;
    public final static int ACCOUNT_COUNT_WITH_PROBLEM = 10;
    public final static String URL_PHONE_BY_GOOD_ACCOUNT = "/api/account/{accountNumber}/phone";
    public final static String URL_PHONE_BY_BAD_ACCOUNT = "/api/account/{accountNumber}/phone-with-problem";
    public final static String ACCOUNT_ERROR_PREFIX = "ERROR_ACC_-";
    public final static String ACCOUNT_GENERATE_PREFIX = "GEN_ACC_-";
    public final static String LOCAL_HOST = "http://localhost:8080";
    public final static String UKNOWN = "UKNOWN";
}