package it.octogram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import java.util.Objects;

import it.octogram.android.utils.DCHelper;
import it.octogram.ui.Cells.Dynamic.BaseButtonCell;
import it.octogram.ui.Cells.Dynamic.ThemeInfo;


@SuppressLint("ViewConstructor")
public class Datacenter extends LinearLayout {
    private final TextView tv;
    private final TextView tv2;
    private final ImageView iv;
    private DCHelper.TInfo tInfo = null;
    private final CardView mainCardView;
    private boolean withBackground = false;
    private boolean needDivider = false;
    private final Theme.ResourcesProvider resourcesProvider;

    public Datacenter(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setWillNotDraw(false);
        setPadding(AndroidUtilities.dp(23), AndroidUtilities.dp(8), AndroidUtilities.dp(23), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER_VERTICAL);

        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider);
        int colorText2 = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider);

        mainCardView = new CardView(context);
        mainCardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mainCardView.setCardElevation(0);
        mainCardView.setRadius(AndroidUtilities.dp(10.0f));
        mainCardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(BaseButtonCell.getBackColor(), BaseButtonCell.getBackgroundAlpha()));

        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new CardView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(10));

        iv = new ImageView(context) {
            @Override
            public void invalidate() {
                super.invalidate();
                if (iv.getBackground() != null) {
                    iv.getBackground().setColorFilter(Theme.getColor(Theme.key_switch2TrackChecked, resourcesProvider), PorterDuff.Mode.SRC_ATOP);
                }
            }
        };
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(AndroidUtilities.dp(30), AndroidUtilities.dp(30));
        iv.setLayoutParams(layoutParams2);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textLayout.setPadding(AndroidUtilities.dp(16), 0, 0, 0);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setGravity(Gravity.LEFT);

        tv = new TextView(context);
        tv.setTextColor(colorText);
        tv.setLines(1);
        tv.setMaxLines(1);
        tv.setSingleLine(true);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        tv.setGravity(Gravity.CENTER);
        tv.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        tv2 = new TextView(context);
        tv2.setTextColor(colorText2);

        tv2.setLines(1);
        tv2.setMaxLines(1);
        tv2.setSingleLine(true);
        tv2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv2.setGravity(Gravity.CENTER);
        tv2.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv2.setEllipsize(TextUtils.TruncateAt.END);

        addView(mainCardView);
        mainCardView.addView(ll);
        ll.addView(iv);
        addView(textLayout);
        textLayout.addView(tv);
        textLayout.addView(tv2);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (withBackground) {
            mainCardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(BaseButtonCell.getBackColor(), BaseButtonCell.getBackgroundAlpha()));
        } else {
            mainCardView.setCardBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setTheme(ThemeInfo themeInfo) {
        if (themeInfo.withBackground) {
            mainCardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(BaseButtonCell.getBackColor(), BaseButtonCell.getBackgroundAlpha()));
        } else {
            mainCardView.setCardBackgroundColor(Color.TRANSPARENT);
        }
        this.withBackground = themeInfo.withBackground;
        mainCardView.setRadius(themeInfo.radius);
        if (tInfo != null) {
            setIdAndDC(tInfo, needDivider);
        }
    }

    public void setIdAndDC(DCHelper.TInfo tInfo, boolean divider) {
        this.tInfo = tInfo;
        tv.setText(tInfo.longDcName);
        tv2.setText(String.valueOf(tInfo.tID));
        Drawable d = ContextCompat.getDrawable(getContext(), DCHelper.getDcIcon(tInfo.dcID));
        Objects.requireNonNull(d).setColorFilter(Theme.getColor(Theme.key_switch2TrackChecked, resourcesProvider), PorterDuff.Mode.SRC_ATOP);
        iv.setBackground(d);
        needDivider = divider;
        setWillNotDraw(!needDivider);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(AndroidUtilities.dp(16), getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(16), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }
}
