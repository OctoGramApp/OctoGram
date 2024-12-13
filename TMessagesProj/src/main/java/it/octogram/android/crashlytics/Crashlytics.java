/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.crashlytics;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
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
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.MessageFormat;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.utils.NotificationColorize;
import it.octogram.android.utils.OctoUtils;

public class Crashlytics {

    private final static File filesDir = OctoUtils.getLogsDir();
    public static void init() {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
            if (exceptionHandler != null) {
                uncaughtException(exceptionHandler, thread, th);
            }
        });
    }

    private static void uncaughtException(@NonNull Thread.UncaughtExceptionHandler exceptionHandler, @NonNull Thread t, @NonNull Throwable e) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        try {
            saveCrashLogs(stacktrace);
        } catch (IOException | IllegalAccessException ignored) {}
        printWriter.close();


        Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = new NotificationChannel(
                    NotificationsController.OTHER_NOTIFICATIONS_CHANNEL,
                    "Other Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = ApplicationLoader.applicationContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                ApplicationLoader.applicationContext,
                NotificationsController.OTHER_NOTIFICATIONS_CHANNEL
        )
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("OctoGram just crashed!")
                .setContentText("Sorry about that!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(stacktrace))
                .setAutoCancel(true)
                .setColor(NotificationColorize.parseNotificationColor())
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
        notificationManagerCompat.notify(1278927891, builder.build());

        exceptionHandler.uncaughtException(t, e);
    }

    private static void saveCrashLogs(String stacktrace) throws IOException, IllegalAccessException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(getLatestCrashFile()));
        writer.write(getSystemInfo());
        writer.write(stacktrace);
        writer.flush();
        writer.close();
    }

    public static String getSystemInfo() throws IllegalAccessException {
        return getSystemInfo(true);
    }

    public static String getSystemInfo(boolean includeConfiguration) throws IllegalAccessException {
        var baseInfo = MessageFormat.format(
                """
                        {0}

                        App Version: {1} ({2})
                        Base Version: {3} ({4})
                        Commit: {5}
                        Device: {6} {7}
                        OS Version: {8}
                        Google Play Services: {9}
                        Performance Class: {10}
                        Locale: {11}
                        Language CW: {12}""",
                LocaleController.getInstance().getFormatterFull().format(System.currentTimeMillis()),
                BuildVars.BUILD_VERSION_STRING,
                BuildConfig.BUILD_VERSION,
                BuildVars.TELEGRAM_VERSION_STRING,
                BuildVars.TELEGRAM_BUILD_VERSION,
                BuildConfig.GIT_COMMIT_HASH,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.VERSION.RELEASE,
                ApplicationLoader.hasPlayServices,
                getPerformanceClassString(),
                LocaleController.getSystemLocaleStringIso639(),
                Resources.getSystem().getConfiguration().locale.getLanguage()
        );

        if (includeConfiguration) {
            baseInfo += "\nConfiguration: " + getOctoConfiguration() + "\n";
        }

        return baseInfo;
    }

    // I don't even know why I did this
    private static String getOctoConfiguration() throws IllegalAccessException {
        StringBuilder builder = new StringBuilder();
        builder.append("{").append("\n");

        for (Field field : OctoConfig.INSTANCE.getClass().getDeclaredFields()) {
            if (field.getType().equals(ConfigProperty.class)) {
                ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);
                // get field name
                String fieldName = field.getName();
                // get field value
                Object fieldValue = null;
                if (configProperty != null) {
                    fieldValue = configProperty.getValue();
                }
                builder.append("\t").append(fieldName).append(": ").append(fieldValue).append("\n");
            }

        }

        builder.append("}");
        return builder.toString();
    }

    private static String getPerformanceClassString() {
        return switch (SharedConfig.getDevicePerformanceClass()) {
            case SharedConfig.PERFORMANCE_CLASS_LOW -> "LOW";
            case SharedConfig.PERFORMANCE_CLASS_AVERAGE -> "AVERAGE";
            case SharedConfig.PERFORMANCE_CLASS_HIGH -> "HIGH";
            default -> "UNKNOWN";
        };
    }

    public static void deleteCrashLogs() {
        for (File file : getArchivedCrashFiles()) {
            if (!file.delete()) {
                OctoLogging.e("Failed to delete file: " + file.getAbsolutePath());
            }
        }
    }

    public static File getLatestArchivedCrashFile() {
        File[] files = getArchivedCrashFiles();
        return files.length > 0 ? files[files.length - 1] : null;
    }

    public static String getLatestCrashDate() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(getLatestCrashFile()));
            String line = reader.readLine();
            reader.close();

            return line.replace(" ", "_").replace(",", "").replace(":", "_");
        } catch (IOException e) {
            OctoLogging.e(e);
            return "null";
        }
    }

    public static File[] getArchivedCrashFiles() {
        return filesDir.listFiles((dir, name) -> name.endsWith(".log"));
    }

    public static File getLatestCrashFile() {
        return new File(filesDir, "latest_crash.log");
    }

    public static void archiveLatestCrash() {
        File file = getLatestCrashFile();
        if (file.exists()) {
            File archived = new File(filesDir, getLatestCrashDate() + ".log");
            if (!file.renameTo(archived)) {
                OctoLogging.e("Failed to archive file: " + file.getAbsolutePath());
            }
        }
    }

    public static File shareLog(File logFile) throws IOException {
        BufferedReader logFileReader = new BufferedReader(new FileReader(logFile));
        StringBuilder logContent = new StringBuilder();
        String logLine;
        while ((logLine = logFileReader.readLine()) != null) {
            logContent.append(logLine).append("\n");
        }
        logFileReader.close();
        File sharedLogFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), logFile.getAbsoluteFile().getName());
        BufferedWriter sharedLogFileWriter = new BufferedWriter(new FileWriter(sharedLogFile));
        sharedLogFileWriter.write(logContent.toString());
        sharedLogFileWriter.flush();
        sharedLogFileWriter.close();
        return sharedLogFile;
    }
}
