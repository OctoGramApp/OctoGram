/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences;

import android.content.Context;
import android.util.Pair;

import org.telegram.messenger.R;

import java.util.List;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.tgkit.preference.OctoPreferences;
import it.octogram.android.preferences.tgkit.preference.types.TGKitListPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSwitchPreference;
import kotlin.Triple;

public class OctoGeneralSettings implements BasePreferencesEntry {

    @Override
    public OctoPreferences getPreferences(Context context) {
        return OctoPreferences.builder("General Settings")
                .sticker(context, R.raw.utyan_umbrella, true, "Here you can customize your general experience with the app, as well as privacy.")
                .category("Privacy", List.of(
                        new TGKitSwitchPreference("Hide phone number", "Hide your phone number from the settings and side menu", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hidePhoneNumber.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hidePhoneNumber);
                            }
                        }, true),
                        new TGKitSwitchPreference("Hide other people's phone number", "Hide other people's phone number from their profile", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideOtherPhoneNumber.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideOtherPhoneNumber);
                            }
                        }, true),
                        new TGKitSwitchPreference("Prompt before calling", "Show a prompt to avoid accidental calls", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.promptBeforeCalling.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.promptBeforeCalling);
                            }
                        }, true)
                ))
                .category("Datacenter and User ID", List.of(
                        new TGKitListPreference("Style", new TGKitListPreference.TGTLContract() {
                            @Override
                            public String getValue() {
                                return OctoConfig.dcIdStyle.getValue();
                            }

                            @Override
                            public void setValue(int id) {
                                OctoConfig.updateStringSetting(OctoConfig.dcIdStyle, getOptions().get(id).second);
                            }

                            @Override
                            public List<Pair<Integer, String>> getOptions() {
                                return List.of(
                                        new Pair<>(0, "OctoGram"),
                                        new Pair<>(1, "Telegram"),
                                        new Pair<>(2, "None")
                                );
                            }

                            @Override
                            public List<Triple<Integer, String, Integer>> getOptionsIcons() {
                                return null;
                            }

                            @Override
                            public boolean hasIcons() {
                                return false;
                            }
                        }, true),
                        new TGKitSwitchPreference("Show user's registration date", "Show user's registration date in profiles. This is achieved thru guessing, so it may show inaccurate results", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.registrationDateInProfiles.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.registrationDateInProfiles);
                            }
                        }, true)
                ))
                .category("Chat", List.of(
                        new TGKitSwitchPreference("Jump to next channel", "Allows to swipe up from a channel to jump to next one", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.jumpToNextChannel.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.jumpToNextChannel);
                            }
                        }, true),
                        new TGKitSwitchPreference("Hide greeting sticker", "Hides the greeting sticker when messaging a new contact", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideGreetingSticker.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideGreetingSticker);
                            }
                        }, true),
                        new TGKitSwitchPreference("Play GIFs as Video", "Allows to play gifs as video files", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.playGifAsVideo.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.playGifAsVideo);
                            }
                        }, true),
                        new TGKitSwitchPreference("Hide keyboard on chat scroll", "Hides the keyboard when scrolling in the chat", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideKeyboardOnScroll.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideKeyboardOnScroll);
                            }
                        }, true),
                        new TGKitSwitchPreference("Hide \"Send as channel\" ", "Hides the send as channel action near the keyboard", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideSendAsChannel.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideSendAsChannel);
                            }
                        }, true),
                        new TGKitSwitchPreference("Show online status", "Shows the online status of other people next to their profile picture in groups", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.showOnlineStatus.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.showOnlineStatus);
                            }
                        }, true),
                        new TGKitSwitchPreference("Hide custom emojis", "Hides all custom emojis in the keyboard or as a reaction.", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideCustomEmojis.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideCustomEmojis);
                            }
                        }, true)
                ))
                .category("Media", List.of(
                        new TGKitSwitchPreference("Noise suppression in calls", "Enables noise suppression and voice normalization while in a call", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.activeNoiseSuppression.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.activeNoiseSuppression);
                            }
                        }, true),
                        new TGKitSwitchPreference("Unmute videos with volume down", "Unmute a video by pressing the volume down button", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.unmuteVideosWithVolumeDown.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.unmuteVideosWithVolumeDown);
                            }
                        }, true),
                        new TGKitSwitchPreference("Disable proximity events", null, new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.disableProximityEvents.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.disableProximityEvents);
                            }
                        }, true),
                        new TGKitSwitchPreference("Start with rear camera", "Record video messages with the rear camera instead of the front camera", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.startWithRearCamera.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.startWithRearCamera);
                            }
                        }, true),
                        new TGKitSwitchPreference("Disable camera preview", "Disables camera preview in the attachments menu", new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.disableCameraPreview.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.disableCameraPreview);
                            }
                        }, true),
                        new TGKitSwitchPreference("Hide sent time on stickers", null, new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideSentTimeOnStickers.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideSentTimeOnStickers);
                            }
                        }, true)
                ))
                .category("Chat folders", List.of(
                        new TGKitSwitchPreference("Hide all chats folders", null, new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideChatFolders.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideChatFolders);
                            }
                        }, true),
                        new TGKitSwitchPreference("Hide folders when forwarding", null, new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.hideFoldersWhenForwarding.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.hideFoldersWhenForwarding);
                            }
                        }, true)
                ))
                .category("Notifications", List.of(
                        new TGKitSwitchPreference("Accent color as notification color", null, new TGKitSwitchPreference.TGSPContract() {
                            @Override
                            public boolean getPreferenceValue() {
                                return OctoConfig.accentColorAsNotificationColor.getValue();
                            }

                            @Override
                            public void toggleValue() {
                                OctoConfig.toggleBooleanSetting(OctoConfig.accentColorAsNotificationColor);
                            }
                        }, true)
                ))
                .build();
    }


}
