package it.octogram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.Forum.ForumUtilities;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Switch;

public class EditTopic extends FrameLayout {

    private final SimpleTextView textView;
    private final BackupImageView imageView;
    private final Switch switchView;
    public TLRPC.TL_forumTopic topic;

    public EditTopic(@NonNull Context context) {
        super(context);

        textView = new SimpleTextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(16);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setMaxLines(1);
        textView.setMaxLines(1);
        textView.setGravity(LayoutHelper.getAbsoluteGravityStart() | Gravity.CENTER_VERTICAL);
        addView(textView, LayoutHelper.createFrameRelatively(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START | Gravity.CENTER_VERTICAL, 81, 0, 61, 0));

        imageView = new BackupImageView(context);
        imageView.setAspectFit(true);
        imageView.setLayerNum(1);
        addView(imageView, LayoutHelper.createFrameRelatively(32, 32, Gravity.START | Gravity.CENTER_VERTICAL, 23, 0, 0, 0));

        switchView = new Switch(context);
        switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        addView(switchView, LayoutHelper.createFrameRelatively(37, 20, Gravity.END | Gravity.CENTER_VERTICAL, 0, 0, 22, 0));

        View overlaySelectorView = new View(context);
        overlaySelectorView.setBackground(Theme.getSelectorDrawable(false));
        addView(overlaySelectorView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec((int) (AndroidUtilities.dp(58) + Theme.dividerPaint.getStrokeWidth()), MeasureSpec.EXACTLY));
    }

    public void setData(TLRPC.TL_forumTopic topic) {
        this.topic = topic;
        textView.setText(topic.title);
        if (topic.id == 1) {
            imageView.setImageDrawable(ForumUtilities.createGeneralTopicDrawable(getContext(), 0.75f, Theme.getColor(Theme.key_chat_inMenu)));
        } else if (topic.icon_emoji_id != 0) {
            imageView.setAnimatedEmojiDrawable(new AnimatedEmojiDrawable(AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, UserConfig.selectedAccount, topic.icon_emoji_id));
        } else {
            imageView.setImageDrawable(ForumUtilities.createTopicDrawable(topic));
        }
    }

    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (switchView != null) {
            switchView.setChecked(checked, animated);
        }
    }

    public boolean isChecked() {
        if (switchView != null) {
            return switchView.isChecked();
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        float w = Theme.dividerPaint.getStrokeWidth();
        int l = 0, r = 0;
        int pad = AndroidUtilities.dp(81);
        if (LocaleController.isRTL) {
            r = pad;
        } else {
            l = pad;
        }
        canvas.drawLine(getPaddingLeft() + l, getHeight() - w, getWidth() - getPaddingRight() - r, getHeight() - w, Theme.dividerPaint);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setEnabled(true);
        info.setClickable(true);
        if (switchView != null) {
            info.setCheckable(true);
            info.setChecked(isChecked());
            info.setClassName("android.widget.Switch");
        } else if (isChecked()) {
            info.setSelected(true);
        }
        info.setContentDescription(textView.getText());
    }
}
