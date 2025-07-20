/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;

import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.DrawerBackgroundState;
import it.octogram.android.DrawerFavoriteOption;
import it.octogram.android.DrawerItem;
import it.octogram.android.OctoConfig;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.SliderRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextDetailRow;
import it.octogram.android.app.ui.cells.HolidaySelectorCell;
import it.octogram.android.app.ui.components.DrawerPreviewCell;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.config.DrawerOrderController;
import it.octogram.android.utils.deeplink.DeepLinkDef;
import it.octogram.android.utils.icons.IconsUtils;


public class OctoAppearanceDrawerSettingsUI implements PreferencesEntry {
    private DrawerPreviewCell drawerPreviewCell;

    private final ConfigProperty<Boolean> canSelectBlur = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectBlurLevel = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canUseDarken = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectDarkLevel = new ConfigProperty<>(null, false);

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        updateItemsVisibility();

        return OctoPreferences.builder(getString(R.string.DrawerTitle))
                .deepLink(DeepLinkDef.APPEARANCE_DRAWER)
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_openin, getString(R.string.Drawer_Test), () -> LaunchActivity.instance.drawerLayoutContainer.openDrawer(true)))
                .category(getString(R.string.DrawerTitle), category -> {
                    fillMainCategory(context, fragment, category);
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.drawerFavoriteOption)
                            .onClick(() -> canChangeFavoriteOption(context))
                            .onSelected(() -> {
                                drawerPreviewCell.updateMiniIcon();
                                LaunchActivity.instance.reloadDrawerMiniIcon();
                                DrawerOrderController.onDrawerFavoriteOptionChanged();
                            })
                            .supplierOptions(() -> List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.NONE.getValue())
                                            .setItemIcon(R.drawable.msg_cancel)
                                            .setItemTitle(getString(R.string.DrawerFavoriteOptionNone)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.DEFAULT.getValue())
                                            .setItemIcon(R.drawable.msg_forward_replace)
                                            .setItemTitle(getString(R.string.DrawerFavoriteOptionDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.SAVED_MESSAGES.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.SAVED_MESSAGES.getValue()))
                                            .setItemTitle(getString(R.string.SavedMessages)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.SETTINGS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.SETTINGS.getValue()))
                                            .setItemTitle(getString(R.string.Settings)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.CONTACTS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.CONTACTS.getValue()))
                                            .setItemTitle(getString(R.string.Contacts)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.CALLS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.CALLS.getValue()))
                                            .setItemTitle(getString(R.string.Calls)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.DOWNLOADS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.DOWNLOADS.getValue()))
                                            .setItemTitle(getString(R.string.DownloadsTabs)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.ARCHIVED_CHATS.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.ARCHIVED_CHATS.getValue()))
                                            .setItemTitle(getString(R.string.ArchivedChats)),
                                    new PopupChoiceDialogOption()
                                            .setId(DrawerFavoriteOption.TELEGRAM_BROWSER.getValue())
                                            .setItemIcon(IconsUtils.getIconWithEventType(DrawerFavoriteOption.TELEGRAM_BROWSER.getValue()))
                                            .setItemTitle("Telegram Browser")
                            ))
                            .title(getString(R.string.DrawerFavoriteOption))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> fragment.presentFragment(new OctoAppearanceDrawerOrderUI()))
                            .icon(R.drawable.msg_new_filter)
                            .propertySelectionTag("drawerElements")
                            .title(getString(R.string.DrawerElements))
                            .description(getString(R.string.DrawerElements_Desc))
                            .build());
                })
                .row(new SwitchRow.SwitchRowBuilder()
                        .onPostUpdate(this::reloadDrawerPreviewInstance)
                        .preferenceValue(OctoConfig.INSTANCE.drawerDarkenBackground)
                        .title(formatString(R.string.DrawerDarkenBackground))
                        .showIf(canUseDarken)
                        .build())
                .category(getString(R.string.DrawerDarkenBackgroundLevel), canSelectDarkLevel, canUseDarken, category -> category.row(new SliderRow.SliderRowBuilder()
                        .min(1)
                        .max(255)
                        .onSelected(() -> drawerPreviewCell.updateDarkerBackgroundLevel(OctoConfig.INSTANCE.drawerDarkenBackgroundLevel.getValue()))
                        .preferenceValue(OctoConfig.INSTANCE.drawerDarkenBackgroundLevel)
                        .showIf(canSelectDarkLevel)
                        .build()))
                .row(new SwitchRow.SwitchRowBuilder()
                        .onPostUpdate(this::reloadDrawerPreviewInstance)
                        .preferenceValue(OctoConfig.INSTANCE.drawerBlurBackground)
                        .title(getString(R.string.DrawerBlurBackground))
                        .showIf(canSelectBlur)
                        .build())
                .category(getString(R.string.DrawerBlurBackgroundLevel), canSelectBlurLevel, canSelectBlur, category -> category.row(new SliderRow.SliderRowBuilder()
                        .min(1)
                        .max(100)
                        .onSelected(() -> drawerPreviewCell.updateImageReceiver())
                        .preferenceValue(OctoConfig.INSTANCE.drawerBlurBackgroundLevel)
                        .showIf(canSelectBlurLevel)
                        .build()))
                .category(getString(R.string.Style), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .propertySelectionTag("eventType")
                            .layout(new HolidaySelectorCell(context, OctoConfig.INSTANCE.eventType.getValue()) {
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
                            .title(getString(R.string.DrawerHeaderAsBubble))
                            .description(getString(R.string.DrawerHeaderAsBubble_Desc))
                            .build());
                })
                .build();
    }

    public DrawerPreviewCell fillMainCategory(Context context, PreferencesFragment fragment, OctoPreferences.OctoPreferencesBuilder category) {
        return fillMainCategory(context, fragment, category, false);
    }

    public DrawerPreviewCell fillMainCategory(Context context, PreferencesFragment fragment, OctoPreferences.OctoPreferencesBuilder category, boolean forceRedirect) {
        category.row(new CustomCellRow.CustomCellRowBuilder()
                .layout(drawerPreviewCell = new DrawerPreviewCell(context))
                .build());
        category.row(new ListRow.ListRowBuilder()
                .currentValue(OctoConfig.INSTANCE.drawerBackground)
                .options(List.of(
                        new PopupChoiceDialogOption()
                                .setId(DrawerBackgroundState.TRANSPARENT.getValue())
                                .setItemIcon(R.drawable.msg_cancel)
                                .setItemTitle(getString(R.string.DrawerBackgroundTransparent)),
                        new PopupChoiceDialogOption()
                                .setId(DrawerBackgroundState.WALLPAPER.getValue())
                                .setItemIcon(R.drawable.msg_background)
                                .setItemTitle(getString(R.string.DrawerBackgroundWallpaper)),
                        new PopupChoiceDialogOption()
                                .setId(DrawerBackgroundState.PROFILE_PIC.getValue())
                                .setItemIcon(R.drawable.msg_view_file)
                                .setItemTitle(getString(R.string.DrawerBackgroundProfilePhoto)),
                        new PopupChoiceDialogOption()
                                .setId(DrawerBackgroundState.COLOR.getValue())
                                .setItemIcon(R.drawable.msg_colors)
                                .setItemTitle(getString(R.string.DrawerBackgroundColor)),
                        new PopupChoiceDialogOption()
                                .setId(DrawerBackgroundState.PREMIUM_DETAILS.getValue())
                                .setItemIcon(R.drawable.menu_feature_premium)
                                .setItemTitle(getString(R.string.DrawerPremiumDetails))
                                .setItemDescription(getString(R.string.DrawerPremiumDetails_Desc))
                ))
                .onClick(() -> {
                    if (forceRedirect) {
                        fragment.presentFragment(new PreferencesFragment(new OctoAppearanceDrawerSettingsUI(), OctoConfig.INSTANCE.drawerBackground.getKey()));
                        return false;
                    }
                    return true;
                })
                .onSelected(this::reloadDrawerPreviewInstance)
                .title(getString(R.string.DrawerBackground))
                .build());
        category.row(new SwitchRow.SwitchRowBuilder()
                .onPostUpdate(this::reloadDrawerPreviewInstance)
                .onClick(() -> {
                    if (forceRedirect) {
                        fragment.presentFragment(new PreferencesFragment(new OctoAppearanceDrawerSettingsUI(), OctoConfig.INSTANCE.drawerShowProfilePic.getKey()));
                        return false;
                    }
                    return true;
                })
                .preferenceValue(OctoConfig.INSTANCE.drawerShowProfilePic)
                .title(getString(R.string.DrawerShowProfilePic))
                .build());
        category.row(new SwitchRow.SwitchRowBuilder()
                .onPostUpdate(this::reloadDrawerPreviewInstance)
                .onClick(() -> {
                    if (forceRedirect) {
                        fragment.presentFragment(new PreferencesFragment(new OctoAppearanceDrawerSettingsUI(), OctoConfig.INSTANCE.drawerGradientBackground.getKey()));
                        return false;
                    }
                    return true;
                })
                .preferenceValue(OctoConfig.INSTANCE.drawerGradientBackground)
                .title(getString(R.string.DrawerGradientBackground))
                .build());

        return drawerPreviewCell;
    }

    private boolean canChangeFavoriteOption(Context context) {
        boolean hasSettingsDrawerButton = false;
        for (int i = 0; i < DrawerOrderController.data.length(); i++) {
            try {
                if (DrawerOrderController.data.getString(i).equals(DrawerItem.Id.SETTINGS.getId())) {
                    hasSettingsDrawerButton = true;
                    break;
                }
            } catch (JSONException ignored) {

            }
        }

        if (!hasSettingsDrawerButton && OctoConfig.INSTANCE.drawerFavoriteOption.getValue() == DrawerFavoriteOption.SETTINGS.getValue()) {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
            warningBuilder.setTitle(getString(R.string.Warning));
            warningBuilder.setMessage(getString(R.string.DrawerFavoriteOptionUnavailable));
            warningBuilder.setPositiveButton(getString(R.string.OK), null);
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
