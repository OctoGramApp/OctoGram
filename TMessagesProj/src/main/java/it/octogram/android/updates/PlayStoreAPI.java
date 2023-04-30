package it.octogram.android.updates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.NotificationCenter;

import java.util.HashMap;

import it.octogram.android.OctoConfig;

public class PlayStoreAPI {
    private static final AppUpdateManager appUpdateManager;
    private final static int UPDATE_REQUEST_CODE = 290;
    private static InstallStateUpdatedListener listener;
    private final static HashMap<String, AppDownloader.UpdateListener> listeners = new HashMap<>();
    private static int percentage = 0;
    private static long bytesDownloaded = 0, totalBytesToDownload = 0;
    private static boolean openingAppUpdate = false;

    @InstallStatus
    private static int lastInstallStatus = InstallStatus.UNKNOWN;

    static {
        appUpdateManager = AppUpdateManagerFactory.create(ApplicationLoader.applicationContext);
        loadUpdateInfo(new UpdateCheckCallback() {
            @SuppressLint("SwitchIntDef")
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && getVersionCode(appUpdateInfo) > BuildVars.BUILD_VERSION) {
                    lastInstallStatus = appUpdateInfo.installStatus();
                    switch (lastInstallStatus) {
                        case InstallStatus.DOWNLOADING:
                        case InstallStatus.PENDING:
                            listener = PlayStoreAPI::handleStatus;
                            appUpdateManager.registerListener(listener);
                            bytesDownloaded = appUpdateInfo.bytesDownloaded();
                            totalBytesToDownload = appUpdateInfo.totalBytesToDownload();
                            percentage = (int) (bytesDownloaded * 100 / totalBytesToDownload);
                            onPreStart();
                            onProgressChange(percentage, bytesDownloaded, totalBytesToDownload);
                            break;
                        case InstallStatus.DOWNLOADED:
                            onPreStart();
                            break;
                    }
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private static void loadUpdateInfo(UpdateCheckCallback updateInfoCallback) {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(updateInfoCallback::onSuccess);
        appUpdateInfoTask.addOnFailureListener(updateInfoCallback::onError);
    }

    public static void checkUpdates(UpdateCheckCallback updateInfoCallback) {
        loadUpdateInfo(new UpdateCheckCallback() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                int versionCode = getVersionCode(appUpdateInfo);
                if (versionCode >= BuildVars.BUILD_VERSION && appUpdateInfo.updateAvailability() >= UpdateAvailability.UPDATE_AVAILABLE) {
                    updateInfoCallback.onSuccess(appUpdateInfo);
                } else {
                    updateInfoCallback.onError(new NoUpdateException("No updates available, current version is " + BuildVars.BUILD_VERSION + " and available version is " + versionCode));
                }
            }

            @Override
            public void onError(Exception e) {
                updateInfoCallback.onError(e);
            }
        });
    }

    public static class NoUpdateException extends Exception {
        public NoUpdateException(String message) {
            super(message);
        }
    }

    public static void openUpdatePopup(Activity activity) {
        if (openingAppUpdate) return;
        openingAppUpdate = true;
        percentage = 0;
        bytesDownloaded = 0;
        totalBytesToDownload = 0;
        loadUpdateInfo(new UpdateCheckCallback() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, activity, UPDATE_REQUEST_CODE);
                    NotificationCenter.getGlobalInstance().addObserver(new NotificationCenter.NotificationCenterDelegate() {
                        @Override
                        public void didReceivedNotification(int id, int account, Object... args) {
                            int request = (int) args[0];
                            int result = (int) args[1];
                            if (request == UPDATE_REQUEST_CODE) {
                                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.onActivityResultReceived);
                                if (result == Activity.RESULT_OK) {
                                    listener = PlayStoreAPI::handleStatus;
                                    appUpdateManager.registerListener(listener);
                                    lastInstallStatus = InstallStatus.PENDING;
                                } else if (result == Activity.RESULT_CANCELED) {
                                    OctoConfig.remindUpdate(getVersionCode(appUpdateInfo));
                                    lastInstallStatus = InstallStatus.UNKNOWN;
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.appUpdateAvailable);
                                }
                            }
                        }
                    }, NotificationCenter.onActivityResultReceived);
                } catch (IntentSender.SendIntentException e) {
                    FileLog.e("PlayStore API Error", e);
                }
                openingAppUpdate = false;
            }

            @Override
            public void onError(Exception e) {
                FileLog.e("PlayStore API Load Update Error", e);
                openingAppUpdate = false;
            }
        });
    }

    @SuppressLint("SwitchIntDef")
    public static void handleStatus(InstallState state) {
        lastInstallStatus = state.installStatus();
        switch (lastInstallStatus) {
            case InstallStatus.PENDING:
                onPreStart();
                break;
            case InstallStatus.DOWNLOADING:
                bytesDownloaded = state.bytesDownloaded();
                totalBytesToDownload = state.totalBytesToDownload();
                percentage = (int) (bytesDownloaded * 100 / totalBytesToDownload);
                onProgressChange(percentage, bytesDownloaded, totalBytesToDownload);
                break;
            case InstallStatus.DOWNLOADED:
            case InstallStatus.CANCELED:
            case InstallStatus.FAILED:
                appUpdateManager.unregisterListener(listener);
                onFinished();
                break;
        }
    }

    public static int getDownloadProgress() {
        return percentage;
    }

    public static boolean isRunningDownload() {
        return lastInstallStatus == InstallStatus.DOWNLOADING || lastInstallStatus == InstallStatus.PENDING;
    }

    public static boolean updateDownloaded() {
        return lastInstallStatus == InstallStatus.DOWNLOADED;
    }

    public static long downloadedBytes() {
        return bytesDownloaded;
    }

    public static long totalBytes() {
        return totalBytesToDownload;
    }

    public static int getVersionCode(AppUpdateInfo appUpdateInfo) {
        if (appUpdateInfo != null) {
            return appUpdateInfo.availableVersionCode() / 10;
        }
        return 0;
    }

    private static void onPreStart() {
        for (AppDownloader.UpdateListener value : listeners.values()) {
            if (value != null) value.onPreStart();
        }
    }

    private static void onProgressChange(int percentage, long downBytes, long totBytes) {
        for (AppDownloader.UpdateListener value : listeners.values()) {
            if (value != null) value.onProgressChange(percentage, downBytes, totBytes);
        }
    }

    private static void onFinished() {
        for (AppDownloader.UpdateListener value : listeners.values()) {
            if (value != null) value.onFinished();
        }
    }

    public static void addListener(String key, AppDownloader.UpdateListener listener) {
        listeners.put(key, listener);
    }

    public static void installUpdate() {
        appUpdateManager.completeUpdate();
    }

    public interface UpdateCheckCallback {
        void onSuccess(AppUpdateInfo appUpdateInfo);

        void onError(Exception e);
    }
}
