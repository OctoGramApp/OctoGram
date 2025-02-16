package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import it.octogram.android.preferences.ui.DcStatusActivity;

public class LockedChatsHelp extends FrameLayout {
    private LinkSpanDrawable.LinksTextView subtitleTextView;
    private ButtonWithCounterView button;

    public LockedChatsHelp(Context context, Theme.ResourcesProvider resourcesProvider, @NonNull Runnable linkCallback, Runnable buttonCallback) {
        super(context);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        addView(layout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        ImageView archiveIcon = new ImageView(context);
        archiveIcon.setBackground(Theme.createCircleDrawable(dp(80), Theme.getColor(Theme.key_avatar_backgroundSaved, resourcesProvider)));
        archiveIcon.setImageResource(R.drawable.solar_key);
        archiveIcon.setScaleType(ImageView.ScaleType.CENTER);
        layout.addView(archiveIcon, LayoutHelper.createLinear(80, 80, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, (buttonCallback != null ? 14 : 0), 0, 14));

        TextView titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        titleTextView.setTypeface(AndroidUtilities.bold());
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setText(LocaleController.getString(R.string.LockedChatsHelpTitle));
        layout.addView(titleTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 0, 32, 9));

        subtitleTextView = new LinkSpanDrawable.LinksTextView(context);
        subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        subtitleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider));
        subtitleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        SpannableStringBuilder linkedSubtitle = AndroidUtilities.replaceSingleTag(LocaleController.getString(R.string.LockedChatsHelpDescription), Theme.key_chat_messageLinkIn, 0, linkCallback);
        SpannableString arrow = new SpannableString(">");
        Drawable imageDrawable = getContext().getResources().getDrawable(R.drawable.msg_arrowright).mutate();
        imageDrawable.setColorFilter(new PorterDuffColorFilter(Theme.key_chat_messageLinkIn, PorterDuff.Mode.SRC_IN));
        ColoredImageSpan span = new ColoredImageSpan(imageDrawable);
        span.setColorKey(Theme.key_chat_messageLinkIn);
        span.setSize(dp(18));
        span.setWidth(dp(11));
        span.setTranslateX(-dp(5));
        arrow.setSpan(span, 0, arrow.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        subtitleTextView.setText(AndroidUtilities.replaceCharSequence(">", linkedSubtitle, arrow));
        layout.addView(subtitleTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 32, 0, 32, 25));

        layout.addView(
                makeHint(R.drawable.solar_key, LocaleController.getString(R.string.LockedChatsHelpHint1Title), LocaleController.getString(R.string.LockedChatsHelpHint1Description), resourcesProvider),
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
        );
        layout.addView(
                makeHint(R.drawable.msg_archive_hide, LocaleController.getString(R.string.LockedChatsHelpHint2Title), LocaleController.getString(R.string.LockedChatsHelpHint2Description), resourcesProvider),
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
        );
        layout.addView(
                makeHint(R.drawable.menu_devices, LocaleController.getString(R.string.LockedChatsHelpHint3Title), LocaleController.getString(R.string.LockedChatsHelpHint3Description), resourcesProvider),
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
        );

        if (buttonCallback != null) {
            button = new ButtonWithCounterView(context, resourcesProvider);
            button.setText(LocaleController.getString(R.string.GotIt), false);
            button.setOnClickListener(e -> buttonCallback.run());
            layout.addView(button, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 14, 18, 14, 0));
        }
    }

    public void asSettingsUI() {
        subtitleTextView.setText(LocaleController.getString(R.string.LockedChatsHelpDescription_Settings));
        if (button != null) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(LocaleController.getString(R.string.LockedChatsHelpHintEditList)).append(" >");
            ColoredImageSpan span = new ColoredImageSpan(R.drawable.msg_arrowright);
            spannableStringBuilder.setSpan(span, spannableStringBuilder.length() - 1, spannableStringBuilder.length(), 0);
            button.setText(spannableStringBuilder, false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(400), MeasureSpec.getSize(widthMeasureSpec)), MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    private FrameLayout makeHint(int resId, CharSequence title, CharSequence subtitle, Theme.ResourcesProvider resourcesProvider) {
        FrameLayout hint = new FrameLayout(getContext());

        ImageView imageView = new ImageView(getContext());
        imageView.setColorFilter(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        imageView.setImageResource(resId);
        hint.addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.TOP, 0, 8, 0, 0));

        LinearLayout textLayout = new LinearLayout(getContext());
        textLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView1 = new TextView(getContext());
        textView1.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        textView1.setTypeface(AndroidUtilities.bold());
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(14));
        textView1.setText(title);
        textLayout.addView(textView1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2.6f, 0, 0));

        TextView textView2 = new TextView(getContext());
        textView2.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourcesProvider));
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(14));
        textView2.setText(subtitle);
        textLayout.addView(textView2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2.6f, 0, 0));
        hint.addView(textLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 41, 0, 0, 0));

        return hint;
    }
}
