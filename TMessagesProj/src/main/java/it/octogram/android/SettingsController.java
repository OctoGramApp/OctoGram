package it.octogram.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import it.octogram.android.camera.CameraXUtils;
import it.octogram.android.entities.EntitiesHelper;
import it.octogram.android.magic.MagicBaseObject;
import it.octogram.android.magic.OWLENC;
import it.octogram.android.translator.Translator;
import it.octogram.android.utils.JavaUtils;
import it.octogram.android.utils.SharedPreferencesHelper;
import it.octogram.ui.Components.Dialogs.FileSettingsNameDialog;

public class SettingsController extends SharedPreferencesHelper {
    protected static boolean configLoaded;
    protected static int DB_VERSION = 0;
    public static final int INVALID_CONFIGURATION = 0;
    public static final int VALID_CONFIGURATION = 1;
    public static final int NEED_UPDATE_CONFIGURATION = 2;

    //UI CHANGES
    public static final int NEED_RECREATE_FORMATTERS = 1;
    public static final int NEED_RECREATE_SHADOW = 2;
    public static final int NEED_FRAGMENT_REBASE_WITH_LAST = 4;
    public static final int NEED_FRAGMENT_REBASE = 8;
    public static final int NEED_UPDATE_CAMERAX = 16;
    public static final int NEED_UPDATE_EMOJI = 32;

    private static boolean isBackupAvailable(String key) {
        switch (key) {
            case "owlEasterSound":
            case "isChineseUser":
            case "verifyLinkTip":
            case "updateData":
            case "oldBuildVersion":
            case "languagePackVersioning":
            case "xiaomiBlockedInstaller":
            case "lastUpdateStatus":
            case "remindedUpdate":
            case "oldDownloadedVersion":
            case "lastUpdateCheck":
            case "translationTarget":
            case "doNotTranslateLanguages":
            case "translationKeyboardTarget":
            case "deepLFormality":
            case "iconStyleSelected":
            case "useMonetIcon":
            case "DB_VERSION":
            case "emojiPackSelected":
            case "NEED_RECREATE_FORMATTERS":
            case "NEED_RECREATE_SHADOW":
            case "NEED_FRAGMENT_REBASE_WITH_LAST":
            case "NEED_FRAGMENT_REBASE":
            case "INVALID_CONFIGURATION":
            case "VALID_CONFIGURATION":
            case "NEED_UPDATE_CONFIGURATION":
            case "TAB_TYPE_TEXT":
            case "TAB_TYPE_ICON":
            case "TAB_TYPE_MIX":
            case "DOWNLOAD_BOOST_DEFAULT":
            case "DOWNLOAD_BOOST_FAST":
            case "DOWNLOAD_BOOST_EXTREME":
            case "NEED_UPDATE_CAMERAX":
            case "NEED_UPDATE_EMOJI":
            case "lastSelectedCompression":
            case "TELEGRAM_CAMERA":
            case "CAMERA_X":
            case "SYSTEM_CAMERA":
                return false;
            default:
                return true;
        }
    }

    public static boolean isNotDeprecatedConfig(String key) {
        switch (key) {
            case "showFireworks":
            case "loopWebMStickers":
            case "useCameraX":
            case "stickerSize":
            case "stickerSize2":
            case "translationTarget":
            case "swipeToPiP":
            case "configLoaded":
            case "unlimitedFavoriteStickers":
            case "unlimitedPinnedDialogs":
            case "increaseAudioMessages":
            case "useMonetIcon":
            case "scrollableChatPreview":
            case "showInActionBar":
            case "cameraXFps":
            case "stickersAutoReorder":
            case "disableStickersAutoReorder":
            case "NEED_UPDATE_CAMERAX":
            case "emojiPackSelected":
            case "disableAppBarShadow":
            case "stickersSorting":
            case "confirmStickersGIFs":
            case "sendConfirm":
            case "showDeleteDownloadedFile":
            case "showCopyPhoto":
            case "showNoQuoteForward":
            case "showSaveMessage":
            case "showRepeat":
            case "showPatpat":
            case "showReportMessage":
            case "showMessageDetails":
                return false;
            default:
                return true;
        }
    }

    public static boolean isLegacy(MessageObject selectedObject) {
        File locFile = MessageHelper.getFileFromMessage(selectedObject);
        PushbackInputStream stream = null;
        try {
            stream = new PushbackInputStream(new FileInputStream(locFile), (int) locFile.length());
            return !new OWLENC.SettingsBackup().isNotLegacy(stream);
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return false;
    }

    public static ArrayList<String> getDifferenceBetweenCurrentConfig(MessageObject selectedObject) {
        ArrayList<String> listDifference = new ArrayList<>();
        File locFile = MessageHelper.getFileFromMessage(selectedObject);
        PushbackInputStream stream = null;
        Map<String, ?> listPreferences = getAll();
        try {
            stream = new PushbackInputStream(new FileInputStream(locFile), (int) locFile.length());
            OWLENC.SettingsBackup settingsBackup = new OWLENC.SettingsBackup();
            if (settingsBackup.isNotLegacy(stream)) {
                settingsBackup.readParams(stream, true);
            }
            settingsBackup.migrate();
            Field[] fields = getFields();
            for (Field field : fields) {
                String keyFound = field.getName();
                if (isBackupAvailable(keyFound)) {
                    if (settingsBackup.contains(keyFound)) {
                        Object value = settingsBackup.get(keyFound);
                        Object defaultValue = field.get(Object.class);
                        if (!Objects.equals(value, defaultValue)) {
                            int addCount = 1;
                            if (value instanceof MagicBaseObject) {
                                addCount = ((MagicBaseObject) value).differenceCount(defaultValue);
                            }
                            for (int i = 0; i < addCount; i++) {
                                listDifference.add(keyFound);
                            }
                        }
                    } else if (listPreferences.containsKey(keyFound)) {
                        listDifference.add(keyFound);
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return listDifference;
    }

    public static int isValidFileSettings(MessageObject selectedObject) {
        File locFile = MessageHelper.getFileFromMessage(selectedObject);
        if (locFile != null && locFile.length() <= 1024 * 25) {
            PushbackInputStream stream = null;
            try {
                stream = new PushbackInputStream(new FileInputStream(locFile), (int) locFile.length());
                OWLENC.SettingsBackup settingsBackup = new OWLENC.SettingsBackup();
                if (settingsBackup.isNotLegacy(stream)) {
                    settingsBackup.readParams(stream, true);
                }
                settingsBackup.migrate();
                int foundValues = 0;
                int foundValidValues = 0;
                Field[] fields = getFields();
                for (Field field : fields) {
                    String keyFound = field.getName();
                    if (settingsBackup.contains(keyFound)) {
                        Object result = settingsBackup.get(keyFound);
                        foundValues++;
                        if (JavaUtils.isInstanceOf(result.getClass(), field.getType())) {
                            foundValidValues++;
                        }
                    }
                }
                for (String key : settingsBackup) {
                    boolean foundValid = false;
                    for (Field field : fields) {
                        String keyFound = field.getName();
                        if ((key.equals(keyFound) && isValidParameter(key, settingsBackup.get(key))) || !isNotDeprecatedConfig(key)) {
                            foundValid = true;
                            break;
                        }
                    }
                    if (!foundValid) {
                        foundValidValues--;
                        break;
                    }
                }
                if (foundValues == foundValidValues) {
                    return VALID_CONFIGURATION;
                } else if (settingsBackup.VERSION > DB_VERSION) {
                    return NEED_UPDATE_CONFIGURATION;
                }
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
        return INVALID_CONFIGURATION;
    }

    private static boolean isValidParameter(String key, Object value) {
        if (value instanceof MagicBaseObject) {
            return ((MagicBaseObject) value).isValid();
        } else if (value instanceof Integer) {
            int integerValue = (int) value;
            switch (key) {
                case "deepLFormality":
                case "tabMode":
                case "cameraType":
                case "dcStyleType":
                case "downloadSpeedBoost":
                    return integerValue >= 0 && integerValue <= 2;
                case "translationProvider":
                    return integerValue == Translator.PROVIDER_GOOGLE ||
                            integerValue == Translator.PROVIDER_YANDEX ||
                            integerValue == Translator.PROVIDER_DEEPL ||
                            integerValue == Translator.PROVIDER_NIU ||
                            integerValue == Translator.PROVIDER_DUCKDUCKGO ||
                            integerValue == Translator.PROVIDER_TELEGRAM;
                case "blurIntensity":
                    return integerValue >= 0 && integerValue <= 100;
                case "eventType":
                case "buttonStyleType":
                    return integerValue >= 0 && integerValue <= 5;
                case "idType":
                case "translatorStyle":
                    return integerValue >= 0 && integerValue <= 1;
                case "cameraResolution":
                    return true;
                case "maxRecentStickers":
                    return integerValue >= 20 && integerValue <= 200;
                case "stickerSizeStack":
                    return integerValue >= 2 && integerValue <= 20;
                case "unlockedSecretIcon":
                    return integerValue >= -1 && integerValue <= 4;
            }
        }
        return value instanceof Boolean;
    }

    private static Field[] getFields() {
        return Arrays.stream(OctoConfig.class.getDeclaredFields())
                .filter(field -> !field.getName().equals("DB_VERSION"))
                .filter(field -> !field.getName().equals("sync"))
                .toArray(Field[]::new);
    }

    public static void restoreBackup(File inputFile, boolean isRestore) {
        PushbackInputStream stream = null;
        try {
            stream = new PushbackInputStream(new FileInputStream(inputFile), (int) inputFile.length());
            OWLENC.SettingsBackup settingsBackup = new OWLENC.SettingsBackup();
            if (settingsBackup.isNotLegacy(stream)) {
                settingsBackup.readParams(stream, true);
            }
            settingsBackup.migrate();
            if (!isRestore) {
                internalResetSettings();
            }
            for (String key : settingsBackup) {
                if (isNotDeprecatedConfig(key) && (isRestore || isBackupAvailable(key))) {
                    Object result = settingsBackup.get(key);
                    if (result instanceof MagicBaseObject) {
                        putValue(key, ((MagicBaseObject) result).serializeToStream());
                    } else {
                        putValue(key, result);
                    }
                }
            }
            if (!isRestore) {
                configLoaded = false;
                OctoConfig.loadConfig(false);
                MenuOrderController.reloadConfig();
            }
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    public static int getDifferenceUI(MessageObject object) {
        return getInternalDifference(getDifferenceBetweenCurrentConfig(object));
    }

    public static int getDifferenceUI() {
        ArrayList<String> listDifference = new ArrayList<>();
        Map<String, ?> mapBackup = getAll();
        for (Map.Entry<?, ?> entry : mapBackup.entrySet()) {
            String key = (String) entry.getKey();
            if (key == null) {
                throw new NullPointerException("key == null");
            }
            listDifference.add(key);
        }
        return getInternalDifference(listDifference);
    }

    private static int getInternalDifference(ArrayList<String> differences) {
        int returnStatus = 0;
        for (int i = 0; i < differences.size(); i++) {
            switch (differences.get(i)) {
                case "fullTime":
                    returnStatus = addWithCheck(returnStatus, NEED_RECREATE_FORMATTERS);
                    returnStatus = addWithCheck(returnStatus, NEED_FRAGMENT_REBASE_WITH_LAST);
                    break;
                case "showAppBarShadow":
                    returnStatus = addWithCheck(returnStatus, NEED_RECREATE_SHADOW);
                case "roundedNumbers":
                case "useSystemFont":
                    returnStatus = addWithCheck(returnStatus, NEED_FRAGMENT_REBASE_WITH_LAST);
                    break;
                case "showPencilIcon":
                    returnStatus = addWithCheck(returnStatus, NEED_FRAGMENT_REBASE);
                    break;
                case "cameraResolution":
                    returnStatus = addWithCheck(returnStatus, NEED_UPDATE_CAMERAX);
                    break;
                case "useSystemEmoji":
                    returnStatus = addWithCheck(returnStatus, NEED_UPDATE_EMOJI);
                    break;
            }
        }
        return returnStatus;
    }

    private static int addWithCheck(int currentTotal, int newValue) {
        if ((currentTotal & newValue) == 0) {
            currentTotal |= newValue;
        }
        return currentTotal;
    }

    public static void doRebuildUIWithDiff(int difference, INavigationLayout parentLayout) {
        NotificationCenter currentAccount = AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter();
        if ((difference & NEED_RECREATE_FORMATTERS) > 0) {
            LocaleController.getInstance().recreateFormatters();
        }
        if ((difference & NEED_RECREATE_SHADOW) > 0) {
            parentLayout.setHeaderShadow(OctoConfig.showAppBarShadow ? parentLayout.getView().getResources().getDrawable(R.drawable.header_shadow).mutate():null);
        }
        if ((difference & NEED_FRAGMENT_REBASE) > 0) {
            parentLayout.rebuildAllFragmentViews(false, false);
        } else if ((difference & NEED_FRAGMENT_REBASE_WITH_LAST) > 0) {
            parentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
        }
        if ((difference & NEED_UPDATE_CAMERAX) > 0) {
            CameraXUtils.loadSuggestedResolution();
        }
        if ((difference & NEED_UPDATE_EMOJI) > 0) {
            Emoji.reloadEmoji();
            currentAccount.postNotificationName(NotificationCenter.emojiLoaded);
        }
        currentAccount.postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_CHAT);
        currentAccount.postNotificationName(NotificationCenter.mainUserInfoChanged);
        currentAccount.postNotificationName(NotificationCenter.dialogFiltersUpdated);
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }

    private static OWLENC.SettingsBackup getBackup(boolean isInternal) {
        OWLENC.SettingsBackup settingsBackup = new OWLENC.SettingsBackup();
        Field[] fields = getFields();
        for (Field field : fields) {
            String key = field.getName();
            try {
                field.setAccessible(true);
                if (((isBackupAvailable(key) || isInternal) && isNotDeprecatedConfig(key) && field.isAccessible())) {
                    settingsBackup.put(key, field.get(Object.class));
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        settingsBackup.VERSION = DB_VERSION;
        return settingsBackup;
    }

    static void executeBackup() {
        new Thread() {
            @Override
            public void run() {
                OWLENC.SettingsBackup settingsBackup = getBackup(true);
                try {
                    FileOutputStream stream = new FileOutputStream(backupFile());
                    stream.write(settingsBackup.serializeToStream());
                    stream.close();
                } catch (IOException e) {
                    FileLog.e(e);
                }
            }
        }.start();
    }

    public static void resetSettings() {
        internalResetSettings();
        configLoaded = false;
        OctoConfig.loadConfig(false);
        //noinspection ResultOfMethodCallIgnored
        backupFile().delete();
        MenuOrderController.reloadConfig();
    }

    public static void restoreBackup(MessageObject messageObject) {
        restoreBackup(MessageHelper.getFileFromMessage(messageObject), false);
    }

    protected static File backupFile() {
        return new File(ApplicationLoader.getFilesDirFixed(), "octogram_data.json");
    }

    public static void internalResetSettings() {
        Map<String, ?> mapBackup = getAll();
        for (Map.Entry<?, ?> entry : mapBackup.entrySet()) {
            String key = (String) entry.getKey();
            if (isBackupAvailable(key)) {
                remove(key);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void shareSettings(BaseFragment fragment) {
        Activity activity = fragment.getParentActivity();
        new FileSettingsNameDialog(activity, fileName -> {
            try {
                File cacheFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName + ".octo");
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
                FileOutputStream stream = new FileOutputStream(cacheFile);
                stream.write(getBackup(false).serializeToStream());
                stream.close();
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", cacheFile);
                } else {
                    uri = Uri.fromFile(cacheFile);
                }
                Intent i = new Intent(Intent.ACTION_SEND);
                if (Build.VERSION.SDK_INT >= 24) {
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, "");
                i.putExtra(Intent.EXTRA_STREAM, uri);
                i.setClass(activity, LaunchActivity.class);
                activity.startActivity(i);
            } catch (IOException e) {
                FileLog.e(e);
            } catch (Exception e) {
                String description = LocaleController.formatString("BrokenBackupDetail", R.string.BrokenBackupDetail, LocaleController.getString("GroupUsername", R.string.GroupUsername));
                BulletinFactory.of(fragment).createErrorBulletinSubtitle(LocaleController.getString("BrokenMLKit", R.string.BrokenMLKit), EntitiesHelper.getUrlNoUnderlineText(AndroidUtilities.fromHtml(description)), null, Bulletin.DURATION_LONG).show();
            }
        });
    }
}
