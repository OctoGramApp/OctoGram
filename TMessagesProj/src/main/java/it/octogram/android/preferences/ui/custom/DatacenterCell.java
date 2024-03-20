package it.octogram.android.preferences.ui.custom;

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

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import java.util.Locale;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.UserAccountInfoController;

@SuppressLint("ViewConstructor")
public class DatacenterCell extends LinearLayout {
    private final TextView dcNameView;
    private final TextView idView;
    private final ImageView dcIconView;
    private final CardView mainCardView;
    private boolean needDivider = false;
    private final Theme.ResourcesProvider resourcesProvider;

    public DatacenterCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setWillNotDraw(false);
        setPadding(AndroidUtilities.dp(23), AndroidUtilities.dp(8), AndroidUtilities.dp(23), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER_VERTICAL);

        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider);
        int colorText2 = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider);

        mainCardView = new CardView(context);
        mainCardView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mainCardView.setCardElevation(0);
        mainCardView.setRadius(AndroidUtilities.dp(10.0f));
        mainCardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(getBackColor(), getBackgroundAlpha()));

        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new CardView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10));

        dcIconView = new ImageView(context) {
            @Override
            public void invalidate() {
                super.invalidate();
                if (dcIconView.getBackground() != null) {
                    ColorFilter filter = new PorterDuffColorFilter(Theme.getColor(Theme.key_switch2TrackChecked, resourcesProvider), PorterDuff.Mode.SRC_ATOP);
                    dcIconView.getBackground().setColorFilter(filter);
                }
            }
        };
        LayoutParams layoutParams2 = new LayoutParams(AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        dcIconView.setLayoutParams(layoutParams2);
        dcIconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textLayout.setPadding(AndroidUtilities.dp(16), 0, 0, 0);
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

        idView = new TextView(context);
        idView.setTextColor(colorText2);

        idView.setLines(1);
        idView.setMaxLines(1);
        idView.setSingleLine(true);
        idView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        idView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        idView.setGravity(Gravity.CENTER);
        idView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        idView.setEllipsize(TextUtils.TruncateAt.END);

        addView(mainCardView);
        mainCardView.addView(ll);
        ll.addView(dcIconView);
        addView(textLayout);
        textLayout.addView(dcNameView);
        textLayout.addView(idView);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        mainCardView.setCardBackgroundColor(Color.TRANSPARENT);
    }

    public void setIdAndDC(UserAccountInfoController.UserAccountInfo tInfo, boolean divider) {
        String formatted = tInfo.dcInfo.dcId != -1 ?
                String.format(Locale.ENGLISH, "%s (DC%d)", tInfo.dcInfo.dcName, tInfo.dcInfo.dcId) :
                tInfo.dcInfo.dcName;
        dcNameView.setText(formatted);
        idView.setText(String.valueOf(tInfo.userId));

        Drawable d = ContextCompat.getDrawable(getContext(), tInfo.dcInfo.icon);
        ColorFilter filter = new PorterDuffColorFilter(Theme.getColor(Theme.key_switch2TrackChecked, resourcesProvider), PorterDuff.Mode.SRC_ATOP);
        Objects.requireNonNull(d).setColorFilter(filter);
        dcIconView.setBackground(d);
        needDivider = divider;
        setWillNotDraw(!needDivider);
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
            canvas.drawLine(AndroidUtilities.dp(16), getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(16), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
}
