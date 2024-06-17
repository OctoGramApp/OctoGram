package it.octogram.android.preferences.ui.custom;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

import it.octogram.android.OctoConfig;

public class AllowSmartNotificationsBottomSheet extends BottomSheet {
    private AllowSmartNotificationsCallback callback;

    public AllowSmartNotificationsBottomSheet(Context context, AllowSmartNotificationsCallback callback) {
        super(context, false);

        this.callback = callback;

        FrameLayout frameLayout = new FrameLayout(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        RLottieImageView rLottieImageView = new RLottieImageView(context);
        rLottieImageView.setScaleType(ImageView.ScaleType.CENTER);
        rLottieImageView.setAnimation(R.raw.error, 46, 46);
        rLottieImageView.playAnimation();
        rLottieImageView.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(72), Theme.getColor(Theme.key_avatar_backgroundRed)));
        frameLayout.addView(rLottieImageView, LayoutHelper.createFrame(72, 72, Gravity.CENTER));
        linearLayout.addView(frameLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 110));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString("Warning", R.string.Warning));
        textView.setPadding(AndroidUtilities.dp(30), 0, AndroidUtilities.dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        //textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MONO));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(String.format("%s\n\n%s", LocaleController.getString("SmartNotificationsPvtDialogMessage", R.string.SmartNotificationsPvtDialogMessage), LocaleController.getString("SmartNotificationsPvtDialogMessagePlus", R.string.SmartNotificationsPvtDialogMessagePlus)));
        textView.setPadding(AndroidUtilities.dp(30), AndroidUtilities.dp(10), AndroidUtilities.dp(30), AndroidUtilities.dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(LocaleController.getString("Cancel", R.string.Cancel));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> dismiss());
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(LocaleController.getString("SmartNotificationsPvtDialogButton", R.string.SmartNotificationsPvtDialogButton));
        textView.setTextColor(Theme.getColor(Theme.key_color_red));
        textView.setOnClickListener(view -> {
            OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats.updateValue(!OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats.getValue());
            dismiss();

            if (callback != null) {
                callback.didSelectEnable();
            }
        });
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));

        setCustomView(linearLayout);
    }

    public interface AllowSmartNotificationsCallback {
        void didSelectEnable();
    }
}
