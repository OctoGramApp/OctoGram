package it.octogram.android.updates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.os.Build;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.XiaomiUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import it.octogram.android.OctoConfig;
import it.octogram.ui.Components.Dialogs.UpdateInstallingDialog;

public class ApkInstaller {
    @SuppressLint("StaticFieldLeak")
    private static InstallReceiver installReceiver;
    @SuppressLint("StaticFieldLeak")
    private static UpdateInstallingDialog updateInstallingDialog;

    private static void prepareApkFile(Activity context, File apk) {
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag |= PendingIntent.FLAG_MUTABLE;
        }
        String action = ApkInstaller.class.getName();
        Intent intent = new Intent(action).setPackage(context.getPackageName());
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, flag);
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }
        try (PackageInstaller.Session session = installer.openSession(installer.createSession(params))) {
            try (FileInputStream in = new FileInputStream(apk); OutputStream out = session.openWrite(apk.getName(), 0, apk.length())) {
                transfer(in, out);
            }
            session.commit(pending.getIntentSender());
        } catch (IOException e) {
            FileLog.e(e);
        }
    }

    public static void installApk(Activity context) {
        File apk = UpdateManager.apkFile();
        if (OctoConfig.xiaomiBlockedInstaller && XiaomiUtilities.isMIUI()) {
            AndroidUtilities.openForView(apk, "update.apk", "application/vnd.android.package-archive", context, null);
            return;
        }
        updateInstallingDialog = new UpdateInstallingDialog(context);
        updateInstallingDialog.show();
        if (installReceiver != null) {
            context.unregisterReceiver(installReceiver);
            installReceiver = null;
        }
        Utilities.globalQueue.postRunnable(() -> {
            installReceiver = register(context, () -> {
                if (installReceiver.resultValue != PackageInstaller.STATUS_PENDING_USER_ACTION) {
                    if (checkFailed(installReceiver.resultValue)) {
                        if (checkFailedByXiaomi(installReceiver.resultValue)) {
                            updateInstallingDialog.cancel();
                            OctoConfig.setXiaomiBlockedInstaller();
                            installApk(context);
                        } else {
                            updateInstallingDialog.setError(getErrorMessage(installReceiver.resultValue));
                        }
                    } else {
                        updateInstallingDialog.cancel();
                    }
                    installReceiver = null;
                }
            });
            prepareApkFile(context, apk);
            Intent intent = installReceiver.waitIntent();
            if (intent != null) {
                context.startActivity(intent);
            }
        });
    }

    public static String getErrorMessage(int status) {
        switch (status) {
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
            case PackageInstaller.STATUS_FAILURE:
                return LocaleController.getString("InstallationBlocked", R.string.InstallationBlocked);
            case PackageInstaller.STATUS_FAILURE_INVALID:
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                return LocaleController.getString("InstallationFailure", R.string.InstallationFailure);
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                return LocaleController.getString("NoMoreSpace", R.string.NoMoreSpace);
            default:
                return LocaleController.getString("UnknownInstallationError", R.string.UnknownInstallationError);
        }
    }

    private static boolean checkFailedByXiaomi(int status) {
        switch (status) {
            case PackageInstaller.STATUS_FAILURE:
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                return XiaomiUtilities.isMIUI();
        }
        return false;
    }

    private static boolean checkFailed(int status) {
        switch (status) {
            case PackageInstaller.STATUS_FAILURE:
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
            case PackageInstaller.STATUS_FAILURE_INVALID:
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                return true;
            default:
                return false;
        }
    }

    private static float checkInstallation(Context context) {
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }
        List<PackageInstaller.SessionInfo> sessionInfoList = installer.getMySessions();
        float statusInstallation = 0f;
        for (int i = 0; i < sessionInfoList.size(); i++) {
            statusInstallation = Math.max(sessionInfoList.get(i).getProgress(), 0);
        }
        return statusInstallation;
    }

    public static void clearUpdates(Context context) {
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }
        List<PackageInstaller.SessionInfo> sessionInfoList = installer.getMySessions();
        for (int i = 0; i < sessionInfoList.size(); i++) {
            installer.abandonSession(sessionInfoList.get(i).getSessionId());
        }
    }

    public static void checkCanceledInstallation(Context context) {
        AndroidUtilities.runOnUIThread(() -> {
            float installationProgress = checkInstallation(context);
            if (updateInstallingDialog != null) {
                updateInstallingDialog.setInstallingProgress(installationProgress);
            }
            if (installationProgress < 0.9f) {
                clearUpdates(context);
                if (updateInstallingDialog != null) {
                    updateInstallingDialog.cancel();
                }
                if (installReceiver != null) {
                    context.unregisterReceiver(installReceiver);
                    installReceiver = null;
                }
            }
        }, 250);
    }

    private static InstallReceiver register(Context context, Runnable onSuccess) {
        InstallReceiver receiver = new InstallReceiver(context, ApplicationLoader.getApplicationId(), onSuccess);
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        context.registerReceiver(receiver, filter);
        context.registerReceiver(receiver, new IntentFilter(ApkInstaller.class.getName()));
        return receiver;
    }

    private static void transfer(InputStream in, OutputStream out) throws IOException {
        int size = 8192;
        byte[] buffer = new byte[size];
        int read;
        while ((read = in.read(buffer, 0, size)) >= 0) {
            out.write(buffer, 0, read);
        }
    }
}
