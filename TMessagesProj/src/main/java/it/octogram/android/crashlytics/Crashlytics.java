/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.crashlytics;

import android.os.Build;

import androidx.annotation.NonNull;

import org.telegram.messenger.*;

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

public class Crashlytics implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        try {
            saveCrashLogs(stacktrace);
        } catch (IOException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        printWriter.close();

        if (exceptionHandler != null) {
            exceptionHandler.uncaughtException(t, e);
        }
    }

    private void saveCrashLogs(String stacktrace) throws IOException, IllegalAccessException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(getLatestCrashFile()));
        writer.write(getSystemInfo());
        writer.write(stacktrace);
        writer.flush();
        writer.close();
    }

    public static String getSystemInfo() throws IllegalAccessException {
        String builder = LocaleController.getInstance().formatterStats.format(System.currentTimeMillis()) + "\n\n" +
                "App Version: " + BuildVars.BUILD_VERSION_STRING + " (" + BuildVars.BUILD_VERSION + ")\n" +
                "Base Version: " + BuildVars.TELEGRAM_VERSION_STRING + " (" + BuildVars.TELEGRAM_BUILD_VERSION + ")\n" +
                "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
                "OS Version: " + Build.VERSION.RELEASE + "\n" +
                "Google Play Services: " + ApplicationLoader.hasPlayServices + "\n" +
                "Performance Class: " + getPerformanceClassString() + "\n" +
                "Locale: " + LocaleController.getSystemLocaleStringIso639() + "\n" +
                "Octogram Configuration: " + getOctoConfiguration() + "\n";
        return builder;
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

            return line.replace(" ", "_").replace(",", "").replace(":", "_");
        } catch (IOException e) {
            FileLog.e(e);
            return "null";
        }
    }

    public static File[] getArchivedCrashFiles() {
        File dir = ApplicationLoader.getFilesDirFixed();
        return dir.listFiles((dir1, name) -> name.endsWith(".log"));
    }

    public static File getLatestCrashFile() {
        return new File(ApplicationLoader.getFilesDirFixed(), "latest_crash.log");
    }

    public static void archiveLatestCrash() {
        File file = getLatestCrashFile();
        if (file.exists()) {
            File archived = new File(ApplicationLoader.getFilesDirFixed(), getLatestCrashDate() + ".log");
            file.renameTo(archived);
        }
    }

    private static File getShareLogFile() {
        return new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), "crash_" + getLatestCrashDate() + ".log");
    }

    public static File shareLogs() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getLatestCrashFile()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        reader.close();
        File file = getShareLogFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(builder.toString());
        writer.flush();
        writer.close();

        archiveLatestCrash();
        return file;
    }

}
