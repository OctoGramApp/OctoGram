/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import static org.telegram.messenger.LocaleController.getString;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.appearance.NotificationColorize;
import it.octogram.android.utils.chat.FileShareHelper;

public class Crashlytics {

    private static final String TAG = "Crashlytics";
    private static final String NOTIFICATION_CHANNEL_ID = NotificationsController.OTHER_NOTIFICATIONS_CHANNEL;
    private static final int CRASH_NOTIFICATION_ID = 1278927891;
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String CRASH_NOTIFICATION_TITLE = "OctoGram just crashed!";
    private static final String CRASH_NOTIFICATION_TEXT = "Sorry about that!";

    private final static File filesDir = OctoUtils.getLogsDir();

    private static boolean _hasPendingCrashForThisSession = false;

    public static void init() {
        OctoLogging.d(TAG, "init: Initializing Crashlytics");
        //OctoLogging.dumpHprofMemory();
        createNotificationChannel();

        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
            if (exceptionHandler != null) {
                uncaughtException(exceptionHandler, thread, th);
            }
        });
        OctoLogging.d(TAG, "init: Crashlytics initialized");
    }

    private static void createNotificationChannel() {
        OctoLogging.d(TAG, "createNotificationChannel: Creating notification channel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Other Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = ApplicationLoader.applicationContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                OctoLogging.d(TAG, "createNotificationChannel: Notification channel created");
            } else {
                OctoLogging.w(TAG, "createNotificationChannel: NotificationManager is null, cannot create notification channel");
            }
        } else {
            OctoLogging.d(TAG, "createNotificationChannel: Skipping notification channel creation on older Android versions");
        }
    }

    private static void uncaughtException(@NonNull Thread.UncaughtExceptionHandler exceptionHandler, @NonNull Thread t, @NonNull Throwable e) {
        var result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        try {
            saveCrashLogs(stacktrace);
        } catch (IOException | IllegalAccessException ignored) {
        }
        printWriter.close();

        showCrashNotification(stacktrace);
        exceptionHandler.uncaughtException(t, e);
        OctoLogging.d(TAG, "uncaughtException: Uncaught exception handling completed");
    }

    private static void saveCrashLogs(String stacktrace) throws IOException, IllegalAccessException {
        File tempFile = new File(filesDir, "pending_crash.txt");

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(getSystemInfo(false).getBytes());
        fos.write(stacktrace.getBytes());
        fos.close();
    }

    public static boolean hasPendingCrash() {
        if (_hasPendingCrashForThisSession) {
            return true;
        }

        File tempFile = new File(filesDir, "pending_crash.txt");
        if (tempFile.exists()) {
            _hasPendingCrashForThisSession = true;

            File archived = new File(filesDir, "Crash20_" + System.currentTimeMillis() + LOG_FILE_EXTENSION);
            archived.deleteOnExit();
            boolean ignored = tempFile.renameTo(archived);

            return true;
        }

        _hasPendingCrashForThisSession = false;
        return false;
    }

    public static void resetPendingCrash() {
        _hasPendingCrashForThisSession = false;
        File tempFile = new File(filesDir, "pending_crash.txt");
        if (tempFile.exists()) {
            _hasPendingCrashForThisSession = true;
        }
    }

    private static void showCrashNotification(String stacktrace) {
        OctoLogging.d(TAG, "showCrashNotification: Showing crash notification");
        Context context = ApplicationLoader.applicationContext;
        Intent intent = new Intent(context, LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(CRASH_NOTIFICATION_TITLE)
                .setContentText(CRASH_NOTIFICATION_TEXT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(stacktrace))
                .setAutoCancel(true)
                .setColor(NotificationColorize.parseNotificationColor())
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(CRASH_NOTIFICATION_ID, builder.build());
        OctoLogging.d(TAG, "showCrashNotification: Crash notification displayed");
    }


    public static String getSystemInfo(boolean includeConfiguration) throws IllegalAccessException {
        return getSystemInfo(includeConfiguration, null);
    }

    public static String getSystemInfo() throws IllegalAccessException {
        return getSystemInfo(true, null);
    }

    public static String getSystemInfo(boolean includeConfiguration, CharSequence lastModified) throws IllegalAccessException {
        OctoLogging.d(TAG, "getSystemInfo: Getting system info (includeConfig=" + includeConfiguration + ")");
        StringBuilder baseInfo = new StringBuilder();

        baseInfo.append(LocaleController.getInstance().getFormatterFull().format(System.currentTimeMillis()));
        baseInfo.append("\n\n");

        if (lastModified != null) {
            baseInfo.append("CrashLog Date: ").append(lastModified).append("\n\n");
        }

        baseInfo.append("App Version: ").append(BuildVars.BUILD_VERSION_STRING).append(" (").append(BuildConfig.BUILD_VERSION).append(")\n")
                .append("Base Version: ").append(BuildVars.TELEGRAM_VERSION_STRING).append(" (").append(BuildVars.TELEGRAM_BUILD_VERSION).append(")\n")
                .append("Commit: ").append(BuildConfig.GIT_COMMIT_HASH).append("\n")
                .append("Device: ").append(Build.MANUFACTURER != null ? Build.MANUFACTURER : "Unknown")
                .append(" ").append(Build.MODEL != null ? Build.MODEL : "Unknown").append("\n")
                .append("OS Version: ").append(Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "Unknown").append("\n")
                .append("Google Play Services: ").append(ApplicationLoader.hasPlayServices ? "Enabled" : "Disabled").append("\n")
                .append("Performance Class: ").append(getPerformanceClassString()).append("\n")
                .append("Locale: ").append(LocaleController.getSystemLocaleStringIso639()).append("\n")
                .append("Language CW: ").append(Resources.getSystem().getConfiguration().locale.getLanguage()).append("\n")
                // TODO .append("VersionId: ").append(getVersionName(BuildConfig.VERSION_NUM))
                .append("Installation Source: ").append(OctoUtils.getInstallerPackageName(ApplicationLoader.applicationContext)).append("\n");

        if (includeConfiguration) {
            baseInfo.append("Configuration: ").append(getOctoConfiguration()).append("\n");
        }

        return baseInfo.toString();
    }

    private static String getOctoConfiguration() throws IllegalAccessException {
        OctoLogging.d(TAG, "getOctoConfiguration: Getting Octo configuration");
        StringBuilder builder = new StringBuilder("{\n");

        for (Field field : OctoConfig.INSTANCE.getClass().getDeclaredFields()) {
            if (field.getType().equals(ConfigProperty.class)) {
                ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);
                String fieldName = field.getName();
                Object fieldValue = null;
                if (configProperty != null) {
                    fieldValue = configProperty.getValue();
                }
                builder.append("\t\"").append(fieldName).append("\": \"").append(fieldValue).append("\",\n");
            }
        }

        builder.append("}");
        return builder.toString();
    }


    private static String getPerformanceClassString() {
        OctoLogging.d(TAG, "getPerformanceClassString: Getting performance class string");
        return switch (SharedConfig.getDevicePerformanceClass()) {
            case SharedConfig.PERFORMANCE_CLASS_LOW -> "LOW";
            case SharedConfig.PERFORMANCE_CLASS_AVERAGE -> "AVERAGE";
            case SharedConfig.PERFORMANCE_CLASS_HIGH -> "HIGH";
            default -> "UNKNOWN";
        };
    }

    public static ArrayList<File> getArchivedCrashFiles() {
        if (filesDir != null && filesDir.exists()) {
            File[] files = filesDir.listFiles((dir, name) -> name.endsWith(LOG_FILE_EXTENSION));

            if (files != null) {
                ArrayList<File> list = new ArrayList<>(Arrays.asList(files));
                list.sort(Comparator.comparingLong(File::lastModified).reversed());

                return list;
            }
        }
        return new ArrayList<>();
    }

    public static void deleteCrashLogs() {
        ArrayList<File> logFiles = getArchivedCrashFiles();
        for (File file : logFiles) {
            boolean ignored = file.delete();
        }
    }

    public static String getLogContent(File logFile) throws IOException {
        FileInputStream downloadedFileStream = new FileInputStream(logFile);

        StringBuilder fileBuilder = new StringBuilder();
        int character;
        while ((character = downloadedFileStream.read()) != -1) {
            fileBuilder.append((char) character);
        }

        downloadedFileStream.close();
        return fileBuilder.toString();
    }

    public static void sendLastLogFromDeepLink(BaseFragment fragment) {
        ArrayList<File> files = getArchivedCrashFiles();
        if (!files.isEmpty()) {
            sendLog(fragment, files.get(0), false);
        } else {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.error, "Debug logs are not available in production builds.").show();
        }
    }

    public static void sendLog(BaseFragment fragment, File file) {
        sendLog(fragment, file, true);
    }

    private static void sendLog(BaseFragment fragment, File file, boolean useGeneric) {
        if (file == null || !file.exists()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getParentActivity());
        builder.setTitle(getString(R.string.OctoSendLastLogTitle));
        builder.setMessage(getString(useGeneric ? R.string.OctoSendLastLogDescGeneric : R.string.OctoSendLastLogDesc));
        builder.setPositiveButton(getString(R.string.OctoSendLastLogShare), (dialogInterface, i) -> {
            try {
                if (!file.exists()) {
                    return;
                }

                String content = Crashlytics.getLogContent(file);

                FileShareHelper.FileShareData data = new FileShareHelper.FileShareData();
                data.fragment = fragment;
                data.rawFileContent = content;
                data.fileName = file.getName();
                data.fileExtension = "";
                data.addOctoLink = false;
                try {
                    data.caption = Crashlytics.getSystemInfo(false);
                } catch (IllegalAccessException ignored) {

                }
                FileShareHelper.init(data);
            } catch (IOException ignored) {}
        });
        builder.setNegativeButton(getString(R.string.Cancel), null);
        builder.show();
    }

    public static String getFileName(File file) {
        if (file == null) {
            return "-";
        }

        String fileName = file.getName();
        if (fileName.startsWith("Crash20_") && fileName.endsWith(LOG_FILE_EXTENSION)) {
            String time = fileName.split("Crash20_")[1].split(LOG_FILE_EXTENSION)[0];
            try {
                long newTime = Long.parseLong(time);
                if (newTime > 0) {
                    return LocaleController.getInstance().getFormatterBannedUntil().format(newTime);
                }
            } catch (Exception ignored) {}
        }

        return LocaleController.getInstance().getFormatterBannedUntil().format(file.lastModified());
    }
}