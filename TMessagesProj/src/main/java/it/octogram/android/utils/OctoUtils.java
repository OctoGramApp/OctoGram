package it.octogram.android.utils;

import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
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
        return BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("pbeta") ? "OctoGram Beta" : "OctoGram";
    }


    public static boolean isTelegramString(String string, int resId) {
        return "Telegram".equals(string) ||
                "Telegram Beta".equals(string) ||
                resId == R.string.AppNameBeta ||
                resId == R.string.AppName ||
                resId == R.string.NotificationHiddenName ||
                resId == R.string.NotificationHiddenChatName ||
                resId == R.string.SecretChatName ||
                resId == R.string.Page1Title ||
                resId == R.string.MapPreviewProviderTelegram;
    }

    public static boolean isTelegramString(String string) {
        return "Telegram".equals(string) || ("Telegram Beta".equals(string));
    }

    public static void showToast(String text) {
        if (text.equals("FILE_REFERENCE_EXPIRED")) {
            return;
        }
        try {
            AndroidUtilities.runOnUIThread(() -> Toast.makeText(ApplicationLoader.applicationContext, text, Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static int getNotificationIcon() {
        return R.drawable.notification;
    }

    public static String fixBrokenLang(String lang) {
        if (lang.equals("in")) {
            return "id";
        }
        return lang;
    }
}

