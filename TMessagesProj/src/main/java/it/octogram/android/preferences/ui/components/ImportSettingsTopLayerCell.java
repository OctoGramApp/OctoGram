package it.octogram.android.preferences.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

@SuppressLint("ViewConstructor")
public class ImportSettingsTopLayerCell extends LinearLayout {
    public ImportSettingsTopLayerCell(Context context, boolean isOut) {
        super(context);
        super.setOrientation(LinearLayout.VERTICAL);
        super.setMinimumWidth(LayoutParams.MATCH_PARENT);

        RLottieImageView imageView = new RLottieImageView(getContext());
        imageView.setAutoRepeat(false);
        imageView.setAnimation(R.raw.saved_folders, AndroidUtilities.dp(130), AndroidUtilities.dp(130));
        imageView.playAnimation();

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString("ImportReady", R.string.ImportReady));
        textView.setPadding(AndroidUtilities.dp(30), 0, AndroidUtilities.dp(30), 0);

        TextView captionView = new TextView(context);
        captionView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        captionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        captionView.setGravity(Gravity.CENTER_HORIZONTAL);
        captionView.setText(LocaleController.getString(isOut ? "ImportReadyDescription" : "ImportReadyDescriptionFromExternal", isOut ? R.string.ImportReadyDescription : R.string.ImportReadyDescriptionFromExternal));
        captionView.setPadding(AndroidUtilities.dp(30), AndroidUtilities.dp(10), AndroidUtilities.dp(30), AndroidUtilities.dp(21));

        addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER, 0, 16, 0, 16));
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addView(captionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
    }
}
