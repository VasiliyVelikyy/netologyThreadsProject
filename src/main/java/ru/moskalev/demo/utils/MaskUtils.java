package ru.moskalev.demo.utils;

import static ru.moskalev.demo.Constants.UKNOWN;

public class MaskUtils {

    public static String maskPhone(String phone) {
        if (phone.contains(UKNOWN)) {
            return phone;
        }

        int len = phone.length();
        return phone.substring(0, 3) + "****" + phone.substring(len - 2);
    }
}
