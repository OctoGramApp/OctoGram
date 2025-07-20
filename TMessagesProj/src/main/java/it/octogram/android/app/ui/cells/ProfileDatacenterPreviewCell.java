/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Locale;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.account.UserAccountInfoController;

@SuppressLint("ViewConstructor")
public class ProfileDatacenterPreviewCell extends LinearLayout {
    private final TextView dcNameView;
    private TextView idView;
    private final AppCompatImageView dcIconView;
    private final CardView mainCardView;
    private boolean needDivider = false;
    private final Theme.ResourcesProvider resourcesProvider;
    private AnimatedTextView animatedIdView;

    public ProfileDatacenterPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        this(context, resourcesProvider, false);
    }

    public ProfileDatacenterPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider, boolean isPreviewMode) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setWillNotDraw(false);
        setPadding(dp(23), dp(8), dp(23), dp(8));
        setGravity(Gravity.CENTER_VERTICAL);

        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider);
        int colorText2 = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider);

        mainCardView = new CardView(context);
        mainCardView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mainCardView.setCardElevation(0);
        mainCardView.setRadius(dp(10.0f));
        mainCardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(getBackColor(), getBackgroundAlpha()));

        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new CardView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setPadding(dp(10), dp(10), dp(10), dp(10));

        dcIconView = new AppCompatImageView(context) {
            @Override
            public void invalidate() {
                super.invalidate();
                if (dcIconView.getBackground() != null) {
                    ColorFilter filter = new PorterDuffColorFilter(Theme.getColor(Theme.key_switch2TrackChecked, resourcesProvider), PorterDuff.Mode.SRC_ATOP);
                    dcIconView.getBackground().setColorFilter(filter);
                }
            }
        };
        LayoutParams layoutParams2 = new LayoutParams(dp(30), dp(30));
        dcIconView.setLayoutParams(layoutParams2);
        dcIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textLayout.setPadding(dp(16), 0, 0, 0);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setGravity(Gravity.LEFT);

        dcNameView = new TextView(context);
        dcNameView.setTextColor(colorText);
        dcNameView.setLines(1);
        dcNameView.setMaxLines(1);
        dcNameView.setSingleLine(true);
        dcNameView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        dcNameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        dcNameView.setGravity(Gravity.CENTER);
        dcNameView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        dcNameView.setEllipsize(TextUtils.TruncateAt.END);

        if (isPreviewMode) {
            animatedIdView = new AnimatedTextView(context);
            animatedIdView.setAnimationProperties(1f, 0, 300, CubicBezierInterpolator.EASE_OUT_QUINT);
            animatedIdView.setTextSize(dp(14));
            animatedIdView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            animatedIdView.setTextColor(colorText2);
        } else {
            idView = new TextView(context);
            idView.setTextColor(colorText2);
            idView.setLines(1);
            idView.setMaxLines(1);
            idView.setSingleLine(true);
            idView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            idView.setGravity(Gravity.LEFT);
            idView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            idView.setEllipsize(TextUtils.TruncateAt.END);
        }

        addView(mainCardView);
        mainCardView.addView(ll);
        ll.addView(dcIconView);
        addView(textLayout);
        textLayout.addView(dcNameView);
        textLayout.addView(isPreviewMode ? animatedIdView : idView, LayoutHelper.createLinear(LayoutParams.MATCH_PARENT, dp(10)));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        mainCardView.setCardBackgroundColor(Color.TRANSPARENT);
    }

    public void setIdAndDC(UserAccountInfoController.UserAccountInfo tInfo, boolean divider) {
        String formatted = tInfo.dcInfo.getDcId() != -1 ?
                String.format(Locale.ENGLISH, "%s (DC%d)", tInfo.dcInfo.getDcName(), tInfo.dcInfo.getDcId()) :
                tInfo.dcInfo.getDcName();
        dcNameView.setText(formatted);

        if (idView != null) {
            idView.setText(String.valueOf(tInfo.userId));
        }

        Drawable d = ContextCompat.getDrawable(getContext(), tInfo.dcInfo.getIcon());
        ColorFilter filter = new PorterDuffColorFilter(Theme.getColor(Theme.key_switch2TrackChecked, resourcesProvider), PorterDuff.Mode.SRC_ATOP);
        Objects.requireNonNull(d).setColorFilter(filter);
        dcIconView.setBackground(d);
        needDivider = divider;
        setWillNotDraw(!needDivider);
    }

    public void setCustomPreviewModeData(int dcIcon, String title, String id) {
        dcNameView.setText(title);
        updateIdView(id);

        Drawable d = ContextCompat.getDrawable(getContext(), dcIcon);
        ColorFilter filter = new PorterDuffColorFilter(Theme.getColor(Theme.key_switch2TrackChecked, resourcesProvider), PorterDuff.Mode.SRC_ATOP);
        Objects.requireNonNull(d).setColorFilter(filter);
        dcIconView.setBackground(d);
    }

    public void updateIdView(String id) {
        if (animatedIdView != null) {
            animatedIdView.getDrawable().setText(id, true, true);
        }
    }

    private float getBackgroundAlpha() {
        int colorBack = Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider);
        float alphaColor = 0;
        for (int ratio = 20; ratio > 0; ratio -= 1) {
            try {
                int blendedColor = ColorUtils.blendARGB(colorBack, getBackColor(), ratio / 100f);
                double contrast = ColorUtils.calculateContrast(colorBack, blendedColor) * 100.0;
                alphaColor = ratio / 100f;
                if (Math.round(contrast) <= 112) { // 112 IS CONTRAST OF 0.07f ALPHA
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        return alphaColor;
    }

    private int getBackColor() {
        return ColorUtils.calculateLuminance(Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider)) > 0.5f ? 0xFF000000 : 0xFFFFFFFF;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
            canvas.drawLine(dp(16), getMeasuredHeight() - 1, getMeasuredWidth() - dp(16), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
}
