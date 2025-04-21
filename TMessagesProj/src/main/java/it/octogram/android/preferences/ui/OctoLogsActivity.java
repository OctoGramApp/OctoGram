/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import it.octogram.android.crashlytics.CrashOption;
import it.octogram.android.crashlytics.Crashlytics;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.ui.components.CrashManagementComponent;

@SuppressLint("NotifyDataSetChanged")
public class OctoLogsActivity extends BaseFragment implements CrashManagementComponent.CrashManagementComponentDelegate {
    private static final String TAG = "OctoLogsActivity";
    private CrashManagementComponent crashManagementComponent;

    @Override
    public boolean onFragmentCreate() {
        OctoLogging.d(TAG, "onFragmentCreate");
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        OctoLogging.d(TAG, "createView");
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("OctoGram Logs");
        actionBar.setAllowOverlayTitle(true);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }

        FrameLayout fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        crashManagementComponent = new CrashManagementComponent(context, this, actionBar, this);
        fragmentView.addView(crashManagementComponent, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView.setTag(Theme.key_windowBackgroundGray);
        return fragmentView;
    }

    @Override
    public void onCrashActionFinished() {
        OctoLogging.d(TAG, "onCrashActionFinished called in Activity");
    }

    @Override
    public BaseFragment getParentFragmentForDialogs() {
        return this;
    }

    @Override
    public int getCurrentAccount() {
        return currentAccount;
    }

    @Override
    public File[] getArchivedCrashFiles() {
        return OctoLogging.getLogFiles();
    }

    @Override
    public File getLatestArchivedCrashFile() {
        File[] files = OctoLogging.getLogFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        return Arrays.stream(files)
                .filter(Objects::nonNull)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }

    @Override
    public CharSequence getSystemInfo(boolean full, CharSequence lastModified) throws IllegalAccessException {
        return Crashlytics.getSystemInfo(full, lastModified);
    }

    @Override
    public File shareLog(File logFile) throws IOException {
        return Crashlytics.shareLog(logFile.getAbsoluteFile());
    }

    @Override
    public void deleteCrashLogs() {
        OctoLogging.deleteLogs();
    }

    @Nullable
    @Override
    public List<CrashManagementComponent.CrashOptionItem> getCrashOptionItems() {
        List<CrashManagementComponent.CrashOptionItem> items = new ArrayList<>();

        items.add(crashManagementComponent.createItem(
                R.string.OpenCrashLog,
                R.drawable.msg_openin,
                CrashOption.OPEN_LOG,
                file -> {
                    if (file == null || !crashManagementComponent.openCrashLogFile(file)) {
                        crashManagementComponent.showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                    }
                }
        ));
        items.add(crashManagementComponent.createItem(
                R.string.SendCrashLog,
                R.drawable.msg_send,
                CrashOption.SEND_LOG,
                file -> {
                    if (file == null || !crashManagementComponent.sendCrashLogFile(file)) {
                        crashManagementComponent.showWarningDialog(getString(R.string.ErrorSendingCrashContent));
                    }
                }
        ));

        return items;
    }

    @Nullable
    @Override
    public String getCrashMenuDialogTitle() {
        return "Logs (Debug)";
    }

    @Override
    public boolean onBackPressed() {
        if (crashManagementComponent != null) {
            if (!crashManagementComponent.onBackPressed()) {
                return false;
            }
        }
        return super.onBackPressed();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (crashManagementComponent != null) {
            crashManagementComponent.onBackPressed();
            crashManagementComponent = null;
        }
    }
}