/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.appearance;

import static org.telegram.messenger.LocaleController.getString;

import it.octogram.android.InterfaceCheckboxUI;
import it.octogram.android.InterfaceSliderUI;
import it.octogram.android.InterfaceSwitchUI;
import it.octogram.android.TabMode;
import it.octogram.android.TabStyle;

public class PopupChoiceDialogOption {
    public int id;
    public String itemTitle;
    String itemDescription = null;
    int itemIcon = 0;
    public InterfaceSwitchUI itemSwitchIconUI;
    public InterfaceCheckboxUI itemCheckboxIconUI;
    public InterfaceSliderUI itemSliderIconUI;
    public TabStyle tabStyleIconUI;
    public TabMode tabModeIconUI;
    boolean clickable = true;

    public PopupChoiceDialogOption setId(int id) {
        this.id = id;
        return this;
    }

    public PopupChoiceDialogOption setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
        return this;
    }

    public PopupChoiceDialogOption setItemTitle(int itemTitle) {
        this.itemTitle = getString(itemTitle);
        return this;
    }

    public PopupChoiceDialogOption setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
        return this;
    }

    public PopupChoiceDialogOption setItemDescription(int itemDescription) {
        this.itemDescription = getString(itemDescription);
        return this;
    }

    public PopupChoiceDialogOption setItemIcon(int itemIcon) {
        this.itemIcon = itemIcon;
        return this;
    }

    public PopupChoiceDialogOption setItemSwitchIconUI(InterfaceSwitchUI itemSwitchIconUI) {
        this.itemSwitchIconUI = itemSwitchIconUI;
        return this;
    }

    public PopupChoiceDialogOption setItemCheckboxIconUI(InterfaceCheckboxUI itemCheckboxIconUI) {
        this.itemCheckboxIconUI = itemCheckboxIconUI;
        return this;
    }

    public PopupChoiceDialogOption setItemSliderIconUI(InterfaceSliderUI itemSliderIconUI) {
        this.itemSliderIconUI = itemSliderIconUI;
        return this;
    }
    public PopupChoiceDialogOption setTabStyleIconUI(TabStyle tabStyleIconUI) {
        this.tabStyleIconUI = tabStyleIconUI;
        return this;
    }

    public PopupChoiceDialogOption setTabModeIconUI(TabMode tabModeIconUI) {
        this.tabModeIconUI = tabModeIconUI;
        return this;
    }

    public boolean hasCustomEntity() {
        return itemSwitchIconUI != null || itemCheckboxIconUI != null || itemSliderIconUI != null || tabStyleIconUI != null || tabModeIconUI != null;
    }

    public PopupChoiceDialogOption setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }
}
