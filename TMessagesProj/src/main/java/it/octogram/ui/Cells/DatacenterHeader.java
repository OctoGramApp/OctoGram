package it.octogram.ui.Cells;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;

import java.util.Objects;

import it.octogram.android.entities.EntitiesHelper;

public class DatacenterHeader extends LinearLayout {
    public DatacenterHeader(Context context) {
        super(context);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        StickerImageView rLottieImageView = new StickerImageView(context, UserConfig.selectedAccount);
        rLottieImageView.setStickerPackName("UtyaDuck");
        rLottieImageView.setStickerNum(31);
        rLottieImageView.getImageReceiver().setAutoRepeat(1);
        addView(rLottieImageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

        TextView textView = new TextView(context);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
        String text = LocaleController.getString("DatacenterStatusDesc", R.string.DatacenterStatusDesc);
        Spannable htmlParsed = new SpannableString(AndroidUtilities.fromHtml(text));
        textView.setText(EntitiesHelper.getUrlNoUnderlineText(htmlParsed));
        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(".  ").append(LocaleController.getString("WebVersion", R.string.WebVersion));
        spannableStringBuilder.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.web_access))), 0, 1, 0);
        buttonTextView.setText(spannableStringBuilder);
        buttonTextView.setOnClickListener(view -> Browser.openUrl(AndroidUtilities.findActivity(context), "https://status.octogram.app/"));

        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 16, 15, 16, 16));
    }
}
