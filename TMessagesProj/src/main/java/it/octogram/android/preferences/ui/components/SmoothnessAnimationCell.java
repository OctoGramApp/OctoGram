/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AppIconBulletinLayout;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LauncherIconController;

import java.util.ArrayList;
import java.util.Objects;

import it.octogram.android.OctoConfig;

@SuppressLint("ViewConstructor")
public class SmoothnessAnimationCell extends FrameLayout {
    public ActionBarLayout navigationLayout;

    public static final int height = dp(100);

    public SmoothnessAnimationCell(Context context) {
        super(context);

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        internalFrameLayout.setPadding(dp(2), dp(2), dp(2), dp(2));

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setAlpha(150);
        border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), dp(5), dp(5));
        border.setCornerRadius(dp(16));
        internalFrameLayout.setBackground(border);

        internalFrameLayout.addView(getNavigationLayout(context));

        setPadding(dp(15), dp(15), dp(15), dp(15));
        setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private FrameLayout getNavigationLayout(Context context) {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setCornerRadius(dp(16));

        navigationLayout = new ActionBarLayout(context, false);
        navigationLayout.setDisallowParentIntercept(true);
        navigationLayout.setFragmentStack(new ArrayList<>());
        navigationLayout.addFragmentToStack(new SmoothnessAnimationUI());
        navigationLayout.updateUseAlternativeNavigation(true);

        FrameLayout navigationFrame = new FrameLayout(context);
        navigationFrame.setClipToPadding(true);
        navigationFrame.setClipToOutline(true);
        navigationFrame.setClipChildren(true);
        navigationFrame.setBackground(border);
        navigationFrame.addView(navigationLayout);

        return navigationFrame;
    }

    static class SmoothnessAnimationUI extends BaseFragment {
        private boolean isFirstPage = true;
        @Override
        public View createView(Context context) {
            if (isFirstPage) {
                MenuDrawable drawable = new MenuDrawable();
                drawable.setRoundCap();
                actionBar.setBackButtonDrawable(drawable);
            } else {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            }

            final int[] clicksCount = {0};

            actionBar.setAllowOverlayTitle(true);
            actionBar.setTitle(LocaleController.formatString(R.string.NavigationPreviewTitle, isFirstPage ? 1 : 2));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int clickId) {
                    if (clickId == -1) {
                        if (isFirstPage) {
                            clicksCount[0]++;

                            if (clicksCount[0] > 10 && !OctoConfig.INSTANCE.unlockedConfetti.getValue()) {
                                AppIconBulletinLayout layout = new AppIconBulletinLayout(getParentActivity(), LauncherIconController.LauncherIcon.CONFETTI, null);
                                layout.textView.setText(LocaleController.getString(R.string.UnlockedHiddenConfettiIcon));
                                layout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                                Bulletin.make(Objects.requireNonNull(LaunchActivity.getLastFragment()), layout, Bulletin.DURATION_SHORT).show();
                                OctoConfig.INSTANCE.unlockedConfetti.updateValue(true);
                            }
                        } else {
                            finishFragment();
                        }
                    }
                }
            });

            FrameLayout contentView = new FrameLayout(context);
            contentView.setClickable(true);
            contentView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            LinkSpanDrawable.LinksTextView textView = new LinkSpanDrawable.LinksTextView(context, null, null);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText4));
            textView.setLinkTextColor(getThemedColor(Theme.key_windowBackgroundWhiteLinkText));
            textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            textView.setText(LocaleController.getString(isFirstPage ? R.string.NavigationPreviewPage1 : R.string.NavigationPreviewPage2));
            contentView.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, dp(15), 0, dp(15), 0));

            if (isFirstPage) {
                contentView.setOnClickListener(v -> {
                    SmoothnessAnimationUI ui = new SmoothnessAnimationUI();
                    ui.asSecondPage();
                    presentFragment(ui);
                });
            }

            return contentView;
        }

        @Override
        public INavigationLayout.BackButtonState getBackButtonState() {
            return !isFirstPage ? INavigationLayout.BackButtonState.BACK : INavigationLayout.BackButtonState.MENU;
        }

        public void asSecondPage() {
            isFirstPage = false;
        }
    }
}
