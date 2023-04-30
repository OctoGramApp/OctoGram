package it.octogram.ui.Components;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PopupSwipeBackLayout;

import it.octogram.android.translator.AutoTranslateConfig;
import it.octogram.ui.AutoTranslateGroupInfo;

public class AutoTranslatePopupWrapper {
    public ActionBarPopupWindow.ActionBarPopupWindowLayout windowLayout;
    private final long dialogId;
    private final int topicId;
    private final ActionBarMenuSubItem defaultItem;
    private ActionBarMenuSubItem enableItem;
    private ActionBarMenuSubItem disableItem;
    private FragmentDelegate delegate;
    private final boolean isAlwaysShare;

    public AutoTranslatePopupWrapper(BaseFragment fragment, boolean isForum, ActionBarMenuItem otherItem, long dialogId, int topicId, boolean isAlwaysShare, Theme.ResourcesProvider resourcesProvider) {
        this.isAlwaysShare = isAlwaysShare;
        Context context = fragment.getParentActivity();
        windowLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(context, 0, resourcesProvider);
        windowLayout.setFitItems(true);
        this.dialogId = dialogId;
        this.topicId = topicId;
        PopupSwipeBackLayout swipeBackLayout = otherItem.getPopupLayout().getSwipeBack();
        if (swipeBackLayout != null) {
            var backItem = ActionBarMenuItem.addItem(windowLayout, R.drawable.msg_arrow_back, LocaleController.getString("Back", R.string.Back), false, resourcesProvider);
            backItem.setOnClickListener(view -> swipeBackLayout.closeForeground());
        }

        defaultItem = ActionBarMenuItem.addItem(windowLayout, 0, LocaleController.getString("Default", R.string.Default), true, resourcesProvider);

        defaultItem.setOnClickListener(view -> {
            if (topicId == 0) {
                AutoTranslateConfig.removeGroupException(dialogId);
            } else {
                AutoTranslateConfig.setDefault(dialogId, topicId);
            }
            updateItems();
            updateLastFragment();
            fragment.getMessagesController().getTranslateController().toggleTranslatingDialog(dialogId, topicId, AutoTranslateConfig.isAutoTranslateEnabled(dialogId, topicId));
        });

        if (topicId == 0 || AutoTranslateConfig.isLastTopicAvailable(dialogId, topicId, false)) {
            enableItem = ActionBarMenuItem.addItem(windowLayout, 0, LocaleController.getString("Enable", R.string.Enable), true, resourcesProvider);
            enableItem.setOnClickListener(view -> {
                AutoTranslateConfig.setEnabled(dialogId, topicId, true);
                updateItems();
                updateLastFragment();
                fragment.getMessagesController().getTranslateController().toggleTranslatingDialog(dialogId, topicId, true);
            });
        }

        if (topicId == 0 || AutoTranslateConfig.isLastTopicAvailable(dialogId, topicId, true)) {
            disableItem = ActionBarMenuItem.addItem(windowLayout, 0, LocaleController.getString("Disable", R.string.Disable), true, resourcesProvider);
            disableItem.setOnClickListener(view -> {
                AutoTranslateConfig.setEnabled(dialogId, topicId, false);
                updateItems();
                updateLastFragment();
                fragment.getMessagesController().getTranslateController().toggleTranslatingDialog(dialogId, topicId, false);
            });
        }
        updateItems();

        if (isForum) {
            ActionBarMenuSubItem customItem = ActionBarMenuItem.addItem(windowLayout, R.drawable.msg_customize, LocaleController.getString("AutoDeleteCustom", R.string.AutoDeleteCustom), false, resourcesProvider);
            customItem.setOnClickListener(view -> {
                otherItem.toggleSubMenu();
                AutoTranslateGroupInfo groupInfo = new AutoTranslateGroupInfo(MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialogId), this::updateItems);
                fragment.presentFragment(groupInfo);
            });
        }

        FrameLayout gap = new FrameLayout(context);
        gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator, resourcesProvider));
        View gapShadow = new View(context);
        gapShadow.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow, resourcesProvider));
        gap.addView(gapShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        gap.setTag(R.id.fit_width_tag, 1);
        windowLayout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

        TextView textView = new TextView(context);
        textView.setTag(R.id.fit_width_tag, 1);
        textView.setPadding(AndroidUtilities.dp(13), AndroidUtilities.dp(8), AndroidUtilities.dp(13), AndroidUtilities.dp(8));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        textView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider));
        textView.setText(LocaleController.getString("AutoTranslateDesc", R.string.AutoTranslateDesc));
        windowLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    public void updateItems() {
        if (topicId == 0) {
            boolean allowed = AutoTranslateConfig.getExceptionsById(true, dialogId);
            boolean disabled = AutoTranslateConfig.getExceptionsById(false, dialogId);
            defaultItem.setChecked(!allowed && !disabled);
            if (enableItem != null) {
                enableItem.setChecked(allowed);
            }
            if (disableItem != null) {
                disableItem.setChecked(disabled);
            }
        } else {
            defaultItem.setChecked(AutoTranslateConfig.isDefault(dialogId, topicId));
            if (enableItem != null) enableItem.setChecked(AutoTranslateConfig.hasAutoTranslateConfig(dialogId, topicId) && AutoTranslateConfig.isAutoTranslateEnabled(dialogId, topicId));
            if (disableItem != null) disableItem.setChecked(AutoTranslateConfig.hasAutoTranslateConfig(dialogId, topicId) && !AutoTranslateConfig.isAutoTranslateEnabled(dialogId, topicId));
        }
    }

    private void updateLastFragment() {
        if (delegate == null) return;
        if (AutoTranslateConfig.getExceptions(isAlwaysShare).isEmpty()) {
            this.delegate.hideLastFragment();
        } else {
            this.delegate.showLastFragment();
        }
    }

    public void setDelegate(FragmentDelegate delegate) {
        this.delegate = delegate;
    }

    public interface FragmentDelegate {
        void hideLastFragment();
        void showLastFragment();
    }
}
