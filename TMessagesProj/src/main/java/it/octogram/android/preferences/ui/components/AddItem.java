/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProgressButton;

import it.octogram.android.OctoConfig;

public class AddItem extends FrameLayout {

    private final SimpleTextView textView;
    private final ProgressButton addButton;
    private boolean needDivider;
    public String menuId;

    public AddItem(Context context) {
        super(context);

        textView = new SimpleTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(16);
        textView.setMaxLines(1);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
        addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 22, 0, 22, 0));

        addButton = new ProgressButton(context);
        addButton.setText(getString(R.string.Add));
        addButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        addButton.setProgressColor(Theme.getColor(Theme.key_featuredStickers_buttonProgress));
        addButton.setBackgroundRoundRect(Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed));
        addView(addButton, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, 28, Gravity.TOP | Gravity.END, 0, 18, 14, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(64));
        measureChildWithMargins(addButton, widthMeasureSpec, 0, heightMeasureSpec, 0);
        measureChildWithMargins(textView, widthMeasureSpec, addButton.getMeasuredWidth(), heightMeasureSpec, 0);
    }

    public void setData(String text, String id, boolean isPremium, boolean divider) {
        needDivider = divider;
        menuId = id;
        setWillNotDraw(!needDivider);
        textView.setText(text);
        if (isPremium) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.msg_premium_liststar);
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuPhoneCats), PorterDuff.Mode.MULTIPLY));
                textView.setRightDrawable(drawable);
            }
        } else {
            textView.setRightDrawable(null);
        }
    }

    public void setAddOnClickListener(OnClickListener onClickListener) {
        addButton.setOnClickListener(onClickListener);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
            canvas.drawLine(0, getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
        }
    }
}