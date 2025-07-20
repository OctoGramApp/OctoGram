/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.data;

import org.telegram.messenger.ApplicationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;

public class LogsMigrator {
    private static final String TAG = "LogsMigrator";

    public static void migrateOldLogs() {
        if (OctoConfig.INSTANCE.isMigrateOldLogs.getValue()) {
            return;
        }

        File oldLogsDir = ApplicationLoader.applicationContext.getFilesDir();
        if (oldLogsDir == null) {
            OctoLogging.e(TAG, "Migration: Failed to retrieve old logs directory.");
            return;
        }

        File[] oldLogs = oldLogsDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (oldLogs == null || oldLogs.length == 0) {
            OctoLogging.e(TAG, "No old logs to migrate");
            return;
        }

        OctoLogging.e(TAG, "Migrating old logs");
        for (File oldLog : oldLogs) {
            File newLog = new File(OctoUtils.getLogsDir(), oldLog.getName());
            File parentFile = newLog.getParentFile();
            try {
                if (parentFile != null && (parentFile.exists() || parentFile.mkdirs())) {
                    copyFile(oldLog, newLog);
                    OctoLogging.e(TAG, "Crashlytics: Copied log: " + oldLog.getAbsolutePath() + " to " + newLog.getAbsolutePath());
                } else {
                    OctoLogging.e(TAG, "Failed to create directories");
                }
            } catch (IOException e) {
                OctoLogging.e(TAG, "Crashlytics: Failed to migrate log: " + oldLog.getAbsolutePath() + " to " + newLog.getAbsolutePath(), e);
            }
        }
        OctoConfig.INSTANCE.isMigrateOldLogs.updateValue(true);
    }

    /**
     * @noinspection ResultOfMethodCallIgnored
     */
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

