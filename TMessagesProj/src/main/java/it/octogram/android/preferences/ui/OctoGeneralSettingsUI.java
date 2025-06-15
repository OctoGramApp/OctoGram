/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import it.octogram.android.ActionBarTitleOption;
import it.octogram.android.ConfigProperty;
import it.octogram.android.CustomEmojiController;
import it.octogram.android.InterfaceRapidButtonsActions;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.CustomActionBarTitleBottomSheet;
import it.octogram.android.preferences.ui.custom.RapidActionsPreviewLayout;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;


public class OctoGeneralSettingsUI implements PreferencesEntry {
    private RapidActionsPreviewLayout rapidActionsPreviewLayout;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        ConfigProperty<Boolean> showCustomTitleRow = new ConfigProperty<>(null, OctoConfig.INSTANCE.actionBarTitleOption.getValue() == ActionBarTitleOption.CUSTOM.getValue());

        ConfigProperty<Boolean> canChooseSecondaryButtonAction = new ConfigProperty<>(null, false);

        Runnable restartStates = () -> canChooseSecondaryButtonAction.updateValue(!OctoConfig.INSTANCE.rapidActionsDefaultConfig.getValue() && OctoConfig.INSTANCE.rapidActionsMainButtonAction.getValue() != InterfaceRapidButtonsActions.HIDDEN.getValue());
        restartStates.run();

        return OctoPreferences.builder(getString(R.string.OctoGeneralSettings))
                .deepLink(DeepLinkDef.GENERAL)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.GENERAL, true, getString(R.string.OctoGeneralSettingsHeader))
                .category(getString(R.string.InterfaceHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showUserIconsInChatsList)
                            .title(getString(R.string.ShowUserIconsInChatsList))
                            .description(getString(R.string.ShowUserIconsInChatsList_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideStories)
                            .requiresRestart(true)
                            .title(getString(R.string.HideStories))
                            .description(getString(R.string.HideStories_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.alwaysShowDownloads)
                            .requiresRestart(true)
                            .title(getString(R.string.AlwaysShowDownloads))
                            .description(getString(R.string.AlwaysShowDownloads_Desc))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.actionBarTitleOption)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.EMPTY.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleCustomEmpty)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.APP_NAME.getValue())
                                            .setItemTitle(getString(R.string.BuildAppName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_NAME.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleAccountName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_USERNAME.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleAccountUsername)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.FOLDER_NAME.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleFolderName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.CUSTOM.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleCustom))
                            ))
                            .onSelected(() -> {
                                showCustomTitleRow.setValue(OctoConfig.INSTANCE.actionBarTitleOption.getValue() == ActionBarTitleOption.CUSTOM.getValue());
                                fragment.rebuildAllFragmentsWithLast();
                            })
                            .title(getString(R.string.ActionBarTitle))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> editCustomName(fragment, context))
                            .value(getCustomNameStatus())
                            .showIf(showCustomTitleRow)
                            .title(getString(R.string.ActionBarTitleCustom))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                Parcelable recyclerViewState = null;
                                RecyclerView.LayoutManager layoutManager = fragment.getListView().getLayoutManager();
                                if (layoutManager != null)
                                    recyclerViewState = layoutManager.onSaveInstanceState();
                                fragment.getParentLayout().rebuildAllFragmentViews(false, false);
                                if (layoutManager != null && recyclerViewState != null)
                                    layoutManager.onRestoreInstanceState(recyclerViewState);
                            })
                            .preferenceValue(OctoConfig.INSTANCE.disableDividers)
                            .title(getString(R.string.HideDividers))
                            .build());
                })
                .category(R.string.ImproveRapidActions, category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(rapidActionsPreviewLayout = new RapidActionsPreviewLayout(context))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                restartStates.run();
                                rapidActionsPreviewLayout.restart();
                                NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.storiesEnabledUpdate);
                            })
                            .preferenceValue(OctoConfig.INSTANCE.rapidActionsDefaultConfig)
                            .title(getString(R.string.ImproveRapidActionsDefault))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .supplierOptions(composeOptions(true, false))
                            .onSelected(() -> {
                                restartStates.run();
                                rapidActionsPreviewLayout.restart();
                                NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.storiesEnabledUpdate);
                            })
                            .currentValue(OctoConfig.INSTANCE.rapidActionsMainButtonAction)
                            .title(getString(R.string.ImproveRapidActionsMainButtonAction))
                            .showIf(OctoConfig.INSTANCE.rapidActionsDefaultConfig, true)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .supplierOptions(composeOptions(true, true))
                            .onSelected(restartStates)
                            .currentValue(OctoConfig.INSTANCE.rapidActionsMainButtonActionLongPress)
                            .title(getString(R.string.ImproveRapidActionsMainButtonActionLongPress))
                            .showIf(canChooseSecondaryButtonAction)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .supplierOptions(composeOptions(false, false))
                            .onSelected(() -> {
                                restartStates.run();
                                rapidActionsPreviewLayout.restart();
                                NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.storiesEnabledUpdate);
                            })
                            .currentValue(OctoConfig.INSTANCE.rapidActionsSecondaryButtonAction)
                            .title(getString(R.string.ImproveRapidActionsSecondaryButtonAction))
                            .showIf(canChooseSecondaryButtonAction)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(rapidActionsPreviewLayout::restart)
                            .preferenceValue(OctoConfig.INSTANCE.useSquaredFab)
                            .title(R.string.SquaredFab)
                            .requiresRestart(true)
                            .build());
                })
                .category(getString(R.string.FontEmojisHeader), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new EmojiPackSettings()))
                            .value(CustomEmojiController.getSelectedPackName())
                            .icon(OctoUtils.getPetIconFixed())
                            .title(getString(R.string.EmojiSets))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                AndroidUtilities.clearTypefaceCache();
                                Parcelable recyclerViewState = null;
                                if (fragment.getListView().getLayoutManager() != null)
                                    recyclerViewState = fragment.getListView().getLayoutManager().onSaveInstanceState();
                                fragment.getParentLayout().rebuildAllFragmentViews(true, true);
                                fragment.getListView().getLayoutManager().onRestoreInstanceState(recyclerViewState);
                            })
                            .icon(R.drawable.msg_text_outlined)
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title(getString(R.string.UseSystemFont))
                            .requiresRestart(true)
                            .build());
                })
                .category(getString(R.string.Notifications), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.accentColorAsNotificationColor)
                            .title(getString(R.string.AccentColorAsNotificationColor))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                checkSmartNotificationsEnabled(fragment);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats)
                            .title(getString(R.string.EnableSmartNotificationsForPrivateChats))
                            .build());
                })
                .build();
    }

    private Supplier<List<PopupChoiceDialogOption>> composeOptions(boolean isMainButton, boolean isLongPress) {
        return () -> {
            ArrayList<PopupChoiceDialogOption> options = new ArrayList<>();
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.HIDDEN.getValue())
                    .setItemIcon(R.drawable.msg_cancel)
                    .setItemTitle(getString(isLongPress ? R.string.SlowmodeOff : R.string.CameraButtonPosition_Hidden)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.POST_STORY.getValue())
                    .setItemIcon(R.drawable.msg_camera)
                    .setItemTitle(getString(R.string.AccDescrCaptureStory)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.SEND_MESSAGE.getValue())
                    .setItemIcon(R.drawable.msg_message_s)
                    .setItemTitle(getString(R.string.NewMessageTitle)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.SAVED_MESSAGES.getValue())
                    .setItemIcon(R.drawable.msg_saved)
                    .setItemTitle(getString(R.string.SavedMessages)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.ARCHIVED_CHATS.getValue())
                    .setItemIcon(R.drawable.msg_archive)
                    .setItemTitle(getString(R.string.ArchivedChats)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.SETTINGS.getValue())
                    .setItemIcon(R.drawable.msg_settings)
                    .setItemTitle(getString(R.string.Settings)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.LOCKED_CHATS.getValue())
                    .setItemIcon(R.drawable.edit_passcode)
                    .setItemTitle(getString(R.string.LockedChats)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.PROXY.getValue())
                    .setItemIcon(R.drawable.msg2_proxy_off)
                    .setItemTitle(getString(R.string.Proxy)));
            options.add(new PopupChoiceDialogOption()
                    .setId(InterfaceRapidButtonsActions.SEARCH.getValue())
                    .setItemIcon(R.drawable.msg_search)
                    .setItemTitle(getString(R.string.Search)));

            for (PopupChoiceDialogOption option : options) {
                checkButtonActions(isMainButton, isLongPress, option);
            }

            return options;
        };
    }

    private void checkButtonActions(boolean isMainButton, boolean isLongPress, PopupChoiceDialogOption data) {
        if (data.id == InterfaceRapidButtonsActions.HIDDEN.getValue()) {
            return;
        }

        data.setClickable(!(
                (
                        isMainButton && OctoConfig.INSTANCE.rapidActionsSecondaryButtonAction.getValue() == data.id
                ) || (
                        isMainButton && !isLongPress && OctoConfig.INSTANCE.rapidActionsMainButtonActionLongPress.getValue() == data.id
                ) || (
                        isMainButton && isLongPress && OctoConfig.INSTANCE.rapidActionsMainButtonAction.getValue() == data.id
                ) || (
                        !isMainButton && OctoConfig.INSTANCE.rapidActionsMainButtonAction.getValue() == data.id
                )
        ));

        if (data.id == InterfaceRapidButtonsActions.POST_STORY.getValue()) {
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                UserConfig userConfig = UserConfig.getInstance(a);
                if (!userConfig.isClientActivated()) {
                    continue;
                }
                boolean storiesEnabled = MessagesController.getInstance(a).storiesEnabled();
                if (!storiesEnabled) {
                    data.setItemDescription(R.string.ImproveRapidActionsMainButtonActionStoriesUnavailableLong);
                    break;
                }
            }
        }
    }

    private void checkSmartNotificationsEnabled(PreferencesFragment fragment) {
        if (OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats.getValue()) {
            return;
        }

        new AlertDialog.Builder(fragment.getContext())
                .setTitle(getString(R.string.Warning))
                .setMessage(getString(R.string.SmartNotificationsPvtDialogMessage))
                .setPositiveButton(getString(R.string.OK), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void editCustomName(PreferencesFragment fragment, Context context) {
        var bottomSheet = new CustomActionBarTitleBottomSheet(context, new CustomActionBarTitleBottomSheet.CustomActionBarTitleCallback() {
            @Override
            public void didRenameSuccessfully(String customName) {
                OctoConfig.INSTANCE.actionBarCustomTitle.updateValue(customName);
                OctoConfig.INSTANCE.actionBarTitleOption.updateValue(ActionBarTitleOption.CUSTOM.getValue());
                fragment.rebuildAllFragmentsWithLast();
            }

            @Override
            public void didReset() {
                OctoConfig.INSTANCE.actionBarCustomTitle.updateValue("Home");
                OctoConfig.INSTANCE.actionBarTitleOption.updateValue(ActionBarTitleOption.EMPTY.getValue());
                fragment.rebuildAllFragmentsWithLast();
            }
        });
        bottomSheet.show();
    }

    private String getCustomNameStatus() {
        String customName = OctoConfig.INSTANCE.actionBarCustomTitle.getValue();
        if (TextUtils.isEmpty(customName)) {
            customName = getString(R.string.ActionBarTitleCustomEmpty);
        }

        return customName;
    }
}
