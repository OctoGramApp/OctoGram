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
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.TabMode;
import it.octogram.android.TabStyle;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.config.FolderUtils;

@SuppressLint("UseCompatLoadingForDrawables")
public class FolderTypeSelectorCell extends FrameLayout {

    public static final int height = dp(148);
    private FilterTabsView filterTabsView;
    private final boolean isSinglePreview;
    private final TabStyle tabStyle;
    private final TabMode tabMode;

    public FolderTypeSelectorCell(Context context) {
        this(context, false, null, null);
    }

    public FolderTypeSelectorCell(Context context, boolean isSinglePreview, TabStyle tabStyle) {
        this(context, isSinglePreview, tabStyle, null);
    }

    public FolderTypeSelectorCell(Context context, boolean isSinglePreview, TabMode tabMode) {
        this(context, isSinglePreview, null, tabMode);
    }

    public FolderTypeSelectorCell(Context context, boolean isSinglePreview, TabStyle tabStyle, TabMode tabMode) {
        super(context);

        this.isSinglePreview = isSinglePreview;
        this.tabStyle = tabStyle;
        this.tabMode = tabMode;

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);

        if (!isSinglePreview) {
            internalFrameLayout.setPadding(dp(2), dp(2), dp(2), dp(2));
            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.RECTANGLE);
            border.setAlpha(150);
            border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), dp(5), dp(5));
            border.setCornerRadius(dp(25));
            internalFrameLayout.setBackground(border);
        }

        internalFrameLayout.addView(getNavigationLayout(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        if (!isSinglePreview) {
            internalFrameLayout.setPadding(dp(3), dp(3), dp(3), dp(3));
            setPadding(dp(15), dp(15), dp(15), dp(15));
        }

        setBackground(Theme.createRoundRectDrawable(isSinglePreview ? 10 : 0, Theme.getColor(isSinglePreview ? Theme.key_actionBarDefault : Theme.key_windowBackgroundWhite)));
        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        fillTabs();
    }

    private FrameLayout getNavigationLayout(Context context) {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Theme.getColor(Theme.key_actionBarDefault));
        border.setCornerRadius(dp(isSinglePreview ? 15 : 25));

        filterTabsView = new FilterTabsView(context, tabStyle, tabMode, null);
        filterTabsView.setDelegate(new FilterTabsView.FilterTabsViewDelegate() {
            @Override
            public void onPageSelected(FilterTabsView.Tab tab, boolean forward) {

            }

            @Override
            public void onPageScrolled(float progress) {

            }

            @Override
            public void onSamePageSelected() {

            }

            @Override
            public int getTabCounter(int tabId) {
                return (tabId == (OctoConfig.INSTANCE.hideOnlyAllChatsFolder.getValue() ? 1 : 0) && !isSinglePreview) ? 100 : 0;
            }

            @Override
            public boolean didSelectTab(FilterTabsView.TabView tabView, boolean selected) {
                return false;
            }

            @Override
            public boolean isTabMenuVisible() {
                return false;
            }

            @Override
            public void onDeletePressed(int id) {

            }

            @Override
            public void onPageReorder(int fromId, int toId) {

            }

            @Override
            public boolean canPerformActions() {
                return false;
            }
        });

        FrameLayout navigationFrame = new FrameLayout(context);
        navigationFrame.setClipToPadding(true);
        navigationFrame.setClipToOutline(true);
        navigationFrame.setClipChildren(true);
        navigationFrame.setBackground(border);
        navigationFrame.addView(filterTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44));

        return navigationFrame;
    }

    public void fillTabs() {
        filterTabsView.resetTabId();
        filterTabsView.removeTabs();

        if (isSinglePreview) {
            filterTabsView.addTab(1, 1, "App", "\uD83C\uDFE0", null, false, false, false);
        } else {
            ArrayList<MessagesController.DialogFilter> filters = MessagesController.getInstance(UserConfig.selectedAccount).getDialogFilters();
            ArrayList<Integer> hiddenFolders = FolderUtils.getHiddenFolders();

            int addedTab = 0;
            for (int a = 0, N = filters.size(); a < N; a++) {
                OctoLogging.e("updated", "up - " + filters.get(a).name + " - " + filters.get(a).id);
                if (filters.get(a).isDefault()) {
                    if (!OctoConfig.INSTANCE.hideOnlyAllChatsFolder.getValue()) {
                        addedTab++;
                        filterTabsView.addTab(a, 0, getString(R.string.FilterAllChats), filters.get(a).emoticon, null, false, true, filters.get(a).locked);
                    }
                } else {
                    if (hiddenFolders.contains(filters.get(a).id)) {
                        continue;
                    }
                    addedTab++;
                    filterTabsView.addTab(a, filters.get(a).localId, filters.get(a).name, filters.get(a).emoticon, filters.get(a).entities, false, false, filters.get(a).locked);
                }
            }

            if (addedTab == 0 || addedTab == 1) {
                filterTabsView.addTab(1, 1, "App", "\uD83C\uDFE0", null, false, addedTab == 0, false);
                filterTabsView.addTab(2, 2, "OctoGram", "\u2764", null, false, false, false);
                filterTabsView.addTab(3, 3, "Telegram", "\u2B50", null, false, false, false);
            }
        }

        filterTabsView.finishAddingTabs(false);
        if (isSinglePreview) {
            filterTabsView.selectFirstTab();
        }
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
