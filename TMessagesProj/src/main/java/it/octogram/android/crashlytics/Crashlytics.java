/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.crashlytics;

import android.app.Notification;
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
import org.telegram.messenger.FileLog;
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

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.NotificationColorize;

public class Crashlytics {

    private final Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    private final static File filesDir = ApplicationLoader.applicationContext.getFilesDir();

    public static void init() {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, th) -> uncaughtException(exceptionHandler, thread, th));
    }

    private static void uncaughtException(@NonNull Thread.UncaughtExceptionHandler exceptionHandler,
                                          @NonNull Thread t,
                                          @NonNull Throwable e) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        try {
            saveCrashLogs(stacktrace);
        } catch (IOException | IllegalAccessException ignored) {
        }
        printWriter.close();


        Intent intent = new Intent(ApplicationLoader.applicationContext, LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationLoader.applicationContext)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("OctoGram just crashed!")
                .setContentText("Sorry about that!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(stacktrace))
                .setAutoCancel(true)
                .setColor(NotificationColorize.parseNotificationColor())
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        } else {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationsController.checkOtherNotificationsChannel();
            builder.setChannelId(NotificationsController.OTHER_NOTIFICATIONS_CHANNEL);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ApplicationLoader.applicationContext);
        notificationManager.notify(1278927891, builder.build());

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
        return LocaleController.getInstance().formatterFull.format(System.currentTimeMillis()) + "\n\n" +
                "App Version: " + BuildVars.BUILD_VERSION_STRING + " (" + BuildConfig.BUILD_VERSION + ")\n" +
                "Base Version: " + BuildVars.TELEGRAM_VERSION_STRING + " (" + BuildVars.TELEGRAM_BUILD_VERSION + ")\n" +
                "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
                "OS Version: " + Build.VERSION.RELEASE + "\n" +
                "Google Play Services: " + ApplicationLoader.hasPlayServices + "\n" +
                "Performance Class: " + getPerformanceClassString() + "\n" +
                "Locale: " + LocaleController.getSystemLocaleStringIso639() + "\n" +
                "Language CW: " + Resources.getSystem().getConfiguration().locale.getLanguage() + "\n" +
                "Configuration: " + getOctoConfiguration() + "\n";
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
        switch (SharedConfig.getDevicePerformanceClass()) {
            case SharedConfig.PERFORMANCE_CLASS_LOW:
                return "LOW";
            case SharedConfig.PERFORMANCE_CLASS_AVERAGE:
                return "AVERAGE";
            case SharedConfig.PERFORMANCE_CLASS_HIGH:
                return "HIGH";
            default:
                return "UNKNOWN";
        }
    }

    public static void deleteCrashLogs() {
        File[] files = getArchivedCrashFiles();
        for (File file : files) {
            file.delete();
        }
    }

    public static File getLatestArchivedCrashFile() {
        File[] files = getArchivedCrashFiles();
        if (files.length > 0) {
            return files[files.length - 1];
        } else {
            return null;
        }
    }

    public static String getLatestCrashDate() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(getLatestCrashFile()));
            String line = reader.readLine();
            reader.close();

            return (line != null) ? line.replace(" ", "_").replace(",", "").replace(":", "_") : "null";
        } catch (IOException e) {
            FileLog.e(e);
            return "null";
        }
    }

    public static File[] getArchivedCrashFiles() {
        return filesDir.listFiles((dir1, name) -> name.endsWith(".log"));
    }

    public static File getLatestCrashFile() {
        return new File(filesDir, "latest_crash.log");
    }

    public static void archiveLatestCrash() {
        File file = getLatestCrashFile();
        if (file.exists()) {
            File archived = new File(filesDir, getLatestCrashDate() + ".log");
            file.renameTo(archived);
        }
    }

    public static File shareLog(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        reader.close();
        File shareLogFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), file.getName());
        BufferedWriter writer = new BufferedWriter(new FileWriter(shareLogFile));
        writer.write(builder.toString());
        writer.flush();
        writer.close();
        return shareLogFile;
    }

}
