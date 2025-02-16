package it.octogram.android.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;

public class FolderUtils {
    public static void updateFilterVisibility(int filterId, boolean show) {
        try {
            String value = OctoConfig.INSTANCE.hiddenFolderAssoc.getValue();
            JSONObject jsonObject = new JSONObject(new JSONTokener(value));

            JSONArray currentAccountList;
            try {
                currentAccountList = jsonObject.getJSONArray(getKey());
            } catch (JSONException ignored) {
                currentAccountList = new JSONArray();
                jsonObject.put(getKey(), currentAccountList);
            }

            JSONArray newArray = new JSONArray();
            boolean found = false;
            for(int i = 0; i < currentAccountList.length(); i++) {
                int currentFolder = currentAccountList.getInt(i);
                if (currentFolder != filterId) {
                    newArray.put(currentFolder);
                } else {
                    found = true;
                }
            }

            if (found && show) {
                if (newArray.length() == 0) {
                    jsonObject.remove(getKey());
                } else {
                    jsonObject.put(getKey(), newArray);
                }

                OctoConfig.INSTANCE.hiddenFolderAssoc.updateValue(jsonObject.toString());
            } else if (!found && !show) {
                currentAccountList.put(filterId);
                OctoConfig.INSTANCE.hiddenFolderAssoc.updateValue(jsonObject.toString());
            }
        } catch (JSONException ignored) {}
    }

    public static ArrayList<Integer> getHiddenFolders() {
        ArrayList<Integer> hiddenFolders = new ArrayList<>();
        try {
            String value = OctoConfig.INSTANCE.hiddenFolderAssoc.getValue();
            JSONObject jsonObject = new JSONObject(new JSONTokener(value));

            JSONArray currentAccountList = jsonObject.getJSONArray(getKey());
            for (int i = 0; i < currentAccountList.length(); i++) {
                hiddenFolders.add(currentAccountList.getInt(i));
            }
        } catch (JSONException ignored) {}
        return hiddenFolders;
    }

    public static String getKey() {
        return "_"+ UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
    }

    public static boolean areThereFolders() {
        ArrayList<MessagesController.DialogFilter> filters = MessagesController.getInstance(UserConfig.selectedAccount).getDialogFilters();
        if (filters.size() > 1) {
            boolean isThereVisibleFolder = false;
            ArrayList<Integer> hiddenFolders = getHiddenFolders();
            for (MessagesController.DialogFilter filter : filters) {
                if (filter.isDefault()) {
                    if (!OctoConfig.INSTANCE.hideOnlyAllChatsFolder.getValue()) {
                        isThereVisibleFolder = true;
                    }
                } else if (!hiddenFolders.contains(filter.id)) {
                    isThereVisibleFolder = true;
                }
            }
            return isThereVisibleFolder;
        }

        return !OctoConfig.INSTANCE.hideOnlyAllChatsFolder.getValue();
    }
}
