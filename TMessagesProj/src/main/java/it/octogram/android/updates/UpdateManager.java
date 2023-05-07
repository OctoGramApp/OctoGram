package it.octogram.android.updates;

import android.app.Activity;
import android.content.pm.PackageInfo;

import androidx.core.util.Pair;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.StoreUtils;
import it.octogram.android.entities.HTMLKeeper;
import it.octogram.android.http.FileDownloader;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.magic.OWLENC;

public class UpdateManager {

    public static void showToast(final String message) {
        Context mContext = ApplicationLoader.applicationContext;
        AndroidUtilities.runOnUIThread(() -> Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show());
    }
    public static boolean checkingForChangelogs = false;

    public static void isDownloadedUpdate(UpdateUICallback updateUICallback) {
        new Thread() {
            @Override
            public void run() {
                boolean result = AppDownloader.updateDownloaded();
                AndroidUtilities.runOnUIThread(() -> updateUICallback.onResult(result));
            }
        }.start();
    }

    public interface UpdateUICallback {
        void onResult(boolean result);
    }

    public static String getApkChannel() {
        return OctoConfig.betaUpdates ? "OctoGramBeta" : "OctoGramAPKs";
    }

    public static void getChangelogs(ChangelogCallback changelogCallback) {
        if (checkingForChangelogs) return;
        checkingForChangelogs = true;
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        new Thread() {
            @Override
            public void run() {
                try {
                    String url = String.format("https://app.octogram.app/get_changelogs?lang=%s&version=%s", locale.getLanguage(), BuildVars.BUILD_VERSION);
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String changelog_text = obj.getString("changelogs");
                    if (!changelog_text.equals("null")) {
                        AndroidUtilities.runOnUIThread(() -> changelogCallback.onSuccess(HTMLKeeper.htmlToEntities(changelog_text, null, true, false)));
                    }
                } catch (Exception ignored) {
                } finally {
                    checkingForChangelogs = false;
                }
            }
        }.start();
    }

    public static void checkUpdates(UpdateCallback updateCallback) {
        if (StoreUtils.isFromPlayStore()) {
            PlayStoreAPI.checkUpdates(new PlayStoreAPI.UpdateCheckCallback() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    checkInternal(updateCallback, appUpdateInfo);
                }

                @Override
                public void onError(Exception e) {
                    if (e instanceof PlayStoreAPI.NoUpdateException) {
                        updateCallback.onSuccess(new UpdateNotAvailable());
                    } else {
                        updateCallback.onError(e);
                    }
                }
            });
        } else {
            checkInternal(updateCallback, null);
        }
    }

    private static void checkInternal(UpdateCallback updateCallback, AppUpdateInfo psAppUpdateInfo) {
        Locale locale = LocaleController.getInstance().getCurrentLocale();
        boolean betaMode = OctoConfig.betaUpdates && !StoreUtils.isDownloadedFromAnyStore();
        new Thread() {
            @Override
            public void run() {
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    int code = pInfo.versionCode / 10;
                    String abi = "unknown";
                    switch (pInfo.versionCode % 10) {
                        case 1:
                        case 3:
                            abi = "arm-v7a";
                            break;
                        case 2:
                        case 4:
                            abi = "x86";
                            break;
                        case 5:
                        case 7:
                            abi = "arm64-v8a";
                            break;
                        case 6:
                        case 8:
                            abi = "x86_64";
                            break;
                        case 0:
                        case 9:
                            abi = "universal";
                            break;
                    }
                    String url = String.format(locale, "https://app.octogram.app/version?lang=%s&beta=%s&abi=%s", locale.getLanguage(), betaMode, URLEncoder.encode(abi, StandardCharsets.UTF_8.name()));
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    String update_status = obj.getString("status");
                    if (update_status.equals("no_updates")) {
                        AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                    } else {
                        int remoteVersion = BuildVars.IGNORE_VERSION_CHECK ? Integer.MAX_VALUE : (psAppUpdateInfo != null ? PlayStoreAPI.getVersionCode(psAppUpdateInfo) : obj.getInt("version"));
                        if (remoteVersion > code) {
                            OWLENC.UpdateAvailable updateAvailable = loadUpdate(obj);
                            OctoConfig.saveUpdateStatus(1);
                            updateAvailable.setPlayStoreMetaData(psAppUpdateInfo);
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(updateAvailable));
                        } else {
                            AndroidUtilities.runOnUIThread(() -> updateCallback.onSuccess(new UpdateNotAvailable()));
                        }
                    }
                } catch (Exception e) {
                    AndroidUtilities.runOnUIThread(() -> updateCallback.onError(e));
                }
            }
        }.start();
    }

    public static class UpdateNotAvailable {
    }

    public static OWLENC.UpdateAvailable loadUpdate(JSONObject obj) throws JSONException {
        return new OWLENC.UpdateAvailable(obj);
    }

    public static boolean isAvailableUpdate() {
        boolean updateValid = false;
        try {
            if(OctoConfig.updateData.isPresent()) {
                OWLENC.UpdateAvailable update = OctoConfig.updateData.get();
                if (update.version > BuildVars.BUILD_VERSION && !update.isReminded()) {
                    updateValid = true;
                }
            }
        } catch (Exception ignored){}
        return updateValid;
    }

    public interface UpdateCallback {
        void onSuccess(Object updateResult);

        void onError(Exception e);
    }

    public interface ChangelogCallback {
        void onSuccess(Pair<String, ArrayList<TLRPC.MessageEntity>> updateResult);
    }

    public static File apkFile() {
        return new File(AndroidUtilities.getCacheDir().getAbsolutePath() + "/update.apk");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteUpdate() {
        File file = apkFile();
        if (file.exists())
            file.delete();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.fileLoadFailed);
    }

    public static boolean updateDownloaded() {
        boolean isCorrupted = true;
        try {
            if (OctoConfig.updateData.isPresent()) {
                if (OctoConfig.updateData.get().fileSize == apkFile().length()) {
                    isCorrupted = false;
                }
            }
        } catch (Exception ignored) {
        }
        boolean isAvailableFile = apkFile().exists() && !FileDownloader.isRunningDownload("appUpdate") && !isCorrupted;
        if (((BuildVars.BUILD_VERSION >= OctoConfig.oldDownloadedVersion && !BuildVars.IGNORE_VERSION_CHECK) || OctoConfig.oldDownloadedVersion == 0) && isAvailableFile) {
            OctoConfig.updateData.set(null);
            OctoConfig.applyUpdateData();
            return false;
        }
        return isAvailableFile;
    }

    public static void installUpdate(Activity activity) {
        ApkInstaller.installApk(activity);
    }
}
