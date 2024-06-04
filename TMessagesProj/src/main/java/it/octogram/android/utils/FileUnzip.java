package it.octogram.android.utils;

import android.content.Context;
import android.os.PowerManager;

import org.telegram.messenger.AndroidUtilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUnzip {
    private static final HashMap<String, UnzipThread> unzipThreads = new HashMap<>();
    private final static HashMap<String, UnzipListener> listeners = new HashMap<>();

    public static void unzipFile(Context context, String id, File input, File output) {
        if (unzipThreads.get(id) != null) return;
        if (output.exists())
            deleteFolder(output);
        UnzipThread unzipThread = new UnzipThread(context, output, id);
        unzipThread.unzipFile(input);
        unzipThreads.put(id, unzipThread);
    }

    public static boolean isRunningUnzip(String id) {
        return unzipThreads.get(id) != null;
    }

    public static void cancel(String id) {
        UnzipThread unzipThread = unzipThreads.get(id);
        if (unzipThread != null) {
            unzipThread.cancel();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFolder(File input) {
        File[] files = input.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        input.delete();
    }

    private static class UnzipThread {
        private final Context mContext;
        private PowerManager.WakeLock mWakeLock;
        private final File mOutput;
        private final String id;
        private boolean isUnzipCanceled = false;

        public UnzipThread(Context context, File outputFile, String id) {
            this.mContext = context;
            this.mOutput = outputFile;
            this.id = id;
        }

        public void cancel() {
            isUnzipCanceled = true;
        }

        public void unzipFile(File inputFile) {
            onPreExecute();
            new Thread() {
                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override
                public void run() {
                    try {
                        if (!mOutput.exists()) {
                            mOutput.mkdir();
                        }
                        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(inputFile));
                        ZipEntry entry = zipIn.getNextEntry();
                        while (entry != null) {
                            File target = new File(mOutput, entry.getName());
                            String canonicalPath = target.getCanonicalPath();
                            if (!canonicalPath.startsWith(mOutput.getAbsolutePath())) {
                                throw new SecurityException();
                            }
                            if (isUnzipCanceled) {
                                zipIn.closeEntry();
                                zipIn.close();
                                AndroidUtilities.runOnUIThread(() -> onPostExecute(true));
                                return;
                            }
                            if (!entry.isDirectory()) {
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
                                byte[] bytesIn = new byte[4096];
                                int read;
                                while ((read = zipIn.read(bytesIn)) != -1) {
                                    if (isUnzipCanceled) {
                                        zipIn.closeEntry();
                                        zipIn.close();
                                        AndroidUtilities.runOnUIThread(() -> onPostExecute(true));
                                        return;
                                    }
                                    bos.write(bytesIn, 0, read);
                                }
                                bos.close();
                            } else {
                                target.mkdir();
                            }
                            zipIn.closeEntry();
                            entry = zipIn.getNextEntry();
                        }
                        zipIn.close();
                        AndroidUtilities.runOnUIThread(() -> onPostExecute(false));
                    } catch (IOException | SecurityException e) {
                        AndroidUtilities.runOnUIThread(() -> onPostExecute(true));
                    }
                }
            }.start();
        }

        private void onPreExecute() {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(10 * 60 * 1000L);
        }

        private void onPostExecute(boolean isCanceled) {
            mWakeLock.release();
            if (isCanceled) {
                FileUnzip.deleteFolder(mOutput);
            }
            unzipThreads.remove(id);
            onFinished(id);
        }
    }

    public static void addListener(String unzipId, String key, UnzipListener listener) {
        listeners.put(unzipId + "_" + key, listener);
    }

    private static void onFinished(String id) {
        for (Map.Entry<String, UnzipListener> value : listeners.entrySet()) {
            if (value != null) {
                String lId = value.getKey().split("_")[0];
                if (lId.equals(id)) {
                    value.getValue().onFinished(id);
                }
            }
        }
    }

    public interface UnzipListener {
        void onFinished(String id);
    }
}
