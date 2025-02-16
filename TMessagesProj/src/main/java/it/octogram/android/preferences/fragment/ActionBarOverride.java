package it.octogram.android.preferences.fragment;

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
import java.util.concurrent.CountDownLatch;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.FingerprintUtils;

public class ActionBarOverride extends ActionBarLayout {

    public ActionBarOverride(Context context, boolean main) {
        super(context, main);
    }

    @Override
    public boolean presentFragment(NavigationParams params) {
        BaseFragment fragment = params.fragment;

        boolean mustRequireFingerprint = false;
        if (fragment instanceof CallLogActivity && OctoConfig.INSTANCE.biometricOpenCallsLog.getValue() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = true;
        } else if (fragment instanceof DialogsActivity f2 && FingerprintUtils.hasFingerprint()) {
            if (Objects.equals(f2.getArguments().getInt("folderId"), 1)) {
                mustRequireFingerprint = OctoConfig.INSTANCE.biometricOpenArchive.getValue();
            } else if (Objects.equals(f2.getArguments().getInt("dialogsType"), DialogsActivity.DIALOGS_TYPE_HIDDEN_CHATS)) {
                mustRequireFingerprint = true;
            }
        } else if (fragment instanceof ChatActivity f2 && FingerprintUtils.hasFingerprint()) {
            if (f2.getArguments().containsKey("enc_id")) {
                mustRequireFingerprint = OctoConfig.INSTANCE.biometricOpenSecretChats.getValue();
            } else if (Objects.equals(f2.getArguments().getLong("user_id"), UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())) {
                mustRequireFingerprint = OctoConfig.INSTANCE.biometricOpenSavedMessages.getValue();
            } else {
                boolean isCurrentChatLocked = FingerprintUtils.isChatLocked(f2.getArguments());
                if (isCurrentChatLocked && !isThereHiddenChatsPage()) {
                    mustRequireFingerprint = true;
                }
            }
        } else if (fragment instanceof PreferencesFragment f2 && f2.isLockedContent() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = !isThereHiddenChatsPage();
        } else if (fragment instanceof UsersSelectActivity f2 && f2.isLockedContent() && FingerprintUtils.hasFingerprint()) {
            mustRequireFingerprint = true;
        }

        if (mustRequireFingerprint) {
            CountDownLatch latch = new CountDownLatch(1);
            boolean[] status = {false};

            FingerprintUtils.checkFingerprint(ApplicationLoader.applicationContext, FingerprintUtils.OPEN_PAGE, new FingerprintUtils.FingerprintResult() {
                @Override
                public void onSuccess() {
                    ActionBarOverride.super.presentFragment(params);
                    status[0] = true;
                    latch.countDown();
                }

                @Override
                public void onFailed() {
                    latch.countDown();
                }
            });

//            try {
//               // latch.await();
//            } catch (InterruptedException ignored) {}

            return status[0];
        }

        return super.presentFragment(params);
    }

    private static boolean isThereHiddenChatsPage() {
        List<BaseFragment> lastFragment = LaunchActivity.instance.getActionBarLayout().getFragmentStack();
        for (BaseFragment f : lastFragment) {
            if (f instanceof DialogsActivity f3 && f3.getArguments() != null && Objects.equals(f3.getArguments().getInt("dialogsType"), DialogsActivity.DIALOGS_TYPE_HIDDEN_CHATS)) {
                return true;
            }
        }
        return false;
    }
}
