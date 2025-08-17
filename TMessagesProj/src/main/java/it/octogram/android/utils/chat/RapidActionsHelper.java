/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.chat;

import static org.telegram.messenger.LocaleController.getString;

import android.os.Bundle;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.SavedMessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.ProxyListActivity;

import it.octogram.android.InterfaceRapidButtonsActions;
import it.octogram.android.OctoConfig;

public class RapidActionsHelper {
    public static void updateIconState(InterfaceRapidButtonsActions action, FrameLayout imageContainer, RLottieImageView imageView, boolean isMainButton) {
        switch (action) {
            case InterfaceRapidButtonsActions.POST_STORY -> {
                imageView.setAnimation(R.raw.write_contacts_fab_icon_camera, isMainButton ? 56 : 36, isMainButton ? 56 : 36);
                imageView.getAnimatedDrawable().setCurrentFrame(0);
                imageView.getAnimatedDrawable().stop();
                imageContainer.setContentDescription(getString(R.string.AccDescrCaptureStory));
            }
            case InterfaceRapidButtonsActions.SEND_MESSAGE -> {
                if (isMainButton) {
                    imageView.setAnimation(R.raw.write_contacts_fab_icon, 52, 52);
                    imageView.getAnimatedDrawable().setCurrentFrame(0);
                    imageView.getAnimatedDrawable().stop();
                } else {
                    imageView.setImageResource(R.drawable.fab_compose_small);
                }
                imageContainer.setContentDescription(getString(R.string.NewMessageTitle));
            }
            case InterfaceRapidButtonsActions.SAVED_MESSAGES -> {
                imageView.setImageResource(R.drawable.msg_saved);
                imageContainer.setContentDescription(getString(R.string.SavedMessages));
            }
            case InterfaceRapidButtonsActions.ARCHIVED_CHATS -> {
                imageView.setImageResource(R.drawable.msg_archive);
                imageContainer.setContentDescription(getString(R.string.ArchivedChats));
            }
            case InterfaceRapidButtonsActions.SETTINGS -> {
                imageView.setImageResource(R.drawable.msg_settings);
                imageContainer.setContentDescription(getString(R.string.Settings));
            }
            case InterfaceRapidButtonsActions.LOCKED_CHATS -> {
                imageView.setImageResource(R.drawable.edit_passcode);
                imageContainer.setContentDescription(getString(R.string.LockedChats));
            }
            case InterfaceRapidButtonsActions.PROXY -> {
                imageView.setImageResource(R.drawable.msg2_proxy_off);
                imageContainer.setContentDescription(getString(R.string.Proxy));
            }
            case InterfaceRapidButtonsActions.SEARCH -> {
                imageView.setImageResource(R.drawable.msg_search);
                imageContainer.setContentDescription(getString(R.string.Search));
            }
        }
    }

    public static void updateIconState(FrameLayout imageContainer, RLottieImageView imageView, boolean isMainButton) {
        updateIconState(getStatus(isMainButton, false), imageContainer, imageView, isMainButton);
    }

    public static boolean canAnimateSendMessageFab() {
        return OctoConfig.INSTANCE.rapidActionsDefaultConfig.getValue() || (getStatus(true, false) == InterfaceRapidButtonsActions.SEND_MESSAGE || getStatus(true, false) == InterfaceRapidButtonsActions.POST_STORY);
    }

    public static boolean useCameraAsSendMessageFab(DialogsActivity activity) {
        if (OctoConfig.INSTANCE.rapidActionsDefaultConfig.getValue()) {
            return activity.storiesEnabled;
        }

        return getStatus(true, false) == InterfaceRapidButtonsActions.POST_STORY;
    }

    public static boolean isButtonHiddenAsCustomConfig(boolean isMainButton) { // return true to hide
        if (OctoConfig.INSTANCE.rapidActionsDefaultConfig.getValue()) {
            return false;
        }

        if (OctoConfig.INSTANCE.rapidActionsMainButtonAction.getValue() == InterfaceRapidButtonsActions.HIDDEN.getValue()) {
            return true;
        }

        return !isMainButton && OctoConfig.INSTANCE.rapidActionsSecondaryButtonAction.getValue() == InterfaceRapidButtonsActions.HIDDEN.getValue();
    }

    public static void handleAction(BaseFragment fragment, boolean isMainButton, boolean isLongPress) {
        InterfaceRapidButtonsActions action = getStatus(isMainButton, isLongPress);
        switch (action) {
            case InterfaceRapidButtonsActions.POST_STORY -> {
                if (fragment instanceof DialogsActivity f2) {
                    f2.openStoryRecorderWrap(isMainButton, isLongPress);
                }
            }
            case InterfaceRapidButtonsActions.SEND_MESSAGE -> {
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                fragment.presentFragment(new ContactsActivity(args));
            }
            case InterfaceRapidButtonsActions.SAVED_MESSAGES ->
                    SavedMessagesController.openSavedMessages();
            case InterfaceRapidButtonsActions.ARCHIVED_CHATS -> {
                Bundle args = new Bundle();
                args.putInt("folderId", 1);
                fragment.presentFragment(new DialogsActivity(args));
            }
            case InterfaceRapidButtonsActions.SETTINGS -> {
                Bundle args = new Bundle();
                args.putLong("user_id", UserConfig.getInstance(UserConfig.selectedAccount).clientUserId);
                fragment.presentFragment(new ProfileActivity(args));
            }
            case InterfaceRapidButtonsActions.LOCKED_CHATS -> {
                Bundle args = new Bundle();
                args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_HIDDEN_CHATS);
                fragment.presentFragment(new DialogsActivity(args));
            }
            case InterfaceRapidButtonsActions.PROXY ->
                    fragment.presentFragment(new ProxyListActivity());
            case InterfaceRapidButtonsActions.SEARCH -> {
                if (fragment instanceof DialogsActivity f2) {
                    f2.showSearch(true, false, true);
                    f2.getActionBar().openSearchField(true);
                    ActionBarMenuItem searchItem = f2.getSearchItem();
                    if (searchItem != null) {
                        AndroidUtilities.showKeyboard(searchItem.getSearchField());
                    }
                }
            }
        }
    }

    public static boolean hasCustomHandling() {
        return !OctoConfig.INSTANCE.rapidActionsDefaultConfig.getValue();
    }

    public static InterfaceRapidButtonsActions getStatus(boolean isMainButton, boolean isLongPress) {
        int status;
        if (isLongPress) {
            status = OctoConfig.INSTANCE.rapidActionsMainButtonActionLongPress.getValue();
        } else if (isMainButton) {
            status = OctoConfig.INSTANCE.rapidActionsMainButtonAction.getValue();
        } else {
            status = OctoConfig.INSTANCE.rapidActionsSecondaryButtonAction.getValue();
        }
        return InterfaceRapidButtonsActions.Companion.getState(status);
    }

    public static float getScaleState(InterfaceRapidButtonsActions action, boolean isMainButton) {
        if (action.getValue() >= InterfaceRapidButtonsActions.SAVED_MESSAGES.getValue() && !isMainButton) {
            return 0.8f;
        }

        return 1f;
    }

    public static float getScaleState(boolean isMainButton) {
        return getScaleState(getStatus(isMainButton, false), isMainButton);
    }
}
