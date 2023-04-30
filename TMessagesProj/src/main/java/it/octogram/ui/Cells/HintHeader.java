package it.octogram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

@SuppressLint("ViewConstructor")
public class HintHeader extends FrameLayout {
    private final RLottieImageView imageView;

    public HintHeader(Context context, int icon, String text) {
        super(context);
        imageView = new RLottieImageView(context);
        imageView.setAnimation(icon, 90, 90);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.playAnimation();
        addView(imageView, LayoutHelper.createFrame(90, 90, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));
        imageView.setOnClickListener(v -> {
            if (!imageView.isPlaying()) {
                imageView.setProgress(0.0f);
                imageView.playAnimation();
            }
        });

        TextView messageTextView = new TextView(context);
        messageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        messageTextView.setGravity(Gravity.CENTER);
        messageTextView.setText(AndroidUtilities.replaceTags(text));
        addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 40, 121, 40, 24));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
    }
}
