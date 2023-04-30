package it.octogram.android.magic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.play.core.appupdate.AppUpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import it.octogram.android.MenuOrderController;
import it.octogram.android.OctoConfig;
import it.octogram.android.updates.PlayStoreAPI;

public class OWLENC {
    public static class DrawerItems extends MagicVector<String> {
        public void migrate(String input) {
            try {
                JSONArray items = new JSONArray(input);
                for (int i = 0; i < items.length(); i++) {
                    add(items.getString(i));
                }
            } catch (JSONException ignored){}
        }

        @Override
        public boolean isValid() {
            for (String subKey : this) {
                boolean foundValid = false;
                for (String item : MenuOrderController.list_items) {
                    if (item.equals(subKey) || subKey.equals(MenuOrderController.DIVIDER_ITEM)) {
                        foundValid = true;
                        break;
                    }
                }
                if (!foundValid) return false;
            }
            return true;
        }

        public void fixItems() {
            // Add missing items
            if (!contains("settings")) {
                add("settings");
            }

            // Clear duplicates
            ArrayList<String> items = new ArrayList<>();
            for (String item : this) {
                if (!items.contains(item) || item.equals(MenuOrderController.DIVIDER_ITEM)) {
                    items.add(item);
                }
            }
            clear();
            addAll(items);
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c421;
        }
    }

    public static class ExcludedLanguages extends MagicHashVector<String> {
        public void migrate(String input) {
            try {
                JSONArray languages = new JSONArray(input);
                for (int i = 0; i < languages.length(); i++) {
                    add(languages.getString(i));
                }
            } catch (JSONException ignored){}
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c422;
        }
    }

    public static class UpdateAvailable extends MagicBaseObject {
        public String title;
        public String description;
        public String note;
        public String banner;
        public int version;
        public String fileLink;
        public long fileSize;

        public UpdateAvailable(JSONObject object) throws JSONException {
            fromJSON(object);
        }

        public UpdateAvailable() {}

        public void fromJSON(JSONObject updateInfo) throws JSONException {
            title = updateInfo.getString("title");
            description = updateInfo.getString("desc");
            note = updateInfo.getString("note");
            banner = updateInfo.getString("banner");
            version = updateInfo.getInt("version");
            fileLink = updateInfo.getString("link_file");
            fileSize = updateInfo.getLong("file_size");
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c419;
        }

        public void setPlayStoreMetaData(@Nullable AppUpdateInfo appUpdateInfo) {
            if (appUpdateInfo == null) return;
            this.fileSize = appUpdateInfo.totalBytesToDownload();
            this.version = PlayStoreAPI.getVersionCode(appUpdateInfo);
        }

        public boolean isReminded() {
            return OctoConfig.remindedUpdate == version;
        }
    }

    public static class LanguagePacksVersions extends MagicHashMapVector<String, String> {
        public void migrate(String input) {
            try {
                fromJson(input);
            } catch (JSONException ignored){}
        }

        public void fromJson(String input) throws JSONException {
            JSONObject languages = new JSONObject(input);
            for (String key : keySet()) {
                languages.put(key, get(key));
            }
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c423;
        }
    }

    public static class ConfirmSending extends MagicBaseObject {
        public boolean sendGifs;
        public boolean sendStickers;
        public boolean sendAudio;
        public boolean sendVideo;


        public void toggleGifs() {
            sendGifs ^= true;
        }

        public void toggleStickers() {
            sendStickers ^= true;
        }

        public void toggleAudio() {
            sendAudio ^= true;
        }

        public void toggleVideo() {
            sendVideo ^= true;
        }

        public int count() {
            int count = 0;
            if (sendGifs) count++;
            if (sendStickers) count++;
            if (sendAudio) count++;
            if (sendVideo) count++;
            return count;
        }

        public void setAll(boolean value) {
            sendGifs = value;
            sendStickers = value;
            sendAudio = value;
            sendVideo = value;
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c424;
        }
    }

    public static class ContextMenu extends MagicBaseObject {
        public boolean clearFromCache;
        public boolean copyPhoto;
        public boolean noQuoteForward;
        public boolean saveMessage;
        public boolean repeatMessage;
        public boolean patpat;
        public boolean reportMessage = true;
        public boolean messageDetails;

        public void toggleClearFromCache() {
            clearFromCache ^= true;
        }

        public void toggleCopyPhoto() {
            copyPhoto ^= true;
        }

        public void toggleNoQuoteForward() {
            noQuoteForward ^= true;
        }

        public void toggleSaveMessage() {
            saveMessage ^= true;
        }

        public void toggleRepeatMessage() {
            repeatMessage ^= true;
        }

        public void togglePatpat() {
            patpat ^= true;
        }

        public void toggleReportMessage() {
            reportMessage ^= true;
        }

        public void toggleMessageDetails() {
            messageDetails ^= true;
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c425;
        }
    }

    public static class SettingsBackup extends MagicBaseObject implements Iterable<String> {
        private final HashMap<String, Object> settings = new HashMap<>();
        public int VERSION;

        public boolean contains(String key) {
            return settings.containsKey(key);
        }

        public Object get(String key) {
            return settings.get(key);
        }

        public void put(String key, Object value) {
            settings.put(key, value);
        }

        public int size() {
            return settings.size();
        }

        @Override
        public int getConstructor() {
            return 0x1cb5c420;
        }

        @NonNull
        @Override
        public Iterator<String> iterator() {
            return settings.keySet().iterator();
        }

        public boolean isNotLegacy(PushbackInputStream stream) throws IOException {
            byte[] fileBytes = new byte[stream.available()];
            int len = stream.read(fileBytes);
            stream.unread(fileBytes, 0, len);
            try {
                JSONObject jsonObject = new JSONObject(new String(fileBytes, StandardCharsets.UTF_8));
                VERSION = jsonObject.getInt("DB_VERSION");
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.equals("DB_VERSION")) continue;
                    settings.put(key, jsonObject.get(key));
                }
            } catch (Exception e) {
                return true;
            }
            return false;
        }

        public void migrate() {
            if (settings.containsKey("drawerItems")) {
                OptionalMagic<DrawerItems> optionalMagic = new OptionalMagic<>();
                if (settings.get("drawerItems") instanceof String) {
                    DrawerItems drawerItems = new DrawerItems();
                    drawerItems.migrate((String) settings.get("drawerItems"));
                    optionalMagic.set(drawerItems);
                } else if (settings.get("drawerItems") instanceof DrawerItems) {
                    optionalMagic.set((DrawerItems) settings.get("drawerItems"));
                } else if (settings.get("drawerItems") instanceof OptionalMagic) {
                    OptionalMagic<?> optionalMagic1 = (OptionalMagic<?>) settings.get("drawerItems");
                    if (optionalMagic1 != null && optionalMagic1.isPresent()) {
                        optionalMagic.set((DrawerItems) optionalMagic1.get());
                    }
                }
                if (optionalMagic.isPresent() && optionalMagic.get().size() == 0) {
                    optionalMagic.set(null);
                }
                if (optionalMagic.isPresent()) {
                    optionalMagic.get().fixItems();
                }
                settings.put("drawerItems", optionalMagic);
            }

            if (settings.containsKey("confirmStickersGIFs") || settings.containsKey("sendConfirm")) {
                ConfirmSending confirmSending = new ConfirmSending();
                Object confirmStickersGIFs = settings.get("confirmStickersGIFs");
                Object sendConfirm = settings.get("sendConfirm");
                if (confirmStickersGIFs instanceof Boolean) {
                    confirmSending.sendStickers = confirmSending.sendGifs = (Boolean) confirmStickersGIFs;
                    settings.remove("confirmStickersGIFs");
                }
                if (sendConfirm instanceof Boolean) {
                    confirmSending.sendAudio = confirmSending.sendVideo = (Boolean) sendConfirm;
                    settings.remove("sendConfirm");
                }
                settings.put("confirmSending", confirmSending);
            }

            if (settings.containsKey("doNotTranslateLanguages") && settings.get("doNotTranslateLanguages") instanceof String) {
                ExcludedLanguages doNotTranslateSettings = new ExcludedLanguages();
                doNotTranslateSettings.migrate((String) settings.get("doNotTranslateLanguages"));
            }

            if (settings.containsKey("languagePackVersioning") && settings.get("languagePackVersioning") instanceof String) {
                LanguagePacksVersions languagePacksVersions = new LanguagePacksVersions();
                languagePacksVersions.migrate((String) settings.get("languagePackVersioning"));
            }

            if (settings.containsKey("showDeleteDownloadedFile") || settings.containsKey("showCopyPhoto") || settings.containsKey("showNoQuoteForward") ||
                    settings.containsKey("showSaveMessage") || settings.containsKey("showRepeat") || settings.containsKey("showPatpat") ||
                    settings.containsKey("showReportMessage") || settings.containsKey("showMessageDetails")) {
                ContextMenu contextMenu = new ContextMenu();
                Object showDeleteDownloadedFile = settings.get("showDeleteDownloadedFile");
                Object showCopyPhoto = settings.get("showCopyPhoto");
                Object showNoQuoteForward = settings.get("showNoQuoteForward");
                Object showSaveMessage = settings.get("showSaveMessage");
                Object showRepeat = settings.get("showRepeat");
                Object showPatpat = settings.get("showPatpat");
                Object showReportMessage = settings.get("showReportMessage");
                Object showMessageDetails = settings.get("showMessageDetails");
                if (showDeleteDownloadedFile instanceof Boolean) {
                    contextMenu.clearFromCache = (Boolean) showDeleteDownloadedFile;
                    settings.remove("showDeleteDownloadedFile");
                }
                if (showCopyPhoto instanceof Boolean) {
                    contextMenu.copyPhoto = (Boolean) showCopyPhoto;
                    settings.remove("showCopyPhoto");
                }
                if (showNoQuoteForward instanceof Boolean) {
                    contextMenu.noQuoteForward = (Boolean) showNoQuoteForward;
                    settings.remove("showNoQuoteForward");
                }
                if (showSaveMessage instanceof Boolean) {
                    contextMenu.saveMessage = (Boolean) showSaveMessage;
                    settings.remove("showSaveMessage");
                }
                if (showRepeat instanceof Boolean) {
                    contextMenu.repeatMessage = (Boolean) showRepeat;
                    settings.remove("showRepeat");
                }
                if (showPatpat instanceof Boolean) {
                    contextMenu.patpat = (Boolean) showPatpat;
                    settings.remove("showPatpat");
                }
                if (showReportMessage instanceof Boolean) {
                    contextMenu.reportMessage = (Boolean) showReportMessage;
                    settings.remove("showReportMessage");
                }
                if (showMessageDetails instanceof Boolean) {
                    contextMenu.messageDetails = (Boolean) showMessageDetails;
                    settings.remove("showMessageDetails");
                }
                settings.put("contextMenu", contextMenu);
            }
        }
    }
}
