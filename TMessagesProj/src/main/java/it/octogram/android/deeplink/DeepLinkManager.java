/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.deeplink;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.HapticFeedbackConstants;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
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
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.ui.DatacenterActivity;
import it.octogram.android.preferences.ui.NavigationSettingsUI;
import it.octogram.android.preferences.ui.OctoAppearanceUI;
import it.octogram.android.preferences.ui.OctoCameraSettingsUI;
import it.octogram.android.preferences.ui.OctoChatsSettingsUI;
import it.octogram.android.preferences.ui.OctoDrawerSettingsUI;
import it.octogram.android.preferences.ui.OctoExperimentsUI;
import it.octogram.android.preferences.ui.OctoGeneralSettingsUI;
import it.octogram.android.preferences.ui.OctoInterfaceSettingsUI;
import it.octogram.android.preferences.ui.OctoMainSettingsUI;
import it.octogram.android.preferences.ui.PinnedEmojisActivity;
import it.octogram.android.preferences.ui.PinnedReactionsActivity;
import it.octogram.android.utils.BrowserUtils;

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
 * @noinspection deprecation
 */
public class DeepLinkManager extends LaunchActivity {
    private static final String TAG = "DeepLinkManager";
    private static long profileUserId = 0;

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
            OctoLogging.d(TAG, "handleDeepLink: " + deepLink);
        }
        var fragment = getCurrentFragment();
        var deepLinkType = getDeepLinkType(deepLink);
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
                fragment.presentFragment(new PreferencesFragment(new OctoExperimentsUI()));
                return true;
            }
            case DeepLinkDef.EXPERIMENTAL_NAVIGATION -> {
                fragment.presentFragment(new NavigationSettingsUI());
                return true;
            }
            case DeepLinkDef.CAMERA -> {
                fragment.presentFragment(new PreferencesFragment(new OctoCameraSettingsUI()));
                return true;
            }
            case DeepLinkDef.GENERAL -> {
                fragment.presentFragment(new PreferencesFragment(new OctoGeneralSettingsUI()));
                return true;
            }
            case DeepLinkDef.OCTOSETTINGS -> {
                fragment.presentFragment(new PreferencesFragment(new OctoMainSettingsUI()));
                return true;
            }
            case DeepLinkDef.APPEARANCE -> {
                fragment.presentFragment(new PreferencesFragment(new OctoAppearanceUI()));
                return true;
            }
            case DeepLinkDef.APPEARANCE_APP -> {
                fragment.presentFragment(new PreferencesFragment(new OctoInterfaceSettingsUI()));
                return true;
            }
            case DeepLinkDef.APPEARANCE_CHAT -> {
                fragment.presentFragment(new PreferencesFragment(new OctoChatsSettingsUI()));
                return true;
            }
            case DeepLinkDef.APPEARANCE_DRAWER -> {
                fragment.presentFragment(new PreferencesFragment(new OctoDrawerSettingsUI()));
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
                fragment.presentFragment(new PinnedEmojisActivity());
                return true;
            }
            case DeepLinkDef.PINNED_REACTIONS -> {
                fragment.presentFragment(new PinnedReactionsActivity());
                return true;
            }
        }
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.d(TAG, "Deep link not recognized: " + deepLink);
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
    private static void handleFrancesco(BaseFragment fragment) {
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
    private static void handleXimi(BaseFragment fragment) {
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
    private static void handleUpdateChecker() {
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
    private static void handleIconUnlock(BaseFragment fragment, ConfigProperty<Boolean> configValue, LauncherIconController.LauncherIcon icon, int iconStringRes) {
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
    private static void handleUserDeepLink(String deepLink) {
        deepLink = deepLink.replace("tg:user", "tg://telegram.org").replace("tg://user", "tg://telegram.org");
        Uri data = Uri.parse(deepLink);
        String userId = data.getQueryParameter("id");

        if (userId != null) {
            try {
                profileUserId = Long.parseLong(userId);
            } catch (NumberFormatException ignore) {
                if (BuildVars.DEBUG_PRIVATE_VERSION) {
                    OctoLogging.d(TAG, "Invalid user ID in deep link: " + userId);
                    OctoLogging.d(TAG, "Invalid user ID in deep link: " + userId);
                }
            }
        }
    }

    /**
     * Determines the deep link type based on the provided deep link string.
     * <p>
     * This method checks the deep link against a predefined list of deep link patterns
     * and returns the corresponding deep link type defined in {@link DeepLinkDef}.
     * If the deep link does not match any known pattern, it returns null.
     *
     * @param deepLink The deep link string to analyze.
     * @return The deep link type as a String, or null if the deep link is not recognized.
     */
    @DeepLinkType
    private static String getDeepLinkType(String deepLink) {
        if (deepLink.equalsIgnoreCase("tg:francesco") || deepLink.equalsIgnoreCase("tg://francesco")) {
            return DeepLinkDef.FRANCESCO;
        } else if (deepLink.equalsIgnoreCase("tg:ximi") || deepLink.equalsIgnoreCase("tg://ximi")) {
            return DeepLinkDef.XIMI;
        } else if (deepLink.equalsIgnoreCase("tg:fox") || deepLink.equalsIgnoreCase("tg://fox")) {
            return DeepLinkDef.FOX;
        } else if (deepLink.equalsIgnoreCase("tg:chupagram") || deepLink.equalsIgnoreCase("tg://chupagram")) {
            return DeepLinkDef.CHUPAGRAM;
        } else if (deepLink.equalsIgnoreCase("tg:yukigram") || deepLink.equalsIgnoreCase("tg://yukigram")) {
            return DeepLinkDef.YUKIGRAM;
        } else if (deepLink.equalsIgnoreCase("tg:experimental") || deepLink.equalsIgnoreCase("tg://experimental")) {
            return DeepLinkDef.EXPERIMENTAL;
        } else if (deepLink.equalsIgnoreCase("tg:experimental/navigation") || deepLink.equalsIgnoreCase("tg://experimental/navigation")) {
            return DeepLinkDef.EXPERIMENTAL_NAVIGATION;
        } else if (deepLink.equalsIgnoreCase("tg:camera") || deepLink.equalsIgnoreCase("tg://camera")) {
            return DeepLinkDef.CAMERA;
        } else if (deepLink.equalsIgnoreCase("tg:general") || deepLink.equalsIgnoreCase("tg://general")) {
            return DeepLinkDef.GENERAL;
        } else if (deepLink.equalsIgnoreCase("tg:octosettings") || deepLink.equalsIgnoreCase("tg://octosettings")) {
            return DeepLinkDef.OCTOSETTINGS;
        } else if (deepLink.equalsIgnoreCase("tg:appearance") || deepLink.equalsIgnoreCase("tg://appearance")) {
            return DeepLinkDef.APPEARANCE;
        } else if (deepLink.equalsIgnoreCase("tg:appearance/app") || deepLink.equalsIgnoreCase("tg://appearance/app")) {
            return DeepLinkDef.APPEARANCE_APP;
        } else if (deepLink.equalsIgnoreCase("tg:appearance/chat") || deepLink.equalsIgnoreCase("tg://appearance/chat")) {
            return DeepLinkDef.APPEARANCE_CHAT;
        } else if (deepLink.equalsIgnoreCase("tg:appearance/drawer") || deepLink.equalsIgnoreCase("tg://appearance/drawer")) {
            return DeepLinkDef.APPEARANCE_DRAWER;
        } else if (deepLink.equalsIgnoreCase("tg:update") || deepLink.equalsIgnoreCase("tg://update")) {
            return DeepLinkDef.UPDATE;
        } else if (deepLink.startsWith("tg:user") || deepLink.startsWith("tg://user")) {
            return DeepLinkDef.USER;
        } else if (deepLink.startsWith("tg:pinned_emojis") || deepLink.startsWith("tg://pinned_emojis")) {
            return DeepLinkDef.PINNED_EMOJIS;
        } else if (deepLink.startsWith("tg:pinned_reactions") || deepLink.startsWith("tg://pinned_reactions")) {
            return DeepLinkDef.PINNED_REACTIONS;
        } else {
            return null;
        }
    }

    /**
     * Retrieves the currently active fragment.
     * <p>
     * This method returns the fragment at the top of the main fragments stack.
     * If the stack is empty, it returns null, indicating no active fragment.
     *
     * @return The currently active fragment, or null if no fragment is active.
     */
    private static BaseFragment getCurrentFragment() {
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
                fragment.presentFragment(new DatacenterActivity());
                drawerLayoutContainer.closeDrawer(false);
                return true;
            case MenuActionDef.DATA_AND_STORAGE:
                fragment.presentFragment(new DataSettingsActivity());
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
    private static ActionIntroActivity getQrActivity(int currentAccount, LaunchActivity launchActivity) {
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
