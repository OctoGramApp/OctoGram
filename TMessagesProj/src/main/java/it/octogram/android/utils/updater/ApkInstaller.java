/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.updater;

import static org.telegram.messenger.LocaleController.getString;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.os.Build;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.XiaomiUtilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.ui.bottomsheets.UpdatingAppBottomSheet;
import it.octogram.android.app.ui.bottomsheets.NewUpdateAvailableBottomSheet;
import it.octogram.android.utils.OctoLogging;

public class ApkInstaller {
    private static boolean hasProgressListener = false;
    private static final ArrayList<Integer> managedSessionIds = new ArrayList<>();

    private static WeakReference<UpdatingAppBottomSheet> currentBottomSheet = null;

    public static void install(TLRPC.Document document, int accountInstance, boolean forceAndroidInstaller) {
        if (document == null) {
            return;
        }

        File f = FileLoader.getInstance(accountInstance).getPathToAttach(document, true);
        if (f == null || !f.exists()) {
            return;
        }

        if (forceAndroidInstaller) {
            fallback(document);
            return;
        }

        if (XiaomiUtilities.isMIUI() && OctoConfig.INSTANCE.isUpdaterXiaomiBlockedInstaller.getValue()) {
            fallback(document);
            return;
        }

        if (currentBottomSheet == null || currentBottomSheet.get() == null || !currentBottomSheet.get().isShown()) {
            UpdatingAppBottomSheet sheet = new UpdatingAppBottomSheet(LaunchActivity.instance, true);
            sheet.show();
            currentBottomSheet = new WeakReference<>(sheet);
            if (NewUpdateAvailableBottomSheet.isVisible && UpdatesManager.updateAppAlertDialog != null && UpdatesManager.updateAppAlertDialog.get() != null) {
                UpdatesManager.updateAppAlertDialog.get().dismiss();
            }
        }

        PackageInstaller installer = LaunchActivity.instance.getPackageManager().getPackageInstaller();

        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }

        try {
            int sessionId = installer.createSession(params);
            managedSessionIds.add(sessionId);
            PackageInstaller.Session session = installer.openSession(sessionId);

            registerProgressListener();

            try (OutputStream out = session.openWrite("base.apk", 0, -1);
                 InputStream in = new FileInputStream(f)) {

                byte[] buffer = new byte[65536];
                int c;
                while ((c = in.read(buffer)) != -1) {
                    out.write(buffer, 0, c);
                }
                session.fsync(out);
            }

            Intent intent = new Intent(LaunchActivity.instance, InstallResultReceiver.class);
            intent.setAction(LaunchActivity.instance.getPackageName() + ".INSTALL_COMPLETE");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    LaunchActivity.instance,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );

            ContextCompat.registerReceiver(LaunchActivity.instance, new InstallResultReceiver(), new IntentFilter(intent.getAction()), ContextCompat.RECEIVER_NOT_EXPORTED);

            session.commit(pendingIntent.getIntentSender());
            session.close();
        } catch (IOException e) {
            fallback(document);
        }
    }

    private static void registerProgressListener() {
        if (hasProgressListener) {
            return;
        }

        hasProgressListener = true;
        LaunchActivity.instance.getPackageManager().getPackageInstaller().registerSessionCallback(new PackageInstaller.SessionCallback() {
            @Override
            public void onCreated(int sessionId) {

            }

            @Override
            public void onBadgingChanged(int sessionId) {

            }

            @Override
            public void onActiveChanged(int sessionId, boolean active) {

            }

            @Override
            public void onProgressChanged(int sessionId, float progress) {
                if (managedSessionIds.contains(sessionId) && currentBottomSheet != null && currentBottomSheet.get() != null) {
                    currentBottomSheet.get().setProgress(progress);
                }
            }

            @Override
            public void onFinished(int sessionId, boolean success) {
                if (managedSessionIds.contains(sessionId)) {
                    managedSessionIds.removeIf(x -> x == sessionId);
                }
            }
        });
    }

    private static void fallback(TLRPC.Document document) {
        AndroidUtilities.runOnUIThread(() -> AndroidUtilities.openForView(document, true, LaunchActivity.instance));
    }

    private static void showError(int status) {
        if (status == PackageInstaller.STATUS_FAILURE_ABORTED) {
            return;
        }

        String message;
        switch (status) {
            case PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_BLOCKED -> message = getString(R.string.UpdatesInstallationBlocked);
            case PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> message = getString(R.string.UpdatesInstallationIncompatible);
            case PackageInstaller.STATUS_FAILURE_CONFLICT -> message = getString(R.string.UpdatesInstallationConflict);
            case PackageInstaller.STATUS_FAILURE_STORAGE -> message = getString(R.string.UpdatesInstallationNoSpace);
            default -> message = getString(R.string.UpdatesInstallationGeneric);
        }

        message += " ("+status+")";

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LaunchActivity.instance);
        alertDialog.setTitle(getString(R.string.AppName));
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(getString(R.string.Retry), (di, w) -> UpdatesManager.INSTANCE.installUpdate(true));
        alertDialog.setNegativeButton(getString(R.string.Cancel), null);
        alertDialog.show();
    }

    public static class InstallResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

            if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (confirmationIntent != null) {
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(confirmationIntent);
                }
            } else {
                if (currentBottomSheet != null && currentBottomSheet.get() != null) {
                    currentBottomSheet.get().dismiss();
                    currentBottomSheet.clear();
                }

                if (status == PackageInstaller.STATUS_SUCCESS) {
                    OctoLogging.d("InstallResult", "Installation succeeded!");
                } else if (XiaomiUtilities.isMIUI() && (status == PackageInstaller.STATUS_FAILURE || status == PackageInstaller.STATUS_FAILURE_BLOCKED || status == PackageInstaller.STATUS_FAILURE_ABORTED)) {
                    OctoConfig.INSTANCE.isUpdaterXiaomiBlockedInstaller.updateValue(true);
                    UpdatesManager.INSTANCE.installUpdate(true);
                } else {
                    OctoLogging.e("InstallResult", "Installation failed: " + msg + " - stat: "+status);
                    showError(status);
                }
            }
        }
    }
}
