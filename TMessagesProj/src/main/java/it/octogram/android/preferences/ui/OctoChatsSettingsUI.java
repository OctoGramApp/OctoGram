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

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import java.util.List;

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.ConfigProperty;
import it.octogram.android.ExpandableRowsIds;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerShape;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.ui.components.StickerSizeCell;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.config.ExpandableRowsOption;

public class OctoChatsSettingsUI implements PreferencesEntry {
    private StickerSizeCell stickerSizeCell;
    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        int centeredTitleState = OctoConfig.INSTANCE.uiTitleCenteredState.getValue();
        ConfigProperty<Boolean> isTitleUncentered = new ConfigProperty<>(null, centeredTitleState != ActionBarCenteredTitle.JUST_IN_CHATS.getValue() && centeredTitleState != ActionBarCenteredTitle.ALWAYS.getValue());

        return OctoPreferences.builder(getString(R.string.ChatTitle))
                .deepLink(DeepLinkDef.APPEARANCE_CHAT)
                .category(getString(R.string.Chats), category -> category.row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(stickerSizeCell = new StickerSizeCell(context))
                        .build()
                ))
                .category(getString(R.string.StickersSizeHeader), category -> {
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(2)
                            .max(20)
                            .onSelected(() -> stickerSizeCell.invalidatePreviewMessages())
                            .preferenceValue(OctoConfig.INSTANCE.maxStickerSize)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.stickerShape)
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(StickerShape.DEFAULT.getValue()).setItemTitle(getString(R.string.StyleTypeDefault)),
                                    new PopupChoiceDialogOption().setId(StickerShape.ROUND.getValue()).setItemTitle(getString(R.string.StickerShapeRounded)),
                                    new PopupChoiceDialogOption().setId(StickerShape.MESSAGE.getValue()).setItemTitle(getString(R.string.StyleTypeMessage))
                            ))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .title(getString(R.string.StickerShape))
                            .build());
                })
                .category(getString(R.string.BehaviorsHeader), category -> {
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.REPLIES_AND_LINKS.getId())
                            .setIcon(R.drawable.menu_reply)
                            .setMainTitle(getString(R.string.RepliesLinksHeader))
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.RepliesLinksShowColors))
                                    .property(OctoConfig.INSTANCE.repliesLinksShowColors)
                                    .onPostUpdate(() -> stickerSizeCell.invalidatePreviewMessages())
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.RepliesLinksShowEmojis))
                                    .property(OctoConfig.INSTANCE.repliesLinksShowEmojis)
                                    .onPostUpdate(() -> stickerSizeCell.invalidatePreviewMessages())
                            )
                            .build()
                    );
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.PROMPT_BEFORE_SENDING.getId())
                            .setIcon(R.drawable.msg_send)
                            .setMainTitle(getString(R.string.PromptBeforeSending))
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingStickers))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingStickers)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingGIFs))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingGIFs)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingVoiceMessages))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingVoiceMessages)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingVideoMessages))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingVideoMessages)
                            )
                            .build()
                    );
                })
                .category(getString(R.string.FormattingHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.invalidatePreviewMessages())
                            .preferenceValue(OctoConfig.INSTANCE.hideSentTimeOnStickers)
                            .title(getString(R.string.RemoveTimeOnStickers))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                LocaleController.getInstance().recreateFormatters();
                                stickerSizeCell.invalidatePreviewMessages();
                            })
                            .preferenceValue(OctoConfig.INSTANCE.formatTimeWithSeconds)
                            .title(getString(R.string.FormatTimeWithSeconds))
                            .description(getString(R.string.FormatTimeWithSeconds_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.reloadActionBar())
                            .preferenceValue(OctoConfig.INSTANCE.numberRounding)
                            .title(getString(R.string.NumberRounding))
                            .description(getString(R.string.NumberRounding_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.invalidatePreviewMessages())
                            .preferenceValue(OctoConfig.INSTANCE.pencilIconForEditedMessages)
                            .title(getString(R.string.PencilIconForEdited))
                            .description(getString(R.string.PencilIconForEdited_Desc))
                            .build());
                })
                .category(getString(R.string.HeaderHeader), isTitleUncentered, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.reloadActionBar())
                            .preferenceValue(OctoConfig.INSTANCE.searchIconInHeader)
                            .title(getString(R.string.SearchIconInHeader))
                            .showIf(isTitleUncentered)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.reloadActionBar())
                            .preferenceValue(OctoConfig.INSTANCE.slidingTitle)
                            .title(getString(R.string.SlidingTitle))
                            .description(getString(R.string.SlidingTitle_Desc))
                            .showIf(isTitleUncentered)
                            .build());
                })
                .category(getString(R.string.BlurHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .title(getString(R.string.ForceChatBlurEffect))
                            .build());
                    category.row(new HeaderRow(getString(R.string.ForceChatBlurEffectName), OctoConfig.INSTANCE.forceChatBlurEffect));
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(0)
                            .max(255)
                            .preferenceValue(OctoConfig.INSTANCE.blurEffectStrength)
                            .showIf(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AppearanceHiddenOptions))
                        .showIf(isTitleUncentered, true)
                        .build())
                .build();
    }
}
