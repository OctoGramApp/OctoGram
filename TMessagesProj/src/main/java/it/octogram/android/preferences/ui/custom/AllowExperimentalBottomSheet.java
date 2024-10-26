package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

import it.octogram.android.OctoConfig;

public class AllowExperimentalBottomSheet extends BottomSheet {
    public AllowExperimentalBottomSheet(Context context) {
        super(context, false);

        var frameLayout = new FrameLayout(context);
        var linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        var rLottieImageView = new RLottieImageView(context);
        rLottieImageView.setScaleType(AppCompatImageView.ScaleType.CENTER);
        rLottieImageView.setAnimation(R.raw.error, 46, 46);
        rLottieImageView.playAnimation();
        rLottieImageView.setBackground(Theme.createCircleDrawable(dp(72), Theme.getColor(Theme.key_avatar_backgroundRed)));
        frameLayout.addView(rLottieImageView, LayoutHelper.createFrame(72, 72, Gravity.CENTER));
        linearLayout.addView(frameLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 110));

        var textView = new AppCompatTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString(R.string.Warning));
        textView.setPadding(dp(30), 0, dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new AppCompatTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        //textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MONO));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString(R.string.OctoExperimentsDialogMessage));
        textView.setPadding(dp(30), dp(10), dp(30), dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        var buttonTextView = new AppCompatTextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(LocaleController.getString(R.string.Cancel));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> dismiss());
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        textView = new AppCompatTextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(LocaleController.getString(R.string.OctoExperimentsDialogButton));
        textView.setTextColor(Theme.getColor(Theme.key_color_red));
        textView.setOnClickListener(view -> {
            OctoConfig.INSTANCE.experimentsEnabled.updateValue(!OctoConfig.INSTANCE.experimentsEnabled.getValue());
            dismiss();
        });
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));

        setCustomView(linearLayout);
    }
}
