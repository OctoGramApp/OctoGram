package it.octogram.ui;

import android.transition.TransitionManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;

import it.octogram.android.CustomEmojiController;
import it.octogram.android.OctoConfig;
import it.octogram.ui.Cells.BlurIntensity;
import it.octogram.ui.Cells.DrawerProfilePreview;
import it.octogram.ui.Cells.DynamicButtonSelector;
import it.octogram.ui.Cells.ThemeSelectorDrawer;

public class OctoGramAppearanceSettings extends BaseSettingsActivity implements NotificationCenter.NotificationCenterDelegate {
    private DrawerProfilePreview profilePreviewCell;

    private int drawerRow;
    private int drawerAvatarAsBackgroundRow;
    private int showGradientRow;
    private int showAvatarRow;
    private int drawerDarkenBackgroundRow;
    private int drawerBlurBackgroundRow;
    private int drawerDividerRow;
    private int editBlurHeaderRow;
    private int editBlurRow;
    private int editBlurDividerRow;
    private int themeDrawerHeader;
    private int themeDrawerRow;
    private int themeDrawerDividerRow;
    private int menuItemsRow;
    private int dynamicButtonHeaderRow;
    private int dynamicButtonRow;
    private int dynamicDividerRow;
    private int fontsAndEmojiHeaderRow;
    private int useSystemFontRow;
    private int fontsAndEmojiDividerRow;
    private int chatHeaderRow;
    private int chatHeaderDividerRow;
    private int appearanceHeaderRow;
    private int forcePacmanRow;
    private int showSantaHatRow;
    private int showFallingSnowRow;
    private int messageTimeSwitchRow;
    private int roundedNumberSwitchRow;
    private int smartButtonsRow;
    private int appBarShadowRow;
    private int slidingTitleRow;
    private int searchIconInActionBarRow;
    private int appearanceDividerRow;
    private int showPencilIconRow;
    private int showInActionBarRow;
    private int chooseEmojiPackRow;

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("Appearance", R.string.Appearance);
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiPacksLoaded);
        CustomEmojiController.loadEmojisInfo();
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiPacksLoaded);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == showAvatarRow) {
            OctoConfig.toggleShowAvatarImage();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.showAvatarImage);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == showGradientRow) {
            OctoConfig.toggleShowGradientColor();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.showGradientColor);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == drawerDarkenBackgroundRow) {
            OctoConfig.toggleAvatarBackgroundDarken();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.avatarBackgroundDarken);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == drawerBlurBackgroundRow) {
            OctoConfig.toggleAvatarBackgroundBlur();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.avatarBackgroundBlur);
            }
            reloadMainInfo();
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (OctoConfig.avatarBackgroundBlur) {
                listAdapter.notifyItemRangeInserted(drawerDividerRow, 3);
            } else {
                listAdapter.notifyItemRangeRemoved(drawerDividerRow, 3);
            }
            updateRowsId();
        } else if (position == drawerAvatarAsBackgroundRow) {
            OctoConfig.toggleAvatarAsDrawerBackground();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.avatarAsDrawerBackground);
            }
            reloadMainInfo();
            TransitionManager.beginDelayedTransition(profilePreviewCell);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (OctoConfig.avatarAsDrawerBackground) {
                updateRowsId();
                listAdapter.notifyItemRangeInserted(showGradientRow, 4 + (OctoConfig.avatarBackgroundBlur ? 3 : 0));
            } else {
                listAdapter.notifyItemRangeRemoved(showGradientRow, 4 + (OctoConfig.avatarBackgroundBlur ? 3 : 0));
                updateRowsId();
            }
        } else if (position == menuItemsRow) {
            presentFragment(new DrawerOrderSettings());
        } else if (position == useSystemFontRow) {
            OctoConfig.toggleUseSystemFont();
            AndroidUtilities.clearTypefaceCache();
            rebuildAllFragmentsWithLast();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.useSystemFont);
            }
        } else if (position == forcePacmanRow) {
            OctoConfig.togglePacmanForced();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.pacmanForced);
            }
        } else if (position == smartButtonsRow) {
            OctoConfig.toggleSmartButtons();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.smartButtons);
            }
        } else if (position == appBarShadowRow) {
            OctoConfig.toggleAppBarShadow();
            parentLayout.setHeaderShadow(OctoConfig.showAppBarShadow ? parentLayout.getView().getResources().getDrawable(R.drawable.header_shadow).mutate():null);
            rebuildAllFragmentsWithLast();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.showAppBarShadow);
            }
        } else if (position == showSantaHatRow) {
            OctoConfig.toggleShowSantaHat();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.showSantaHat);
            }
            Theme.lastHolidayCheckTime = 0;
            Theme.dialogs_holidayDrawable = null;
            reloadMainInfo();
        } else if (position == showFallingSnowRow) {
            OctoConfig.toggleShowSnowFalling();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.showSnowFalling);
            }
            Theme.lastHolidayCheckTime = 0;
            Theme.dialogs_holidayDrawable = null;
            reloadMainInfo();
        } else if (position == slidingTitleRow) {
            OctoConfig.toggleSlidingChatTitle();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.slidingChatTitle);
            }
        } else if (position == messageTimeSwitchRow) {
            OctoConfig.toggleFullTime();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.fullTime);
            }
            LocaleController.getInstance().recreateFormatters();
        } else if (position == roundedNumberSwitchRow) {
            OctoConfig.toggleRoundedNumbers();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.roundedNumbers);
            }
        } else if (position == searchIconInActionBarRow) {
            OctoConfig.toggleSearchIconInActionBar();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.searchIconInActionBar);
            }
        } else if (position == showPencilIconRow) {
            OctoConfig.toggleShowPencilIcon();
            parentLayout.rebuildAllFragmentViews(false, false);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.showPencilIcon);
            }
        } else if (position == showInActionBarRow) {
            OctoConfig.toggleShowNameInActionBar();
            reloadDialogs();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(OctoConfig.showNameInActionBar);
            }
        } else if (position == chooseEmojiPackRow) {
            presentFragment(new EmojiPackSettings());
        }
    }

    @Override
    protected void updateRowsId() {
        super.updateRowsId();
        showGradientRow = -1;
        showAvatarRow = -1;
        drawerDarkenBackgroundRow = -1;
        drawerBlurBackgroundRow = -1;
        editBlurHeaderRow = -1;
        editBlurRow = -1;
        editBlurDividerRow = -1;
        showSantaHatRow = -1;
        showFallingSnowRow = -1;

        drawerRow = rowCount++;
        drawerAvatarAsBackgroundRow = rowCount++;
        if (OctoConfig.avatarAsDrawerBackground) {
            showGradientRow = rowCount++;
            showAvatarRow = rowCount++;
            drawerDarkenBackgroundRow = rowCount++;
            drawerBlurBackgroundRow = rowCount++;
        }
        drawerDividerRow = rowCount++;
        if (OctoConfig.avatarBackgroundBlur && OctoConfig.avatarAsDrawerBackground) {
            editBlurHeaderRow = rowCount++;
            editBlurRow = rowCount++;
            editBlurDividerRow = rowCount++;
        }

        themeDrawerHeader = rowCount++;
        themeDrawerRow = rowCount++;
        menuItemsRow = rowCount++;
        themeDrawerDividerRow = rowCount++;

        dynamicButtonHeaderRow = rowCount++;
        dynamicButtonRow = rowCount++;
        dynamicDividerRow = rowCount++;

        fontsAndEmojiHeaderRow = rowCount++;
        chooseEmojiPackRow = rowCount++;
        useSystemFontRow = rowCount++;
        fontsAndEmojiDividerRow = rowCount++;

        chatHeaderRow = rowCount++;
        appBarShadowRow = rowCount++;
        showInActionBarRow = rowCount++;
        slidingTitleRow = rowCount++;
        searchIconInActionBarRow = rowCount++;
        chatHeaderDividerRow = rowCount++;

        appearanceHeaderRow = rowCount++;
        forcePacmanRow = rowCount++;
        if (((Theme.getEventType() == 0 && OctoConfig.eventType == 0) || OctoConfig.eventType == 1)) {
            showSantaHatRow = rowCount++;
            showFallingSnowRow = rowCount++;
        }
        messageTimeSwitchRow = rowCount++;
        roundedNumberSwitchRow = rowCount++;
        showPencilIconRow = rowCount++;
        smartButtonsRow = rowCount++;
        appearanceDividerRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.emojiPacksLoaded) {
            if (CustomEmojiController.getLoadingStatus() == CustomEmojiController.FAILED) {
                AndroidUtilities.runOnUIThread(CustomEmojiController::loadEmojisInfo, 1000);
            } else {
                listAdapter.notifyItemChanged(chooseEmojiPackRow, PARTIAL);
            }
        }
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == editBlurHeaderRow) {
                        headerCell.setText(LocaleController.getString("BlurIntensity", R.string.BlurIntensity));
                    } else if (position == themeDrawerHeader) {
                        headerCell.setText(LocaleController.getString("SideBarIconSet", R.string.SideBarIconSet));
                    } else if (position == dynamicButtonHeaderRow) {
                        headerCell.setText(LocaleController.getString("ButtonShape", R.string.ButtonShape));
                    } else if (position == fontsAndEmojiHeaderRow) {
                        headerCell.setText(LocaleController.getString("FontsAndEmojis", R.string.FontsAndEmojis));
                    } else if (position == appearanceHeaderRow) {
                        headerCell.setText(LocaleController.getString("Appearance", R.string.Appearance));
                    } else if (position == chatHeaderRow) {
                        headerCell.setText(LocaleController.getString("ChatHeader", R.string.ChatHeader));
                    }
                    break;
                case SWITCH:
                    TextCheckCell textCheckCell = (TextCheckCell) holder.itemView;
                    if (position == showGradientRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShadeBackground", R.string.ShadeBackground), OctoConfig.showGradientColor, true);
                    } else if (position == showAvatarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowAvatar", R.string.ShowAvatar), OctoConfig.showAvatarImage, drawerBlurBackgroundRow != -1);
                    } else if (position == drawerAvatarAsBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarAsBackground", R.string.AvatarAsBackground), OctoConfig.avatarAsDrawerBackground, OctoConfig.avatarAsDrawerBackground);
                    } else if (position == drawerBlurBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarBlur", R.string.AvatarBlur), OctoConfig.avatarBackgroundBlur, !OctoConfig.avatarBackgroundBlur);
                    } else if (position == drawerDarkenBackgroundRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AvatarDarken", R.string.AvatarDarken), OctoConfig.avatarBackgroundDarken, true);
                    } else if (position == useSystemFontRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("UseSystemFonts", R.string.UseSystemFonts), OctoConfig.useSystemFont, true);
                    } else if (position == messageTimeSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("FormatTimeSeconds", R.string.FormatTimeSeconds), LocaleController.getString("FormatTimeSecondsDesc", R.string.FormatTimeSecondsDesc), OctoConfig.fullTime, true, true);
                    } else if (position == roundedNumberSwitchRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("NumberRounding", R.string.NumberRounding), LocaleController.getString("NumberRoundingDesc", R.string.NumberRoundingDesc), OctoConfig.roundedNumbers, true, true);
                    } else if (position == forcePacmanRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("PacManAnimation", R.string.PacManAnimation), OctoConfig.pacmanForced, true);
                    } else if (position == smartButtonsRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShortcutsForAdmins", R.string.ShortcutsForAdmins), OctoConfig.smartButtons, false);
                    } else if (position == appBarShadowRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AppBarShadow", R.string.AppBarShadow), OctoConfig.showAppBarShadow, true);
                    } else if (position == showSantaHatRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ChristmasHat", R.string.ChristmasHat), OctoConfig.showSantaHat, true);
                    } else if (position == showFallingSnowRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("FallingSnow", R.string.FallingSnow), OctoConfig.showSnowFalling, true);
                    } else if (position == slidingTitleRow) {
                        textCheckCell.setTextAndValueAndCheck(LocaleController.getString("SlidingTitle", R.string.SlidingTitle), LocaleController.getString("SlidingTitleDesc", R.string.SlidingTitleDesc), OctoConfig.slidingChatTitle, true, true);
                    } else if (position == searchIconInActionBarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("SearchIconTitleBar", R.string.SearchIconTitleBar), OctoConfig.searchIconInActionBar, false);
                    } else if (position == showPencilIconRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("ShowPencilIcon", R.string.ShowPencilIcon), OctoConfig.showPencilIcon, true);
                    } else if (position == showInActionBarRow) {
                        textCheckCell.setTextAndCheck(LocaleController.getString("AccountNameTitleBar", R.string.AccountNameTitleBar), OctoConfig.showNameInActionBar, true);
                    }
                    break;
                case PROFILE_PREVIEW:
                    DrawerProfilePreview cell = (DrawerProfilePreview) holder.itemView;
                    cell.setUser(getUserConfig().getCurrentUser(), false);
                    break;
                case TEXT_CELL:
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == menuItemsRow) {
                        textCell.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                        textCell.setTextAndIcon(LocaleController.getString("MenuItems", R.string.MenuItems), R.drawable.msg_newfilter, false);
                    }
                    break;
                case SETTINGS:
                    TextSettingsCell textSettingsCell = (TextSettingsCell) holder.itemView;
                    textSettingsCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == chooseEmojiPackRow) {
                        textSettingsCell.setDrawLoading(CustomEmojiController.isLoading(), 30, partial);
                        String emojiPack = CustomEmojiController.getSelectedPackName();
                        textSettingsCell.setTextAndValue(LocaleController.getString("EmojiSets", R.string.EmojiSets), emojiPack, true);
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.SWITCH || viewType == ViewType.TEXT_CELL || viewType == ViewType.SETTINGS;
        }

        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            switch (viewType) {
                case PROFILE_PREVIEW:
                    view = profilePreviewCell = new DrawerProfilePreview(context);
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case BLUR_INTENSITY:
                    view = new BlurIntensity(context) {
                        @Override
                        protected void onBlurIntensityChange(int percentage, boolean layout) {
                            super.onBlurIntensityChange(percentage, layout);
                            OctoConfig.saveBlurIntensity(percentage);
                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(editBlurRow);
                            if (holder != null && holder.itemView instanceof BlurIntensity) {
                                BlurIntensity cell = (BlurIntensity) holder.itemView;
                                if (layout) {
                                    cell.requestLayout();
                                } else {
                                    cell.invalidate();
                                }
                            }
                            reloadMainInfo();
                            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case THEME_SELECTOR:
                    view = new ThemeSelectorDrawer(context, OctoConfig.eventType) {
                        @Override
                        protected void onSelectedEvent(int eventSelected) {
                            super.onSelectedEvent(eventSelected);
                            int previousEvent = OctoConfig.eventType;
                            OctoConfig.saveEventType(eventSelected);
                            if (Theme.getEventType() == 0) {
                                if (previousEvent == 0) {
                                    previousEvent = 1;
                                } else if (eventSelected == 0) {
                                    eventSelected = 1;
                                }
                            }
                            if (previousEvent == 1 && eventSelected != 1) {
                                listAdapter.notifyItemRangeRemoved(forcePacmanRow + 1, 2);
                            } else if (previousEvent != 1 && eventSelected == 1) {
                                listAdapter.notifyItemRangeInserted(forcePacmanRow + 1, 2);
                            }
                            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
                            Theme.lastHolidayCheckTime = 0;
                            Theme.dialogs_holidayDrawable = null;
                            reloadMainInfo();
                            updateRowsId();
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case DYNAMIC_BUTTON_SELECTOR:
                    view = new DynamicButtonSelector(context) {
                        @Override
                        protected void onSelectionChange() {
                            super.onSelectionChange();
                            reloadInterface();
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == drawerDividerRow || position == editBlurDividerRow || position == themeDrawerDividerRow ||
                    position == dynamicDividerRow || position == fontsAndEmojiDividerRow || position == appearanceDividerRow ||
                    position == chatHeaderDividerRow) {
                return ViewType.SHADOW;
            } else if (position == editBlurHeaderRow || position == themeDrawerHeader || position == dynamicButtonHeaderRow ||
                    position == fontsAndEmojiHeaderRow || position == appearanceHeaderRow || position == chatHeaderRow) {
                return ViewType.HEADER;
            } else if (position == roundedNumberSwitchRow || position == messageTimeSwitchRow ||
                    position == useSystemFontRow || position == drawerAvatarAsBackgroundRow ||
                    position == drawerDarkenBackgroundRow || position == drawerBlurBackgroundRow || position == showGradientRow ||
                    position == showAvatarRow || position == forcePacmanRow || position == smartButtonsRow ||
                    position == appBarShadowRow || position == showSantaHatRow || position == showFallingSnowRow ||
                    position == slidingTitleRow || position == searchIconInActionBarRow || position == showPencilIconRow ||
                    position == showInActionBarRow) {
                return ViewType.SWITCH;
            } else if (position == drawerRow) {
                return ViewType.PROFILE_PREVIEW;
            } else if (position == editBlurRow) {
                return ViewType.BLUR_INTENSITY;
            } else if (position == menuItemsRow) {
                return ViewType.TEXT_CELL;
            } else if (position == themeDrawerRow) {
                return ViewType.THEME_SELECTOR;
            } else if (position == dynamicButtonRow) {
                return ViewType.DYNAMIC_BUTTON_SELECTOR;
            } else if (position == chooseEmojiPackRow) {
                return ViewType.SETTINGS;
            }
            throw new IllegalArgumentException("Invalid position");
        }
    }
}
