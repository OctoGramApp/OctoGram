/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.crashlytics;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.LaunchActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.appearance.NotificationColorize;

public class Crashlytics {

    private static final String TAG = "Crashlytics";
    private static final String NOTIFICATION_CHANNEL_ID = NotificationsController.OTHER_NOTIFICATIONS_CHANNEL;
    private static final int CRASH_NOTIFICATION_ID = 1278927891;
    private static final String LATEST_CRASH_LOG_FILE_NAME = "latest_crash.log";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String CRASH_NOTIFICATION_TITLE = "OctoGram just crashed!";
    private static final String CRASH_NOTIFICATION_TEXT = "Sorry about that!";

    private final static File filesDir = OctoUtils.getLogsDir();

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
        BufferedWriter writer = new BufferedWriter(new FileWriter(getLatestCrashFile()));
        OctoLogging.d(TAG, "saveCrashLogs: Saving crash logs to: " + getLatestCrashFile().getAbsolutePath());
        writer.write(getSystemInfo(false));
        writer.write(stacktrace);
        writer.flush();
        writer.close();
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

    /**
     * Reflectively retrieves and formats the OctoConfig properties.
     *
     * @return A JSON-like string representation of OctoConfig.
     * @throws IllegalAccessException If reflection access fails.
     */
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

    public static void deleteCrashLogs() {
        OctoLogging.d(TAG, "deleteCrashLogs: Deleting crash logs");
        File[] files = getArchivedCrashFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    OctoLogging.e(TAG, "deleteCrashLogs: Failed to delete crash log file: " + file.getAbsolutePath());
                } else {
                    OctoLogging.d(TAG, "deleteCrashLogs: Deleted crash log file: " + file.getAbsolutePath());
                }
            }
        } else {
            OctoLogging.d(TAG, "deleteCrashLogs: No archived crash log files found to delete");
        }
    }

    public static File getLatestArchivedCrashFile() {
        OctoLogging.d(TAG, "getLatestArchivedCrashFile: Getting latest archived crash file");
        File[] files = getArchivedCrashFiles();
        if (files != null && files.length > 0) {
            return files[files.length - 1];
        }
        OctoLogging.d(TAG, "getLatestArchivedCrashFile: No archived crash files found");
        return null;
    }

    public static String getLatestCrashDate() {
        OctoLogging.d(TAG, "getLatestCrashDate: Getting latest crash date from log file");
        File latestCrashFile = getLatestCrashFile();
        if (!latestCrashFile.exists()) {
            OctoLogging.w(TAG, "getLatestCrashDate: Latest crash file does not exist, returning 'No_Crash_Log_Found'");
            return "No_Crash_Log_Found";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(latestCrashFile))) {
            String line = reader.readLine();
            if (line != null) {
                String date = line.replace(" ", "_").replace(",", "").replace(":", "_");
                OctoLogging.d(TAG, "getLatestCrashDate: Latest crash date found: " + date);
                return date;
            } else {
                OctoLogging.w(TAG, "getLatestCrashDate: Date not found in crash log file, returning 'Date_Not_Found_in_Log'");
                return "Date_Not_Found_in_Log";
            }
        } catch (IOException e) {
            OctoLogging.e(TAG, "getLatestCrashDate: Error reading latest crash log file", e);
            return "Error_Reading_Log_Date";
        }
    }

    @Nullable
    public static File[] getArchivedCrashFiles() {
        OctoLogging.d(TAG, "getArchivedCrashFiles: Getting archived crash files");
        if (filesDir != null && filesDir.exists()) {
            File[] files = filesDir.listFiles((dir, name) -> name.endsWith(LOG_FILE_EXTENSION));
            OctoLogging.d(TAG, "getArchivedCrashFiles: Found " + (files != null ? files.length : 0) + " archived crash files");
            return files;
        }
        OctoLogging.d(TAG, "getArchivedCrashFiles: Logs directory does not exist or is null, returning null for archived crash files");
        return null;
    }

    public static File getLatestCrashFile() {
        File latestCrashFile = new File(filesDir, LATEST_CRASH_LOG_FILE_NAME);
        OctoLogging.d(TAG, "getLatestCrashFile: Getting latest crash file: " + latestCrashFile.getAbsolutePath());
        return latestCrashFile;
    }

    public static void archiveLatestCrash() {
        OctoLogging.d(TAG, "archiveLatestCrash: Archiving latest crash log");
        File file = getLatestCrashFile();
        if (file.exists()) {
            String timestamp = getLatestCrashDate();
            String sanitizedTimestamp = timestamp.replaceAll("[\\\\/:*?\"<>|]", "_");
            File archived = new File(filesDir, sanitizedTimestamp + LOG_FILE_EXTENSION);
            if (!file.renameTo(archived)) {
                OctoLogging.e(TAG, "archiveLatestCrash: Failed to archive crash log file: " + file.getAbsolutePath());
            } else {
                OctoLogging.d(TAG, "archiveLatestCrash: Successfully archived crash log to: " + archived.getAbsolutePath());
            }
        } else {
            OctoLogging.w(TAG, "archiveLatestCrash: Latest crash file does not exist, cannot archive");
        }
    }

    public static File shareLog(File logFile) throws IOException {
        OctoLogging.d(TAG, "shareLog: Sharing log file: " + logFile.getAbsolutePath());
        File sharedLogFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), logFile.getName());
        try (BufferedReader logFileReader = new BufferedReader(new FileReader(logFile));
             BufferedWriter sharedLogFileWriter = new BufferedWriter(new FileWriter(sharedLogFile))) {

            StringBuilder logContent = new StringBuilder();
            String logLine;
            while ((logLine = logFileReader.readLine()) != null) {
                logContent.append(logLine).append("\n");
            }
            sharedLogFileWriter.write(logContent.toString());

        }
        OctoLogging.d(TAG, "shareLog: Log file shared to: " + sharedLogFile.getAbsolutePath());
        return sharedLogFile;
    }
}