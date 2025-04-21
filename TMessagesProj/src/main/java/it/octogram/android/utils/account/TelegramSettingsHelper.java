/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.account;

import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ProfileActivity;

import java.util.Objects;
import java.util.Set;

import it.octogram.android.preferences.fragment.PreferencesFragment;

public class TelegramSettingsHelper {
    private static final Set<String> TELEGRAM_SETTINGS_CLASSES = Set.of(
            //BUSINESS
            "GreetMessagesActivity", "OpeningHoursActivity", "BusinessLinksActivity",
            "ChatbotsActivity", "AwayMessagesActivity", "BusinessIntroActivity",
            "LocationActivity", "OpeningHoursDayActivity", "QuickRepliesActivity",
            "PremiumPreviewFragment",

            //STARS
            "StarsIntroActivity",

            //STANDARD
            "WallpapersListActivity", "DataAutoDownloadActivity", "ArchivedStickersActivity",
            "CameraScanActivity", "SessionsActivity", "QrActivity", "ProxyListActivity",
            "FiltersSetupActivity", "DataSettingsActivity", "SaveToGallerySettingsActivity",
            "LanguageSelectActivity", "TwoStepVerificationActivity", "TwoStepVerificationSetupActivity",
            "PrivacyUsersActivity", "LiteModeSettingsActivity", "PassportActivity",
            "ChangeUsernameActivity", "ThemeActivity", "ProxySettingsActivity",
            "NotificationsSettingsActivity", "DataUsage2Activity", "FilterCreateActivity",
            "PrivacyControlActivity", "PeerColorActivity", "ReactionsDoubleTapManageActivity",
            "WebBrowserSettings", "FeaturedStickersActivity", "NotificationsCustomSettingsActivity",
            "StickersActivity", "NotificationsSoundActivity", "CacheControlActivity"
    );
    
    private static final Set<String> OCTOGRAM_SETTINGS_CLASSES = Set.of(
            "CrashesActivity", "DestinationLanguageSettings", "EmojiPackSettings",
            "NavigationSettingsUI", "OctoLogsActivity", "PinnedEmojisActivity",
            "PinnedHashtagsActivity", "PinnedReactionsActivity"
    );

    public static boolean isSettingsPage(BaseFragment fragment) {
        if (fragment instanceof ProfileActivity f2 && !f2.getArguments().getBoolean("my_profile") && Objects.equals(f2.getArguments().getLong("user_id", -1), UserConfig.getInstance(UserConfig.selectedAccount).clientUserId)) {
            return true;
        }

        return isTelegramSettings(fragment) || isOctogramSettings(fragment);
    }

    public static boolean isTelegramSettings(BaseFragment fragment) {
        return TELEGRAM_SETTINGS_CLASSES.contains(fragment.getClass().getSimpleName());
    }

    public static boolean isOctogramSettings(BaseFragment fragment) {
        return fragment instanceof PreferencesFragment || OCTOGRAM_SETTINGS_CLASSES.contains(fragment.getClass().getSimpleName());
    }
}