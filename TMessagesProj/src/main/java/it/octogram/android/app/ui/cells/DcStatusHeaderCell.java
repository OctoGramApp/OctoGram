/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.app.ui.DcStatusActivity;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.appearance.MessageStringHelper;

@SuppressLint("ViewConstructor")
public class DcStatusHeaderCell extends LinearLayout {
    private final ButtonWithCounterView buttonWithCounterView;
    private boolean _isWaiting = false;
    private final int pageType;
    private final ColoredImageSpan startMonitorSpan;
    private final ColoredImageSpan stopMonitorSpan;

    public DcStatusHeaderCell(Context context, int pageType, Runnable startMonitoring) {
        super(context);

        startMonitorSpan = new ColoredImageSpan(R.drawable.media_photo_flash_on2);
        stopMonitorSpan = new ColoredImageSpan(R.drawable.msg_pollstop);

        this.pageType = pageType;

        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        StickerImageView rLottieImageView = new StickerImageView(context, UserConfig.selectedAccount);
        rLottieImageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        rLottieImageView.getImageReceiver().setAutoRepeat(1);

        addView(rLottieImageView, LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

        AppCompatTextView textView = new AppCompatTextView(context);
        addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Theme.getColor(Theme.key_chats_message));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));

        String text;
        if (pageType == DcStatusActivity.PAGE_NETWORK) {
            rLottieImageView.setStickerNum(StickerUi.DATACENTER_STATUS.getValue());
            text = getString(R.string.DatacenterStatusSection_Desc);
        } else if (pageType == DcStatusActivity.PAGE_MEDIA) {
            rLottieImageView.setStickerNum(StickerUi.MEDIA_LOADING.getValue());
            text = getString(R.string.DatacenterStatusSection_Desc_Media);
        } else {
            rLottieImageView.setStickerNum(StickerUi.WEB_SEARCH.getValue());
            text = getString(R.string.DatacenterStatusSection_Desc_Web);
        }

        Spannable htmlParsed = new SpannableString(OctoUtils.fromHtml(text));
        textView.setText(MessageStringHelper.getUrlNoUnderlineText(htmlParsed));
        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

        buttonWithCounterView = new ButtonWithCounterView(context, null);
        buttonWithCounterView.setPadding(dp(34), 0, dp(34), 0);
        updateStatus(true);
        buttonWithCounterView.setOnClickListener(view -> {
            if (!_isWaiting && pageType != DcStatusActivity.PAGE_NETWORK) {
                return;
            }

            startMonitoring.run();
        });

        buttonWithCounterView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonWithCounterView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
        addView(buttonWithCounterView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 16, 15, 16, 16));
    }

    public void updateStatus(boolean isWaiting) {
        if (_isWaiting == isWaiting) {
            return;
        }
        _isWaiting = isWaiting;

        if (pageType != DcStatusActivity.PAGE_NETWORK) {
            buttonWithCounterView.setLoading(!isWaiting);
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        int text = getText(isWaiting || pageType != DcStatusActivity.PAGE_NETWORK);

        spannableStringBuilder.append(".  ").append(getString(text));
        spannableStringBuilder.setSpan((isWaiting || pageType != DcStatusActivity.PAGE_NETWORK) ? startMonitorSpan : stopMonitorSpan, 0, 1, 0);
        buttonWithCounterView.setText(spannableStringBuilder, pageType == DcStatusActivity.PAGE_NETWORK);

    }

    private int getText(boolean isWaiting) {
        if (!isWaiting) {
            return R.string.DatacenterStatusSection_Stop;
        }
        return switch (pageType) {
            case DcStatusActivity.PAGE_NETWORK -> R.string.DatacenterStatusSection_Start;
            case DcStatusActivity.PAGE_MEDIA -> R.string.DatacenterStatusSection_Start_Media;
            case DcStatusActivity.PAGE_WEB -> R.string.DatacenterStatusSection_Start_Ping;
            default -> R.string.DatacenterStatusSection_Stop;
        };
    }
}
