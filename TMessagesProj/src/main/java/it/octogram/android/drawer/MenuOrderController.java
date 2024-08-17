package it.octogram.android.drawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import it.octogram.android.DrawerFavoriteOption;
import it.octogram.android.MenuItemId;
import it.octogram.android.OctoConfig;

public class MenuOrderController {
    private static final Object sync = new Object();

    private static boolean configLoaded;
    public static JSONArray data;
    public static final String DIVIDER_ITEM = "divider";
    public static final String[] list_items = new String[]{
            MenuItemId.NEW_GROUP.getId(),
            MenuItemId.CONTACTS.getId(),
            MenuItemId.CALLS.getId(),
            MenuItemId.NEARBY_PEOPLE.getId(),
            MenuItemId.SAVED_MESSAGE.getId(),
            MenuItemId.SETTINGS.getId(),
            MenuItemId.OCTOGRAM_SETTINGS.getId(),
            MenuItemId.NEW_CHANNEL.getId(),
            MenuItemId.NEW_SECRET_CHAT.getId(),
            MenuItemId.INVITE_FRIENDS.getId(),
            MenuItemId.TELEGRAM_FEATURES.getId(),
            MenuItemId.ARCHIVED_MESSAGES.getId(),
            MenuItemId.DATACENTER_STATUS.getId(),
            MenuItemId.QR_LOGIN.getId(),
            MenuItemId.SET_STATUS.getId(),
            MenuItemId.MY_PROFILE.getId(),
            MenuItemId.CONNECTED_DEVICES.getId(),
            MenuItemId.DOWNLOADS.getId(),
            MenuItemId.POWER_USAGE.getId(),
            MenuItemId.PROXY_SETTINGS.getId(),
            MenuItemId.ATTACH_MENU_BOT.getId(),
            MenuItemId.TELEGRAM_BROWSER.getId()
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
                FileLog.e(e);
            }
            if (data.length() == 0 && OctoConfig.INSTANCE.drawerFavoriteOption.getValue() != DrawerFavoriteOption.SETTINGS.getValue()){
                loadDefaultItems();
            }
            configLoaded = true;
        }
    }

    private static String[] getDefaultItems() {
        return new String[]{
                list_items[20],
                list_items[15],
                list_items[14],
                DIVIDER_ITEM,
                list_items[0],
                list_items[1],
                list_items[2],
                list_items[3],
                list_items[4],
                list_items[5],
                list_items[6],
                DIVIDER_ITEM,
                list_items[9],
                list_items[10],
        };
    }

    public static void resetToDefaultPosition() {
        data = new JSONArray();
        loadDefaultItems();
    }

    public static boolean IsDefaultPosition() {
        String[] defaultItems = getDefaultItems();
        int sizeAvailable = sizeAvailable();
        int foundSameItems = 0;
        if (defaultItems.length == sizeAvailable) {
            for (int i = 0; i < sizeAvailable; i++) {
                EditableMenuItem editableMenuItem = MenuOrderController.getSingleAvailableMenuItem(i);
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
            FileLog.e(e);
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
            data.put(MenuItemId.SETTINGS.getId());
            data.put(MenuItemId.OCTOGRAM_SETTINGS.getId());
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
        list.add(
                new EditableMenuItem(
                        list_items[0],
                        LocaleController.getString("NewGroup", R.string.NewGroup),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[1],
                        LocaleController.getString("Contacts", R.string.Contacts),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[2],
                        LocaleController.getString("Calls", R.string.Calls),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[3],
                        LocaleController.getString("PeopleNearby", R.string.PeopleNearby),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[4],
                        LocaleController.getString("SavedMessages", R.string.SavedMessages),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[5],
                        LocaleController.getString("Settings", R.string.Settings),
                        OctoConfig.INSTANCE.drawerFavoriteOption.getValue() != DrawerFavoriteOption.SETTINGS.getValue()
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[6],
                        LocaleController.getString(R.string.OctoGramSettings),
                        OctoConfig.INSTANCE.drawerFavoriteOption.getValue() != DrawerFavoriteOption.SETTINGS.getValue()
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[7],
                        LocaleController.getString("NewChannel", R.string.NewChannel),
                        false
                )
        );
//        list.add(
//                new EditableMenuItem(
//                        list_items[8],
//                        LocaleController.getString("NewSecretChat", R.string.NewSecretChat),
//                        false
//                )
//        );
        list.add(
                new EditableMenuItem(
                        list_items[9],
                        LocaleController.getString("InviteFriends", R.string.InviteFriends),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[10],
                        LocaleController.getString("TelegramFeatures", R.string.TelegramFeatures),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[11],
                        LocaleController.getString("ArchivedChats", R.string.ArchivedChats),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[12],
                        LocaleController.getString("DatacenterStatus", R.string.DatacenterStatus),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[13],
                        LocaleController.getString("AuthAnotherClient", R.string.AuthAnotherClient),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[14],
                        LocaleController.getString("SetEmojiStatus", R.string.SetEmojiStatus),
                        false,
                        true
                )
        );

        list.add(
                new EditableMenuItem(
                        list_items[15],
                        LocaleController.getString(R.string.MyProfile),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[16],
                        LocaleController.getString(R.string.Devices),
                        false
                )
        );

        list.add(
                new EditableMenuItem(
                        list_items[17],
                        LocaleController.getString(R.string.DownloadMenuItem),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[18],
                        LocaleController.getString(R.string.PowerUsage),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[19],
                        LocaleController.getString(R.string.ProxySettings),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[20],
                        LocaleController.getString(R.string.AttachedMenuBot),
                        false
                )
        );
        list.add(
                new EditableMenuItem(
                        list_items[21],
                        "Telegram Browser",
                        false
                )
        );

        for (int i = 0; i < data.length(); i++) {
            try {
                if (data.getString(i).equals(DIVIDER_ITEM)) {
                    list.add(
                            new EditableMenuItem(
                                    DIVIDER_ITEM,
                                    LocaleController.getString("Divider", R.string.Divider),
                                    false
                            )
                    );
                }
            } catch (JSONException ignored) {
            }
        }
        return list;
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
                FileLog.e(e);
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
                if (i != position && (idTmp.equals(MenuItemId.DIVIDER.getId()) || seenItems.add(idTmp))) {
                    result.put(idTmp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
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

        public EditableMenuItem(String menu_id, String menu_text, boolean menu_default) {
            this(menu_id, menu_text, menu_default, false);
        }

        public EditableMenuItem(String menu_id, String menu_text, boolean menu_default, boolean is_premium) {
            id = menu_id;
            text = menu_text;
            isDefault = menu_default;
            isPremium = is_premium;
        }
    }

}