/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.bottomsheets;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;

public class ArticleTranslationsBottomSheet extends BottomSheet {
    private final LinearProgressView progressView;
    private boolean isDismissingByCall = false;

    public ArticleTranslationsBottomSheet(Context context, boolean needFocus) {
        this(context, needFocus, false);
    }

    public ArticleTranslationsBottomSheet(Context context, boolean needFocus, boolean isDownloading) {
        super(context, needFocus);
        fixNavigationBar(getThemedColor(Theme.key_dialogBackground));

        setCanDismissWithSwipe(false);
        setCanDismissWithTouchOutside(false);

        setDelegate(new BottomSheetDelegateInterface() {
            @Override
            public void onOpenAnimationStart() {

            }

            @Override
            public void onOpenAnimationEnd() {

            }

            @Override
            public boolean canDismiss() {
                return isDismissingByCall;
            }
        });

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);


        StickerImageView imageView = new StickerImageView(context, UserConfig.selectedAccount);
        imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView.setStickerNum(StickerUi.TRANSLATOR.getValue());
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

        TextView textView = new TextView(context);
        textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString(isDownloading ? R.string.DownloadingModelsTranslator : R.string.TranslatingArticleTranslator));
        textView.setPadding(dp(30), 0, dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextColor(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_dialogTextBlack), 150));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString(isDownloading ? R.string.DownloadingModelsTranslator_Desc : R.string.TranslatingArticleTranslator_Desc));
        textView.setPadding(dp(30), dp(10), dp(30), dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        progressView = new LinearProgressView(context);
        linearLayout.addView(progressView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 5, Gravity.CENTER_HORIZONTAL, 60, 0, 60, 16));

        setCustomView(linearLayout);
    }

    @Override
    public void onBackPressed() {

    }

    public void setProgress(float state) {
        if (progressView != null) {
            progressView.setProgressAnimated(state);
        }
    }

    @Override
    public void dismiss() {
        setCanDismissWithSwipe(true);
        setCanDismissWithTouchOutside(true);
        isDismissingByCall = true;
        super.dismiss();
    }

    public static class LinearProgressView extends View {
        private float progress = 0;
        private Paint backgroundPaint;
        private Paint progressPaint;
        private ValueAnimator currentAnimator;

        public LinearProgressView(Context context) {
            super(context);
            init();
            setWillNotDraw(false);
        }

        private void init() {
            backgroundPaint = new Paint();
            backgroundPaint.setColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector));

            progressPaint = new Paint();
            progressPaint.setColor(Theme.getColor(Theme.key_radioBackgroundChecked));
        }

        public void setProgressAnimated(float targetProgress) {
            if (currentAnimator != null && currentAnimator.isRunning()) {
                currentAnimator.cancel();
            }

            float start = this.progress;

            currentAnimator = ValueAnimator.ofFloat(start, targetProgress);
            currentAnimator.setDuration(200);
            currentAnimator.addUpdateListener(animation -> {
                this.progress = (float) animation.getAnimatedValue();
                LinearProgressView.this.invalidate();
            });
            currentAnimator.start();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            canvas.drawRoundRect(0, 0, width, height, dp(3), dp(3), backgroundPaint);

            float progressWidth = progress * width;
            canvas.drawRoundRect(0, 0, progressWidth, height, dp(3), dp(3), progressPaint);
        }
    }
}
