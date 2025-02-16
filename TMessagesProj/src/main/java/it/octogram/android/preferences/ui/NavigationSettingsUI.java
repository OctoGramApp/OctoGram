/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AdjustPanLayoutHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CircularProgressDrawable;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.SlideChooseView;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.ui.components.SmoothnessAnimationCell;

public class NavigationSettingsUI extends BaseFragment {

    private SlideChooseView smoothLevelChooseView;

    private TextCheckCell alternativeNavigationCell;
    private ActionBarMenuItem doneButton;
    private HeaderCell smootherHeaderCell;
    private TextInfoPrivacyCell divider;
    private HeaderCell optionsHeaderCell;
    private TextCheckCell optionsCheckCell;
    private SmoothnessAnimationCell smoothnessAnimationCell;
    private TextInfoPrivacyCell dividerActionBarAnimation;
    private ScrollView scrollView;

    private boolean finished;

    private final ArrayList<Integer> displayedSmoothLevels = new ArrayList<>();
    private final int[] defaultSmoothLevels = new int[]{200, 400, 500, 600, 800, 1000};

    boolean scrollToStart;
    boolean scrollToEnd;

    boolean useAlternativeNavigation = false;
    int navigationSmoothness = 1000;
    boolean animatedActionBar = false;

    @Override
    public boolean onFragmentCreate() {
        useAlternativeNavigation = OctoConfig.INSTANCE.alternativeNavigation.getValue();
        navigationSmoothness = OctoConfig.INSTANCE.navigationSmoothness.getValue();
        animatedActionBar = OctoConfig.INSTANCE.animatedActionBar.getValue();

        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.Navigation));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1 && checkDiscard()) {
                    finishFragment();
                } else if (id == 1) {
                    checkDiscard(true);
                }
            }
        });


        actionBar.setLongClickable(true);
        actionBar.setOnLongClickListener(v -> {
            String link = "tg://experimental/navigation";
            showDialog(new ShareAlert(context, null, link, false, link, false));

            return true;
        });

        Drawable checkmark = ContextCompat.getDrawable(context, R.drawable.ic_ab_done);
        if (checkmark != null) {
            checkmark.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.MULTIPLY));
            Drawable doneButtonDrawable = new CrossfadeDrawable(checkmark, new CircularProgressDrawable(Theme.getColor(Theme.key_actionBarDefaultIcon)));
            doneButton = actionBar.createMenu().addItemWithWidth(1, doneButtonDrawable, dp(56), LocaleController.getString(R.string.Done));
            checkDone(false);
        }

        scrollView = new ScrollView(context);
        SizeNotifierFrameLayout contentView = new SizeNotifierFrameLayout(context) {

            @Override
            protected AdjustPanLayoutHelper createAdjustPanLayoutHelper() {
                AdjustPanLayoutHelper panLayoutHelper = new AdjustPanLayoutHelper(this) {

                    @Override
                    protected void onTransitionStart(boolean keyboardVisible, int contentHeight) {
                        super.onTransitionStart(keyboardVisible, contentHeight);
                        scrollView.getLayoutParams().height = contentHeight;
                    }

                    @Override
                    protected void onTransitionEnd() {
                        super.onTransitionEnd();
                        scrollView.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
                        scrollView.requestLayout();
                    }

                    @Override
                    protected void onPanTranslationUpdate(float y, float progress, boolean keyboardVisible) {
                        super.onPanTranslationUpdate(y, progress, keyboardVisible);
                        setTranslationY(0);
                    }

                    @Override
                    protected boolean heightAnimationEnabled() {
                        return !finished;
                    }
                };
                panLayoutHelper.setCheckHierarchyHeight(true);
                return panLayoutHelper;
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                adjustPanLayoutHelper.onAttach();
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                adjustPanLayoutHelper.onDetach();
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                int scrollY = scrollView.getScrollY();
                super.onLayout(changed, l, t, r, b);

                if (scrollY != scrollView.getScrollY() && !scrollToEnd) {
                    scrollView.setTranslationY(scrollView.getScrollY() - scrollY);
                    scrollView.animate().cancel();
                    scrollView.animate().translationY(0).setDuration(AdjustPanLayoutHelper.keyboardDuration).setInterpolator(AdjustPanLayoutHelper.keyboardInterpolator).start();
                }
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (scrollToEnd) {
                    scrollToEnd = false;
                    scrollView.smoothScrollTo(0, Math.max(0, scrollView.getChildAt(0).getMeasuredHeight() - scrollView.getMeasuredHeight()));
                } else if (scrollToStart) {
                    scrollToStart = false;
                    scrollView.smoothScrollTo(0, 0);
                }
            }
        };

        fragmentView = contentView;

        LinearLayout linearLayout = new LinearLayout(context) {

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                super.dispatchDraw(canvas);
            }
        };
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(100);
        linearLayout.setLayoutTransition(transition);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout);

        alternativeNavigationCell = new TextCheckCell(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                canvas.save();
                canvas.clipRect(0, 0, getWidth(), getHeight());
                super.onDraw(canvas);
                canvas.restore();
            }
        };
        alternativeNavigationCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundUnchecked));
        alternativeNavigationCell.setColors(Theme.key_windowBackgroundCheckText, Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        alternativeNavigationCell.setDrawCheckRipple(true);
        alternativeNavigationCell.setHeight(56);
        alternativeNavigationCell.setTag(Theme.key_windowBackgroundUnchecked);
        alternativeNavigationCell.setTextAndCheck(LocaleController.getString(R.string.AlternativeNavigation), false, false);
        alternativeNavigationCell.setTypeface(AndroidUtilities.bold());
        alternativeNavigationCell.setOnClickListener(view -> {
            TextCheckCell cell = (TextCheckCell) view;
            useAlternativeNavigation = !cell.isChecked();

            reloadUIElements();

            smoothnessAnimationCell.navigationLayout.updateSpringStiffness(navigationSmoothness);
            smoothnessAnimationCell.navigationLayout.updateUseActionbarCrossfade(animatedActionBar);
        });
        linearLayout.addView(alternativeNavigationCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 56));

        TextInfoPrivacyCell hintCell = new TextInfoPrivacyCell(context);
        hintCell.setBackground(Theme.getThemedDrawableByKey(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        hintCell.setText(LocaleController.getString(R.string.AlternativeNavigation_Desc));
        linearLayout.addView(hintCell);

        smootherHeaderCell = new HeaderCell(context);
        smootherHeaderCell.setText(LocaleController.getString(R.string.SmootherNavigation));
        linearLayout.addView(smootherHeaderCell);
        smoothLevelChooseView = new SlideChooseView(context);
        linearLayout.addView(smoothLevelChooseView);
        smoothLevelChooseView.setCallback(index -> {
            if (index < displayedSmoothLevels.size()) {
                navigationSmoothness = displayedSmoothLevels.get(index);
                smoothnessAnimationCell.navigationLayout.updateSpringStiffness(navigationSmoothness);

                checkDone(true);
            }
        });
        resetSmoothLevels();

        linearLayout.addView(smoothnessAnimationCell = new SmoothnessAnimationCell(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, SmoothnessAnimationCell.height));

        divider = new TextInfoPrivacyCell(context);
        divider.setText(LocaleController.getString(R.string.SmootherNavigation_Desc));
        linearLayout.addView(divider);

        optionsHeaderCell = new HeaderCell(context);
        optionsHeaderCell.setText(LocaleController.getString(R.string.NavigationSettings));
        linearLayout.addView(optionsHeaderCell);
        optionsCheckCell = new TextCheckCell(context);
        optionsCheckCell.setOnClickListener(view -> {
            TextCheckCell cell = (TextCheckCell) view;
            cell.setChecked(animatedActionBar = !cell.isChecked());
            smoothnessAnimationCell.navigationLayout.updateUseActionbarCrossfade(animatedActionBar);

            checkDone(true);
        });
        optionsCheckCell.setTextAndCheck(LocaleController.getString(R.string.AnimatedActionBar), false, false);
        linearLayout.addView(optionsCheckCell);

        dividerActionBarAnimation = new TextInfoPrivacyCell(context);
        dividerActionBarAnimation.setText(LocaleController.getString(R.string.AnimatedActionBarn_Desc));
        linearLayout.addView(dividerActionBarAnimation);

        contentView.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        smootherHeaderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        smoothLevelChooseView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        optionsHeaderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        optionsCheckCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        contentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        dividerActionBarAnimation.setBackground(Theme.getThemedDrawableByKey(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        divider.setBackground(Theme.getThemedDrawableByKey(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));

        contentView.setClipChildren(false);
        scrollView.setClipChildren(false);
        linearLayout.setClipChildren(false);
        reloadUIElements(false);

        return contentView;
    }

    private void reloadUIElements() {
        reloadUIElements(true);
    }

    private void reloadUIElements(boolean animate) {
        int visibility = useAlternativeNavigation ? View.VISIBLE : View.GONE;

        smootherHeaderCell.setVisibility(visibility);
        smoothLevelChooseView.setVisibility(visibility);
        divider.setVisibility(visibility);
        optionsHeaderCell.setVisibility(visibility);
        optionsCheckCell.setVisibility(visibility);
        dividerActionBarAnimation.setVisibility(visibility);
        smoothnessAnimationCell.setVisibility(visibility);

        optionsCheckCell.setChecked(OctoConfig.INSTANCE.animatedActionBar.getValue());

        int alternativeNavigationColorUpdate = Theme.getColor(useAlternativeNavigation ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);
        if (animate) {
            alternativeNavigationCell.setBackgroundColorAnimated(useAlternativeNavigation, alternativeNavigationColorUpdate);
        } else {
            alternativeNavigationCell.setBackgroundColor(alternativeNavigationColorUpdate);
        }
        alternativeNavigationCell.setChecked(useAlternativeNavigation);

        checkDone(animate);
    }

    private void checkDone(boolean animated) {
        if (doneButton == null) return;

        boolean hasChanges = OctoConfig.INSTANCE.alternativeNavigation.getValue() != useAlternativeNavigation;
        hasChanges |= OctoConfig.INSTANCE.navigationSmoothness.getValue() != navigationSmoothness;
        hasChanges |= OctoConfig.INSTANCE.animatedActionBar.getValue() != animatedActionBar;

        if (!useAlternativeNavigation && !OctoConfig.INSTANCE.alternativeNavigation.getValue() && hasChanges) {
            hasChanges = false;
        }

        doneButton.setEnabled(hasChanges);
        if (animated) {
            doneButton.animate().alpha(hasChanges ? 1.0f : 0.0f).scaleX(hasChanges ? 1.0f : 0.0f).scaleY(hasChanges ? 1.0f : 0.0f).setDuration(180).start();
        } else {
            doneButton.setAlpha(hasChanges ? 1.0f : 0.0f);
            doneButton.setScaleX(hasChanges ? 1.0f : 0.0f);
            doneButton.setScaleY(hasChanges ? 1.0f : 0.0f);
        }
    }

    private void resetSmoothLevels() {
        displayedSmoothLevels.clear();

        int selectedIndex = 0;
        int currentSmoothness = OctoConfig.INSTANCE.navigationSmoothness.getValue();

        ArrayList<String> arrayOptions = new ArrayList<>();
        for (int i = 0; i < defaultSmoothLevels.length; i++) {
            int level = defaultSmoothLevels[i];

            displayedSmoothLevels.add(level);
            arrayOptions.add(String.valueOf(level));

            if (level == currentSmoothness) {
                selectedIndex = i;
            }
        }

        String[] arrayOptionsDefined = arrayOptions.toArray(new String[0]);
        smoothLevelChooseView.setOptions(selectedIndex, arrayOptionsDefined);
    }

    private boolean checkDiscard() {
        return checkDiscard(false);
    }

    private boolean checkDiscard(boolean soft) {
        if (doneButton.getAlpha() == 1.0f) {
            if (soft) {
                applyAndFinish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString(R.string.UserRestrictionsApplyChanges));
                builder.setMessage(LocaleController.getString(R.string.NavigationDiscardReload));
                builder.setPositiveButton(LocaleController.getString(R.string.ApplyTheme), (dialogInterface, i) -> applyAndFinish());
                builder.setNegativeButton(LocaleController.getString(R.string.PassportDiscard), (dialog, which) -> finishFragment());

                AlertDialog dialog;
                showDialog(dialog = builder.create());
                TextView button = (TextView) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (button != null) {
                    button.setTextColor(getThemedColor(Theme.key_text_RedBold));
                }
            }
            return false;
        }
        return true;
    }

    private void applyAndFinish() {
        OctoConfig.INSTANCE.alternativeNavigation.updateValue(useAlternativeNavigation);
        OctoConfig.INSTANCE.navigationSmoothness.updateValue(navigationSmoothness);
        OctoConfig.INSTANCE.animatedActionBar.updateValue(animatedActionBar);

        INavigationLayout currentLayout = LaunchActivity.instance.getActionBarLayout();
        if (currentLayout != null) {
            currentLayout.updateUseAlternativeNavigation(useAlternativeNavigation);
            currentLayout.updateSpringStiffness(navigationSmoothness);
            currentLayout.updateUseActionbarCrossfade(animatedActionBar);
            currentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
        }

        finishFragment(true);
    }

    @Override
    public boolean canBeginSlide() {
        return checkDiscard();
    }

    @Override
    public boolean onBackPressed() {
        return checkDiscard();
    }

    @Override
    public void finishFragment() {
        scrollView.getLayoutParams().height = scrollView.getHeight();
        finished = true;
        super.finishFragment();
    }
}
