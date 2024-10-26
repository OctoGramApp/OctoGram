package it.octogram.android.utils;

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
import androidx.core.text.HtmlCompat;

import org.apache.commons.lang3.StringUtils;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
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

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.Datacenter;
import it.octogram.android.MediaFilter;
import it.octogram.android.OctoConfig;


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
        File file = FileLoader.getInstance(UserConfig.selectedAccount).getPathToMessage(message.messageOwner);
        return file.exists() ? file : null; // TODO: handle cache
    }

    public static String getCurrentAbi(boolean addUniversalDetails) {
        try {
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            switch (pInfo.versionCode % 10) {
                case 1:
                case 3:
                    return "arm-v7a";
                case 2:
                case 4:
                    return "x86";
                case 5:
                case 7:
                    return "arm64-v8a";
                case 6:
                case 8:
                    return "x86_64";
                case 0:
                case 9:
                    if (!addUniversalDetails) {
                        return "universal";
                    }

                    return "universal/" + Build.CPU_ABI + " " + Build.CPU_ABI2;
            }
        } catch (PackageManager.NameNotFoundException e) {
            FileLog.e(e);
        }

        return "universal";
    }

    public static String getCurrentAbi() {
        return getCurrentAbi(true);
    }

    public static String getDomain() {
        return "OctoGram.me";
    }

    public static File getLogsDir() {
        String OCTO_PATH = "/octologs";
        File dir = null;
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                dir = new File(ApplicationLoader.applicationContext.getExternalFilesDir(null), OCTO_PATH);
            } else {
                dir = new File(ApplicationLoader.applicationContext.getCacheDir().getAbsolutePath(), OCTO_PATH);
            }
        } catch (Exception ignored) {}

        if (dir == null) {
            try {
                dir = new File(ApplicationLoader.applicationContext.getFilesDir().getAbsolutePath(), OCTO_PATH);
            } catch (Exception ignored) {}
        }

        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static TLRPC.MessagesFilter getTLRPCFilterFromId(int id) {
        TLRPC.MessagesFilter filter;

        if (id == MediaFilter.ALL.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterEmpty();
        } else if (id == MediaFilter.PHOTOS.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterPhotos();
        } else if (id == MediaFilter.VIDEOS.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterVideo();
        } else if (id == MediaFilter.VOICE_MESSAGES.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterRoundVoice();
        } else if (id == MediaFilter.VIDEO_MESSAGES.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterRoundVideo();
        } else if (id == MediaFilter.FILES.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterDocument();
        } else if (id == MediaFilter.MUSIC.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterMusic();
        } else if (id == MediaFilter.GIFS.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterGif();
        } else if (id == MediaFilter.LOCATIONS.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterGeo();
        } else if (id == MediaFilter.CONTACTS.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterContacts();
        } else if (id == MediaFilter.MENTIONS.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterMyMentions();
        } else if (id == MediaFilter.URL.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterUrl();
        } else if (id == MediaFilter.PINNED_MESSAGES.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterPinned();
        } else if (id == MediaFilter.CHAT_PHOTOS.getValue()) {
            filter = new TLRPC.TL_inputMessagesFilterChatPhotos();
        } else {
            filter = new TLRPC.TL_inputMessagesFilterEmpty();
        }
        return filter;
    }

    public static Spanned fromHtml(@NonNull String source) {
        return fromHtml(source, null);
    }
    public static Spanned fromHtml(@NonNull String source, Html.TagHandler tagHandler) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY,null, tagHandler);
    }

    public static ArrayList<String> getStringParts(String query, int maxBlockSize) throws IOException {
        ArrayList<String> parts = new ArrayList<>();
        if (query == null || query.isEmpty() || maxBlockSize <= 0) {
            parts.add(query);
            return parts;
        }

        while(query.length() > maxBlockSize) {
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

    public static int getDcIcon(){
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

    public static int getCustomStreamType(TLRPC.Chat chat) {
        if (chat != null && OctoConfig.INSTANCE.mediaInGroupCall.getValue()) {
            WebRtcAudioTrack.setAudioTrackUsageAttribute(AudioAttributes.USAGE_MEDIA);
            return AudioManager.STREAM_MUSIC;
        } else {
            WebRtcAudioTrack.setAudioTrackUsageAttribute(AudioAttributes.USAGE_VOICE_COMMUNICATION);
            return AudioManager.STREAM_VOICE_CALL;
        }
    }

    public static float clamp(float x, float min, float max) {
        return Math.max(min, Math.min(max, x));
    }
}

