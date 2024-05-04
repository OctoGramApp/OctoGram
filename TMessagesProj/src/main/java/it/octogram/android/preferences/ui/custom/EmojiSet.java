/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;

import java.util.Objects;

import it.octogram.android.CustomEmojiController;
import it.octogram.android.http.FileDownloader;
import it.octogram.android.utils.FileUnzip;

public class EmojiSet extends FrameLayout {
    private final TextView textView;
    private final AnimatedTextView valueTextView;
    private final BackupImageView imageView;
    private final ImageView optionsButton;
    private final RadialProgressView radialProgress;
    private final CheckBox2 checkBox;
    public String packId;
    public String packFileLink;
    public String versionWithMD5;
    private Long packFileSize;
    private boolean needDivider;

    public EmojiSet(Context context) {
        super(context);

        imageView = new BackupImageView(context);
        imageView.setAspectFit(true);
        imageView.setLayerNum(1);
        addView(imageView, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 13, 9, LocaleController.isRTL ? 13 : 0, 0));

        optionsButton = new ImageView(context);
        optionsButton.setFocusable(false);
        optionsButton.setScaleType(ImageView.ScaleType.CENTER);
        optionsButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addedIcon), PorterDuff.Mode.MULTIPLY));
        optionsButton.setImageResource(R.drawable.floating_check);
        optionsButton.setVisibility(GONE);
        addView(optionsButton, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, (LocaleController.isRTL ? 10 : 0), 9, (LocaleController.isRTL ? 0 : 10), 0));

        radialProgress = new RadialProgressView(context);
        radialProgress.setNoProgress(false);
        radialProgress.setProgressColor(Theme.getColor(Theme.key_featuredStickers_addedIcon));
        radialProgress.setStrokeWidth(2.8F);
        radialProgress.setSize(AndroidUtilities.dp(30));
        radialProgress.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(40), AndroidUtilities.dp(40)));
        radialProgress.setVisibility(INVISIBLE);
        addView(radialProgress, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, (LocaleController.isRTL ? 10 : 0), 9, (LocaleController.isRTL ? 0 : 10), 0));

        textView = new AppCompatTextView(context) {
            @Override
            public void setText(CharSequence text, BufferType type) {
                text = Emoji.replaceEmoji(text, getPaint().getFontMetricsInt(), AndroidUtilities.dp(14), false);
                super.setText(text, type);
            }
        };
        NotificationCenter.listenEmojiLoading(textView);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity(LayoutHelper.getAbsoluteGravityStart());
        addView(textView, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 71, 9, 70, 0));

        valueTextView = new AnimatedTextView(context);
        valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        valueTextView.setAnimationProperties(.55f, 0, 320, CubicBezierInterpolator.EASE_OUT_QUINT);
        valueTextView.setTextSize(AndroidUtilities.dp(13));
        valueTextView.setGravity(LayoutHelper.getAbsoluteGravityStart());
        addView(valueTextView, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 71, 25, 70, 0));

        checkBox = new CheckBox2(context, 21);
        checkBox.setColor(-1, Theme.key_windowBackgroundWhite, Theme.key_checkboxCheck);
        checkBox.setDrawUnchecked(false);
        checkBox.setDrawBackgroundAsArc(3);
        addView(checkBox, LayoutHelper.createFrameRelatively(24, 24, Gravity.START, 34, 30, 0, 0));
    }

    public void setData(CustomEmojiController.EmojiPackBase emojiPackInfo, boolean animated, boolean divider) {
        needDivider = divider;
        textView.setText(emojiPackInfo.getPackName());
        packFileSize = emojiPackInfo.getFileSize();
        packId = emojiPackInfo.getPackId();
        if (emojiPackInfo instanceof CustomEmojiController.EmojiPackInfo) {
            versionWithMD5 = ((CustomEmojiController.EmojiPackInfo) emojiPackInfo).getVersionWithMd5();
        } else {
            versionWithMD5 = null;
        }
        packFileLink = emojiPackInfo.getFileLocation();
        if (Objects.equals(packId, "default")) {
            valueTextView.setText(LocaleController.getString("Default", R.string.Default), animated);
        } else if (CustomEmojiController.emojiDir(packId, versionWithMD5).exists() || TextUtils.isEmpty(versionWithMD5)) {
            valueTextView.setText(LocaleController.getString("InstalledEmojiSet", R.string.InstalledEmojiSet), animated);
        } else {
            String status = LocaleController.getString("DownloadUpdate", R.string.DownloadUpdate);
            if (CustomEmojiController.isInstalledOldVersion(packId, versionWithMD5)) {
                status = LocaleController.getString("UpdateEmojiSet", R.string.UpdateEmojiSet);
            }
            valueTextView.setText(String.format(
                    "%s %s",
                    status,
                    AndroidUtilities.formatFileSize(packFileSize)
            ), animated);
        }
        if (!animated) checkDownloaded(false);
        textView.setTranslationY(0);
        imageView.setImage(emojiPackInfo.getPreview(), null, null);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (animated) {
            optionsButton.animate().cancel();
            optionsButton.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!checked) {
                        optionsButton.setVisibility(INVISIBLE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    if (checked) {
                        optionsButton.setVisibility(VISIBLE);
                    }
                }
            }).alpha(checked ? 1 : 0).scaleX(checked ? 1 : 0.1f).scaleY(checked ? 1 : 0.1f).setDuration(150).start();
        } else {
            optionsButton.setVisibility(checked ? VISIBLE : INVISIBLE);
            optionsButton.setScaleX(checked ? 1f : 0.1f);
            optionsButton.setScaleY(checked ? 1f : 0.1f);
            optionsButton.setAlpha(checked ? 1f : 0f);
        }
    }

    public void setSelected(boolean selected, boolean animated) {
        checkBox.setChecked(selected, animated);
    }

    public boolean isChecked() {
        return optionsButton.getVisibility() == VISIBLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(71), getHeight() - 1, getWidth() - getPaddingRight() - (LocaleController.isRTL ? AndroidUtilities.dp(71) : 0), getHeight() - 1, Theme.dividerPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(58) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    public void setProgress(boolean visible, boolean animated) {
        if (animated) {
            radialProgress.animate().cancel();
            radialProgress.animate().setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!visible) {
                        radialProgress.setVisibility(INVISIBLE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    if (visible) {
                        radialProgress.setVisibility(VISIBLE);
                    }
                }
            }).alpha(visible ? 1 : 0).setDuration(150).start();
        } else {
            radialProgress.setVisibility(visible ? VISIBLE : INVISIBLE);
        }
    }

    public void setProgress(float percentage, long downBytes, boolean animated) {
        radialProgress.setProgress(percentage / 100f);
        valueTextView.setText(LocaleController.formatString(
                "AccDescrDownloadProgress",
                R.string.AccDescrDownloadProgress,
                AndroidUtilities.formatFileSize(downBytes),
                AndroidUtilities.formatFileSize(packFileSize)
        ), animated);
    }

    public void checkDownloaded(boolean animated) {
        if (Objects.equals(packId, "default")) return;
        if (CustomEmojiController.emojiTmpDownloaded(packId) || CustomEmojiController.emojiDir(packId, versionWithMD5).exists() || TextUtils.isEmpty(versionWithMD5)) {
            setProgress(false, animated);
            if (FileUnzip.isRunningUnzip(packId)) {
                valueTextView.setText(LocaleController.getString("InstallingEmojiSet", R.string.InstallingEmojiSet), animated);
            } else {
                valueTextView.setText(LocaleController.getString("InstalledEmojiSet", R.string.InstalledEmojiSet), animated);
            }
        } else if (FileDownloader.isRunningDownload(packId)) {
            setProgress(true, animated);
            setChecked(false, animated);
            setProgress(FileDownloader.getDownloadProgress(packId), FileDownloader.downloadedBytes(packId), animated);
        } else {
            setProgress(false, animated);
            setChecked(false, animated);
            String status = LocaleController.getString("DownloadUpdate", R.string.DownloadUpdate);
            if (CustomEmojiController.isInstalledOldVersion(packId, versionWithMD5)) {
                status = LocaleController.getString("UpdateEmojiSet", R.string.UpdateEmojiSet);
            }
            valueTextView.setText(String.format(
                    "%s %s",
                    status,
                    AndroidUtilities.formatFileSize(packFileSize)
            ), animated);
        }
    }
}
