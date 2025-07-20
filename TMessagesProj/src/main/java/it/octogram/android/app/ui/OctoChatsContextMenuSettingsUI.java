/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.ContextMenuBriefingState;
import it.octogram.android.OctoConfig;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.ui.cells.ChatSettingsPreviewsCell;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.deeplink.DeepLinkDef;

public class OctoChatsContextMenuSettingsUI implements PreferencesEntry {
    private ChatSettingsPreviewsCell chatSettingsPreviewsCell;

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
                                AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10);
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
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextMenuBottomShortcuts)
                            .title(getString(R.string.ContextMenuBriefingState_Shortcuts_Bottom))
                            .showIf(isShortcutsSelected)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextMenuShortcutsTitles)
                            .title(getString(R.string.ContextMenuBriefingState_Shortcuts_Title))
                            .showIf(isShortcutsSelected)
                            .build());
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(chatSettingsPreviewsCell = new ChatSettingsPreviewsCell(context, ChatSettingsPreviewsCell.PreviewType.CONTEXT_MENU))
                            .build()
                    );
                })
                .category(R.string.ContextMenuElements, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextReplyMessage)
                            .title(getString(R.string.MessageOptionsReplyTitle))
                            .description(getString(R.string.ContextMenuElements_ReplyHidden))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextReplyPrivateChat)
                            .title(getString(R.string.CustomF_ReplyPvt))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextClearFromCache)
                            .title(getString(R.string.ClearFromCache))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextCopyPhoto)
                            .title(getString(R.string.CopyPhoto))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextSaveMessage)
                            .title(getString(R.string.AddToSavedMessages))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextReportMessage)
                            .title(getString(R.string.ReportChat))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextMessageDetails)
                            .title(getString(R.string.MessageDetails))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> chatSettingsPreviewsCell.invalidate(), 10))
                            .preferenceValue(OctoConfig.INSTANCE.contextNoQuoteForward)
                            .title(getString(R.string.NoQuoteForward))
                            .build());


                })
                .build();
    }
}
