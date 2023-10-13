package it.octogram.android.utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.R;

public class OctoUtils {
    public static String phoneNumberReplacer(String input, String phoneCountry) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }

        int currentNum = 0;
        StringBuilder output = new StringBuilder(input.replaceAll(phoneCountry, ""));

        for (int i = 0; i < output.length(); i++) {
            char c = output.charAt(i);
            if (Character.isDigit(c)) {
                currentNum = (currentNum % 9) + 1;
                output.setCharAt(i, Character.forDigit(currentNum, 10));
            }
        }

        return formatPhoneNumber(output.toString());
    }

    public static String formatPhoneNumber(String phoneNumber) {
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        if (digitsOnly.length() < 10) {
            return null;
        }

        String formattedNumber = digitsOnly.substring(0, 10);
        String areaCode = formattedNumber.substring(0, 3);
        String middleDigits = formattedNumber.substring(3, 6);
        String lastDigits = formattedNumber.substring(6);
        formattedNumber = "(" + areaCode + ") " + middleDigits + "-" + lastDigits;

        return formattedNumber;
    }

    public static String getCorrectAppName() {
        if (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("pbeta")) {
            return "OctoGram Beta";
        } else {
            return "OctoGram";
        }
    }


    public static boolean isTelegramString(String string, int resId) {
        return "Telegram".equals(string) || ("Telegram Beta".equals(string) || resId == R.string.AppNameBeta) || resId == R.string.AppName;
    }

    public static boolean isTelegramString(String string) {
        return "Telegram".equals(string) || ("Telegram Beta".equals(string));
    }
}

