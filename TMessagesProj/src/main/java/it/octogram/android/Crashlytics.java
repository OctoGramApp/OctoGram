package it.octogram.android;

import android.os.Build;
import androidx.annotation.NonNull;
import org.telegram.messenger.*;

import java.io.*;

public class Crashlytics implements Thread.UncaughtExceptionHandler {
    private final Thread.UncaughtExceptionHandler defaultUEH;

    public Crashlytics() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        try {
            saveCrashLogs(stacktrace);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        printWriter.close();
        defaultUEH.uncaughtException(t, e);
    }

    private static File getLogFile() {
        return new File(ApplicationLoader.getFilesDirFixed(), "last_crash.log");
    }

    private static File getShareLogFile() {
        return new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), "Logcat.log");
    }

    private static void saveCrashLogs(String logcat) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFile()));
        writer.write(logcat);
        writer.flush();
        writer.close();
    }

    public static boolean isCrashed() {
        return getLogFile().exists();
    }

    public static File shareLogs() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getLogFile()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        reader.close();
        deleteCrashLogs();
        File file = getShareLogFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(builder.toString());
        writer.flush();
        writer.close();
        return file;
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

    public static String getCrashReportMessage() {
        return getReportMessage() + "\n" +
                "Crash Date: " + LocaleController.getInstance().formatterStats.format(System.currentTimeMillis()) +
                "\n\n#crash";
    }

    public static String getReportMessage() {
        return "App Version: " + BuildVars.BUILD_VERSION_STRING + " (" + (BuildVars.DEBUG_PRIVATE_VERSION ? BuildConfig.GIT_COMMIT_HASH : BuildVars.BUILD_VERSION) + ")\n" +
                "Base Version: " + BuildVars.TELEGRAM_VERSION_STRING + " (" + BuildVars.TELEGRAM_BUILD_VERSION + ")\n" +
                "Device: " + AndroidUtilities.capitalize(Build.MANUFACTURER) + " " + Build.MODEL + "\n" +
                "OS Version: " + Build.VERSION.RELEASE + "\n" +
                "Google Play Services: " + ApplicationLoader.hasPlayServices + "\n" +
                "Performance Class: " + getPerformanceClassString() + "\n" +
                "Locale: " + LocaleController.getSystemLocaleStringIso639() + "\n" +
                "CameraX: " + OctoConfig.INSTANCE.cameraXEnabled.getValue();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteCrashLogs() {
        File file = getLogFile();
        if (file.exists()) {
            file.delete();
        }
    }
}
