package it.octogram.android.utils;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import it.octogram.android.OctoConfig;

public class LogsMigrator {
    public static void migrateOldLogs() {
        if (OctoConfig.INSTANCE.isMigrateOldLogs.getValue()) {
            return;
        }

        File oldLogsDir = ApplicationLoader.applicationContext.getFilesDir();
        if (oldLogsDir == null) {
            FileLog.e("Migration: Failed to retrieve old logs directory.");
            return;
        }

        File[] oldLogs = oldLogsDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (oldLogs == null || oldLogs.length == 0) {
            FileLog.e("Crashlytics: No old logs to migrate");
            return;
        }

        FileLog.e("Crashlytics: Migrating old logs");
        for (File oldLog : oldLogs) {
            File newLog = new File(OctoUtils.getLogsDir(), oldLog.getName());
            File parentFile = newLog.getParentFile();
            try {
                if (parentFile != null && (parentFile.exists() || parentFile.mkdirs())) {
                    copyFile(oldLog, newLog);
                    FileLog.e("Crashlytics: Copied log: " + oldLog.getAbsolutePath() + " to " + newLog.getAbsolutePath());
                } else {
                    FileLog.e("Failed to create directories");
                }
            } catch (IOException e) {
                FileLog.e("Crashlytics: Failed to migrate log: " + oldLog.getAbsolutePath() + " to " + newLog.getAbsolutePath(), e);
            }
        }
        OctoConfig.INSTANCE.isMigrateOldLogs.updateValue(true);
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        try (FileInputStream in = new FileInputStream(sourceFile);
             FileOutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }

        long lastModified = sourceFile.lastModified();
        destFile.setLastModified(lastModified);
    }
}

