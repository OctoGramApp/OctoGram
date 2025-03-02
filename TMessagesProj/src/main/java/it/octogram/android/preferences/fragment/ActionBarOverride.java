package it.octogram.android.preferences.fragment;

import android.annotation.SuppressLint;
import android.content.Context;

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

@SuppressLint("ViewConstructor")
public class ActionBarOverride extends ActionBarLayout {
    private long lastUnlockedAccount = 0;
    private BlockingAccountDialog accountView;

    public ActionBarOverride(Context context, boolean main) {
        super(context, main);
    }

    @Override
    public boolean presentFragment(NavigationParams params) {
        BaseFragment fragment = params.fragment;

        boolean mustRequireFingerprint = false;
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
            } else if (Objects.equals(f2.getArguments().getLong("user_id", -1L), UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())) {
                mustRequireFingerprint = OctoConfig.INSTANCE.biometricOpenSavedMessages.getValue();
            } else {
                boolean isCurrentChatLocked = FingerprintUtils.isChatLocked(f2.getArguments());
                if (isCurrentChatLocked && isThereVisibleChatsPage()) {
                    mustRequireFingerprint = true;
                }
            }
        } else if (fragment instanceof PreferencesFragment f2 && f2.isLockedContent() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = isThereVisibleChatsPage();
        } else if (fragment instanceof UsersSelectActivity f2 && f2.isLockedContent() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = true;
        }

        if (mustRequireFingerprint) {
            boolean[] status = {false};

            FingerprintUtils.checkFingerprint(ApplicationLoader.applicationContext, FingerprintUtils.OPEN_PAGE, new FingerprintUtils.FingerprintResult() {
                @Override
                public void onSuccess() {
                    ActionBarOverride.super.presentFragment(params);
                    status[0] = true;
                }

                @Override
                public void onFailed() {

                }
            });

            return status[0];
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

    public void unlock(long id) {
        lastUnlockedAccount = id;
        if (accountView != null) {
            accountView.onForcedDismiss();
            accountView = null;
            LaunchActivity.instance.drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
    }

    public void handleAccountState() {
        long currentId = UserConfig.getInstance(UserConfig.selectedAccount).clientUserId;
        if (FingerprintUtils.hasFingerprintCached() && FingerprintUtils.isAccountLocked(currentId)) {
            if ((accountView == null || !accountView.isShowing()) && lastUnlockedAccount != currentId) {
                getBlockingAccountDialog(currentId);
                accountView.show();
                LaunchActivity.instance.drawerLayoutContainer.setAllowOpenDrawer(false, false);
            }
        } else {
            lastUnlockedAccount = currentId;
        }
    }

    private void getBlockingAccountDialog(long currentId) {
        accountView = new BlockingAccountDialog(LaunchActivity.instance);
        accountView.setDelegate(new BlockingAccountView.BlockingViewDelegate() {
            @Override
            public void onUnlock() {
                destroy();
                lastUnlockedAccount = currentId;
            }

            @Override
            public void destroy() {
                accountView.onForcedDismiss();
                accountView = null;
                LaunchActivity.instance.drawerLayoutContainer.setAllowOpenDrawer(true, false);
            }
        });
    }

    private static boolean isThereVisibleChatsPage() {
        List<BaseFragment> lastFragment = LaunchActivity.instance.getActionBarLayout().getFragmentStack();
        for (BaseFragment f : lastFragment) {
            if (f instanceof DialogsActivity f3 && f3.getArguments() != null && Objects.equals(f3.getArguments().getInt("dialogsType", -1), DialogsActivity.DIALOGS_TYPE_HIDDEN_CHATS)) {
                return false;
            }
        }
        return true;
    }
}
