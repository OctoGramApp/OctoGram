package it.octogram.android.preferences.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class TextDetailCellMultiline extends LinearLayout {

    private final SimpleTextView spoilersTextView;
    private final TextView valueTextView;
    private boolean needDivider;
    private boolean contentDescriptionValueFirst;
    private CharSequence charSequence;

    public TextDetailCellMultiline(Context context) {
        super(context);

        setOrientation(VERTICAL);
        spoilersTextView = new SimpleTextView(context);
        spoilersTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        spoilersTextView.setTextSize(16);
        spoilersTextView.setMaxLines(Integer.MAX_VALUE);
        spoilersTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        spoilersTextView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        spoilersTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(spoilersTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 23, 8, 23, 0));

        valueTextView = new TextView(context);
        valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        valueTextView.setLines(1);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        valueTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(valueTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 23, 3, 23, 8));
    }

    public boolean haveSpoilers() {
        return spoilersTextView.hasSpoilers();
    }

    @SuppressLint("Recycle")
    public void revealSpoilers() {
        spoilersTextView.hasSpoilers();
    }

    public void setTextAndValue(CharSequence text, String value, boolean divider) {
        charSequence = text;
        spoilersTextView.setText(text);
        valueTextView.setText(value);
        needDivider = divider;
        setWillNotDraw(!needDivider);
    }

    public void setTextWithAnimatedEmojiAndValue(CharSequence text, ArrayList<TLRPC.MessageEntity> entities, CharSequence value, boolean divider) {
        text = MessageObject.replaceAnimatedEmoji(text, entities, spoilersTextView.getPaint().getFontMetricsInt());
        setTextWithEmojiAndValue(text, value, divider);
    }

    public void setTextWithEmojiAndValue(CharSequence text, CharSequence value, boolean divider) {
        charSequence = text;
        spoilersTextView.setText(Emoji.replaceEmoji(text, spoilersTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(14), false));
        valueTextView.setText(value);
        needDivider = divider;
        setWillNotDraw(!divider);
    }

    public void setContentDescriptionValueFirst(boolean contentDescriptionValueFirst) {
        this.contentDescriptionValueFirst = contentDescriptionValueFirst;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        spoilersTextView.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(
                    LocaleController.isRTL ? 0 : AndroidUtilities.dp(20),
                    getMeasuredHeight() - 1,
                    getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0),
                    getMeasuredHeight() - 1,
                    Theme.dividerPaint
            );
        }
    }

    public CharSequence getText() {
        return charSequence;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        final CharSequence text = spoilersTextView.getText();
        final CharSequence valueText = valueTextView.getText();
        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(valueText)) {
            info.setText((contentDescriptionValueFirst ? valueText : text) + ": " + (contentDescriptionValueFirst ? text : valueText));
        }
    }
}
