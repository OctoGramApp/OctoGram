/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import android.util.Log;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.time.FastDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * @noinspection CallToPrintStackTrace, ResultOfMethodCallIgnored, SizeReplaceableByIsEmpty
 */
public class OctoLogging {

    private static final String TAG = "OctoLogging";
    private static final String FILE_EXTENSION = "_debug_log.txt";
    private static final String FILE_EXTENSION_HEAP_DUMP = "_heap.hprof";

    private static OctoLogging instance;
    private final static File filesDir = OctoUtils.getLogsDir();

    private OutputStreamWriter streamWriter;
    private FastDateFormat dateFormat;
    private DispatchQueue logQueue;
    private boolean isInitialized;

    private OctoLogging() {
        initialize();
    }

    public static synchronized OctoLogging getInstance() {
        if (instance == null) {
            instance = new OctoLogging();
        }
        return instance;
    }

    public static void ensureInitialized() {
        getInstance().initialize();
    }

    public static void w(String message) {
        log(Log.WARN, TAG, message, null);
    }

    public static void w(String tag, String message) {
        log(Log.WARN, tag, message, null);
    }

    public static void d(String message) {
        log(Log.DEBUG, TAG, message, null);
    }

    public static void d(String tag, String message) {
        log(Log.DEBUG, tag, message, null);
    }

    public static void d(String tag, String message, Exception throwable) {
        log(Log.DEBUG, tag, message, throwable);
    }

    public static void d(String tag, String message, Throwable throwable) {
        log(Log.DEBUG, tag, message, throwable);
    }

    public static void e(String message) {
        log(Log.ERROR, TAG, message, null);
    }

    public static void e(Throwable throwable) {
        log(Log.ERROR, TAG, null, throwable);
    }

    public static void e(String message, Throwable throwable) {
        log(Log.ERROR, TAG, message, throwable);
    }

    public static void e(String tag, String message) {
        log(Log.ERROR, tag, message, null);
    }

    public static void e(String tag, String message, Exception throwable) {
        log(Log.ERROR, tag, message, throwable);
    }

    public static void e(String tag, String message, OutOfMemoryError throwable) {
        log(Log.ERROR, tag, message, throwable);
    }

    /**
     * Logs a message with the given priority, message, and optional throwable.
     * <p>
     * This method logs the message to the Android logcat and optionally to a file
     * if file logging is enabled. The message is formatted with a timestamp and
     * log level. If a throwable is provided, its stack trace is also logged.
     *
     * @param level     The priority/severity of the log message (e.g., Log.DEBUG, Log.ERROR).
     * @param tag       The tag to associate with the log message.
     * @param message   The log message to be recorded.
     * @param throwable An optional throwable associated with the log message. May be null.
     */
    private static void log(int level, String tag, String message, Throwable throwable) {
        if (!BuildVars.DEBUG_PRIVATE_VERSION) {
            return;
        }
        ensureInitialized();

        String logMessage;
        if (BuildConfig.DEBUG) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stackTrace[4];
            String fileName = caller.getFileName();
            int lineNumber = caller.getLineNumber();

            logMessage = String.format(Locale.US, "%s at %s line:%d", formatLogMessage(message, throwable), fileName, lineNumber);
        } else {
            logMessage = formatLogMessage(message, throwable);
        }

        Log.println(level, tag, logMessage);

        OctoLogging instance = getInstance();
        if (instance.streamWriter != null) {
            instance.logQueue.postRunnable(() -> {
                try {
                    String timestamp = instance.dateFormat.format(System.currentTimeMillis());
                    instance.streamWriter.write(String.format(Locale.US, "%s %s/%s: %s%n", timestamp, getLogLevel(level), tag, logMessage));

                    if (throwable != null) {
                        writeThrowableToFile(instance.streamWriter, throwable);
                    }

                    instance.streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public static String parseStackTrace(String message, Throwable throwable) {
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StackTraceElement[] stackTraceElements = throwable.getStackTrace();
            StringBuilder stackTraceBuilder = new StringBuilder();

            stackTraceBuilder.append("Exception: ").append(throwable.getClass().getSimpleName()).append(" - ").append(throwable.getMessage()).append("\nStack:\n");

            for (StackTraceElement element : stackTraceElements) {
                stackTraceBuilder.append("  ")
                        .append(element.getClassName()).append(".")
                        .append(element.getMethodName()).append("(")
                        .append(element.getFileName()).append(":")
                        .append(element.getLineNumber()).append(")\n");
            }

            return message + "\n" + stackTraceBuilder;
        } else {
            return message;
        }
    }

    private static String formatLogMessage(String message, Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        if (message != null) {
            builder.append(message);
        }
        if (throwable != null) {
            if (builder.length() > 0) builder.append(": ");
            builder.append(throwable);
        }
        return builder.toString();
    }

    private static void writeThrowableToFile(OutputStreamWriter writer, Throwable throwable) throws Exception {
        Throwable current = throwable;
        while (current != null) {
            writer.write("Caused by: " + current + "\n");
            for (StackTraceElement element : current.getStackTrace()) {
                writer.write("\tat " + element + "\n");
            }
            current = current.getCause();
        }
    }

    private static String getLogLevel(int priority) {
        return switch (priority) {
            case Log.DEBUG -> "D";
            case Log.ERROR -> "E";
            default -> "I";
        };
    }

    public static void w(String tag, String message, Exception e) {
        log(Log.WARN, tag, message, e);
    }
    /*public static void dumpHprofMemory(){
        getInstance().dumpMemory();
    }
    public static void fatal(Throwable e) {
        if (!BuildVars.DEBUG_PRIVATE_VERSION) {
            return;
        }
        if (e instanceof OutOfMemoryError) {
            getInstance().dumpMemory();
        }

        ensureInitialized();
        e.printStackTrace();
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " FATAL/octogram: " + e + "\n");
                    StackTraceElement[] stack = e.getStackTrace();
                    for (StackTraceElement stackTraceElement : stack) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " FATAL/octogram: \tat " + stackTraceElement + "\n");
                    }
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/octogram: Caused by " + cause + "\n");
                        stack = cause.getStackTrace();
                        for (StackTraceElement stackTraceElement : stack) {
                            getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/octogram: \tat " + stackTraceElement + "\n");
                        }
                    }
                    getInstance().streamWriter.flush();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                if (BuildVars.DEBUG_PRIVATE_VERSION) {
                    System.exit(2);
                }
            });
        } else {
            e.printStackTrace();
            if (BuildVars.DEBUG_PRIVATE_VERSION) {
                System.exit(2);
            }
        }
    }

    private static long dumpedHeap;
    private void dumpMemory() {
        if (System.currentTimeMillis() - dumpedHeap < 30_000) return;
        dumpedHeap = System.currentTimeMillis();
        try {
            Debug.dumpHprofData(new File(filesDir, getInstance().dateFormat.format(System.currentTimeMillis()) + FILE_EXTENSION_HEAP_DUMP).getAbsolutePath());
        } catch (Exception e2) {
            OctoLogging.e(e2);
        }
    }*/

    private void initialize() {
        if (isInitialized) return;

        dateFormat = FastDateFormat.getInstance("dd_MM_yyyy_HH_mm_ss.SSS", Locale.US);
        String logFileName = FastDateFormat.getInstance("dd_MM_yyyy", Locale.US).format(System.currentTimeMillis()) + FILE_EXTENSION;

        try {
            File logFile = new File(filesDir, logFileName);
            logFile.createNewFile();

            FileOutputStream stream = new FileOutputStream(logFile);
            streamWriter = new OutputStreamWriter(stream);
            streamWriter.write("----- Start log " + logFileName + " -----\n");
            streamWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logQueue = new DispatchQueue("logQueue");
        isInitialized = true;
    }

    public static ArrayList<File> getLogFiles() {
        File[] files = filesDir.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));

        if (files != null) {
            ArrayList<File> list = new ArrayList<>(Arrays.asList(files));
            list.sort(Comparator.comparingLong(File::lastModified).reversed());

            return list;
        }
        return new ArrayList<>();
    }

    public static void deleteLogs() {
        ArrayList<File> logFiles = getLogFiles();
        for (File file : logFiles) {
            boolean ignored = file.delete();
        }
    }
}
