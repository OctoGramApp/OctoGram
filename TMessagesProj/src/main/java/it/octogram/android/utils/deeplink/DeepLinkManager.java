/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.deeplink;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.HapticFeedbackConstants;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionIntroActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AppIconBulletinLayout;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.DataSettingsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.LauncherIconController;
import org.telegram.ui.LiteModeSettingsActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.ProxyListActivity;
import org.telegram.ui.SessionsActivity;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.ui.DcStatusActivity;
import it.octogram.android.app.ui.OctoAppearanceDrawerSettingsUI;
import it.octogram.android.app.ui.OctoAppearanceUI;
import it.octogram.android.app.ui.OctoCameraSettingsUI;
import it.octogram.android.app.ui.OctoChatsAiFeaturesUI;
import it.octogram.android.app.ui.OctoChatsAiProvidersUI;
import it.octogram.android.app.ui.OctoChatsContextMenuSettingsUI;
import it.octogram.android.app.ui.OctoChatsPinnedEmojisActivity;
import it.octogram.android.app.ui.OctoChatsPinnedHashtagsActivity;
import it.octogram.android.app.ui.OctoChatsPinnedReactionsActivity;
import it.octogram.android.app.ui.OctoChatsTranslatorUI;
import it.octogram.android.app.ui.OctoChatsUI;
import it.octogram.android.app.ui.OctoExperimentsNavigationUI;
import it.octogram.android.app.ui.OctoExperimentsUI;
import it.octogram.android.app.ui.OctoGeneralSettingsUI;
import it.octogram.android.app.ui.OctoInfoSettingsUI;
import it.octogram.android.app.ui.OctoLogsActivity;
import it.octogram.android.app.ui.OctoMainSettingsUI;
import it.octogram.android.app.ui.OctoPrivacySettingsUI;
import it.octogram.android.crashlytics.CrashViewType;
import it.octogram.android.crashlytics.Crashlytics;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.network.BrowserUtils;

/**
 * Manages deep links and navigation actions within the application.
 * <p>
 * This class handles deep links received by the application and performs actions
 * based on the link content. It supports various deep links for accessing
 * specific features, settings, or user profiles.
 * <p>
 * It also handles navigation actions triggered from the main menu,
 * directing the user to the appropriate fragments or activities.
 * Additionally, it manages the unlocking and display of new app icons
 * based on specific deep links.
 *
 * @noinspection deprecation, SequencedCollectionMethodCanBeUsed
 */
public class DeepLinkManager extends LaunchActivity {
    static final String TAG = "DeepLinkManager";
    static long profileUserId = 0;

    /**
     * Handles deep links received by the application.
     * <p>
     * This method processes deep links and performs actions based on the link content.
     * It supports various deep links for accessing specific features, settings, or user profiles.
     *
     * @param deepLink The deep link URL to handle.
     */
    public static boolean handleDeepLink(String deepLink) {
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.d(TAG, "handleDeepLink: " + deepLink);
        }

        Uri uri;
        String parameter = null;
        try {
            uri = Utilities.uriParseSafe(deepLink);
            if (uri != null) {
                parameter = uri.getQueryParameter("t");
            }
        } catch (UnsupportedOperationException ignored) {
            return false;
        }

        var fragment = getCurrentFragment();
        var deepLinkType = getDeepLinkType(uri);
        if (fragment == null) return false;
        if (deepLinkType == null) return false;
        if (!deepLinkType.equals(DeepLinkDef.USER)) {
            profileUserId = 0;
        }
        switch (deepLinkType) {
            case DeepLinkDef.FOX -> {
                handleIconUnlock(fragment, OctoConfig.INSTANCE.unlockedFoxIcon, LauncherIconController.LauncherIcon.FOXGRAM, R.string.UnlockedHiddenFoxIcon);
                return true;
            }
            case DeepLinkDef.CHUPAGRAM -> {
                handleIconUnlock(fragment, OctoConfig.INSTANCE.unlockedChupa, LauncherIconController.LauncherIcon.CHUPA, R.string.UnlockedHiddenChupaIcon);
                return true;
            }
            case DeepLinkDef.YUKIGRAM -> {
                handleIconUnlock(fragment, OctoConfig.INSTANCE.unlockedYuki, LauncherIconController.LauncherIcon.YUKI, R.string.UnlockedHiddenYukiIcon);
                return true;
            }
            case DeepLinkDef.EXPERIMENTAL -> {
                fragment.presentFragment(new PreferencesFragment(new OctoExperimentsUI(), parameter));
                return true;
            }
            case DeepLinkDef.EXPERIMENTAL_NAVIGATION -> {
                fragment.presentFragment(new OctoExperimentsNavigationUI());
                return true;
            }
            case DeepLinkDef.CAMERA -> {
                fragment.presentFragment(new PreferencesFragment(new OctoCameraSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.GENERAL -> {
                fragment.presentFragment(new PreferencesFragment(new OctoGeneralSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.OCTOSETTINGS -> {
                fragment.presentFragment(new PreferencesFragment(new OctoMainSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.CHATS -> {
                fragment.presentFragment(new PreferencesFragment(new OctoChatsUI(), parameter));
                return true;
            }
            case DeepLinkDef.CHATS_CONTEXTMENU -> {
                fragment.presentFragment(new PreferencesFragment(new OctoChatsContextMenuSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.APPEARANCE -> {
                fragment.presentFragment(new PreferencesFragment(new OctoAppearanceUI(), parameter));
                return true;
            }
            case DeepLinkDef.APPEARANCE_APP -> {
                //fragment.presentFragment(new PreferencesFragment(new OctoInterfaceSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.APPEARANCE_CHAT -> {
                //fragment.presentFragment(new PreferencesFragment(new OctoChatsSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.APPEARANCE_DRAWER -> {
                fragment.presentFragment(new PreferencesFragment(new OctoAppearanceDrawerSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.TRANSLATOR -> {
                fragment.presentFragment(new PreferencesFragment(new OctoChatsTranslatorUI(), parameter));
                return true;
            }
            case DeepLinkDef.AI_FEATURES -> {
                fragment.presentFragment(new PreferencesFragment(new OctoChatsAiFeaturesUI(), parameter));
                return true;
            }
            case DeepLinkDef.AI_FEATURES_PROVIDERS -> {
                fragment.presentFragment(new PreferencesFragment(new OctoChatsAiProvidersUI(), parameter));
                return true;
            }
            case DeepLinkDef.INFO -> {
                fragment.presentFragment(new PreferencesFragment(new OctoInfoSettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.PRIVACY -> {
                fragment.presentFragment(new PreferencesFragment(new OctoPrivacySettingsUI(), parameter));
                return true;
            }
            case DeepLinkDef.PRIVACY_CHATS -> {
                fragment.presentFragment(new PreferencesFragment(new OctoPrivacySettingsUI(), "lockedChats"));
                return true;
            }
            case DeepLinkDef.UPDATE -> {
                handleUpdateChecker();
                return true;
            }
            case DeepLinkDef.USER -> {
                handleUserDeepLink(deepLink);
                return true;
            }
            case DeepLinkDef.FRANCESCO -> {
                handleFrancesco(fragment);
                return true;
            }
            case DeepLinkDef.XIMI -> {
                handleXimi(fragment);
                return true;
            }
            case DeepLinkDef.PINNED_EMOJIS -> {
                fragment.presentFragment(new OctoChatsPinnedEmojisActivity());
                return true;
            }
            case DeepLinkDef.PINNED_REACTIONS -> {
                fragment.presentFragment(new OctoChatsPinnedReactionsActivity());
                return true;
            }
            case DeepLinkDef.PINNED_HASHTAGS -> {
                fragment.presentFragment(new OctoChatsPinnedHashtagsActivity());
                return true;
            }
            case DeepLinkDef.DC_STATUS -> {
                DcStatusActivity activity = new DcStatusActivity();
                if (parameter != null) {
                    activity.handleParameter(parameter);
                }
                fragment.presentFragment(activity);
                return true;
            }
            case DeepLinkDef.OCTO_CRASH_LOGS -> {
                fragment.presentFragment(new OctoLogsActivity(CrashViewType.CRASH_LOGS));
                return true;
            }
            case DeepLinkDef.OCTO_LOGS -> {
                if (BuildVars.DEBUG_PRIVATE_VERSION) {
                    fragment.presentFragment(new OctoLogsActivity(CrashViewType.DEBUG_LOGS));
                } else {
                    BulletinFactory.of(fragment).createSimpleBulletin(R.raw.error, "Debug logs are not available in production builds.").show();
                }
                return true;
            }
            case DeepLinkDef.COPY_REPORT_DETAILS -> {
                if (BuildVars.DEBUG_PRIVATE_VERSION) {
                    String reportDetails = null;
                    try {
                        reportDetails = Crashlytics.getSystemInfo(false, null);
                    } catch (IllegalAccessException e) {
                        OctoLogging.e(TAG, "Failed to get system info for report details", e);
                    }
                    AndroidUtilities.addToClipboard(reportDetails);
                    BulletinFactory.of(fragment).createSimpleBulletin(R.raw.info, "Report details copied to clipboard.").show();
                }
                return true;
            }
        }
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.d(TAG, "Deep link not recognized: " + deepLink);
        }
        profileUserId = 0;
        return false;
    }

    /**
     * Handles the Francesco deep link action.
     * <p>
     * This method toggles the "useTranslationsArgsFix" configuration property
     * and displays a bulletin to inform the user about the change.
     *
     * @param fragment The fragment associated with the Francesco deep link event.
     */
    static void handleFrancesco(BaseFragment fragment) {
        var bulletinText = "";
        if (OctoConfig.INSTANCE.useTranslationsArgsFix.getValue()) {
            bulletinText = "Broken name fix has been disabled.";
        } else {
            bulletinText = "Broken name fix has been enabled.";
        }
        BulletinFactory.of(fragment).createSimpleBulletin(R.raw.info, bulletinText).show();
        OctoConfig.INSTANCE.useTranslationsArgsFix.updateValue(!OctoConfig.INSTANCE.useTranslationsArgsFix.getValue());
    }

    /**
     * Handles the Ximi deep link action.
     * <p>
     * This method toggles the "forceHideLockScreenPopup" configuration property
     * and displays a bulletin to inform the user about the change.
     *
     * @param fragment The fragment associated with the Ximi deep link event.
     */
    static void handleXimi(BaseFragment fragment) {
        final String bulletinText = OctoConfig.INSTANCE.forceHideLockScreenPopup.getValue()
                ? "Force hide lock screen popup has been disabled."
                : "Force hide lock screen popup has been enabled.";

        BulletinFactory.of(fragment)
                .createSimpleBulletin(R.raw.info, bulletinText)
                .show();

        OctoConfig.INSTANCE.forceHideLockScreenPopup.updateValue(
                !OctoConfig.INSTANCE.forceHideLockScreenPopup.getValue()
        );
    }

    /**
     * Handles the update checker action.
     * <p>
     * This method triggers the app update checker to verify if a new version is available.
     * If a new version is found, the user is prompted to update the app.
     */
    static void handleUpdateChecker() {
        if (LaunchActivity.instance != null) {
            LaunchActivity.instance.checkAppUpdate(true, null);
        }
    }

    /**
     * Handles the unlocking and display of a new app icon.
     * <p>
     * This method checks if the specified icon has been unlocked previously. If not, it:
     * - Creates a bulletin (notification) to inform the user about the unlocked icon.
     * - Triggers haptic feedback to provide tactile confirmation.
     * - Starts a fireworks animation (if available in the current activity).
     * - Displays the bulletin for a short duration.
     * - Updates the icon's unlocked status to true.
     *
     * @param fragment      The fragment associated with the icon unlock event.
     * @param configValue   The configuration property representing the icon's unlocked state.
     * @param icon          The LauncherIcon object representing the unlocked icon.
     * @param iconStringRes The resource ID of the string to display in the unlock notification.
     */
    static void handleIconUnlock(BaseFragment fragment, ConfigProperty<Boolean> configValue, LauncherIconController.LauncherIcon icon, int iconStringRes) {
        if (LaunchActivity.instance != null) {
            if (!configValue.getValue()) {
                AppIconBulletinLayout layout = new AppIconBulletinLayout(fragment.getParentActivity(), icon, null);
                layout.textView.setText(LocaleController.getString(iconStringRes));
                layout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                LaunchActivity.instance.getFireworksOverlay().start();
                Bulletin.make(fragment, layout, Bulletin.DURATION_SHORT).show();
                configValue.updateValue(true);
            }
        }
    }

    /**
     * Handles a deep link that refers to a Telegram user.
     * <p>
     * This method extracts the user ID from the deep link and stores it in the `profileUserId` variable.
     * <p>
     * The deep link is expected to be in one of the following formats:
     * - `tg:user?id=<user_id>`
     * - `tg://user?id=<user_id>`
     * <p>
     * The method replaces the `tg:user` and `tg://user` prefixes with `tg://telegram.org`
     * to ensure the deep link is parsed correctly.
     * <p>
     * If the deep link contains a valid user ID, it is parsed and stored in `profileUserId`.
     * If the user ID is invalid (e.g., not a number), an error is logged.
     *
     * @param deepLink The deep link string to handle.
     */
    static void handleUserDeepLink(String deepLink) {
        deepLink = deepLink.replace("tg:user", "tg://telegram.org").replace("tg://user", "tg://telegram.org");
        Uri data = Utilities.uriParseSafe(deepLink);
        if (data != null) {
            String userId = data.getQueryParameter("id");

            if (userId != null) {
                try {
                    profileUserId = Long.parseLong(userId);
                } catch (NumberFormatException ignore) {
                    if (BuildVars.DEBUG_PRIVATE_VERSION) {
                        OctoLogging.d(TAG, "Invalid user ID in deep link: " + userId);
                    }
                }
            }
        }
    }

    /**
     * Determines the deep link type based on the provided deep link URI.
     * <p>
     * This method checks the deep link against a predefined list of deep link patterns
     * and returns the corresponding deep link type defined in {@link DeepLinkDef}.
     * If the deep link does not match any known pattern, it returns null.
     *
     * @param uri The deep link URI to analyze.
     * @return The deep link type as a String, or null if the deep link is not recognized.
     */
    @DeepLinkType
    static String getDeepLinkType(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (!"tg".equalsIgnoreCase(scheme)) {
            return null;
        }
        String authority = uri.getAuthority();
        if (authority == null) {
            return null;
        }
        authority = authority.toLowerCase();
        return switch (authority) {
            case "francesco" -> DeepLinkDef.FRANCESCO;
            case "ximi" -> DeepLinkDef.XIMI;
            case "fox" -> DeepLinkDef.FOX;
            case "chupagram" -> DeepLinkDef.CHUPAGRAM;
            case "yukigram" -> DeepLinkDef.YUKIGRAM;
            case "experimental" -> {
                if (uri.getPath() != null) {
                    if (uri.getPath().equalsIgnoreCase("/navigation"))
                        yield DeepLinkDef.EXPERIMENTAL_NAVIGATION;
                }
                yield DeepLinkDef.EXPERIMENTAL;
            }
            case "camera" -> DeepLinkDef.CAMERA;
            case "general" -> DeepLinkDef.GENERAL;
            case "octosettings" -> DeepLinkDef.OCTOSETTINGS;
            case "chats" -> {
                if (uri.getPath() != null) {
                    if (uri.getPath().equalsIgnoreCase("/cm")) yield DeepLinkDef.CHATS_CONTEXTMENU;
                }
                yield DeepLinkDef.CHATS;
            }
            case "appearance" -> {
                if (uri.getPath() != null) {
                    if (uri.getPath().equalsIgnoreCase("/app")) yield DeepLinkDef.APPEARANCE_APP;
                    if (uri.getPath().equalsIgnoreCase("/chat")) yield DeepLinkDef.APPEARANCE_CHAT;
                    if (uri.getPath().equalsIgnoreCase("/drawer"))
                        yield DeepLinkDef.APPEARANCE_DRAWER;
                }
                yield DeepLinkDef.APPEARANCE;
            }
            case "translator" -> DeepLinkDef.TRANSLATOR;
            case "update" -> DeepLinkDef.UPDATE;
            case "pinned_emojis" -> DeepLinkDef.PINNED_EMOJIS;
            case "pinned_reactions" -> DeepLinkDef.PINNED_REACTIONS;
            case "pinned_hashtags" -> DeepLinkDef.PINNED_HASHTAGS;
            case "octogram" -> DeepLinkDef.INFO;
            case "dc" -> DeepLinkDef.DC_STATUS;
            case "ai" -> {
                if (uri.getPath() != null) {
                    if (uri.getPath().equalsIgnoreCase("/providers"))
                        yield DeepLinkDef.AI_FEATURES_PROVIDERS;
                }
                yield DeepLinkDef.AI_FEATURES;
            }
            case "privacy" -> {
                if (uri.getPath() != null) {
                    if (uri.getPath().equalsIgnoreCase("/chats")) yield DeepLinkDef.PRIVACY_CHATS;
                }
                yield DeepLinkDef.PRIVACY;
            }
            case "locked_chats" -> DeepLinkDef.PRIVACY_CHATS;
            case "crashlogs" -> DeepLinkDef.OCTO_CRASH_LOGS;
            case "debuglogs" -> DeepLinkDef.OCTO_LOGS;
            case "reportdetails" -> DeepLinkDef.COPY_REPORT_DETAILS;
            default -> {
                if (authority.startsWith("user")) {
                    yield DeepLinkDef.USER;
                }
                yield null;
            }
        };
    }

    /**
     * Retrieves the currently active fragment.
     * <p>
     * This method returns the fragment at the top of the main fragments stack.
     * If the stack is empty, it returns null, indicating no active fragment.
     *
     * @return The currently active fragment, or null if no fragment is active.
     */
    static BaseFragment getCurrentFragment() {
        if (mainFragmentsStack.isEmpty()) return null;
        return mainFragmentsStack.get(mainFragmentsStack.size() - 1);
    }

    /**
     * Handles opening a user profile by their ID.
     * <p>
     * This method is triggered when a deep link or internal navigation request is made to open a specific user's profile.
     * It checks if a valid user ID is present and, if so, creates a new ProfileActivity instance with the user ID as an argument.
     * The ProfileActivity is then presented to the user, and the drawer behavior is adjusted based on whether the device is a tablet or not.
     *
     * @param launchActivity The LaunchActivity instance from which the profile should be opened.
     */
    public static void handleOpenProfileById(LaunchActivity launchActivity) {
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.d(TAG, "handleOpenProfileById: " + profileUserId);
        }
        if (profileUserId != 0) {
            Bundle args = new Bundle();
            args.putLong("user_id", profileUserId);
            var fragment = new ProfileActivity(args);
            AndroidUtilities.runOnUIThread(() -> launchActivity.presentFragment(fragment, false, false));
            if (AndroidUtilities.isTablet()) {
                launchActivity.getActionBarLayout().showLastFragment();
                launchActivity.getRightActionBarLayout().showLastFragment();
                launchActivity.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            } else {
                launchActivity.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            }
        }
    }

    public static boolean handleMenuAction(@MenuAction int id, int currentAccount, LaunchActivity launchActivity) {
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.d(TAG, "handleMenuAction: " + id);
        }
        var fragment = mainFragmentsStack.get(mainFragmentsStack.size() - 1);
        var drawerLayoutContainer = launchActivity.drawerLayoutContainer;
        var actionBarLayout = launchActivity.getActionBarLayout();
        var rightActionBarLayout = launchActivity.getRightActionBarLayout();

        switch (id) {
            case MenuActionDef.PREFERENCES_ID:
                fragment.presentFragment(new PreferencesFragment(new OctoMainSettingsUI()));
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.SEARCH_DIALOGS_ID:
                var lastFragment = LaunchActivity.getLastFragment();
                if (lastFragment instanceof DialogsActivity) {
                    DialogsActivity dialogsActivity;
                    dialogsActivity = (DialogsActivity) lastFragment;
                    dialogsActivity.showSearch(true, true, true);
                    dialogsActivity.getActionBar().openSearchField(true);
                    drawerLayoutContainer.closeDrawer(false);
                }
                return true;
            case MenuActionDef.SESSIONS_ID:
                fragment.presentFragment(new SessionsActivity(0));
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.DIALOGS_FOLDER_ID:
                var args = new Bundle();
                args.putInt("folderId", 1);
                fragment.presentFragment(new DialogsActivity(args));
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.DATACENTER_ID:
                fragment.presentFragment(new DcStatusActivity());
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.DATA_AND_STORAGE:
                fragment.presentFragment(new DataSettingsActivity());
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.AI_FEATURE:
                fragment.presentFragment(new PreferencesFragment(new OctoChatsAiFeaturesUI()));
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.QR_LOGIN_ID:
                var fg = getQrActivity(currentAccount, launchActivity);
                actionBarLayout.presentFragment(fg, false, true, true, false);
                if (AndroidUtilities.isTablet()) {
                    actionBarLayout.showLastFragment();
                    rightActionBarLayout.showLastFragment();
                    drawerLayoutContainer.setAllowOpenDrawer(false, false);
                } else {
                    drawerLayoutContainer.setAllowOpenDrawer(true, false);
                }
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.LITE_MODE_ID:
                fragment.presentFragment(new LiteModeSettingsActivity());
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.PROXY_LIST_ID:
                fragment.presentFragment(new ProxyListActivity());
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.BROWSER_HOME_ID:
                BrowserUtils.openBrowserHome(() -> drawerLayoutContainer.closeDrawer(false));
                return true;
        }
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.d(TAG, "Menu action not recognized: " + id);
        }
        return false;
    }

    /**
     * Creates and configures an ActionIntroActivity for QR code login.
     * <p>
     * This method initializes an ActionIntroActivity, sets it up for QR code login, and assigns a delegate
     * to handle the scanned QR code data. The delegate processes the login token embedded in the QR code
     * and sends an authentication request to the Telegram server.
     *
     * @param currentAccount The ID of the account to log in to.
     * @param launchActivity The parent activity launching the QR login activity.
     * @return The configured ActionIntroActivity instance for QR code login.
     */
    @NonNull
    static ActionIntroActivity getQrActivity(int currentAccount, LaunchActivity launchActivity) {
        var fg = new ActionIntroActivity(ActionIntroActivity.ACTION_TYPE_QR_LOGIN);
        fg.setQrLoginDelegate(code -> {
            AlertDialog progressDialog = new AlertDialog(launchActivity, 3);
            progressDialog.setCanCancel(false);
            progressDialog.show();
            byte[] token = Base64.decode(code.substring("tg://login?token=".length()), Base64.URL_SAFE);
            TLRPC.TL_auth_acceptLoginToken req = new TLRPC.TL_auth_acceptLoginToken();
            req.token = token;
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                try {
                    progressDialog.dismiss();
                } catch (Exception ignore) {
                }
                if (!(response instanceof TLRPC.TL_authorization)) {
                    AndroidUtilities.runOnUIThread(() -> AlertsCreator.showSimpleAlert(fg, LocaleController.getString(R.string.AuthAnotherClient), LocaleController.getString(R.string.ErrorOccurred) + "\n" + error.text));
                }
            }));
        });
        return fg;
    }
}
