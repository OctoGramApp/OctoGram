package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.ContextMenuBriefingState;
import it.octogram.android.OctoConfig;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.ui.components.ChatSettingsPreviews;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;

public class OctoContextMenuSettingsUI implements PreferencesEntry {
    private ChatSettingsPreviews chatSettingsPreviews;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        ConfigProperty<Boolean> isShortcutsSelected = new ConfigProperty<>(null, false);
        Runnable updateState = () -> isShortcutsSelected.updateValue(OctoConfig.INSTANCE.contextMenuBriefingState.getValue() == ContextMenuBriefingState.ENABLED_SHORTCUTS.getState());
        updateState.run();

        return OctoPreferences.builder(getString(R.string.ContextMenu))
                .deepLink(DeepLinkDef.CHATS_CONTEXTMENU)
                .category(R.string.Appearance, category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .onSelected(() -> {
                                updateState.run();
                                AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10);
                            })
                            .currentValue(OctoConfig.INSTANCE.contextMenuBriefingState)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ContextMenuBriefingState.DISABLED.getState())
                                            .setItemTitle(getString(R.string.ContextMenuBriefingState_Disabled)),
                                    new PopupChoiceDialogOption()
                                            .setId(ContextMenuBriefingState.ENABLED_SUBCATEGORIES.getState())
                                            .setItemTitle(getString(R.string.ContextMenuBriefingState_Subcategories)),
                                    new PopupChoiceDialogOption()
                                            .setId(ContextMenuBriefingState.ENABLED_SHORTCUTS.getState())
                                            .setItemTitle(getString(R.string.ContextMenuBriefingState_Shortcuts))
                            ))
                            .title(getString(R.string.ContextMenuBriefingState))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextMenuBottomShortcuts)
                            .title(getString(R.string.ContextMenuBriefingState_Shortcuts_Bottom))
                            .showIf(isShortcutsSelected)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextMenuShortcutsTitles)
                            .title(getString(R.string.ContextMenuBriefingState_Shortcuts_Title))
                            .showIf(isShortcutsSelected)
                            .build());
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(chatSettingsPreviews = new ChatSettingsPreviews(context, ChatSettingsPreviews.PreviewType.CONTEXT_MENU))
                            .build()
                    );
                })
                .category(R.string.ContextMenuElements, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextReplyMessage)
                            .title(getString(R.string.MessageOptionsReplyTitle))
                            .description(getString(R.string.ContextMenuElements_ReplyHidden))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextReplyPrivateChat)
                            .title(getString(R.string.CustomF_ReplyPvt))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextClearFromCache)
                            .title(getString(R.string.ClearFromCache))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextCopyPhoto)
                            .title(getString(R.string.CopyPhoto))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextSaveMessage)
                            .title(getString(R.string.AddToSavedMessages))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextReportMessage)
                            .title(getString(R.string.ReportChat))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextMessageDetails)
                            .title(getString(R.string.MessageDetails))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviews.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextNoQuoteForward)
                            .title(getString(R.string.NoQuoteForward))
                            .build());


                })
                .build();
    }
}
