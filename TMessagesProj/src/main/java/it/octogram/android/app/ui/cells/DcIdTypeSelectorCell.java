/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.PeerColorActivity;

import java.util.ArrayList;
import java.util.Locale;

import it.octogram.android.Datacenter;
import it.octogram.android.DcIdStyle;
import it.octogram.android.DcIdType;
import it.octogram.android.OctoConfig;

@SuppressLint({"UseCompatLoadingForDrawables", "ViewConstructor"})
public class DcIdTypeSelectorCell extends FrameLayout {

    public static final int height = dp(38);
    private ProfileDatacenterPreviewCell owlgramView;

    private LinearLayout telegramView;
    private AnimatedTextView animatedIdView;
    private FrameLayout profileViewFrame;
    private PeerColorActivity.ProfilePreview profilePreview;
    private int lastState = -1;
    private boolean wasShown = false;
    private final TLRPC.User user = AccountInstance.getInstance(UserConfig.selectedAccount).getUserConfig().getCurrentUser();
    private final Integer dc_id = AccountInstance.getInstance(UserConfig.selectedAccount).getConnectionsManager().getCurrentDatacenterId();
    private static final String telegramBotApiChatId = "-1001966997491";
    private final String defaultChatId = String.format(Locale.US, "%s", user.id);
    private final Theme.ResourcesProvider resourceProvider;

    private final FrameLayout navigationLayout;
    private final ImageView hiddenViewImage;
    private AnimatorSet discussValueAnimator;

    private final PorterDuffColorFilter defaultHiddenImageFilter;

    public DcIdTypeSelectorCell(Context context, Theme.ResourcesProvider resourceProvider) {
        super(context);
        this.resourceProvider = resourceProvider;

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        internalFrameLayout.setPadding(dp(3), dp(3), dp(3), dp(3));

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourceProvider), dp(5), dp(5));
        border.setAlpha(150);
        border.setCornerRadius(dp(25));
        internalFrameLayout.setBackground(border);
        
        navigationLayout = getNavigationLayout(context);
        navigationLayout.setAlpha(OctoConfig.INSTANCE.showDcId.getValue() ? 1f : 0.2f);
        internalFrameLayout.addView(navigationLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        hiddenViewImage = new ImageView(getContext());
        hiddenViewImage.setImageResource(R.drawable.msg_archive_hide);
        hiddenViewImage.setAlpha(OctoConfig.INSTANCE.showDcId.getValue() ? 0f : 1f);
        hiddenViewImage.setScaleType(ImageView.ScaleType.CENTER);
        hiddenViewImage.setColorFilter(defaultHiddenImageFilter = new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_fieldOverlayText), PorterDuff.Mode.SRC_IN));
        internalFrameLayout.addView(hiddenViewImage, LayoutHelper.createFrame(48, 48, Gravity.CENTER));
        wasShown = OctoConfig.INSTANCE.showDcId.getValue();

        if (OctoConfig.INSTANCE.dcIdStyle.getValue() == DcIdStyle.MINIMAL.getValue()) {
            MessagesController.PeerColor color = getOwnPeerColorState();
            if (color != null) {
                hiddenViewImage.setColorFilter(new PorterDuffColorFilter(color.getStoryColor1(Theme.isCurrentThemeDark()), PorterDuff.Mode.SRC_IN));
                editedColorFix = true;
            }
        }

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

        owlgramView = new ProfileDatacenterPreviewCell(context, null, true);
        owlgramView.setCustomPreviewModeData(dcInfo.getIcon(), longCaption, getCurrentChatIDFormat());
        navigationFrame.addView(owlgramView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, height, Gravity.CENTER_VERTICAL));

        telegramView = composeTelegramView(context, longCaption, getCurrentChatIDFormat());
        navigationFrame.addView(telegramView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.LEFT));

        MessagesController.PeerColor peerColor = getOwnPeerColorState();

        profileViewFrame = new FrameLayout(context);
        GradientDrawable border2 = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{
                        peerColor != null ? peerColor.getBgColor1(Theme.isCurrentThemeDark()) : Theme.getColor(Theme.key_avatar_backgroundActionBarBlue),
                        peerColor != null ? peerColor.getBgColor2(Theme.isCurrentThemeDark()) : Theme.getColor(Theme.key_avatar_backgroundActionBarBlue)
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

    private MessagesController.PeerColor getOwnPeerColorState() {
        if (!UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
            return null;
        }

        MessagesController.PeerColors peerColors = MessagesController.getInstance(UserConfig.selectedAccount).profilePeerColors;
        MessagesController.PeerColor peerColor = peerColors == null ? null : peerColors.getColor(UserObject.getProfileColorId(user));
        boolean canUsePeerColors = false;
        if (peerColor != null)  {
            canUsePeerColors = peerColor.getBgColor1(Theme.isCurrentThemeDark()) != peerColor.getBgColor2(Theme.isCurrentThemeDark());
            canUsePeerColors &= peerColor.getBgColor1(Theme.isCurrentThemeDark()) != Theme.getColor(Theme.key_windowBackgroundWhite);
            canUsePeerColors &= peerColor.getBgColor2(Theme.isCurrentThemeDark()) != Theme.getColor(Theme.key_windowBackgroundWhite);
        }

        return canUsePeerColors ? peerColor : null;
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

        updateShowDcId();

        if (lastState == currentState) {
            return;
        }

        int lastStateSaved = lastState;
        lastState = currentState;

        View toHide, toShow;
        if ((toHide = getAssocChild(lastStateSaved)) != null) {
            animateChild(toHide, true);
        }

        if ((toShow = getAssocChild(currentState)) != null) {
            animateChild(toShow, false);
        }
    }

    private boolean editedColorFix = false;
    private void updateShowDcId() {
        boolean isShow = OctoConfig.INSTANCE.showDcId.getValue();
        if (isShow == wasShown) {
            return;
        }

        if (!isShow) {
            if (OctoConfig.INSTANCE.dcIdStyle.getValue() == DcIdStyle.MINIMAL.getValue() && !editedColorFix) {
                MessagesController.PeerColor color = getOwnPeerColorState();
                if (color != null) {
                    hiddenViewImage.setColorFilter(new PorterDuffColorFilter(color.getStoryColor1(Theme.isCurrentThemeDark()), PorterDuff.Mode.SRC_IN));
                    editedColorFix = true;
                }
            } else if (OctoConfig.INSTANCE.dcIdStyle.getValue() != DcIdStyle.MINIMAL.getValue() && editedColorFix) {
                hiddenViewImage.setColorFilter(defaultHiddenImageFilter);
                editedColorFix = false;
            }
        }

        if (discussValueAnimator != null) {
            discussValueAnimator.cancel();
            discussValueAnimator = null;
        }

        wasShown = isShow;

        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofFloat(navigationLayout, "alpha", isShow ? 0.2f : 1f, isShow ? 1f : 0.2f));
        animators.add(ObjectAnimator.ofFloat(hiddenViewImage, "alpha", isShow ? 1f : 0f, isShow ? 0f : 1f));
        animators.add(ObjectAnimator.ofFloat(hiddenViewImage, "scaleX", isShow ? 1f : 0.6f, isShow ? 0.6f : 1f));
        animators.add(ObjectAnimator.ofFloat(hiddenViewImage, "scaleY", isShow ? 1f : 0.6f, isShow ? 0.6f : 1f));

        AnimatorSet set = discussValueAnimator = new AnimatorSet();
        set.setDuration(200);
        set.playTogether(animators);
        set.start();
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

    private void animateChild(View view, boolean disappear) {
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
