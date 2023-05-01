package it.octogram.ui.Cells.Dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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
public class LinearButtonCell extends BaseButtonCell {
    private final int[] colors;
    private final TextView tv;
    private final ImageView mt;


    @SuppressLint("ClickableViewAccessibility")
    public LinearButtonCell(Context context, Theme.ResourcesProvider resourcesProvider, String text, int iconId, int color) {
        super(context, resourcesProvider);
        colors = new int[]{
                color,
                Theme.key_windowBackgroundWhiteBlackText,
        };

        setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f));
        setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);

        CardView cardView = new CardView(context);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(AndroidUtilities.dp(50), AndroidUtilities.dp(50)));
        cardView.setCardElevation(0);
        cardView.setRadius(AndroidUtilities.dp(25));
        cardView.setCardBackgroundColor(Color.TRANSPARENT);

        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mt = new ImageView(context);
        mt.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mt.setClickable(true);
        mt.setOnTouchListener(this::delegateOnClick);
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, AndroidUtilities.getTransparentColor(getBackColor(), 0.2f)));
        mt.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        iv = new RLottieImageView(context);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(AndroidUtilities.dp(27), AndroidUtilities.dp(27));
        layoutParams2.setMargins(0, 0, 0, 0);
        layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
        iv.setLayoutParams(layoutParams2);
        if (iconId == R.raw.camera_outline) {
            cameraDrawable = new RLottieDrawable(R.raw.camera_outline, String.valueOf(R.raw.camera_outline), AndroidUtilities.dp(27 * 2), AndroidUtilities.dp(27 * 2), false, null);
            iv.setAnimation(cameraDrawable);
            iv.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            iv.setBackground(ContextCompat.getDrawable(context, iconId));
        }

        tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, AndroidUtilities.dp(5), 0, 0);
        tv.setLayoutParams(layoutParams);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        tv.setGravity(Gravity.CENTER);
        tv.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        addView(cardView);
        cardView.addView(rl);
        rl.addView(mt);
        rl.addView(iv);
        addView(tv);
        updateColors();
    }

    @Override
    public void updateColors() {
        tv.setTextColor(getThemedColor(colors[0]));
        if (iv.getBackground() != null) {
            iv.getBackground().setColorFilter(new PorterDuffColorFilter(getThemedColor(colors[0]), PorterDuff.Mode.MULTIPLY));
        } else {
            iv.setColorFilter(new PorterDuffColorFilter(getThemedColor(colors[0]), PorterDuff.Mode.MULTIPLY));
        }
        mt.setBackground(Theme.createSimpleSelectorRoundRectDrawable(0, Color.TRANSPARENT, AndroidUtilities.getTransparentColor(getBackColor(), 0.2f)));
    }

    public static int getPreviewHeight() {
        return AndroidUtilities.dp(8 + 50 + 5 + 10 + 8);
    }

    public static void getPreview(Canvas canvas, int x, int y, int w, int pos, Context context, Paint paint) {
        if (paint != null) {
            RoundedButtonCell.getPreview(canvas, x, y, w, paint);
            return;
        }
        int color = getThemedColor(Theme.key_switchTrack);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(AndroidUtilities.getTransparentColor(color, 0.5f));

        int topTextMargin = Math.round((w * 15F) / 100F);
        int textWidth = Math.round((w * 100F) / 100F);
        int textHeight = Math.round((w * 19F) / 100F);
        int originalBHeight = w - textHeight - topTextMargin;
        int buttonHeight = Math.round(((originalBHeight) * 80f) / 100f);
        int marginDiff = originalBHeight - buttonHeight;
        topTextMargin += marginDiff;
        int xButton = x + (w >> 1) - (buttonHeight >> 1);

        int totalHeight = buttonHeight + topTextMargin + textHeight;

        int totalYMiddle = y + (w >> 1) - (totalHeight >> 1);

        int xText = x + (w >> 1) - (textWidth >> 1);
        int yText = totalYMiddle + buttonHeight + topTextMargin;


        int iconId;
        switch (pos) {
            case 1:
                iconId = R.drawable.msg_videocall;
                break;
            case 2:
                iconId = R.drawable.msg_calls;
                break;
            case 3:
                iconId = R.drawable.msg_block;
                break;
            default:
                iconId = R.drawable.profile_newmsg;
                break;
        }
        Drawable d = ContextCompat.getDrawable(context, iconId);
        if (d != null) {
            Rect rect = new Rect(xButton, totalYMiddle + (marginDiff >> 1), xButton + buttonHeight, totalYMiddle + (marginDiff >> 1) + buttonHeight);
            d.setBounds(rect);
            d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            d.setAlpha(Math.round(255 * 0.5f));
            d.draw(canvas);
            d.clearColorFilter();
            d.setAlpha(255);
        }
        RectF rectText = new RectF(xText, yText, xText + textWidth, yText + textHeight);
        canvas.drawRoundRect(rectText, textHeight >> 1, textHeight >> 1, paint);
    }

    public ThemeInfo getTheme() {
        return new ThemeInfo(
                false,
                0
        );
    }
}
