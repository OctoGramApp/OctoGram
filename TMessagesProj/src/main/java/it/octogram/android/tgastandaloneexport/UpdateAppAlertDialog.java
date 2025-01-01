/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.tgastandaloneexport;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.SvgHelper;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

import java.io.File;

import it.octogram.android.preferences.ui.components.CustomUpdatesCheckCell;
import it.octogram.android.utils.UpdatesManager;

@SuppressLint("ViewConstructor")
public class UpdateAppAlertDialog extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    private final TLRPC.TL_help_appUpdate appUpdate;
    private final int accountNum;

    private final Drawable shadowDrawable;
    private final NestedScrollView scrollView;

    private AnimatorSet shadowAnimation;

    private final View shadow;

    private final LinearLayout linearLayout;

    private int scrollOffsetY;

    private final int[] location = new int[2];

    private final BottomSheetCell doneButton;
    private final BottomSheetCell scheduleButton;

    public static class BottomSheetCell extends FrameLayout {

        private final View background;
        private final TextView[] textView = new TextView[2];
        private final boolean hasBackground;
        private final boolean isScheduleButton;
        private boolean isFirstUpdate = true;
        private int state;

        public BottomSheetCell(Context context, boolean withoutBackground, boolean isScheduleButton) {
            super(context);

            hasBackground = !withoutBackground;
            this.isScheduleButton = isScheduleButton;
            setBackground(null);

            background = new View(context);
            if (hasBackground) {
                background.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 4));
            }
            addView(background, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 16, withoutBackground ? 0 : 16, 16, 16));

            for (int a = 0; a < 2; a++) {
                textView[a] = new TextView(context);
                textView[a].setLines(1);
                textView[a].setSingleLine(true);
                textView[a].setGravity(Gravity.CENTER_HORIZONTAL);
                textView[a].setEllipsize(TextUtils.TruncateAt.END);
                textView[a].setGravity(Gravity.CENTER);
                if (hasBackground) {
                    textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
                    //textView[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                } else {
                    textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                }
                textView[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                textView[a].setPadding(0, 0, 0, hasBackground ? 0 : AndroidUtilities.dp(13));
                addView(textView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                if (a == 1) {
                    textView[a].setAlpha(0.0f);
                }
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(hasBackground ? 80 : 50), MeasureSpec.EXACTLY));
        }

        public void updateState(int state, float loadProgress) {
            boolean isCellEnabled = state != CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING;
            if (isScheduleButton) {
                isCellEnabled = state == CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD;
            }
            background.setClickable(isCellEnabled);
            background.setEnabled(isCellEnabled);

            if (isScheduleButton) {
                if (state == CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD && this.state != CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD) {
                    for (int a = 0; a < 2; a++) {
                        textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                    }
                    setText(LocaleController.getString(R.string.AppUpdateRemindMeLater), !isFirstUpdate);
                } else if (state == CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING && this.state != CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING) {
                    for (int a = 0; a < 2; a++) {
                        textView[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                    }
                    setText("You can close this popup during the download.", !isFirstUpdate);
                } else if (state == CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY && this.state != CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY) {
                    for (int a = 0; a < 2; a++) {
                        textView[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
                    }
                    setText("Install the update by pressing the above button.", !isFirstUpdate);
                }
            } else {
                switch (state) {
                    case CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD:
                        setText(LocaleController.formatString(R.string.AppUpdateDownloadNow), !isFirstUpdate);
                        break;
                    case CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING:
                        setText(LocaleController.formatString(R.string.AppUpdateDownloading, (int) (loadProgress * 100)), !isFirstUpdate);
                        break;
                    case CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY:
                        setText(LocaleController.getString(R.string.UpdatesSettingsCheckButtonInstall), !isFirstUpdate);
                        break;
                }

                if (state == CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING && this.state != CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING) {
                    for (int a = 0; a < 2; a++) {
                        textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton));
                    }
                    background.setAlpha(1f);
                    background.animate().alpha(0f).setDuration(200).start();
                } else if (state != CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING && this.state == CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING) {
                    for (int a = 0; a < 2; a++) {
                        textView[a].setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
                    }
                    background.setAlpha(0f);
                    background.animate().alpha(1f).setDuration(200).start();
                }
            }

            this.state = state;
            this.isFirstUpdate = false;
        }

        public void updateState(int state) {
            updateState(state, 0);
        }

        public void setText(CharSequence text, boolean animated) {
            if (!animated) {
                textView[0].setText(text);
            } else {
                textView[1].setText(text);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(CubicBezierInterpolator.EASE_OUT);
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(textView[0], View.ALPHA, 1.0f, 0.0f),
                        ObjectAnimator.ofFloat(textView[0], View.TRANSLATION_Y, 0, -AndroidUtilities.dp(10)),
                        ObjectAnimator.ofFloat(textView[1], View.ALPHA, 0.0f, 1.0f),
                        ObjectAnimator.ofFloat(textView[1], View.TRANSLATION_Y, AndroidUtilities.dp(10), 0)
                );
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TextView temp = textView[0];
                        textView[0] = textView[1];
                        textView[1] = temp;
                    }
                });
                animatorSet.start();
            }
        }

        public int getState() {
            return state;
        }
    }

    public UpdateAppAlertDialog(Context context, TLRPC.TL_help_appUpdate update, int account) {
        super(context, false);
        appUpdate = update;
        accountNum = account;
        setCanceledOnTouchOutside(false);

        setApplyTopPadding(false);
        setApplyBottomPadding(false);

        shadowDrawable = ContextCompat.getDrawable(context, R.drawable.sheet_shadow_round);
        if (shadowDrawable != null) {
            shadowDrawable.mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));
        }

        FrameLayout container = getFrameLayout(context);
        containerView = container;

        scrollView = new NestedScrollView(context) {

            private boolean ignoreLayout;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                measureChildWithMargins(linearLayout, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int contentHeight = linearLayout.getMeasuredHeight();
                int padding = (height / 5 * 2);
                int visiblePart = height - padding;
                if (contentHeight - visiblePart < AndroidUtilities.dp(90) || contentHeight < height / 2 + AndroidUtilities.dp(90)) {
                    padding = height - contentHeight;
                }
                if (padding < 0) {
                    padding = 0;
                }
                if (getPaddingTop() != padding) {
                    ignoreLayout = true;
                    setPadding(0, padding, 0, 0);
                    ignoreLayout = false;
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateLayout();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onScrollChanged(int l, int t, int oldL, int oldT) {
                super.onScrollChanged(l, t, oldL, oldT);
                updateLayout();
            }
        };
        scrollView.setFillViewport(true);
        scrollView.setWillNotDraw(false);
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(false);
        container.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 130));

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));

        if (appUpdate.sticker != null) {
            BackupImageView imageView = new BackupImageView(context);
            SvgHelper.SvgDrawable svgThumb = DocumentObject.getSvgThumb(appUpdate.sticker.thumbs, Theme.key_windowBackgroundGray, 1.0f);
            TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(appUpdate.sticker.thumbs, 90);
            ImageLocation imageLocation = ImageLocation.getForDocument(thumb, appUpdate.sticker);

            if (svgThumb != null) {
                imageView.setImage(ImageLocation.getForDocument(appUpdate.sticker), "250_250", svgThumb, 0, "update");
            } else {
                imageView.setImage(ImageLocation.getForDocument(appUpdate.sticker), "250_250", imageLocation, null, 0, "update");
            }
            linearLayout.addView(imageView, LayoutHelper.createLinear(160, 160, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 17, 8, 17, 0));
        }

        TextView textView = new TextView(context);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(LocaleController.getString(R.string.AppUpdateCustomTitle));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 23, 16, 23, 0));

        TextView messageTextView = new TextView(getContext());
        messageTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        messageTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        messageTextView.setLinkTextColor(Theme.getColor(Theme.key_dialogTextLink));
        messageTextView.setText(LocaleController.formatString("AppUpdateVersionAndSize", R.string.AppUpdateVersionAndSize, appUpdate.version, AndroidUtilities.formatFileSize(appUpdate.document.size)));
        messageTextView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        linearLayout.addView(messageTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 23, 0, 23, 5));

        TextView changelogTextView = new TextView(getContext());
        changelogTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        changelogTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        changelogTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        changelogTextView.setLinkTextColor(Theme.getColor(Theme.key_dialogTextLink));
        if (TextUtils.isEmpty(appUpdate.text)) {
            changelogTextView.setText(AndroidUtilities.replaceTags(LocaleController.getString(R.string.AppUpdateChangelogEmpty)));
        } else {
            SpannableStringBuilder builder = new SpannableStringBuilder(appUpdate.text);
            MessageObject.addEntitiesToText(builder, update.entities, false, false, false, false);
            changelogTextView.setText(builder);
        }
        changelogTextView.setGravity(Gravity.LEFT | Gravity.TOP);
        linearLayout.addView(changelogTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 23, 15, 23, 0));

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight(), Gravity.BOTTOM | Gravity.LEFT);
        frameLayoutParams.bottomMargin = AndroidUtilities.dp(130);
        shadow = new View(context);
        shadow.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        shadow.setAlpha(0.0f);
        shadow.setTag(1);
        container.addView(shadow, frameLayoutParams);

        doneButton = new BottomSheetCell(context, false, false);
        doneButton.background.setOnClickListener(v -> {
            if (doneButton.getState() == CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY) {
                UpdatesManager.installUpdate();
            } else if (doneButton.getState() == CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD) {
                FileLoader.getInstance(accountNum).loadFile(appUpdate.document, "update", FileLoader.PRIORITY_NORMAL, 1);
            }
        });
        container.addView(doneButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.LEFT | Gravity.BOTTOM, 0, 0, 0, 50));

        scheduleButton = new BottomSheetCell(context, true, true);
        scheduleButton.background.setOnClickListener(v -> dismiss());
        container.addView(scheduleButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.LEFT | Gravity.BOTTOM, 0, 0, 0, 0));

        updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD);
        if (UpdatesManager.canAutoDownloadUpdates()) {
            FileLoader.getInstance(accountNum).loadFile(appUpdate.document, "update", FileLoader.PRIORITY_NORMAL, 1);
        }
    }

    @NonNull
    private FrameLayout getFrameLayout(Context context) {
        FrameLayout container = new FrameLayout(context) {
            @Override
            public void setTranslationY(float translationY) {
                super.setTranslationY(translationY);
                updateLayout();
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && scrollOffsetY != 0 && ev.getY() < scrollOffsetY) {
                    dismiss();
                    return true;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override @SuppressLint("ClickableViewAccessibility")
            public boolean onTouchEvent(MotionEvent e) {
                return !isDismissed() && super.onTouchEvent(e);
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int top = (int) (scrollOffsetY - backgroundPaddingTop - getTranslationY());
                if (shadowDrawable != null) {
                    shadowDrawable.setBounds(0, top, getMeasuredWidth(), getMeasuredHeight());
                    shadowDrawable.draw(canvas);
                }
            }
        };
        container.setWillNotDraw(false);
        return container;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getInstance(0).addObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(0).addObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getInstance(0).addObserver(this, NotificationCenter.fileLoadFailed);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getInstance(0).removeObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(0).removeObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getInstance(0).removeObserver(this, NotificationCenter.fileLoadFailed);
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
            updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY);
        } else if (id == NotificationCenter.fileLoadProgressChanged) {
            Long loadedSize = (Long) args[1];
            Long totalSize = (Long) args[2];
            float loadProgress = loadedSize / (float) totalSize;

            updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING, loadProgress);
        } else if (id == NotificationCenter.fileLoadFailed) {
            // force re-check data from the beginning

            File completePathFileName = FileLoader.getInstance(0).getPathToAttach(SharedConfig.pendingAppUpdate.document, true);
            if (completePathFileName.exists()) {
                updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY);
            } else {
                if (FileLoader.getInstance(0).isLoadingFile(name)) {
                    Float p = ImageLoader.getInstance().getFileProgress(name);
                    updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING, (p != null ? p : 0.0f));
                } else {
                    updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD);
                }
            }
        }
    }

    private void updateState(int state, float loadProgress) {
        doneButton.updateState(state, loadProgress);
        scheduleButton.updateState(state, 0);
    }

    private void updateState(int state) {
        updateState(state, 0);
    }

    private void runShadowAnimation(final boolean show) {
        if (show && shadow.getTag() != null || !show && shadow.getTag() == null) {
            shadow.setTag(show ? null : 1);
            if (show) {
                shadow.setVisibility(View.VISIBLE);
            }
            if (shadowAnimation != null) {
                shadowAnimation.cancel();
            }
            shadowAnimation = new AnimatorSet();
            shadowAnimation.playTogether(ObjectAnimator.ofFloat(shadow, View.ALPHA, show ? 1.0f : 0.0f));
            shadowAnimation.setDuration(150);
            shadowAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        if (!show) {
                            shadow.setVisibility(View.INVISIBLE);
                        }
                        shadowAnimation = null;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        shadowAnimation = null;
                    }
                }
            });
            shadowAnimation.start();
        }
    }

    private void updateLayout() {
        View child = linearLayout.getChildAt(0);
        child.getLocationInWindow(location);
        int top = location[1] - AndroidUtilities.dp(24);
        int newOffset = Math.max(top, 0);
        runShadowAnimation(!(location[1] + linearLayout.getMeasuredHeight() <= container.getMeasuredHeight() - AndroidUtilities.dp(113) + containerView.getTranslationY()));
        if (scrollOffsetY != newOffset) {
            scrollOffsetY = newOffset;
            scrollView.invalidate();
        }
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }
}