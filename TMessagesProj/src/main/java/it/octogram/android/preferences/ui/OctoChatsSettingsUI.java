/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import java.util.List;

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.ConfigProperty;
import it.octogram.android.ExpandableRowsIds;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerShape;
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
import it.octogram.android.utils.ExpandableRowsOption;
import it.octogram.android.utils.PopupChoiceDialogOption;

/** @noinspection deprecation*/
public class OctoChatsSettingsUI implements PreferencesEntry {
    private StickerSizeCell stickerSizeCell;
    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        int centeredTitleState = OctoConfig.INSTANCE.uiTitleCenteredState.getValue();
        ConfigProperty<Boolean> isTitleUncentered = new ConfigProperty<>(null, centeredTitleState != ActionBarCenteredTitle.JUST_IN_CHATS.getValue() && centeredTitleState != ActionBarCenteredTitle.ALWAYS.getValue());

        return OctoPreferences.builder(LocaleController.formatString(R.string.ChatTitle))
                //.sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.APPEARANCE, true, LocaleController.formatString("OctoAppearanceSettingsHeader", R.string.OctoAppearanceSettingsHeader))
                .category(LocaleController.getString(R.string.Chats), category -> category.row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(stickerSizeCell = new StickerSizeCell(context))
                        .build()
                ))
                .category(LocaleController.getString(R.string.StickersSizeHeader), category -> {
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(2)
                            .max(20)
                            .onSelected(() -> stickerSizeCell.invalidatePreviewMessages())
                            .preferenceValue(OctoConfig.INSTANCE.maxStickerSize)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.stickerShape)
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(StickerShape.DEFAULT.getValue()).setItemTitle(LocaleController.getString(R.string.StyleTypeDefault)),
                                    new PopupChoiceDialogOption().setId(StickerShape.ROUND.getValue()).setItemTitle(LocaleController.getString(R.string.StickerShapeRounded)),
                                    new PopupChoiceDialogOption().setId(StickerShape.MESSAGE.getValue()).setItemTitle(LocaleController.getString(R.string.StyleTypeMessage))
                            ))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .title(LocaleController.formatString(R.string.StickerShape))
                            .build());
                })
                .category(LocaleController.getString(R.string.BehaviorsHeader), category -> {
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.REPLIES_AND_LINKS.getId())
                            .setIcon(R.drawable.menu_reply)
                            .setMainTitle(LocaleController.formatString(R.string.RepliesLinksHeader))
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(LocaleController.getString(R.string.RepliesLinksShowColors))
                                    .setProperty(OctoConfig.INSTANCE.repliesLinksShowColors)
                                    .setOnClick(() -> stickerSizeCell.invalidatePreviewMessages())
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(LocaleController.getString(R.string.RepliesLinksShowEmojis))
                                    .setProperty(OctoConfig.INSTANCE.repliesLinksShowEmojis)
                                    .setOnClick(() -> stickerSizeCell.invalidatePreviewMessages())
                            )
                            .build()
                    );
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.PROMPT_BEFORE_SENDING.getId())
                            .setIcon(R.drawable.msg_send)
                            .setMainTitle(LocaleController.formatString(R.string.PromptBeforeSending))
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(LocaleController.getString(R.string.PromptBeforeSendingStickers))
                                    .setProperty(OctoConfig.INSTANCE.promptBeforeSendingStickers)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(LocaleController.getString(R.string.PromptBeforeSendingGIFs))
                                    .setProperty(OctoConfig.INSTANCE.promptBeforeSendingGIFs)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(LocaleController.getString(R.string.PromptBeforeSendingVoiceMessages))
                                    .setProperty(OctoConfig.INSTANCE.promptBeforeSendingVoiceMessages)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(LocaleController.getString(R.string.PromptBeforeSendingVideoMessages))
                                    .setProperty(OctoConfig.INSTANCE.promptBeforeSendingVideoMessages)
                            )
                            .build()
                    );
                })
                .category(LocaleController.formatString(R.string.FormattingHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.invalidatePreviewMessages())
                            .preferenceValue(OctoConfig.INSTANCE.hideSentTimeOnStickers)
                            .title(LocaleController.formatString(R.string.RemoveTimeOnStickers))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                LocaleController.getInstance().recreateFormatters();
                                stickerSizeCell.invalidatePreviewMessages();
                            })
                            .preferenceValue(OctoConfig.INSTANCE.formatTimeWithSeconds)
                            .title(LocaleController.formatString(R.string.FormatTimeWithSeconds))
                            .description(LocaleController.formatString(R.string.FormatTimeWithSeconds_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.reloadActionBar())
                            .preferenceValue(OctoConfig.INSTANCE.numberRounding)
                            .title(LocaleController.formatString(R.string.NumberRounding))
                            .description(LocaleController.formatString(R.string.NumberRounding_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.invalidatePreviewMessages())
                            .preferenceValue(OctoConfig.INSTANCE.pencilIconForEditedMessages)
                            .title(LocaleController.formatString(R.string.PencilIconForEdited))
                            .description(LocaleController.formatString(R.string.PencilIconForEdited_Desc))
                            .build());
                })
                .category(LocaleController.getString(R.string.HeaderHeader), isTitleUncentered, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.reloadActionBar())
                            .preferenceValue(OctoConfig.INSTANCE.searchIconInHeader)
                            .title(LocaleController.getString(R.string.SearchIconInHeader))
                            .showIf(isTitleUncentered)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickerSizeCell.reloadActionBar())
                            .preferenceValue(OctoConfig.INSTANCE.slidingTitle)
                            .title(LocaleController.getString(R.string.SlidingTitle))
                            .description(LocaleController.getString(R.string.SlidingTitle_Desc))
                            .showIf(isTitleUncentered)
                            .build());
                })
                .category(LocaleController.formatString(R.string.BlurHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .title(LocaleController.getString(R.string.ForceChatBlurEffect))
                            .build());
                    category.row(new HeaderRow(LocaleController.getString(R.string.ForceChatBlurEffectName), OctoConfig.INSTANCE.forceChatBlurEffect));
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(0)
                            .max(255)
                            .preferenceValue(OctoConfig.INSTANCE.blurEffectStrength)
                            .showIf(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.formatString(R.string.AppearanceHiddenOptions))
                        .showIf(isTitleUncentered, true)
                        .build())
                .build();
    }
}
