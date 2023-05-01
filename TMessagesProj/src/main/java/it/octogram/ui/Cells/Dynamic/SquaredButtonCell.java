package it.octogram.ui.Cells.Dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

@SuppressLint("ViewConstructor")
public class SquaredButtonCell extends BaseButtonCell {
    private final int[] colors;
    private final TextView tv;
    private final CardView cardView;
    private final ImageView mt;

    @SuppressLint("ClickableViewAccessibility")
    public SquaredButtonCell(Context context, Theme.ResourcesProvider resourcesProvider, String text, int iconId, int color) {
        super(context, resourcesProvider);
        colors = new int[]{
                color,
                Theme.key_windowBackgroundWhiteBlackText,
        };

        setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER);

        cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(80)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(10.0f));
        cardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(getBackColor(), getBackgroundAlpha()));

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout ll = new LinearLayout(context);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
        ll.setLayoutParams(layoutParams1);
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.VERTICAL);

        mt = new ImageView(context);
        mt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt.setClickable(true);
        mt.setOnTouchListener(this::delegateOnClick);
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, AndroidUtilities.getTransparentColor(getBackColor(), 0.2f)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        iv = new RLottieImageView(context);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(AndroidUtilities.dp(26), AndroidUtilities.dp(26));
        layoutParams2.setMargins(0, 0, 0, AndroidUtilities.dp(5));
        iv.setLayoutParams(layoutParams2);
        if (iconId == R.raw.camera_outline) {
            cameraDrawable = new RLottieDrawable(R.raw.camera_outline, String.valueOf(R.raw.camera_outline), AndroidUtilities.dp(26 * 2), AndroidUtilities.dp(26 * 2), false, null);
            iv.setAnimation(cameraDrawable);
            iv.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            iv.setBackground(ContextCompat.getDrawable(context, iconId));
        }

        tv = new TextView(context);
        tv.setText(text);
        tv.setLines(1);
        tv.setMaxLines(1);
        tv.setSingleLine(true);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        tv.setGravity(Gravity.CENTER);
        tv.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        ll.addView(iv);
        ll.addView(tv);
        rl.addView(mt);
        rl.addView(ll);
        cardView.addView(rl);
        addView(cardView);
        updateColors();
    }

    @Override
    public void updateColors() {
        tv.setTextColor(getThemedColor(colors[1]));
        if (iv.getBackground() != null) {
            iv.getBackground().setColorFilter(new PorterDuffColorFilter(getThemedColor(colors[0]), PorterDuff.Mode.MULTIPLY));
        } else {
            iv.setColorFilter(new PorterDuffColorFilter(getThemedColor(colors[0]), PorterDuff.Mode.MULTIPLY));
        }
        cardView.setCardBackgroundColor(AndroidUtilities.getTransparentColor(getBackColor(), getBackgroundAlpha()));
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, AndroidUtilities.getTransparentColor(getBackColor(), 0.2f)));
    }

    public static int getPreviewHeight() {
        return AndroidUtilities.dp(8 + 80 + 8);
    }

    public static void getPreview(Canvas canvas, int x, int y, int w, Paint paint) {
        int color = getThemedColor(Theme.key_switchTrack);
        boolean isFlicker = paint != null;
        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(AndroidUtilities.getTransparentColor(color, 0.5f));
        }
        RectF rectF = new RectF(x, y, x + w, y + w);
        int rad1 = Math.round((w * 14.77F) / 100F);
        paint.setColor(AndroidUtilities.getTransparentColor(color, 0.5f));
        canvas.drawRoundRect(rectF, rad1, rad1, paint);

        if (isFlicker) return;
        int marginBottomText = Math.round((w * 12F) / 100F);
        int textWidth = Math.round((w * 80F) / 100F);
        int textHeight = Math.round((w * 19F) / 100F);
        int xText = x + (w >> 1) - (textWidth >> 1);
        int yText = y + w - marginBottomText - textHeight;
        RectF rectText = new RectF(xText, yText, xText + textWidth, yText + textHeight);
        paint.setColor(AndroidUtilities.getTransparentColor(color, 0.75f));
        canvas.drawRoundRect(rectText, textHeight >> 1, textHeight >> 1, paint);
    }

    public ThemeInfo getTheme() {
        return new ThemeInfo(
                true,
                AndroidUtilities.dp(10)
        );
    }
}
