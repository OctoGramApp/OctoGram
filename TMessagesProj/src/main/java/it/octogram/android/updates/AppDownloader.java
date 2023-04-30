package it.octogram.android.updates;

import android.app.Activity;

import it.octogram.android.StoreUtils;
import it.octogram.android.http.FileDownloader;

public class AppDownloader {

    public static int getDownloadProgress() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.getDownloadProgress();
        } else {
            return FileDownloader.getDownloadProgress("appUpdate");
        }
    }

    public static boolean isRunningDownload() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.isRunningDownload();
        } else {
            return FileDownloader.isRunningDownload("appUpdate");
        }
    }

    public static boolean updateDownloaded() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.updateDownloaded();
        } else {
            return UpdateManager.updateDownloaded();
        }
    }

    public static long downloadedBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.downloadedBytes();
        } else {
            return FileDownloader.downloadedBytes("appUpdate");
        }
    }

    public static long totalBytes() {
        if (StoreUtils.isFromPlayStore()) {
            return PlayStoreAPI.totalBytes();
        } else {
            return FileDownloader.totalBytes("appUpdate");
        }
    }

    public static void setListener(String id, UpdateListener listener) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.addListener(id, listener);
        } else {
            FileDownloader.addListener("appUpdate", id, new FileDownloader.FileDownloadListener() {
                @Override
                public void onPreStart(String id) {
                    listener.onPreStart();
                }

                @Override
                public void onProgressChange(String id, int percentage, long downBytes, long totBytes) {
                    listener.onProgressChange(percentage, downBytes, totBytes);
                }

                @Override
                public void onFinished(String id, boolean isFailed) {
                    listener.onFinished();
                }
            });
        }
    }

    public static void installUpdate(Activity activity) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.installUpdate();
        } else {
            UpdateManager.installUpdate(activity);
        }
    }

    public interface UpdateListener {
        void onPreStart();

        void onProgressChange(int percentage, long downBytes, long totBytes);

        void onFinished();
    }
}
