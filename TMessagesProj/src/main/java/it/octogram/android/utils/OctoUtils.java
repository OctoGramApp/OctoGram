/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import org.apache.commons.lang3.StringUtils;
import org.telegram.PhoneFormat.CallingCodeInfo;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.webrtc.voiceengine.WebRtcAudioTrack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.Datacenter;
import it.octogram.android.IconsUIType;
import it.octogram.android.MediaFilter;
import it.octogram.android.OctoConfig;
import it.octogram.android.PhoneNumberAlternative;
import it.octogram.android.logs.OctoLogging;
import kotlin.uuid.Uuid;

public class OctoUtils {
    private static final String TAG = "OctoUtils";

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
        //noinspection ConstantValue
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
            OctoLogging.e(TAG, e);
        }
    }

    public static int getNotificationIcon() {
        return R.drawable.notification;
    }

    public static String fixBrokenLang(String lang) {
        return switch (lang) {
            case "in" -> "id";
            case "es" -> "es-ES";
            default -> lang;
        };
    }

    public static void fixBrokenStringArgs(Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                args[i] = OctoUtils.fixBrokenStringData((String) args[i]);
            }
        }
    }

    public static String fixBrokenStringData(String data) {
        if (data.contains("\u2067")) {
            data = TextUtils.replace(data, new String[]{"\u2067"}, new String[]{""}).toString();
        }

        return data;
    }

    public static CharSequence fixBrokenStringData(CharSequence data) {
        if (data.toString().contains("\u2067")) {
            data = TextUtils.replace(data, new String[]{"\u2067"}, new CharSequence[]{""}).toString();
        }

        return data;
    }

    public static File getFileContentFromMessage(MessageObject message) {
        if (!TextUtils.isEmpty(message.messageOwner.attachPath)) {
            File file = new File(message.messageOwner.attachPath);
            if (file.exists()) {
                return file;
            }
        }

        File file = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(message.messageOwner);
        return file.exists() ? file : null; // TODO: handle cache
    }

    public static String getCurrentAbi(boolean addUniversalDetails) {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            switch (pInfo.versionCode % 10) {
                case 1, 3 -> {
                    return "arm-v7a";
                }
                case 2, 4 -> {
                    return "x86";
                }
                case 5, 7 -> {
                    return "arm64-v8a";
                }
                case 6, 8 -> {
                    return "x86_64";
                }
                case 0, 9 -> {
                    if (!addUniversalDetails) {
                        return "universal";
                    }

                    return "universal/" + Build.CPU_ABI + " " + Build.CPU_ABI2;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            OctoLogging.e(TAG, e);
        }

        return "universal";
    }

    public static String getCurrentAbi() {
        return getCurrentAbi(true);
    }

    public static String getDomain() {
        return "octogramapp.github.io";
    }

    private static final String LOGS_DIRECTORY = "octologs";

    /**
     * Returns the directory for storing logs, creating it if it doesn't exist.
     * Attempts to use external storage first, falls back to internal storage if necessary.
     *
     * @return The logs directory File object, or null if creation fails
     */
    public static File getLogsDir() {
        // Define constants at class level instead of method level

        // Get application context once
        var context = ApplicationLoader.applicationContext;
        if (context == null) {
            return null;
        }

        File logsDir = getPreferredLogsDirectory(context);

        // Create directory if it doesn't exist
        if (logsDir != null && !logsDir.exists() && !logsDir.mkdirs()) {
            OctoLogging.e(TAG, "Failed to create logs directory at: " + logsDir.getAbsolutePath());
            return null;
        }

        return logsDir;
    }

    /**
     * Determines and returns the preferred logs directory based on storage availability.
     *
     * @param context The application context
     * @return The preferred logs directory File object, or null if all attempts fail
     */
    private static File getPreferredLogsDirectory(Context context) {
        File directory = null;

        // Try external storage first
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            try {
                File externalDir = context.getExternalFilesDir(null);
                if (externalDir != null) {
                    directory = new File(externalDir, LOGS_DIRECTORY);
                }
            } catch (SecurityException e) {
                OctoLogging.w(TAG, "Failed to access external storage", e);
            }
        }

        // Fall back to cache directory
        if (directory == null) {
            try {
                directory = new File(context.getCacheDir(), LOGS_DIRECTORY);
            } catch (SecurityException e) {
                OctoLogging.w(TAG, "Failed to access cache directory", e);
            }
        }

        // Last resort: internal files directory
        if (directory == null) {
            try {
                directory = new File(context.getFilesDir(), LOGS_DIRECTORY);
            } catch (SecurityException e) {
                OctoLogging.e(TAG, "Failed to access internal storage", e);
            }
        }

        return directory;
    }

    /**
     * Creates and returns a TLRPC.MessagesFilter based on the provided MediaFilter ID.
     *
     * @param id The MediaFilter ID value
     * @return A TLRPC.MessagesFilter instance corresponding to the provided ID
     */
    public static TLRPC.MessagesFilter getTLRPCFilterFromId(int id) {
        return switch (MediaFilter.fromValue(id)) {
            case MediaFilter.PHOTOS -> new TLRPC.TL_inputMessagesFilterPhotos();
            case MediaFilter.VIDEOS -> new TLRPC.TL_inputMessagesFilterVideo();
            case MediaFilter.VOICE_MESSAGES -> new TLRPC.TL_inputMessagesFilterRoundVoice();
            case MediaFilter.VIDEO_MESSAGES -> new TLRPC.TL_inputMessagesFilterRoundVideo();
            case MediaFilter.FILES -> new TLRPC.TL_inputMessagesFilterDocument();
            case MediaFilter.MUSIC -> new TLRPC.TL_inputMessagesFilterMusic();
            case MediaFilter.GIFS -> new TLRPC.TL_inputMessagesFilterGif();
            case MediaFilter.LOCATIONS -> new TLRPC.TL_inputMessagesFilterGeo();
            case MediaFilter.CONTACTS -> new TLRPC.TL_inputMessagesFilterContacts();
            case MediaFilter.MENTIONS -> new TLRPC.TL_inputMessagesFilterMyMentions();
            case MediaFilter.URL -> new TLRPC.TL_inputMessagesFilterUrl();
            case MediaFilter.PINNED_MESSAGES -> new TLRPC.TL_inputMessagesFilterPinned();
            case MediaFilter.CHAT_PHOTOS -> new TLRPC.TL_inputMessagesFilterChatPhotos();
            default -> new TLRPC.TL_inputMessagesFilterEmpty();
        };
    }

    public static Spanned fromHtml(@NonNull String source) {
        return fromHtml(source, null);
    }

    public static Spanned fromHtml(@NonNull String source, Html.TagHandler tagHandler) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler);
    }

    public static ArrayList<String> getStringParts(String query, int maxBlockSize) throws IOException {
        ArrayList<String> parts = new ArrayList<>();
        if (query == null || query.isEmpty() || maxBlockSize <= 0) {
            parts.add(query);
            return parts;
        }

        while (query.length() > maxBlockSize) {
            String maxBlockStr = query.substring(0, maxBlockSize);

            int currentStop = maxBlockStr.lastIndexOf("\n\n");
            if (currentStop == -1) {
                currentStop = maxBlockStr.lastIndexOf("\n");
            }
            if (currentStop == -1) {
                currentStop = maxBlockStr.lastIndexOf(". ");
            }
            if (currentStop == -1) {
                currentStop = maxBlockStr.length();
            }

            parts.add(query.substring(0, currentStop + 1));
            query = query.substring(currentStop + 1);
        }

        if (!query.isEmpty()) {
            parts.add(query);
        }

        if (parts.size() >= 80) {
            throw new IOException("Too many parts");
        }

        return parts;
    }

    public static String formatBitrate(double bitrate) {
        if (bitrate < 1000) {
            return bitrate + " bps";
        } else if (bitrate < 1000000) {
            return String.format(Locale.getDefault(), "%.2f kbps", bitrate / 1000.0);
        } else if (bitrate < 1000000000) {
            return String.format(Locale.getDefault(), "%.2f Mbps", bitrate / 1000000.0);
        } else {
            return String.format(Locale.getDefault(), "%.2f Gbps", bitrate / 1000000000.0);
        }
    }

    public static int getDcIcon() {
        return Datacenter.Companion.getDcInfo(AccountInstance.getInstance(UserConfig.selectedAccount).getConnectionsManager().getCurrentDatacenterId()).getIcon();
    }

    public static String safeToString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static boolean canShowCenteredTitle(ChatActivity parentFragment) {
        if (OctoConfig.INSTANCE.uiTitleCenteredState.getValue() != ActionBarCenteredTitle.ALWAYS.getValue() && OctoConfig.INSTANCE.uiTitleCenteredState.getValue() != ActionBarCenteredTitle.JUST_IN_CHATS.getValue()) {
            return false;
        }

        if (parentFragment == null) {
            // it's probably related to the settings preview
            return false;
        }

        if (parentFragment.isReplyChatComment() || parentFragment.isReport()) {
            return false;
        }

        return parentFragment.getChatMode() != ChatActivity.MODE_SEARCH && parentFragment.getChatMode() != ChatActivity.MODE_SAVED;
    }

    public static boolean canShowCenteredTitle(ChatActivity.ChatActivityFragmentView parentFragment) {
        return canShowCenteredTitle(parentFragment.getChatActivity());
    }

    public static void featureNotAvailable(Theme.ResourcesProvider resourceProvider) {
        BulletinFactory.global().createErrorBulletin("This feature is currently not available", resourceProvider).show();
    }

    public static void featureNotAvailable() {
        BulletinFactory.global().createErrorBulletin("This feature is currently not available", null).show();
    }

    public static int getCustomStreamType(TLRPC.Chat chat) {
        if (chat != null && OctoConfig.INSTANCE.mediaInGroupCall.getValue()) {
            WebRtcAudioTrack.setAudioTrackUsageAttribute(AudioAttributes.USAGE_MEDIA);
            return AudioManager.STREAM_MUSIC;
        } else {
            WebRtcAudioTrack.setAudioTrackUsageAttribute(AudioAttributes.USAGE_VOICE_COMMUNICATION);
            return AudioManager.STREAM_VOICE_CALL;
        }
    }

    public static int getPetIconFixed() {
        if (OctoConfig.INSTANCE.uiIconsType.getValue() == IconsUIType.SOLAR.getValue()) {
            return R.drawable.solar_msg_emoji_cat_ui;
        } else {
            return R.drawable.msg_emoji_cat;
        }
    }

    public static CharSequence hidePhoneNumber(@NonNull TLRPC.User user) {
        OctoLogging.d(TAG, "hidePhoneNumber: " + user);
        var phone = user.phone;
        String text = PhoneFormat.getInstance().format("+" + phone);

        if (OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue()) {
            PhoneNumberAlternative alternative = PhoneNumberAlternative.Companion.fromInt(OctoConfig.INSTANCE.phoneNumberAlternative.getValue());

            switch (alternative) {
                case PhoneNumberAlternative.SHOW_FAKE_PHONE_NUMBER -> {
                    CallingCodeInfo info = PhoneFormat.getInstance().findCallingCodeInfo(phone);
                    String phoneCountry = info != null ? info.callingCode : "";
                    return String.format(Locale.US, "+%s %s", phoneCountry, OctoUtils.phoneNumberReplacer(phone, phoneCountry));
                }
                case PhoneNumberAlternative.SHOW_USERNAME -> {
                    if (user.username != null && !user.username.isEmpty()) {
                        return user.username;
                    }
                    return user.first_name != null ? user.first_name : "Unknown";
                }
                default -> {
                    return user.first_name != null ? user.first_name : "Unknown";
                }
            }
        }

        return text;
    }

    public static CharSequence hiddenPhoneNumberSample(@NonNull TLRPC.User user) {
        var phone = user.phone;
        CallingCodeInfo info = PhoneFormat.getInstance().findCallingCodeInfo(phone);
        String phoneCountry = info != null ? info.callingCode : "";
        return String.format(Locale.US, "+%s %s", phoneCountry, OctoUtils.phoneNumberReplacer(phone, phoneCountry));
    }

    static char[] SPOILER_CHARS = new char[]{
            '⠌', '⡢', '⢑', '⠨', '⠥', '⠮', '⡑'
    };

    public static String createSpoiledName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        Random random = new Random();
        StringBuilder spoiledName = new StringBuilder();
        spoiledName.append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            spoiledName.append(SPOILER_CHARS[random.nextInt(SPOILER_CHARS.length)]);
        }
        return spoiledName.toString();
    }

    public static int getNavBarColor(Theme.ResourcesProvider resourcesProvider) {
        return Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider);
    }

    public static int getNavBarColor() {
        return Theme.getColor(Theme.key_windowBackgroundWhite);
    }

    public static String generateRandomString() {
        return safeToString(Uuid.Companion.random()).replace("-", "");
    }

    @Nullable
    public static String getInstallerPackageName(@NonNull Context context) {
        final String packageName = context.getPackageName();

        // Use modern API for Android R (API 30) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                PackageManager pm = context.getPackageManager();
                InstallSourceInfo sourceInfo = pm.getInstallSourceInfo(packageName);

                String installingPackage = sourceInfo.getInstallingPackageName();
                if (!TextUtils.isEmpty(installingPackage)) {
                    return installingPackage;
                }

                String initiatingPackage = sourceInfo.getInitiatingPackageName();
                if (!TextUtils.isEmpty(initiatingPackage)) {
                    return initiatingPackage;
                }

            } catch (PackageManager.NameNotFoundException e) {
                OctoLogging.w(TAG, "Package not found when getting install source info", e);
            } catch (Exception e) {
                OctoLogging.w(TAG, "Failed to get install source info via modern API", e);
            }
        }

        // Fallback to legacy API for older versions or if modern API fails
        try {
            PackageManager pm = context.getPackageManager();
            String installerPackage = pm.getInstallerPackageName(packageName);
            return TextUtils.isEmpty(installerPackage) ? null : installerPackage;
        } catch (Exception e) {
            OctoLogging.w(TAG, "Failed to get installer package name via legacy API", e);
            return null;
        }
    }
}

