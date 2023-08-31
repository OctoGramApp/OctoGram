package it.octogram.android.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileDownloader {
    @SuppressLint("StaticFieldLeak")
    private static final HashMap<String, DownloadThread> downloadThreads = new HashMap<>();
    private final static HashMap<String, FileDownloadListener> listeners = new HashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean downloadFile(Context context, String id, File output, String link) {
        if (downloadThreads.get(id) != null) return false;
        if (output.exists())
            output.delete();
        DownloadThread downloadThread = new DownloadThread(context, output, id);
        downloadThread.downloadFile(link);
        downloadThreads.put(id, downloadThread);
        return true;
    }

    public static void cancel(String id) {
        DownloadThread downloadThread = downloadThreads.get(id);
        if (downloadThread != null) {
            downloadThread.cancel();
        }
    }

    public static boolean isRunningDownload(String id) {
        return downloadThreads.get(id) != null;
    }

    public static long downloadedBytes(String id) {
        DownloadThread downloadThread = downloadThreads.get(id);
        if (downloadThread != null) {
            return downloadThread.total;
        }
        return 0;
    }

    public static long totalBytes(String id) {
        DownloadThread downloadThread = downloadThreads.get(id);
        if (downloadThread != null) {
            return downloadThread.fileLength;
        }
        return 0;
    }

    public static int getDownloadProgress(String id) {
        DownloadThread downloadThread = downloadThreads.get(id);
        if (downloadThread != null) {
            return downloadThread.percentage;
        }
        return 0;
    }

    private static class DownloadThread {
        private final Context mContext;
        private PowerManager.WakeLock mWakeLock;
        private final File mTargetFile;
        public long total = 0;
        public long fileLength = 0;
        public int percentage = 0;
        private boolean isDownloadCanceled = false;
        private final String id;

        public DownloadThread(Context context, File targetFile, String id) {
            this.mContext = context;
            this.mTargetFile = targetFile;
            this.id = id;
        }

        public void cancel() {
            isDownloadCanceled = true;
        }

        public void downloadFile(String link) {
            onPreExecute();
            new Thread() {
                @Override
                public void run() {
                    InputStream input = null;
                    OutputStream output = null;
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(link);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();
                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            AndroidUtilities.runOnUIThread(() -> onPostExecute(true, true));
                            return;
                        }
                        fileLength = connection.getContentLength();
                        input = connection.getInputStream();
                        output = new FileOutputStream(mTargetFile, false);

                        byte[] data = new byte[4096];
                        total = 0;
                        int count;
                        long last_update = System.currentTimeMillis();
                        AndroidUtilities.runOnUIThread(() -> onProgressUpdate(0, 0, fileLength));
                        while ((count = input.read(data)) != -1) {
                            if (isDownloadCanceled) {
                                input.close();
                                AndroidUtilities.runOnUIThread(() -> onPostExecute(true, false));
                                return;
                            }
                            total += count;
                            if (fileLength > 0) {
                                long curr_time = System.currentTimeMillis();
                                if (curr_time - last_update > 1000) {
                                    last_update = curr_time;
                                    percentage = (int) (total * 100 / fileLength);
                                    AndroidUtilities.runOnUIThread(() -> onProgressUpdate(percentage, total, fileLength));
                                }
                            }
                            output.write(data, 0, count);
                        }
                        AndroidUtilities.runOnUIThread(() -> onProgressUpdate((int) (total * 100 / fileLength), total, fileLength));
                        AndroidUtilities.runOnUIThread(() -> onPostExecute(false, false));
                    } catch (Exception e) {
                        AndroidUtilities.runOnUIThread(() -> onPostExecute(true, true));
                    } finally {
                        try {
                            if (output != null)
                                output.close();
                            if (input != null)
                                input.close();
                        } catch (IOException ignored) {
                        }
                        if (connection != null)
                            connection.disconnect();
                    }
                }
            }.start();
        }

        private void onProgressUpdate(int percentage, long downBytes, long totBytes) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadProgressChanged);
            onProgressChange(id, percentage, downBytes, totBytes);
        }

        private void onPreExecute() {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(10 * 60 * 1000L);
            onPreStart(id);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void onPostExecute(boolean isCanceled, boolean isFailed) {
            mWakeLock.release();
            if (isCanceled) {
                mTargetFile.delete();
            }
            downloadThreads.remove(id);
            onFinished(id, isFailed);
        }
    }

    public static void addListener(String downloadID, String key, FileDownloadListener listener) {
        listeners.put(downloadID + "_" + key, listener);
    }

    private static void onPreStart(String id) {
        for (Map.Entry<String, FileDownloadListener> value : listeners.entrySet()) {
            if (value != null) {
                String lId = value.getKey().split("_")[0];
                if (lId.equals(id)) value.getValue().onPreStart(id);
            }
        }
    }

    private static void onProgressChange(String id, int percentage, long downBytes, long totBytes) {
        for (Map.Entry<String, FileDownloadListener> value : listeners.entrySet()) {
            if (value != null) {
                String lId = value.getKey().split("_")[0];
                if (lId.equals(id)) {
                    value.getValue().onProgressChange(id, percentage, downBytes, totBytes);
                }
            }
        }
    }

    private static void onFinished(String id, boolean isFailed) {
        for (Map.Entry<String, FileDownloadListener> value : listeners.entrySet()) {
            if (value != null) {
                String lId = value.getKey().split("_")[0];
                if (lId.equals(id)) {
                    value.getValue().onFinished(id, isFailed);
                }
            }
        }
    }

    public ArrayList<String> getActiveDownloads() {
        ArrayList<String> activeDownloads = new ArrayList<>();
        for (Map.Entry<String, DownloadThread> value : downloadThreads.entrySet()) {
            if (value != null) {
                activeDownloads.add(value.getKey());
            }
        }
        return activeDownloads;
    }

    public interface FileDownloadListener {
        void onPreStart(String id);

        void onProgressChange(String id, int percentage, long downBytes, long totBytes);

        void onFinished(String id, boolean isFailed);
    }
}
