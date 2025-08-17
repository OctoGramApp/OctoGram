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

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.utils.updater.UpdatesManager;

@SuppressLint("ViewConstructor")
public class UpdatesHeaderCell extends LinearLayout {
    private final ButtonWithCounterView buttonWithCounterView;
    private final AnimatedTextView textView;

    private UpdatesManager.UpdatesManagerCallback callback;

    public UpdatesHeaderCell(Context context) {
        super(context);

        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        StickerImageView rLottieImageView = new StickerImageView(context, UserConfig.selectedAccount);
        rLottieImageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        rLottieImageView.getImageReceiver().setAutoRepeat(1);
        rLottieImageView.setStickerNum(StickerUi.UPDATES.getValue());

        addView(rLottieImageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

        textView = new AnimatedTextView(context, true, false, false);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 20, 0, 36, 26, 36, 0));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Theme.getColor(Theme.key_chats_message));
        textView.setTextSize(dp(14));
        textView.setText(getString(R.string.UpdatesSettingsCheckUpdated));

        buttonWithCounterView = new ButtonWithCounterView(context, null);
        buttonWithCounterView.setPadding(dp(34), 0, dp(34), 0);
        buttonWithCounterView.setOnClickListener(view -> {
            if (!_isClickable) {
                return;
            }
            UpdatesManager.INSTANCE.onUpdateButtonPressed();
        });
        buttonWithCounterView.setText(getString(R.string.UpdatesSettingsCheckButtonChecking), false);
        buttonWithCounterView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonWithCounterView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        addView(buttonWithCounterView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 16, 15, 16, 16));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        UpdatesManager.INSTANCE.addCallback(callback = new UpdatesManager.UpdatesManagerCallback() {
            @Override
            public boolean onGetStateAfterAdd() {
                return true;
            }

            @Override
            public void checkingForUpdates() {
                AndroidUtilities.runOnUIThread(() -> {
                    buttonWithCounterView.setText(getString(R.string.UpdatesSettingsCheckButtonChecking), true);
                    handleButtonClickable(true);
                    buttonWithCounterView.setFlickeringLoading(true);
                }, 10);
            }

            @Override
            public void onNoUpdateAvailable() {
                AndroidUtilities.runOnUIThread(() -> {
                    textView.setText(getString(R.string.UpdatesSettingsCheckUpdated), true);
                    buttonWithCounterView.setText(getString(R.string.UpdatesSettingsCheckButton), true);
                    handleButtonClickable(true);
                    buttonWithCounterView.setFlickeringLoading(false);
                }, 10);
            }

            @Override
            public void onUpdateAvailable(TLRPC.TL_help_appUpdate update) {
                AndroidUtilities.runOnUIThread(() -> {
                    textView.setText(getString(R.string.UpdatesSettingsCheckUpdateAvailable), true);
                    buttonWithCounterView.setText(getString(R.string.UpdatesSettingsCheckButtonDownload), true);
                    handleButtonClickable(true);
                    buttonWithCounterView.setFlickeringLoading(true);
                }, 10);
            }

            @Override
            public void onUpdateDownloading(float percent) {
                AndroidUtilities.runOnUIThread(() -> {
                    textView.setText(getString(R.string.UpdatesSettingsCheckUpdateDownloading), true);
                    buttonWithCounterView.setText(formatString(R.string.AppUpdateDownloading, (int) (percent * 100)), true);
                    handleButtonClickable(false);
                    buttonWithCounterView.setFlickeringLoading(true);
                }, 10);
            }

            @Override
            public void onUpdateReady() {
                AndroidUtilities.runOnUIThread(() -> {
                    textView.setText(getString(R.string.UpdatesSettingsCheckUpdateReady), true);
                    buttonWithCounterView.setText(getString(R.string.UpdatesSettingsCheckButtonUpdate), true);
                    handleButtonClickable(true);
                    buttonWithCounterView.setFlickeringLoading(false);
                }, 10);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        UpdatesManager.INSTANCE.removeCallback(callback);
        callback = null;
    }

    private boolean _isClickable = true;

    private void handleButtonClickable(boolean isClickable) {
        if (_isClickable == isClickable) {
            return;
        }
        _isClickable = isClickable;

        if (buttonWithCounterView.getTag() instanceof ViewPropertyAnimator) {
            ((ViewPropertyAnimator) buttonWithCounterView.getTag()).cancel();
        }

        buttonWithCounterView.setClickable(isClickable);
        buttonWithCounterView.setEnabled(isClickable);

        //buttonWithCounterView.setAlpha(isClickable ? 0.7f : 1f);
        ViewPropertyAnimator animator = buttonWithCounterView.animate().alpha(isClickable ? 1f : 0.8f).setDuration(200);
        buttonWithCounterView.setTag(animator);
        animator.start();
    }
}
