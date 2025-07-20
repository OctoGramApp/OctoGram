/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

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
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AdjustPanLayoutHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
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
import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.ui.cells.AlternativeNavigationPreviewCell;
import it.octogram.android.utils.deeplink.DeepLinkDef;

public class OctoExperimentsNavigationUI extends BaseFragment {

    private SlideChooseView bounceChooseView;
    private SlideChooseView smoothLevelChooseView;

    private TextCheckCell alternativeNavigationCell;
    private ActionBarMenuItem doneButton;
    private HeaderCell smootherHeaderCell;
    private HeaderCell bounceHeaderCell;
    private TextInfoPrivacyCell divider;
    private HeaderCell optionsHeaderCell;
    private ShadowSectionCell shadowSectionCell;
    private TextCheckCell optionsCheckCell;
    private AlternativeNavigationPreviewCell alternativeNavigationPreviewCell;
    private TextInfoPrivacyCell dividerActionBarAnimation;
    private ScrollView scrollView;

    private boolean finished;

    private final ArrayList<Integer> displayedBounceLevels = new ArrayList<>();
    private final int[] defaultBounceLevels = new int[]{0, 20, 40, 60, 80};

    private final ArrayList<Integer> displayedSmoothLevels = new ArrayList<>();
    private final int[] defaultSmoothLevels = new int[]{200, 400, 500, 600, 800, 1000};

    boolean scrollToStart;
    boolean scrollToEnd;

    boolean useAlternativeNavigation = false;
    int navigationSmoothness = 1000;
    int navigationBounceLevel = 0;
    boolean animatedActionBar = false;

    @Override
    public boolean onFragmentCreate() {
        useAlternativeNavigation = OctoConfig.INSTANCE.alternativeNavigation.getValue();
        navigationSmoothness = OctoConfig.INSTANCE.navigationSmoothness.getValue();
        navigationBounceLevel = OctoConfig.INSTANCE.navigationBounceLevel.getValue();
        animatedActionBar = OctoConfig.INSTANCE.animatedActionBar.getValue();

        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.Navigation));
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
            String link = String.format(Locale.US, "tg://%s", DeepLinkDef.EXPERIMENTAL_NAVIGATION);
            showDialog(new ShareAlert(context, null, link, false, link, false, true));

            return true;
        });

        Drawable checkmark = ContextCompat.getDrawable(context, R.drawable.ic_ab_done);
        if (checkmark != null) {
            checkmark.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.MULTIPLY));
            Drawable doneButtonDrawable = new CrossfadeDrawable(checkmark, new CircularProgressDrawable(Theme.getColor(Theme.key_actionBarDefaultIcon)));
            doneButton = actionBar.createMenu().addItemWithWidth(1, doneButtonDrawable, dp(56), getString(R.string.Done));
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

        alternativeNavigationCell = new TextCheckCell(context);
        alternativeNavigationCell.setColors(Theme.key_windowBackgroundCheckText, Theme.key_switchTrackBlue, Theme.key_switchTrackBlueChecked, Theme.key_switchTrackBlueThumb, Theme.key_switchTrackBlueThumbChecked);
        alternativeNavigationCell.setHeight(56);
        alternativeNavigationCell.setTextAndCheck(getString(R.string.AlternativeNavigation), false, false);
        alternativeNavigationCell.setTypeface(AndroidUtilities.bold());
        alternativeNavigationCell.setOnClickListener(view -> {
            TextCheckCell cell = (TextCheckCell) view;
            useAlternativeNavigation = !cell.isChecked();

            reloadUIElements();

            if (animatedActionBar) {
                alternativeNavigationPreviewCell.navigationLayout.updateUseActionbarCrossfade(false);
            }

            AndroidUtilities.runOnUIThread(() -> {
                alternativeNavigationPreviewCell.navigationLayout.updateSpringStiffness(navigationSmoothness);
                alternativeNavigationPreviewCell.navigationLayout.updateUseActionbarCrossfade(animatedActionBar);
            }, 200);
        });
        linearLayout.addView(alternativeNavigationCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 56));

        TextInfoPrivacyCell hintCell = new TextInfoPrivacyCell(context);
        hintCell.setBackground(Theme.getThemedDrawableByKey(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        hintCell.setText(getString(R.string.AlternativeNavigation_Desc));
        linearLayout.addView(hintCell);

        smootherHeaderCell = new HeaderCell(context);
        smootherHeaderCell.setText(getString(R.string.SmootherNavigation));
        linearLayout.addView(smootherHeaderCell);
        smoothLevelChooseView = new SlideChooseView(context);
        linearLayout.addView(smoothLevelChooseView);
        smoothLevelChooseView.setCallback(index -> {
            if (index < displayedSmoothLevels.size()) {
                navigationSmoothness = displayedSmoothLevels.get(index);
                alternativeNavigationPreviewCell.navigationLayout.updateSpringStiffness(navigationSmoothness);

                checkDone(true);
            }
        });

        linearLayout.addView(alternativeNavigationPreviewCell = new AlternativeNavigationPreviewCell(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AlternativeNavigationPreviewCell.height));

        divider = new TextInfoPrivacyCell(context);
        divider.setText(getString(R.string.SmootherNavigation_Desc));
        linearLayout.addView(divider);

        bounceHeaderCell = new HeaderCell(context);
        bounceHeaderCell.setText(getString(R.string.BounceNavigation));
        linearLayout.addView(bounceHeaderCell);
        bounceChooseView = new SlideChooseView(context);
        bounceChooseView.setCallback(index -> {
            if (index < displayedBounceLevels.size()) {
                navigationBounceLevel = displayedBounceLevels.get(index);
                alternativeNavigationPreviewCell.navigationLayout.updateBounceLevel((float) navigationBounceLevel / 100);

                checkDone(true);
            }
        });
        linearLayout.addView(bounceChooseView);

        resetSliders();

        shadowSectionCell = new ShadowSectionCell(context);
        linearLayout.addView(shadowSectionCell);

        optionsHeaderCell = new HeaderCell(context);
        optionsHeaderCell.setText(getString(R.string.NavigationSettings));
        linearLayout.addView(optionsHeaderCell);
        optionsCheckCell = new TextCheckCell(context);
        optionsCheckCell.setOnClickListener(view -> {
            TextCheckCell cell = (TextCheckCell) view;
            cell.setChecked(animatedActionBar = !cell.isChecked());
            alternativeNavigationPreviewCell.navigationLayout.updateUseActionbarCrossfade(animatedActionBar);

            checkDone(true);
        });
        optionsCheckCell.setTextAndCheck(getString(R.string.AnimatedActionBar), false, false);
        linearLayout.addView(optionsCheckCell);

        dividerActionBarAnimation = new TextInfoPrivacyCell(context);
        dividerActionBarAnimation.setText(getString(R.string.AnimatedActionBarn_Desc));
        linearLayout.addView(dividerActionBarAnimation);

        contentView.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        smootherHeaderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        smoothLevelChooseView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bounceHeaderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        bounceChooseView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        optionsHeaderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        optionsCheckCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        shadowSectionCell.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        contentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        dividerActionBarAnimation.setBackground(Theme.getThemedDrawableByKey(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        divider.setBackground(Theme.getThemedDrawableByKey(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));

//        contentView.setClipChildren(false);
//        scrollView.setClipChildren(false);
//        linearLayout.setClipChildren(false);
        reloadUIElements(false);

        AndroidUtilities.runOnUIThread(() -> {
            alternativeNavigationPreviewCell.navigationLayout.updateSpringStiffness(navigationSmoothness);
            alternativeNavigationPreviewCell.navigationLayout.updateUseActionbarCrossfade(animatedActionBar);
            alternativeNavigationPreviewCell.navigationLayout.updateBounceLevel((float) (navigationBounceLevel / 100));
        }, 100);

        return contentView;
    }

    private void reloadUIElements() {
        reloadUIElements(true);
    }

    private void reloadUIElements(boolean animate) {
        int visibility = useAlternativeNavigation ? View.VISIBLE : View.GONE;

        smootherHeaderCell.setVisibility(visibility);
        shadowSectionCell.setVisibility(visibility);
        smoothLevelChooseView.setVisibility(visibility);
        bounceHeaderCell.setVisibility(visibility);
        bounceChooseView.setVisibility(visibility);
        divider.setVisibility(visibility);
        optionsHeaderCell.setVisibility(visibility);
        optionsCheckCell.setVisibility(visibility);
        dividerActionBarAnimation.setVisibility(visibility);
        alternativeNavigationPreviewCell.setVisibility(visibility);

        optionsCheckCell.setChecked(animatedActionBar);

        int alternativeNavigationColorUpdate = Theme.getColor(useAlternativeNavigation ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);
        if (animate) {
            if (useAlternativeNavigation) {
                alternativeNavigationCell.setBackgroundColorAnimated(true, alternativeNavigationColorUpdate);
            } else {
                alternativeNavigationCell.setBackgroundColorAnimatedReverse(alternativeNavigationColorUpdate);
            }
        } else {
            alternativeNavigationCell.setBackgroundColor(alternativeNavigationColorUpdate);
        }
        alternativeNavigationCell.setTextAndCheck(getString(R.string.AlternativeNavigation), useAlternativeNavigation, false);

        checkDone(animate);
    }

    private void checkDone(boolean animated) {
        if (doneButton == null) return;

        boolean hasChanges = hasChanges();

        doneButton.setEnabled(hasChanges);
        if (animated) {
            doneButton.animate().alpha(hasChanges ? 1.0f : 0.0f).scaleX(hasChanges ? 1.0f : 0.0f).scaleY(hasChanges ? 1.0f : 0.0f).setDuration(180).start();
        } else {
            doneButton.setAlpha(hasChanges ? 1.0f : 0.0f);
            doneButton.setScaleX(hasChanges ? 1.0f : 0.0f);
            doneButton.setScaleY(hasChanges ? 1.0f : 0.0f);
        }
    }

    private boolean hasChanges() {
        boolean hasChanges = OctoConfig.INSTANCE.alternativeNavigation.getValue() != useAlternativeNavigation;
        hasChanges |= OctoConfig.INSTANCE.navigationSmoothness.getValue() != navigationSmoothness;
        hasChanges |= OctoConfig.INSTANCE.navigationBounceLevel.getValue() != navigationBounceLevel;
        hasChanges |= OctoConfig.INSTANCE.animatedActionBar.getValue() != animatedActionBar;

        if (!useAlternativeNavigation && !OctoConfig.INSTANCE.alternativeNavigation.getValue() && hasChanges) {
            hasChanges = false;
        }
        return hasChanges;
    }

    private void resetSliders() {
        displayedSmoothLevels.clear();
        displayedBounceLevels.clear();

        int selectedIndex = 0;

        ArrayList<String> arrayOptions = new ArrayList<>();
        for (int i = 0; i < defaultSmoothLevels.length; i++) {
            int level = defaultSmoothLevels[i];

            displayedSmoothLevels.add(level);
            arrayOptions.add(String.valueOf(level));

            if (level == OctoConfig.INSTANCE.navigationSmoothness.getValue()) {
                selectedIndex = i;
            }
        }

        String[] arrayOptionsDefined = arrayOptions.toArray(new String[0]);
        smoothLevelChooseView.setOptions(selectedIndex, arrayOptionsDefined);

        selectedIndex = 0;

        arrayOptions.clear();
        for (int i = 0; i < defaultBounceLevels.length; i++) {
            int level = defaultBounceLevels[i];

            displayedBounceLevels.add(level);
            arrayOptions.add(level == 0 ? getString(R.string.SlowmodeOff) : (level+"%"));

            if (level == OctoConfig.INSTANCE.navigationBounceLevel.getValue()) {
                selectedIndex = i;
            }
        }

        arrayOptionsDefined = arrayOptions.toArray(new String[0]);
        bounceChooseView.setOptions(selectedIndex, arrayOptionsDefined);
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
                builder.setTitle(getString(R.string.UserRestrictionsApplyChanges));
                builder.setMessage(getString(R.string.NavigationDiscardReload));
                builder.setPositiveButton(getString(R.string.ApplyTheme), (dialogInterface, i) -> applyAndFinish());
                builder.setNegativeButton(getString(R.string.PassportDiscard), (dialog, which) -> finishFragment());

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
        AndroidUtilities.runOnUIThread(() -> {
            OctoConfig.INSTANCE.alternativeNavigation.updateValue(useAlternativeNavigation);
            OctoConfig.INSTANCE.navigationSmoothness.updateValue(navigationSmoothness);
            OctoConfig.INSTANCE.navigationBounceLevel.updateValue(navigationBounceLevel);
            OctoConfig.INSTANCE.animatedActionBar.updateValue(animatedActionBar);

            INavigationLayout currentLayout = LaunchActivity.instance.getActionBarLayout();
            if (currentLayout != null) {
                currentLayout.updateUseAlternativeNavigation(useAlternativeNavigation);
                currentLayout.updateSpringStiffness(navigationSmoothness);
                currentLayout.updateBounceLevel((float) navigationBounceLevel / 100);
                if (animatedActionBar) {
                    currentLayout.updateUseActionbarCrossfade(false);
                }
                currentLayout.updateUseActionbarCrossfade(animatedActionBar);
                currentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
            }

            finishFragment(true);
        });
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
