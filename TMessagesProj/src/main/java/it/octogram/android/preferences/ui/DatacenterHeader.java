package it.octogram.android.preferences.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;
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

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.utils.MessageStringHelper;
import it.octogram.android.utils.OctoUtils;


public class DatacenterHeader extends LinearLayout {
    public DatacenterHeader(Context context) {
        super(context);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        StickerImageView rLottieImageView = new StickerImageView(context, UserConfig.selectedAccount);
        rLottieImageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        rLottieImageView.setStickerNum(StickerUi.DATACENTER_STATUS.getValue());
        rLottieImageView.getImageReceiver().setAutoRepeat(1);

        addView(rLottieImageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

        AppCompatTextView textView = new AppCompatTextView(context);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
        String text = LocaleController.getString(R.string.DatacenterStatusSection_Desc);
        Spannable htmlParsed = new SpannableString(OctoUtils.fromHtml(text));
        textView.setText(MessageStringHelper.getUrlNoUnderlineText(htmlParsed));
        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

        AppCompatTextView buttonTextView = new AppCompatTextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(".  ").append(LocaleController.getString(R.string.WebVersion));
        spannableStringBuilder.setSpan(new ColoredImageSpan(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.device_web_other))), 0, 1, 0);
        buttonTextView.setText(spannableStringBuilder);
        buttonTextView.setOnClickListener(view -> Browser.openUrl(AndroidUtilities.findActivity(context), String.format("https://%s/dcstatus", OctoUtils.getDomain())));

        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 16, 15, 16, 16));
    }
}
