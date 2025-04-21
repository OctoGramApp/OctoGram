/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.PeerColorActivity;

import java.util.Locale;

import it.octogram.android.Datacenter;
import it.octogram.android.DcIdStyle;
import it.octogram.android.DcIdType;
import it.octogram.android.OctoConfig;

@SuppressLint({"UseCompatLoadingForDrawables", "ViewConstructor"})
public class DcInfoSelector extends FrameLayout {

    public static final int height = dp(38);
    private DatacenterCell owlgramView;

    private LinearLayout telegramView;
    private AnimatedTextView animatedIdView;
    private FrameLayout profileViewFrame;
    private PeerColorActivity.ProfilePreview profilePreview;
    private int lastState = -1;
    private final TLRPC.User user = AccountInstance.getInstance(UserConfig.selectedAccount).getUserConfig().getCurrentUser();
    private final Integer dc_id = AccountInstance.getInstance(UserConfig.selectedAccount).getConnectionsManager().getCurrentDatacenterId();
    private static final String telegramBotApiChatId = "-1001966997491";
    private final String defaultChatId = String.format(Locale.US, "%s", user.id);
    private final Theme.ResourcesProvider resourceProvider;

    public DcInfoSelector(Context context, Theme.ResourcesProvider resourceProvider) {
        super(context);
        this.resourceProvider = resourceProvider;

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        internalFrameLayout.setPadding(dp(3), dp(3), dp(3), dp(3));

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(dp(1), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourceProvider), 150), dp(5), dp(5));
        border.setCornerRadius(dp(25));
        internalFrameLayout.setBackground(border);

        internalFrameLayout.addView(getNavigationLayout(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        setPadding(dp(15), dp(15), dp(15), dp(15));
        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private FrameLayout getNavigationLayout(Context context) {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setCornerRadius(dp(15));

        FrameLayout navigationFrame = new FrameLayout(context);
        navigationFrame.setClipToPadding(true);
        navigationFrame.setClipToOutline(true);
        navigationFrame.setClipChildren(true);
        navigationFrame.setBackground(border);

        Datacenter dcInfo = Datacenter.Companion.getDcInfo(dc_id);
        String longCaption = String.format(Locale.ENGLISH, "dc%d (%s)", dcInfo.getDcId(), dcInfo.getDcName());

        owlgramView = new DatacenterCell(context, null, true);
        owlgramView.setCustomPreviewModeData(dcInfo.getIcon(), longCaption, getCurrentChatIDFormat());
        navigationFrame.addView(owlgramView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, height, Gravity.CENTER_VERTICAL));

        telegramView = composeTelegramView(context, longCaption, getCurrentChatIDFormat());
        navigationFrame.addView(telegramView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.LEFT));

        MessagesController.PeerColors peerColors = MessagesController.getInstance(UserConfig.selectedAccount).profilePeerColors;
        MessagesController.PeerColor peerColor = peerColors == null ? null : peerColors.getColor(UserObject.getProfileColorId(user));
        boolean canUsePeerColors = false;
        if (peerColor != null)  {
            canUsePeerColors = peerColor.getBgColor1(Theme.isCurrentThemeDark()) != peerColor.getBgColor2(Theme.isCurrentThemeDark());
            canUsePeerColors &= peerColor.getBgColor1(Theme.isCurrentThemeDark()) != Theme.getColor(Theme.key_windowBackgroundWhite);
            canUsePeerColors &= peerColor.getBgColor2(Theme.isCurrentThemeDark()) != Theme.getColor(Theme.key_windowBackgroundWhite);
        }

        profileViewFrame = new FrameLayout(context);
        GradientDrawable border2 = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{
                        canUsePeerColors ? peerColor.getBgColor1(Theme.isCurrentThemeDark()) : Theme.getColor(Theme.key_avatar_backgroundActionBarBlue),
                        canUsePeerColors ? peerColor.getBgColor2(Theme.isCurrentThemeDark()) : Theme.getColor(Theme.key_avatar_backgroundActionBarBlue)
                }
        );
        border2.setCornerRadius(dp(25));
        profileViewFrame.setBackground(border2);
        profilePreview = new PeerColorActivity.ProfilePreview(context, UserConfig.selectedAccount, 0, resourceProvider);
        profilePreview.setColor(UserObject.getProfileColorId(user), false);
        profilePreview.setEmoji(UserObject.getProfileEmojiId(user), false, false);
        profilePreview.setCustomInfoText(String.format(Locale.ENGLISH, "id: %s (dc%s)", getCurrentChatIDFormat(), dc_id));
        profileViewFrame.addView(profilePreview, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL));
        navigationFrame.addView(profileViewFrame, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL));

        preloadUI();

        return navigationFrame;
    }

    private LinearLayout composeTelegramView(Context context, String valueText, String idText) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(dp(23), 0, dp(23), 0);
        linearLayout.setLayoutParams(LayoutHelper.createLinear(LayoutParams.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int colorText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourceProvider);
        int colorText2 = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2, resourceProvider);

        animatedIdView = new AnimatedTextView(context);
        animatedIdView.setAnimationProperties(1f, 0, 300, CubicBezierInterpolator.EASE_OUT_QUINT);
        animatedIdView.setTextSize(dp(16));
        animatedIdView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        animatedIdView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        animatedIdView.setText(idText);
        animatedIdView.setTextColor(colorText);
        linearLayout.addView(animatedIdView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(11), Gravity.LEFT));

        LinkSpanDrawable.LinksTextView valueTextView = getTextView(context, valueText, colorText2);
        linearLayout.addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, dp(12), Gravity.LEFT));

        return linearLayout;
    }

    private static LinkSpanDrawable.LinksTextView getTextView(Context context, String valueText, int colorText2) {
        LinkSpanDrawable.LinksTextView valueTextView = new LinkSpanDrawable.LinksTextView(context, null);
        valueTextView.setLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        valueTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        valueTextView.setEllipsize(TextUtils.TruncateAt.END);
        valueTextView.setText(valueText);
        valueTextView.setTextColor(colorText2);
        return valueTextView;
    }

    private void preloadUI() {
        int currentState = OctoConfig.INSTANCE.dcIdStyle.getValue();
        lastState = currentState;
        owlgramView.setAlpha(currentState == DcIdStyle.OWLGRAM.getValue() ? 1f : 0f);
        telegramView.setAlpha(currentState == DcIdStyle.TELEGRAM.getValue() ? 1f : 0f);
        profileViewFrame.setAlpha(currentState == DcIdStyle.MINIMAL.getValue() ? 1f : 0f);
    }

    public void update() {
        int currentState = OctoConfig.INSTANCE.dcIdStyle.getValue();

        if (lastState == currentState) {
            return;
        }

        int lastStateSaved = lastState;
        lastState = currentState;

        if (currentState == DcIdStyle.NONE.getValue()) {
            return;
        }

        boolean disableAnimations = lastStateSaved == DcIdStyle.NONE.getValue();

        View toHide, toShow;
        if (disableAnimations) {
            animateChild(owlgramView, true, true);
            animateChild(telegramView, true, true);
            animateChild(profileViewFrame, true, true);
        } else if ((toHide = getAssocChild(lastStateSaved)) != null) {
            animateChild(toHide, true, false);
        }

        if ((toShow = getAssocChild(currentState)) != null) {
            animateChild(toShow, false, disableAnimations);
        }
    }

    public void updateChatID() {
        owlgramView.updateIdView(getCurrentChatIDFormat());
        animatedIdView.setText(getCurrentChatIDFormat());
        profilePreview.setCustomInfoText(String.format(Locale.ENGLISH, "id: %s (dc4)", getCurrentChatIDFormat()));
    }

    private View getAssocChild(int id) {
        return switch (DcIdStyle.Companion.fromInt(id)) {
            case DcIdStyle.OWLGRAM -> owlgramView;
            case DcIdStyle.TELEGRAM -> telegramView;
            case DcIdStyle.MINIMAL -> profileViewFrame;
            default -> null;
        };
    }


    private String getCurrentChatIDFormat() {
        return OctoConfig.INSTANCE.dcIdType.getValue() == DcIdType.BOT_API.getValue() ? telegramBotApiChatId : defaultChatId;
    }

    private void animateChild(View view, boolean disappear, boolean disableAnimations) {
        if (disableAnimations) {
            view.setAlpha(disappear ? 0f : 1f);
            view.setScaleX(1f);
            view.setScaleY(1f);
            return;
        }

        view.setAlpha(disappear ? 1f : 0f);
        view.setScaleX(disappear ? 1f : 0.8f);
        view.setScaleY(disappear ? 1f : 0.8f);
        view.animate().alpha(disappear ? 0f : 1f).scaleX(disappear ? 1.2f : 1f).scaleY(disappear ? 1.2f : 1f).setDuration(200).start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
