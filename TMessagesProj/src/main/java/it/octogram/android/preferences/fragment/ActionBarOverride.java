package it.octogram.android.preferences.fragment;

import android.annotation.SuppressLint;
import android.content.Context;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.CallLogActivity;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.UsersSelectActivity;

import java.util.List;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.FingerprintUtils;
import it.octogram.android.utils.TelegramSettingsHelper;

@SuppressLint("ViewConstructor")
public class ActionBarOverride extends ActionBarLayout {
    private long lastUnlockedAccount = 0;
    private BlockingAccountDialog accountView;
    private BlockingAccountDialog pageView;

    public ActionBarOverride(Context context, boolean main) {
        super(context, main);
    }

    @Override
    public boolean presentFragment(NavigationParams params) {
        BaseFragment fragment = params.fragment;

        boolean mustRequireFingerprint = false;
        boolean ignoreAskEvery = false;
        if (fragment instanceof CallLogActivity && OctoConfig.INSTANCE.biometricOpenCallsLog.getValue() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = true;
        } else if (fragment instanceof DialogsActivity f2 && FingerprintUtils.hasFingerprint() && f2.getArguments() != null) {
            if (Objects.equals(f2.getArguments().getInt("folderId", -1), 1)) {
                mustRequireFingerprint = OctoConfig.INSTANCE.biometricOpenArchive.getValue();
            } else if (Objects.equals(f2.getArguments().getInt("dialogsType", -1), DialogsActivity.DIALOGS_TYPE_HIDDEN_CHATS)) {
                mustRequireFingerprint = true;
            }
        } else if (fragment instanceof ChatActivity f2 && FingerprintUtils.hasFingerprint() && f2.getArguments() != null) {
            if (f2.getArguments().containsKey("enc_id")) {
                mustRequireFingerprint = OctoConfig.INSTANCE.biometricOpenSecretChats.getValue();
            } else {
                boolean isCurrentChatLocked = FingerprintUtils.isChatLocked(f2.getArguments());
                if (isCurrentChatLocked && needToUnlockAfterDialogsActivity(f2)) {
                    mustRequireFingerprint = true;
                }
            }
        } else if (fragment instanceof PreferencesFragment f2 && f2.isLockedContent() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = needToUnlockAfterDialogsActivity(f2);
        } else if (fragment instanceof UsersSelectActivity f2 && f2.isLockedContent() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = true;
            ignoreAskEvery = true;
        } else if (fragment instanceof BaseFragment f2 && TelegramSettingsHelper.isSettingsPage(f2) && OctoConfig.INSTANCE.biometricOpenSettings.getValue() && FingerprintUtils.hasFingerprintCached()) {
            mustRequireFingerprint = needToUnlockAfterSettingsActivity(f2);
        }

        if (mustRequireFingerprint) {
            if (OctoConfig.INSTANCE.advancedBiometricUnlock.getValue()) {
                if (pageView == null || !pageView.isShowing()) {
                    getBlockingPageDialog(fragment);
                    pageView.show();
                }
                return super.presentFragment(params);
            }

            FingerprintUtils.checkFingerprint(ApplicationLoader.applicationContext, FingerprintUtils.FingerprintAction.OPEN_PAGE, ignoreAskEvery, () -> ActionBarOverride.super.presentFragment(params));

            return false;
        } else {
            handleAccountState();
        }

        return super.presentFragment(params);
    }

    @Override
    public boolean presentFragment(BaseFragment fragment) {
        handleAccountState();
        return presentFragment(new NavigationParams(fragment));
    }

    @Override
    public boolean presentFragment(BaseFragment fragment, boolean removeLast) {
        handleAccountState();
        return presentFragment(new NavigationParams(fragment).setRemoveLast(removeLast));
    }

    @Override
    public boolean presentFragmentAsPreview(BaseFragment fragment) {
        handleAccountState();
        return presentFragment(new NavigationParams(fragment).setPreview(true));
    }

    @Override
    public boolean addFragmentToStack(BaseFragment fragment) {
        handleAccountState();
        return super.addFragmentToStack(fragment);
    }

    @Override
    public boolean addFragmentToStack(BaseFragment fragment, int position) {
        handleAccountState();
        return super.addFragmentToStack(fragment, position);
    }

    public void lock() {
        lastUnlockedAccount = -1;
        handleAccountState();
    }

    public void unlock(long id) {
        lastUnlockedAccount = id;
        if (accountView != null) {
            accountView.onForcedDismiss();
            accountView = null;
        }
    }

    public void handleAccountState() {
        long currentId = UserConfig.getInstance(UserConfig.selectedAccount).clientUserId;
        if (FingerprintUtils.hasLockedAccounts() && FingerprintUtils.hasFingerprintCached() && FingerprintUtils.isAccountLocked(currentId)) {
            if ((accountView == null || !accountView.isShowing()) && lastUnlockedAccount != currentId) {
                getBlockingAccountDialog(currentId);
                accountView.show();
            }
        } else {
            unlock(currentId);
        }
    }

    private void getBlockingAccountDialog(long currentId, BaseFragment relatedPage) {
        BlockingAccountDialog view = new BlockingAccountDialog(LaunchActivity.instance, relatedPage != null);
        view.setDelegate(new BlockingAccountView.BlockingViewDelegate() {
            @Override
            public void onUnlock() {
                destroy(true);
                if (relatedPage == null) {
                    lastUnlockedAccount = currentId;
                }
            }

            public void destroy(boolean authorized) {
                if ((relatedPage == null && accountView == null) || (relatedPage != null && pageView == null)) {
                    return;
                }

                if (relatedPage != null && !authorized) {
                    LaunchActivity.instance.getActionBarLayout().removeFragmentFromStack(relatedPage, true);
                    AndroidUtilities.runOnUIThread(pageView::onForcedDismiss, 150);
                } else {
                    (relatedPage != null ? pageView : accountView).onForcedDismiss();
                }

                accountView = null;
                pageView = null;
            }

            @Override
            public void destroy() {
                destroy(false);
            }
        });

        if (relatedPage != null) {
            pageView = view;
        } else {
            accountView = view;
        }
    }

    private void getBlockingAccountDialog(long currentId) {
        getBlockingAccountDialog(currentId, null);
    }

    private void getBlockingPageDialog(BaseFragment page) {
        getBlockingAccountDialog(0, page);
    }

    private static boolean needToUnlockAfterDialogsActivity(BaseFragment ignored) {
        List<BaseFragment> lastFragment = LaunchActivity.instance.getActionBarLayout().getFragmentStack();
        for (BaseFragment f : lastFragment) {
            if (f != ignored && f instanceof DialogsActivity f3 && f3.getArguments() != null && Objects.equals(f3.getArguments().getInt("dialogsType", -1), DialogsActivity.DIALOGS_TYPE_HIDDEN_CHATS)) {
                return false;
            }
        }
        return true;
    }

    private static boolean needToUnlockAfterSettingsActivity(BaseFragment ignored) {
        List<BaseFragment> lastFragment = LaunchActivity.instance.getActionBarLayout().getFragmentStack();
        for (BaseFragment f : lastFragment) {
            if (f != ignored && TelegramSettingsHelper.isSettingsPage(f)) {
                return false;
            }
        }
        return true;
    }
}
