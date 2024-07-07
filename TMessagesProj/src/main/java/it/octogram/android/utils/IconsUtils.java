package it.octogram.android.utils;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

import it.octogram.android.DrawerFavoriteOption;
import it.octogram.android.MenuItemId;
import it.octogram.android.OctoConfig;

public class IconsUtils {
    public static int getIconWithEventType(MenuItemId id) {
        int eventType = getEventType();

        return switch (id) {
            case CONTACTS -> getContactsIcon(eventType);
            case CALLS -> getCallsIcon(eventType);
            case NEW_GROUP -> getNewGroupIcon(eventType);
            case NEW_CHANNEL -> getNewChannelIcon(eventType);
            case NEW_SECRET_CHAT -> getNewSecretChatIcon(eventType);
            case SAVED_MESSAGE -> getSavedMessagesIcon(eventType);
            case SETTINGS -> getSettingsIcon(eventType);
            case INVITE_FRIENDS -> getInviteIcon(eventType);
            case TELEGRAM_FEATURES -> getHelpIcon(eventType);
            case NEARBY_PEOPLE -> getPeopleNearbyIcon(eventType);
            default -> 0;
        };
    }

    public static int getIconWithEventType(int id) {
        int eventType = getEventType();

        if (id == DrawerFavoriteOption.SAVED_MESSAGES.getValue()) {
            return getSavedMessagesIcon(eventType);
        } else if (id == DrawerFavoriteOption.SETTINGS.getValue()) {
            return getSettingsIcon(eventType);
        } else if (id == DrawerFavoriteOption.CONTACTS.getValue()) {
            return getContactsIcon(eventType);
        } else if (id == DrawerFavoriteOption.CALLS.getValue()) {
            return getCallsIcon(eventType);
        } else if (id == DrawerFavoriteOption.DOWNLOADS.getValue()) {
            return R.drawable.media_download;
        } else if (id == DrawerFavoriteOption.ARCHIVED_CHATS.getValue()) {
            return R.drawable.msg_archive;
        } else {
            return 0;
        }
    }

    private static int getEventType() {
        if (OctoConfig.INSTANCE.eventType.getValue() > 0) {
            return OctoConfig.INSTANCE.eventType.getValue() - 1;
        }

        return Theme.getEventType();
    }

    private static int getNewSecretChatIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_secret_ny;
            case 1 -> R.drawable.msg_secret_14;
            case 2 -> R.drawable.msg_secret_hw;
            case 3 -> R.drawable.menu_secret_cn;
            default -> R.drawable.msg_secret;
        };
    }

    private static int getNewChannelIcon(int eventType) {
        return eventType == 3 ? R.drawable.menu_broadcast_cn : R.drawable.msg_channel;
    }

    private static int getNewGroupIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_groups_ny;
            case 1 -> R.drawable.msg_groups_14;
            case 2 -> R.drawable.msg_groups_hw;
            case 3 -> R.drawable.menu_groups_cn;
            default -> R.drawable.msg_groups;
        };
    }

    private static int getContactsIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_contacts_ny;
            case 1 -> R.drawable.msg_contacts_14;
            case 2 -> R.drawable.msg_contacts_hw;
            case 3 -> R.drawable.menu_contacts_cn;
            default -> R.drawable.msg_contacts;
        };
    }

    private static int getCallsIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_calls_ny;
            case 1 -> R.drawable.msg_calls_14;
            case 2 -> R.drawable.msg_calls_hw;
            case 3 -> R.drawable.menu_calls_cn;
            default -> R.drawable.msg_calls;
        };
    }

    private static int getSavedMessagesIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_saved_ny;
            case 1 -> R.drawable.msg_saved_14;
            case 2 -> R.drawable.msg_saved_hw;
            case 3 -> R.drawable.menu_bookmarks_cn;
            default -> R.drawable.msg_saved;
        };
    }

    private static int getSettingsIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_settings_ny;
            case 1 -> R.drawable.msg_settings_14;
            case 2 -> R.drawable.msg_settings_hw;
            case 3 -> R.drawable.menu_settings_cn;
            default -> R.drawable.msg_settings_old;
        };
    }

    private static int getInviteIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_invite_ny;
            case 1 -> R.drawable.msg_secret_ny;
            case 2 -> R.drawable.msg_invite_hw;
            case 3 -> R.drawable.menu_secret_cn;
            default -> R.drawable.msg_invite;
        };
    }

    private static int getHelpIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_help_ny;
            case 2, 3 -> R.drawable.msg_help_hw;
            default -> R.drawable.msg_help;
        };
    }

    private static int getPeopleNearbyIcon(int eventType) {
        return switch(eventType) {
            case 0 -> R.drawable.msg_nearby_ny;
            case 1 -> R.drawable.msg_secret_14;
            case 2 -> R.drawable.msg_secret_hw;
            case 3 -> R.drawable.menu_nearby_cn;
            default -> R.drawable.msg_nearby;
        };
    }
}
