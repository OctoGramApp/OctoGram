/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.ai.MessagesModelsWrapper;
import it.octogram.android.ai.helper.CustomModelsHelper;
import it.octogram.android.ai.helper.MainAiHelper;

public class ChatUnreadCell extends FrameLayout {

    private final TextView textView;
    private final Theme.ResourcesProvider resourcesProvider;

    public ChatUnreadCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        this(context, resourcesProvider, 0, null, null);
    }

    public ChatUnreadCell(Context context, Theme.ResourcesProvider resourcesProvider, int unreadCount, TLRPC.User currentUser, TLRPC.Chat currentChat) {
        super(context);
        this.resourcesProvider = resourcesProvider;
//
//        backgroundLayout = new FrameLayout(context);
//        backgroundLayout.setBackgroundResource(R.drawable.newmsg_divider);
//        backgroundLayout.getBackground().setColorFilter(new PorterDuffColorFilter(getColor(Theme.key_chat_unreadMessagesStartBackground), PorterDuff.Mode.MULTIPLY));
//        addView(backgroundLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 27, Gravity.LEFT | Gravity.TOP, 0, 7, 0, 0));
//
//        imageView = new ImageView(context);
//        imageView.setImageResource(R.drawable.ic_ab_new);
//        imageView.setColorFilter(new PorterDuffColorFilter(getColor(Theme.key_chat_unreadMessagesStartArrowIcon), PorterDuff.Mode.MULTIPLY));
//        imageView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
//        backgroundLayout.addView(imageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 10, 0));
//
//        textView = new TextView(context);
//        textView.setPadding(0, 0, 0, AndroidUtilities.dp(1));
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
//        textView.setTextColor(getColor(Theme.key_chat_unreadMessagesStartText));
//        textView.setTypeface(AndroidUtilities.bold());
//        addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        int rad;
        if (SharedConfig.bubbleRadius > 2) {
            rad = dp(SharedConfig.bubbleRadius - 2);
        } else {
            rad = dp(SharedConfig.bubbleRadius);
        }

        boolean supportsChatContext = (currentUser != null || currentChat != null) && unreadCount > 6;
        if (supportsChatContext) {
            supportsChatContext = MainAiHelper.canUseAiFeatures() && OctoConfig.INSTANCE.aiFeaturesChatContext.getValue();
        }

        LinearLayout backgroundLayout = new LinearLayout(context);
        backgroundLayout.setOrientation(LinearLayout.VERTICAL);
        backgroundLayout.setBackground(Theme.createRoundRectDrawable(rad, getColor(Theme.key_chat_unreadMessagesStartBackground)));
        addView(backgroundLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 10, 0, 10));

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getColor(Theme.key_chat_unreadMessagesStartText));
        textView.setTypeface(AndroidUtilities.bold());
        backgroundLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, supportsChatContext ? 0 : 15, 5, supportsChatContext ? 0 : 15, !supportsChatContext ? 5 : 0));

        if (supportsChatContext) {
            backgroundLayout.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_chat_unreadMessagesStartBackground), rad));
            backgroundLayout.setOnClickListener(v -> MessagesModelsWrapper.handleMessagesSelectionWithModel(context, CustomModelsHelper.VIRTUAL_CHAT_CONTEXT_MODEL_ID, currentUser, currentChat, new ArrayList<>(), unreadCount));

            TextView chatContextAvailable = new TextView(context);
            chatContextAvailable.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            chatContextAvailable.setTextColor(getColor(Theme.key_chat_unreadMessagesStartText));
            chatContextAvailable.setTypeface(AndroidUtilities.bold());
            chatContextAvailable.setGravity(Gravity.CENTER);
            chatContextAvailable.setAlpha(0.5f);
            chatContextAvailable.setText(LocaleController.getString(R.string.AiFeatures_Features_ChatContext_Available));
            backgroundLayout.addView(chatContextAvailable, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 25, 0, 25, 5));
        }
        //addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 32, 0, 32, 0));
    }

    public void setText(String text) {
        textView.setText(text);
    }

//    public ImageView getImageView() {
//        return imageView;
//    }

    public TextView getTextView() {
        return textView;
    }

//    public FrameLayout getBackgroundLayout() {
//        return backgroundLayout;
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80), MeasureSpec.AT_MOST));
    }

    private int getColor(int key) {
        Integer color = resourcesProvider != null ? resourcesProvider.getColor(key) : null;
        return color != null ? color : Theme.getColor(key);
    }
}
