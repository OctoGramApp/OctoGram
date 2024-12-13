/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui.components;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;

@SuppressLint("ViewConstructor")
public class CustomUpdatesCheckCell extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {
    private final AnimatedTextView leftTextView;
    private final TextView checkAvailableUpdatesView;
    private int state;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getInstance(0).addObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(0).addObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getInstance(0).addObserver(this, NotificationCenter.fileLoadFailed);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getInstance(0).removeObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(0).removeObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getInstance(0).removeObserver(this, NotificationCenter.fileLoadFailed);
    }

    public CustomUpdatesCheckCell(Context context, CheckAvailableUpdatesDelegate callback) {
        this(context, 21, callback);
    }

    public CustomUpdatesCheckCell(Context context, int padding, CheckAvailableUpdatesDelegate callback) {
        super(context);

        leftTextView = new AnimatedTextView(context);
        leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        leftTextView.setTextSize(AndroidUtilities.dp(16));
        leftTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        leftTextView.setText(LocaleController.getString(R.string.UpdatesSettingsCheck));
        leftTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(leftTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, LocaleController.isRTL ? 70 : padding, 0, LocaleController.isRTL ? padding : 70, 0));

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.setDuration(LayoutTransition.CHANGING, 100);

        LinearLayout layoutRight = new LinearLayout(context);
        layoutRight.setLayoutTransition(layoutTransition);
        layoutRight.setGravity(LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT);

        checkAvailableUpdatesView = new TextView(context);
        checkAvailableUpdatesView.setText(LocaleController.getString(R.string.UpdatesSettingsCheckButton));
        checkAvailableUpdatesView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        checkAvailableUpdatesView.setTextSize(16);
        checkAvailableUpdatesView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(16), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        checkAvailableUpdatesView.setGravity(Gravity.CENTER_VERTICAL);
        checkAvailableUpdatesView.setSingleLine(true);
        checkAvailableUpdatesView.setEllipsize(TextUtils.TruncateAt.END);
        checkAvailableUpdatesView.setOnClickListener(view -> callback.onClick());
        checkAvailableUpdatesView.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16), 0);
        layoutRight.addView(checkAvailableUpdatesView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 35));

        addView(layoutRight, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 22, 0, 22, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(150, MeasureSpec.EXACTLY)
        );
    }

    public void updateState(int state, float loadProgress) {
        switch (state) {
            case CheckCellState.NO_UPDATE_AVAILABLE:
                leftTextView.setText(LocaleController.getString(R.string.UpdatesSettingsCheck));
                checkAvailableUpdatesView.setText(LocaleController.getString(R.string.UpdatesSettingsCheckButton));
                resetButtonState();
            break;
            case CheckCellState.CHECKING_UPDATES:
                leftTextView.setText(LocaleController.getString(R.string.UpdatesSettingsCheck));
                checkAvailableUpdatesView.setText(LocaleController.getString(R.string.UpdatesSettingsCheckButtonChecking));
                disableButtonClick();
            break;
            case CheckCellState.UPDATE_NEED_DOWNLOAD:
                leftTextView.setText(LocaleController.getString(R.string.UpdatesSettingsCheckAvailable));
                checkAvailableUpdatesView.setText(LocaleController.getString(R.string.UpdatesSettingsCheckButtonDownload));
                resetButtonState();
            break;
            case CheckCellState.UPDATE_IS_DOWNLOADING:
                leftTextView.setText(LocaleController.formatString(R.string.AppUpdateDownloading, (int) (loadProgress * 100)));
                hideButton();
            break;
            case CheckCellState.UPDATE_IS_READY:
                leftTextView.setText(LocaleController.getString(R.string.UpdatesSettingsCheckReady));
                checkAvailableUpdatesView.setText(LocaleController.getString(R.string.UpdatesSettingsCheckButtonInstall));
                resetButtonState();
        }

        if (state == CheckCellState.UPDATE_IS_DOWNLOADING && this.state != CheckCellState.UPDATE_IS_DOWNLOADING) {
            leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        } else if (state != CheckCellState.UPDATE_IS_DOWNLOADING && this.state == CheckCellState.UPDATE_IS_DOWNLOADING) {
            leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        }

        this.state = state;
    }

    public int getCurrentState() {
        return state;
    }

    public void updateState(int state) {
        updateState(state, 0);
    }

    private void resetButtonState() {
        checkAvailableUpdatesView.setClickable(true);
        checkAvailableUpdatesView.setEnabled(true);
        checkAvailableUpdatesView.setVisibility(VISIBLE);
    }

    private void disableButtonClick() {
        checkAvailableUpdatesView.setClickable(false);
        checkAvailableUpdatesView.setEnabled(false);
        checkAvailableUpdatesView.setVisibility(VISIBLE);
    }

    private void hideButton() {
        checkAvailableUpdatesView.setClickable(false);
        checkAvailableUpdatesView.setEnabled(false);
        checkAvailableUpdatesView.setVisibility(GONE);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (!SharedConfig.isAppUpdateAvailable()) {
            return;
        }

        String path = (String) args[0];
        String name = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);

        if (!name.equals(path)) {
            return;
        }

        if (id == NotificationCenter.fileLoaded) {
            updateState(CheckCellState.UPDATE_IS_READY);
        } else if (id == NotificationCenter.fileLoadProgressChanged) {
            Long loadedSize = (Long) args[1];
            Long totalSize = (Long) args[2];
            float loadProgress = loadedSize / (float) totalSize;

            updateState(CheckCellState.UPDATE_IS_DOWNLOADING, loadProgress);
        } else if (id == NotificationCenter.fileLoadFailed) {
            // force re-check data from the beginning

            File completePathFileName = FileLoader.getInstance(0).getPathToAttach(SharedConfig.pendingAppUpdate.document, true);
            if (completePathFileName.exists()) {
                updateState(CheckCellState.UPDATE_IS_READY);
            } else {
                if (FileLoader.getInstance(0).isLoadingFile(name)) {
                    Float p = ImageLoader.getInstance().getFileProgress(name);
                    updateState(CheckCellState.UPDATE_IS_DOWNLOADING, (p != null ? p : 0.0f));
                } else {
                    updateState(CheckCellState.UPDATE_NEED_DOWNLOAD);
                }
            }
        }
    }

    public interface CheckAvailableUpdatesDelegate {
        void onClick();
    }

    public static class CheckCellState {
        public static final int NO_UPDATE_AVAILABLE = 1;
        public static final int CHECKING_UPDATES = 2;
        public static final int UPDATE_NEED_DOWNLOAD = 3;
        public static final int UPDATE_IS_DOWNLOADING = 4;
        public static final int UPDATE_IS_READY = 5;
    }
}
