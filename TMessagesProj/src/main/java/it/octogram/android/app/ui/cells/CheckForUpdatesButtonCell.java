/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.LayoutHelper;

import it.octogram.android.utils.UpdatesManager;

@SuppressLint("ViewConstructor")
public class CheckForUpdatesButtonCell extends FrameLayout {
    private final AnimatedTextView leftTextView;
    private final TextView checkAvailableUpdatesView;
    private int state;
    private UpdatesManager.UpdatesManagerCallback callback;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        UpdatesManager.INSTANCE.addCallback(callback = new UpdatesManager.UpdatesManagerCallback() {
            @Override
            public boolean onGetStateAfterAdd() {
                return true;
            }

            @Override
            public void onNoUpdateAvailable() {
                AndroidUtilities.runOnUIThread(() -> updateState(CheckCellState.NO_UPDATE_AVAILABLE));
            }

            @Override
            public void onUpdateAvailable(TLRPC.TL_help_appUpdate update) {
                AndroidUtilities.runOnUIThread(() -> updateState(CheckCellState.UPDATE_NEED_DOWNLOAD));
            }

            @Override
            public void onUpdateDownloading(float percent) {
                AndroidUtilities.runOnUIThread(() -> updateState(CheckCellState.UPDATE_IS_DOWNLOADING, percent));
            }

            @Override
            public void onUpdateReady() {
                AndroidUtilities.runOnUIThread(() -> updateState(CheckCellState.UPDATE_IS_READY));
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        UpdatesManager.INSTANCE.removeCallback(callback);
        callback = null;
    }

    public CheckForUpdatesButtonCell(Context context) {
        this(context, 21);
    }

    public CheckForUpdatesButtonCell(Context context, int padding) {
        super(context);

        leftTextView = new AnimatedTextView(context);
        leftTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        leftTextView.setTextSize(dp(16));
        leftTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        leftTextView.setText(getString(R.string.UpdatesSettingsCheck));
        leftTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(leftTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, LocaleController.isRTL ? 70 : padding, 0, LocaleController.isRTL ? padding : 70, 0));

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.setDuration(LayoutTransition.CHANGING, 100);

        LinearLayout layoutRight = new LinearLayout(context);
        layoutRight.setLayoutTransition(layoutTransition);
        layoutRight.setGravity(LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT);

        checkAvailableUpdatesView = new TextView(context);
        checkAvailableUpdatesView.setText(getString(R.string.UpdatesSettingsCheckButton));
        checkAvailableUpdatesView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        checkAvailableUpdatesView.setTextSize(16);
        checkAvailableUpdatesView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(16), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        checkAvailableUpdatesView.setGravity(Gravity.CENTER_VERTICAL);
        checkAvailableUpdatesView.setSingleLine(true);
        checkAvailableUpdatesView.setEllipsize(TextUtils.TruncateAt.END);
        checkAvailableUpdatesView.setOnClickListener(view -> UpdatesManager.INSTANCE.onUpdateButtonPressed());
        checkAvailableUpdatesView.setPadding(dp(16), 0, dp(16), 0);
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
                leftTextView.setText(getString(R.string.UpdatesSettingsCheck));
                checkAvailableUpdatesView.setText(getString(R.string.UpdatesSettingsCheckButton));
                resetButtonState();
                break;
            case CheckCellState.CHECKING_UPDATES:
                leftTextView.setText(getString(R.string.UpdatesSettingsCheck));
                checkAvailableUpdatesView.setText(getString(R.string.UpdatesSettingsCheckButtonChecking));
                disableButtonClick();
                break;
            case CheckCellState.UPDATE_NEED_DOWNLOAD:
                leftTextView.setText(getString(R.string.UpdatesSettingsCheckAvailable));
                checkAvailableUpdatesView.setText(getString(R.string.UpdatesSettingsCheckButtonDownload));
                resetButtonState();
                break;
            case CheckCellState.UPDATE_IS_DOWNLOADING:
                leftTextView.setText(formatString(R.string.AppUpdateDownloading, (int) (loadProgress * 100)));
                hideButton();
                break;
            case CheckCellState.UPDATE_IS_READY:
                leftTextView.setText(getString(R.string.UpdatesSettingsCheckReady));
                checkAvailableUpdatesView.setText(getString(R.string.UpdatesSettingsCheckButtonInstall));
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

    public static class CheckCellState {
        public static final int NO_UPDATE_AVAILABLE = 1;
        public static final int CHECKING_UPDATES = 2;
        public static final int UPDATE_NEED_DOWNLOAD = 3;
        public static final int UPDATE_IS_DOWNLOADING = 4;
        public static final int UPDATE_IS_READY = 5;
    }
}
