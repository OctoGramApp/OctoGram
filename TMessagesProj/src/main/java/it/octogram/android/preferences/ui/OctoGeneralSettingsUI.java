/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.util.Pair;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.List;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;

public class OctoGeneralSettingsUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        return OctoPreferences.builder("General Settings")
                .sticker(context, R.raw.utyan_umbrella, true, "Here you can customize your general experience with the app, as well as privacy.")
                .category("Privacy", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hidePhoneNumber)
                            .title("Hide phone number")
                            .description("Hide your phone number from other users")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideOtherPhoneNumber)
                            .title("Hide other people's phone number")
                            .description("Hide other people's phone number from their profile")
                            .showIf(OctoConfig.INSTANCE.hidePhoneNumber)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showFakePhoneNumber)
                            .title("Show fake phone number")
                            .description("Show a fake phone number in your profile")
                            .showIf(OctoConfig.INSTANCE.hidePhoneNumber)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.promptBeforeCalling)
                            .title("Prompt before calling")
                            .description("Show a prompt to avoid accidental calls")
                            .build());
                })
                .category("Datacenter and User ID", category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.dcIdStyle)
                            .options(List.of(
                                    new Pair<>(0, "OctoGram"),
                                    new Pair<>(1, "Telegram"),
                                    new Pair<>(2, "None")
                            ))
                            .title("Style")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.registrationDateInProfiles)
                            .title("Show registration date")
                            .description("Show user's registration date in profiles. This is achieved thru guessing, so it may show inaccurate results")
                            .build());
                })
                .category("Chat", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.jumpToNextChannel)
                            .title("Jump to next channel")
                            .description("Allows to swipe up from a channel to jump to next one")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showGreetingSticker)
                            .showIf(OctoConfig.INSTANCE.jumpToNextChannel)
                            .title("Hide greeting sticker")
                            .description("Hides the greeting sticker when messaging a new contact")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.playGifAsVideo)
                            .title("Play GIFs as Video")
                            .description("Allows to play gifs as video files")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideKeyboardOnScroll)
                            .title("Hide keyboard on chat scroll")
                            .description("Hides the keyboard when scrolling in the chat")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideSendAsChannel)
                            .title(LocaleController.getString(R.string.HideSendAsChannel))
                            .description("Hides the send as channel action near the keyboard")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showOnlineStatus)
                            .title("Show online status")
                            .description("Shows the online status of other people next to their profile picture in groups")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideCustomEmojis)
                            .title("Hide custom emojis")
                            .description("Hides all custom emojis in the keyboard or as a reaction.")
                            .build());
                })
                .category("Media", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.activeNoiseSuppression)
                            .title("Noise suppression in calls")
                            .description("Enables noise suppression and voice normalization while in a call")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown)
                            .title("Unmute videos with volume down")
                            .description("Unmute a video by pressing the volume down button")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.disableProximityEvents)
                            .title("Disable proximity events")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.startWithRearCamera)
                            .title("Start with rear camera")
                            .description("Record video messages with the rear camera instead of the front camera")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.disableCameraPreview)
                            .title("Disable camera preview")
                            .description("Disables camera preview in the attachments menu")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideSentTimeOnStickers)
                            .title("Hide sent time on stickers")
                            .build());
                })
                .category("Chat folders", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideChatFolders)
                            .title("Hide all chats folders")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideFoldersWhenForwarding)
                            .title("Hide folders when forwarding")
                            .build());
                })
                .category("Notifications", category -> category.row(new SwitchRow.SwitchRowBuilder()
                        .preferenceValue(OctoConfig.INSTANCE.accentColorAsNotificationColor)
                        .title("Accent color as notification color")
                        .build()))
                .build();
    }

}
