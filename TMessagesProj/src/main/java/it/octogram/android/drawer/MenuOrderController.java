/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.drawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import it.octogram.android.DrawerFavoriteOption;
import it.octogram.android.DrawerItem;
import it.octogram.android.OctoConfig;
import it.octogram.android.logs.OctoLogging;

public class MenuOrderController {
    private static final Object sync = new Object();

    private static boolean configLoaded;
    public static JSONArray data;
    public static final String DIVIDER_ITEM = "divider";
    public static final String[] list_items = new String[]{
            DrawerItem.Id.NEW_GROUP.getId(),
            DrawerItem.Id.CONTACTS.getId(),
            DrawerItem.Id.CALLS.getId(),
            DrawerItem.Id.SAVED_MESSAGE.getId(),
            DrawerItem.Id.SETTINGS.getId(),
            DrawerItem.Id.OCTOGRAM_SETTINGS.getId(),
            DrawerItem.Id.NEW_CHANNEL.getId(),
            DrawerItem.Id.INVITE_FRIENDS.getId(),
            DrawerItem.Id.TELEGRAM_FEATURES.getId(),
            DrawerItem.Id.ARCHIVED_MESSAGES.getId(),
            DrawerItem.Id.DATACENTER_STATUS.getId(),
            DrawerItem.Id.QR_LOGIN.getId(),
            DrawerItem.Id.SET_STATUS.getId(),
            DrawerItem.Id.MY_PROFILE.getId(),
            DrawerItem.Id.CONNECTED_DEVICES.getId(),
            DrawerItem.Id.DOWNLOADS.getId(),
            DrawerItem.Id.POWER_USAGE.getId(),
            DrawerItem.Id.PROXY_SETTINGS.getId(),
            DrawerItem.Id.ATTACH_MENU_BOT.getId(),
            DrawerItem.Id.TELEGRAM_BROWSER.getId(),
            DrawerItem.Id.DATA_AND_STORAGE.getId()
    };

    static {
        loadConfig();
    }

    public static void reloadConfig() {
        configLoaded = false;
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            if (configLoaded) {
                return;
            }
            String items = OctoConfig.INSTANCE.drawerItems.getValue();
            try {
                data = new JSONArray(items);
            } catch (JSONException e) {
                OctoLogging.e(e);
            }
            if (data.length() == 0 && OctoConfig.INSTANCE.drawerFavoriteOption.getValue() != DrawerFavoriteOption.SETTINGS.getValue()) {
                loadDefaultItems();
            }
            configLoaded = true;
        }
    }

    private static String[] getDefaultItems() {
        return new String[]{
                list_items[18],
                list_items[13],
                DIVIDER_ITEM,
                list_items[0],
                list_items[1],
                list_items[2],
                list_items[3],
                list_items[4],
                list_items[5],
                DIVIDER_ITEM,
                list_items[8],
                list_items[7],
        };
    }

    public static void resetToDefaultPosition() {
        data = new JSONArray();
        loadDefaultItems();
    }

    public static boolean isDefaultPosition() {
        String[] defaultItems = getDefaultItems();
        int sizeAvailable = sizeAvailable();
        int foundSameItems = 0;
        if (defaultItems.length == sizeAvailable) {
            for (int i = 0; i < sizeAvailable; i++) {
                EditableMenuItem editableMenuItem = getSingleAvailableMenuItem(i);
                if (editableMenuItem != null && defaultItems[i].equals(editableMenuItem.id)) {
                    foundSameItems++;
                }
            }
        }
        return sizeAvailable == foundSameItems;
    }

    private static void loadDefaultItems() {
        String[] defaultItems = getDefaultItems();
        for (String defaultItem : defaultItems) {
            data.put(defaultItem);
        }
        OctoConfig.INSTANCE.setDrawerItems(data.toString());
    }

    private static int getArrayPosition(String id) {
        return getArrayPosition(id, 0);
    }

    private static int getArrayPosition(String id, int startFrom) {
        try {
            for (int i = startFrom; i < data.length(); i++) {
                if (data.getString(i).equals(id)) {
                    return i;
                }
            }
        } catch (JSONException ignored) {
        }
        return -1;
    }

    public static int getPositionItem(String id, boolean isDefault) {
        return getPositionItem(id, isDefault, 0);
    }

    public static int getPositionItem(String id, boolean isDefault, int startFrom) {
        int position = getArrayPosition(id, startFrom);
        if (position == -1 && isDefault) {
            position = 0;
            data.put(id);
            OctoConfig.INSTANCE.setDrawerItems(data.toString());
        }
        return position;
    }

    public static void changePosition(int oldPosition, int newPosition) {
        try {
            String data1 = data.getString(newPosition);
            String data2 = data.getString(oldPosition);
            data.put(oldPosition, data1);
            data.put(newPosition, data2);
        } catch (JSONException e) {
            OctoLogging.e(e);
        }
        OctoConfig.INSTANCE.setDrawerItems(data.toString());
    }

    public static EditableMenuItem getSingleAvailableMenuItem(int position) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault, position) == position) {
                return list.get(i);
            }
        }
        return null;
    }

    public static Boolean isAvailable(String id) {
        return isAvailable(id, 0);
    }

    public static Boolean isAvailable(String id, int startFrom) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault, startFrom) != -1) {
                if (list.get(i).id.equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static EditableMenuItem getSingleNotAvailableMenuItem(int position) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int curr_pos = -1;
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault) == -1) {
                curr_pos++;
            }
            if (curr_pos == position) {
                return list.get(i);
            }
        }
        return null;
    }

    private static void ensureMinimumItems() {
        if (data.length() == 0 && OctoConfig.INSTANCE.drawerFavoriteOption.getValue() != DrawerFavoriteOption.SETTINGS.getValue()) {
            data.put(DrawerItem.Id.SETTINGS.getId());
            data.put(DrawerItem.Id.OCTOGRAM_SETTINGS.getId());
            OctoConfig.INSTANCE.setDrawerItems(data.toString());
        }
    }

    public static int sizeHints() {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int size = 0;
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault) == -1) {
                size++;
            }
        }
        return size;
    }

    public static int sizeAvailable() {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int size = 0;
        for (int i = 0; i < list.size(); i++) {
            if (getPositionItem(list.get(i).id, list.get(i).isDefault) != -1) {
                size++;
            }
        }
        return size;
    }

    public static int getPositionOf(String id) {
        ArrayList<EditableMenuItem> list = getMenuItemsEditable();
        int sizeNAv = 0;
        for (int i = 0; i < list.size(); i++) {
            boolean isAv = getPositionItem(list.get(i).id, list.get(i).isDefault) != -1;
            if (list.get(i).id.equals(id) && !isAv) {
                return sizeNAv;
            }
            if (!isAv) {
                sizeNAv++;
            }
        }
        for (int i = 0; i < sizeAvailable(); i++) {
            EditableMenuItem editableMenuItem = getSingleAvailableMenuItem(i);
            if (editableMenuItem != null && editableMenuItem.id.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public static ArrayList<EditableMenuItem> getMenuItemsEditable() {
        ArrayList<EditableMenuItem> list = new ArrayList<>();

        for (int i = 0; i < list_items.length; i++) {
            boolean isFavoriteSettings = i == 4 || i == 5;
            list.add(new EditableMenuItem(list_items[i], getMenuStringRes(i), isFavoriteSettings));
        }

        try {
            for (int i = 0; i < data.length(); i++) {
                if (DIVIDER_ITEM.equals(data.getString(i))) {
                    list.add(new EditableMenuItem(DIVIDER_ITEM, R.string.Divider, false));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    private static int getMenuStringRes(int index) {
        return switch (index) {
            case 0 -> R.string.NewGroup;
            case 1 -> R.string.Contacts;
            case 2 -> R.string.Calls;
            case 3 -> R.string.SavedMessages;
            case 4 -> R.string.Settings;
            case 5 -> R.string.OctoGramSettings;
            case 6 -> R.string.NewChannel;
            case 7 -> R.string.InviteFriends;
            case 8 -> R.string.TelegramFeatures;
            case 9 -> R.string.ArchivedChats;
            case 10 -> R.string.DatacenterStatus;
            case 11 -> R.string.AuthAnotherClient;
            case 12 -> R.string.SetEmojiStatus;
            case 13 -> R.string.MyProfile;
            case 14 -> R.string.Devices;
            case 15 -> R.string.DownloadMenuItem;
            case 16 -> R.string.PowerUsage;
            case 17 -> R.string.ProxySettings;
            case 18 -> R.string.AttachedMenuBot;
            case 19 -> R.string.OctoTgBrowser;
            case 20 -> R.string.DataSettings;
            default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }


    public static void addItem(String id) {
        if (getArrayPosition(id) == -1 || id.equals(DIVIDER_ITEM)) {
            addAsFirst(id);
        }
    }

    private static void addAsFirst(String id) {
        JSONArray result = new JSONArray();
        result.put(id);
        for (int i = 0; i < data.length(); i++) {
            try {
                result.put(data.get(i));
            } catch (JSONException e) {
                OctoLogging.e(e);
            }
        }
        data = result;
        OctoConfig.INSTANCE.setDrawerItems(data.toString());
    }

    public static void removeItem(int position) {
        HashSet<String> seenItems = new HashSet<>();
        JSONArray result = new JSONArray();

        for (int i = 0; i < data.length(); i++) {
            try {
                String idTmp = data.getString(i);
                if (i != position && (idTmp.equals(DrawerItem.Id.DIVIDER.getId()) || seenItems.add(idTmp))) {
                    result.put(idTmp);
                }
            } catch (JSONException e) {
                OctoLogging.e(e);
            }
        }

        data = result;
        OctoConfig.INSTANCE.setDrawerItems(data.toString());
    }

    public static boolean isMenuItemsImportValid(String menuItems) {
        try {
            JSONArray jsonArray = new JSONArray(menuItems);
            ArrayList<String> reparsedJsonArray = reparseMenuItems(jsonArray);
            return !reparsedJsonArray.isEmpty();
        } catch (JSONException e) {
            return false;
        }
    }

    public static String reparseMenuItemsAsString(String menuItems) {
        try {
            JSONArray jsonArray = new JSONArray(menuItems);
            ArrayList<String> reparsedJsonArray = reparseMenuItems(jsonArray);
            return reparsedJsonArray.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
            // can't happen as isMenuItemsImportValid check already happened at this point
        }
    }

    private static ArrayList<String> reparseMenuItems(JSONArray jsonArray) throws JSONException {
        ArrayList<String> reparsedJsonArray = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            String item = jsonArray.getString(i);

            if (!item.equals(DIVIDER_ITEM)) {
                if (!isMenuItemValid(item)) {
                    continue;
                }

                if (reparsedJsonArray.contains(item)) {
                    continue;
                }
            }

            reparsedJsonArray.add(item);
        }

        return reparsedJsonArray;
    }

    public static void onDrawerFavoriteOptionChanged() {
        ensureMinimumItems();
    }

    /**
     * Handles the change of the favorite option in the drawer.
     * <p>
     * This method triggers the execution of {@link #onDrawerFavoriteOptionChanged()}.
     * This is typically called when the user interacts with a favorite option control
     * within the drawer, such as a toggle or checkbox.
     */
    public static void handleDrawerFavoriteOptionChange() {
        onDrawerFavoriteOptionChanged();
    }

    public static boolean isMenuItemValid(String menuItem) {
        return Arrays.asList(list_items).contains(menuItem);
    }

    public static class EditableMenuItem {
        public final String id;
        public final String text;
        public final boolean isDefault;
        public final boolean isPremium;

        public EditableMenuItem(String menu_id, int menu_text, boolean menu_default) {
            this(menu_id, menu_text, menu_default, false);
        }

        public EditableMenuItem(String menu_id, int menu_text, boolean menu_default, boolean is_premium) {
            id = menu_id;
            text = LocaleController.getString(menu_text);
            isDefault = menu_default;
            isPremium = is_premium;
        }
    }

}