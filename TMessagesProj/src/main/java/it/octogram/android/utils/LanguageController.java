package it.octogram.android.utils;

import android.util.Xml;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.http.StandardHTTPRequest;

public class LanguageController {

    private static File getFileFromLang(String langCode) {
        return new File(ApplicationLoader.getFilesDirFixed(), "octogram_" + langCode.toLowerCase().replace("-", "_") + ".xml");
    }

    public static void loadRemoteLanguageFromCache(Locale locale, boolean withReload) {
        new Thread(() -> {
            try {
                String langCode = OctoUtils.fixBrokenLang(locale.getLanguage());
                File fileFromLang = getFileFromLang(langCode);

                if (fileFromLang.exists() && withReload) {
                    LocaleController.addLocaleValue(getLocaleFileStrings(fileFromLang));
                    AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface));
                }

                String remoteUrl = String.format("https://raw.githubusercontent.com/OctoGramApp/assets/lang_packs/LanguagePacks/version_%s.json", langCode);
                JSONObject obj = new JSONObject(new StandardHTTPRequest(remoteUrl).request());
                JSONObject versioning = new JSONObject(OctoConfig.INSTANCE.languagePackVersioning.getValue());

                if (obj.has("md5")) {
                    String remoteMD5 = obj.getString("md5");

                    if (fileFromLang.exists() && versioning.optString(langCode, "").equals(remoteMD5)) {
                        return;
                    }

                    loadRemoteLanguage(langCode);
                    versioning.put(langCode, remoteMD5);
                    OctoConfig.INSTANCE.setPackLangVersion(versioning.toString());
                }
            } catch (Exception ignore) {}
        }).start();
    }

    private static void loadRemoteLanguage(String langCode) throws IOException, JSONException {
        String url = String.format("https://raw.githubusercontent.com/OctoGramApp/assets/lang_packs/LanguagePacks/%s.json", langCode);
        JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
        if (!obj.has("error")) {
            saveInternalFile(langCode, obj);
            LocaleController.addLocaleValue(getLocaleFileStrings(getFileFromLang(langCode)));
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface));
        }
    }

    private static void saveInternalFile(String langCode, JSONObject object) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFileFromLang(langCode)))) {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
            for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                String key = it.next();
                String value = object.optString(key);
                writer.write(String.format("<string name=\"%s\">%s</string>\n", key, value));
            }
            writer.write("</resources>");
        }
    }

    private static HashMap<String, String> getLocaleFileStrings(File file) {
        HashMap<String, String> stringMap = new HashMap<>();

        try (FileInputStream stream = new FileInputStream(file)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, "UTF-8");

            int eventType = parser.getEventType();
            String name = null, value = null, attrName = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    name = parser.getName();
                    int c = parser.getAttributeCount();
                    attrName = (c > 0) ? parser.getAttributeValue(0) : null;
                } else if (eventType == XmlPullParser.TEXT && attrName != null) {
                    value = parser.getText();
                    if (value != null) {
                        value = value.trim().replace("\\n", "\n").replace("\\", "").replace("&lt;", "<");
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    value = attrName = name = null;
                }

                if (name != null && name.equals("string") && value != null && attrName != null && !value.isEmpty() && !attrName.isEmpty()) {
                    stringMap.put(attrName, value);
                    name = value = attrName = null;
                }

                eventType = parser.next();
            }

        } catch (Exception ignored) {
        }
        return stringMap;
    }
}
