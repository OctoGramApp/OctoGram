/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import org.json.JSONException;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.DrawerBackgroundState;
import it.octogram.android.DrawerFavoriteOption;
import it.octogram.android.MenuItemId;
import it.octogram.android.OctoConfig;
import it.octogram.android.drawer.MenuOrderController;
import it.octogram.android.icons.IconsUtils;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.ui.components.DrawerPreviewCell;
import it.octogram.android.preferences.ui.custom.DrawerOrderSettings;
import it.octogram.android.preferences.ui.custom.ThemeSelectorCell;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoDrawerSettingsUI implements PreferencesEntry {
    private DrawerPreviewCell drawerPreviewCell;

    private final ConfigProperty<Boolean> canSelectBlur = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectBlurLevel = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canUseDarken = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectDarkLevel = new ConfigProperty<>(null, false);

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        updateItemsVisibility();

        return OctoPreferences.builder(LocaleController.formatString(R.string.DrawerTitle))
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_openin, LocaleController.getString(R.string.Drawer_Test), () -> LaunchActivity.instance.drawerLayoutContainer.openDrawer(true)))
                .category("Drawer", category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(drawerPreviewCell = new DrawerPreviewCell(context))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.drawerBackground)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerBackgroundState.TRANSPARENT.getValue())
                                            .setItemIcon(R.drawable.msg_cancel)
                                            .setItemTitle(LocaleController.getString(R.string.DrawerBackgroundTransparent)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerBackgroundState.WALLPAPER.getValue())
                                            .setItemIcon(R.drawable.msg_background)
                                            .setItemTitle(LocaleController.getString(R.string.DrawerBackgroundWallpaper)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerBackgroundState.PROFILE_PIC.getValue())
                                            .setItemIcon(R.drawable.msg_view_file)
                                            .setItemTitle(LocaleController.getString(R.string.DrawerBackgroundProfilePhoto)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerBackgroundState.COLOR.getValue())
                                            .setItemIcon(R.drawable.msg_colors)
                                            .setItemTitle(LocaleController.getString(R.string.DrawerBackgroundColor))
                            ))
                            .onSelected(this::reloadDrawerPreviewInstance)
                            .title(LocaleController.getString(R.string.DrawerBackground))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(this::reloadDrawerPreviewInstance)
                            .preferenceValue(OctoConfig.INSTANCE.drawerShowProfilePic)
                            .title(LocaleController.getString(R.string.DrawerShowProfilePic))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(this::reloadDrawerPreviewInstance)
                            .preferenceValue(OctoConfig.INSTANCE.drawerGradientBackground)
                            .title(LocaleController.getString(R.string.DrawerGradientBackground))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.drawerFavoriteOption)
                            .onClick(() -> canChangeFavoriteOption(context))
                            .onSelected(() -> {
                                drawerPreviewCell.updateMiniIcon();
                                LaunchActivity.instance.reloadDrawerMiniIcon();
                                MenuOrderController.onDrawerFavoriteOptionChanged();
                            })
                            .supplierOptions(() -> List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.NONE.getValue())
                                            .setItemIcon(R.drawable.msg_cancel)
                                            .setItemTitle(LocaleController.getString(R.string.DrawerFavoriteOptionNone)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.DEFAULT.getValue())
                                            .setItemIcon(R.drawable.msg_forward_replace)
                                            .setItemTitle(LocaleController.getString(R.string.DrawerFavoriteOptionDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.SAVED_MESSAGES.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.SAVED_MESSAGES.getValue()))
                                            .setItemTitle(LocaleController.getString(R.string.SavedMessages)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.SETTINGS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.SETTINGS.getValue()))
                                            .setItemTitle(LocaleController.getString(R.string.Settings)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.CONTACTS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.CONTACTS.getValue()))
                                            .setItemTitle(LocaleController.getString(R.string.Contacts)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.CALLS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.CALLS.getValue()))
                                            .setItemTitle(LocaleController.getString(R.string.Calls)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.DOWNLOADS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.DOWNLOADS.getValue()))
                                            .setItemTitle(LocaleController.getString(R.string.DownloadsTabs)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.ARCHIVED_CHATS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.ARCHIVED_CHATS.getValue()))
                                            .setItemTitle(LocaleController.getString(R.string.ArchivedChats)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.TELEGRAM_BROWSER.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.TELEGRAM_BROWSER.getValue()))
                                            .setItemTitle("Telegram Browser")
                            ))
                            .title(LocaleController.getString(R.string.DrawerFavoriteOption))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> fragment.presentFragment(new DrawerOrderSettings()))
                            .icon(R.drawable.msg_new_filter)
                            .title(LocaleController.getString(R.string.DrawerElements))
                            .description(LocaleController.getString(R.string.DrawerElements_Desc))
                            .build());
                })
                .row(new SwitchRow.SwitchRowBuilder()
                        .onPostUpdate(this::reloadDrawerPreviewInstance)
                        .preferenceValue(OctoConfig.INSTANCE.drawerDarkenBackground)
                        .title(LocaleController.formatString(R.string.DrawerDarkenBackground))
                        .showIf(canUseDarken)
                        .build())
                .category(LocaleController.getString(R.string.DrawerDarkenBackgroundLevel), canSelectDarkLevel, canUseDarken, category -> category.row(new SliderRow.SliderRowBuilder()
                        .min(1)
                        .max(255)
                        .onSelected(() -> drawerPreviewCell.updateDarkerBackgroundLevel(OctoConfig.INSTANCE.drawerDarkenBackgroundLevel.getValue()))
                        .preferenceValue(OctoConfig.INSTANCE.drawerDarkenBackgroundLevel)
                        .showIf(canSelectDarkLevel)
                        .build()))
                .row(new SwitchRow.SwitchRowBuilder()
                        .onPostUpdate(this::reloadDrawerPreviewInstance)
                        .preferenceValue(OctoConfig.INSTANCE.drawerBlurBackground)
                        .title(LocaleController.getString(R.string.DrawerBlurBackground))
                        .showIf(canSelectBlur)
                        .build())
                .category(LocaleController.getString(R.string.DrawerBlurBackgroundLevel), canSelectBlurLevel, canSelectBlur, category -> category.row(new SliderRow.SliderRowBuilder()
                        .min(1)
                        .max(100)
                        .onSelected(() -> drawerPreviewCell.updateImageReceiver())
                        .preferenceValue(OctoConfig.INSTANCE.drawerBlurBackgroundLevel)
                        .showIf(canSelectBlurLevel)
                        .build()))
                .category(LocaleController.getString(R.string.Style), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(new ThemeSelectorCell(context, OctoConfig.INSTANCE.eventType.getValue()) {
                                @Override
                                protected void onSelectedEvent(int eventSelected) {
                                    super.onSelectedEvent(eventSelected);
                                    OctoConfig.INSTANCE.eventType.updateValue(eventSelected);

                                    Theme.lastHolidayCheckTime = 0;
                                    Theme.dialogs_holidayDrawable = null;

                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);

                                    drawerPreviewCell.updateMiniIcon();
                                    LaunchActivity.instance.reloadDrawerMiniIcon();
                                }
                            })
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.reloadDrawerHeader()))
                            .preferenceValue(OctoConfig.INSTANCE.drawerProfileAsBubble)
                            .title(LocaleController.getString(R.string.DrawerHeaderAsBubble))
                            .description(LocaleController.getString(R.string.DrawerHeaderAsBubble_Desc))
                            .build());
                })
                .build();
    }

    private boolean canChangeFavoriteOption(Context context) {
        boolean hasSettingsDrawerButton = false;
        for (int i = 0; i < MenuOrderController.data.length(); i++) {
            try {
                if (MenuOrderController.data.getString(i).equals(MenuItemId.SETTINGS.getId())) {
                    hasSettingsDrawerButton = true;
                    break;
                }
            } catch (JSONException ignored) {

            }
        }

        if (!hasSettingsDrawerButton && OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.SETTINGS.getValue()) {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
            warningBuilder.setTitle(LocaleController.getString(R.string.Warning));
            warningBuilder.setMessage(LocaleController.getString(R.string.DrawerFavoriteOptionUnavailable));
            warningBuilder.setPositiveButton(LocaleController.getString(R.string.OK), null);
            AlertDialog dialog = warningBuilder.create();
            dialog.show();

            return false;
        }

        return true;
    }

    private void updateItemsVisibility() {
        canSelectBlur.setValue(OctoConfig.INSTANCE.drawerBackground.getValue() == DrawerBackgroundState.PROFILE_PIC.getValue());
        canSelectBlurLevel.setValue(canSelectBlur.getValue() && OctoConfig.INSTANCE.drawerBlurBackground.getValue());
        canUseDarken.setValue(OctoConfig.INSTANCE.drawerBackground.getValue() != DrawerBackgroundState.COLOR.getValue());
        canSelectDarkLevel.setValue(canUseDarken.getValue() && OctoConfig.INSTANCE.drawerDarkenBackground.getValue());
    }

    private void reloadDrawerPreviewInstance() {
        updateItemsVisibility();
        AndroidUtilities.runOnUIThread(() -> {
            drawerPreviewCell.reloadInstance();
            LaunchActivity.instance.reloadDrawerState();
        });
    }
}
