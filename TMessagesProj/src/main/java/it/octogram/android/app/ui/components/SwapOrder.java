/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class SwapOrder extends FrameLayout {
    private final SimpleTextView textView;
    private final ImageView moveImageView;
    private final ImageView optionsImageView;
    private boolean needDivider;
    public String menuId;

    @SuppressLint("ClickableViewAccessibility")
    public SwapOrder(Context context) {
        super(context);
        setWillNotDraw(false);
        moveImageView = new ImageView(context);
        moveImageView.setFocusable(false);
        moveImageView.setScaleType(ImageView.ScaleType.CENTER);
        moveImageView.setImageResource(R.drawable.list_reorder);
        moveImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_stickers_menu), PorterDuff.Mode.MULTIPLY));
        moveImageView.setContentDescription(getString(R.string.FilterReorder));
        moveImageView.setClickable(true);
        moveImageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                OnTouchListener listener = (OnTouchListener) moveImageView.getTag();
                if (listener != null) {
                    listener.onTouch(v, event);
                }
                v.performClick();
                return true;
            }
            return false;
        });
        addView(moveImageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 6, 0, 6, 0));

        textView = new SimpleTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(16);
        textView.setMaxLines(1);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 80 : 64, 17, LocaleController.isRTL ? 64 : 80, 0));

        optionsImageView = new ImageView(context);
        optionsImageView.setFocusable(false);
        optionsImageView.setScaleType(ImageView.ScaleType.CENTER);
        optionsImageView.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_stickers_menuSelector)));
        optionsImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_color_red), PorterDuff.Mode.MULTIPLY));
        optionsImageView.setImageResource(R.drawable.msg_delete);
        optionsImageView.setContentDescription(getString(R.string.Delete));
        addView(optionsImageView, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 6, 0, 6, 0));
    }

    public void setRightDrawable(Drawable drawable) {
        textView.setRightDrawable(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY));
    }

    public void setData(String text, boolean isDefault, boolean isPremium, String id, boolean divider) {
        textView.setText(text);
        needDivider = divider;
        menuId = id;
        optionsImageView.setVisibility(isDefault ? GONE : VISIBLE);
        if (isPremium) {
            Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.msg_premium_liststar);
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuPhoneCats), PorterDuff.Mode.MULTIPLY));
                textView.setRightDrawable(drawable);
            }
        } else {
            textView.setRightDrawable(null);
        }
    }

    public void setOnDeleteClick(OnClickListener listener) {
        optionsImageView.setOnClickListener(listener);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : dp(62), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? dp(62) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setOnReorderButtonTouchListener(OnTouchListener listener) {
        moveImageView.setOnTouchListener(listener);
    }
}