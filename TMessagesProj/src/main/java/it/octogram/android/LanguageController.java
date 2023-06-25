package it.octogram.android;

import android.util.Xml;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
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

import it.octogram.android.http.StandardHTTPRequest;

public class LanguageController {

    private static File getFileFromLang(String langCode) {
        return new File(ApplicationLoader.getFilesDirFixed(), "octogram_" + langCode.toLowerCase().replace("-", "_") + ".xml");
    }

    public static void loadRemoteLanguageFromCache(Locale locale, boolean withReload) {
        String langCode = locale.getLanguage() + "-r" + locale.getCountry();
        new Thread() {
            @Override
            public void run() {
                try {
                    File fileFromLang = getFileFromLang(langCode);
                    if (getFileFromLang(langCode).exists() && withReload) {
                        LocaleController.addLocaleValue(getLocaleFileStrings(fileFromLang));
                        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface));
                    }
                    String url = String.format("https://assets.octogram.site/LanguagePacks/version_%s.json?%s", locale.getLanguage(), Math.random() * 1000000);
                    JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
                    if (!obj.has("error")) {
                        String remoteMD5 = obj.getString("md5");
                        if (getFileFromLang(langCode).exists()) {
                            if (OctoConfig.languagePackVersioning.containsKey(langCode)) {
                                if (OctoConfig.languagePackVersioning.get(langCode).equals(remoteMD5)) {
                                    return;
                                }
                            }
                        }
                        loadRemoteLanguage(langCode);
                        OctoConfig.languagePackVersioning.put(langCode, remoteMD5);
                        OctoConfig.applyLanguagePackVersioning();
                    }
                } catch (Exception ignored) {
                }
            }
        }.start();
    }

    private static void loadRemoteLanguage(String langCode) throws IOException, JSONException {
        String url = String.format("https://assets.octogram.site/LanguagePacks/%s.json?%s", langCode.replace("-r", ""), Math.random() * 1000000);
        JSONObject obj = new JSONObject(new StandardHTTPRequest(url).request());
        if (!obj.has("error")) {
            saveInternalFile(langCode, obj);
            LocaleController.addLocaleValue(getLocaleFileStrings(getFileFromLang(langCode)));
            AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface));
        }
    }

    private static void saveInternalFile(String langCode, JSONObject object) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(getFileFromLang(langCode)));
        writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        writer.write("<resources>\n");
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = object.optString(key);
            writer.write(String.format("<string name=\"%1$s\">%2$s</string>\n", key, value));
        }
        writer.write("</resources>");
        writer.close();
    }

    private static HashMap<String, String> getLocaleFileStrings(File file) {
        FileInputStream stream = null;
        try {
            if (!file.exists()) {
                return new HashMap<>();
            }
            HashMap<String, String> stringMap = new HashMap<>();
            XmlPullParser parser = Xml.newPullParser();
            stream = new FileInputStream(file);
            parser.setInput(stream, "UTF-8");
            int eventType = parser.getEventType();
            String name = null;
            String value = null;
            String attrName = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    name = parser.getName();
                    int c = parser.getAttributeCount();
                    if (c > 0) {
                        attrName = parser.getAttributeValue(0);
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (attrName != null) {
                        value = parser.getText();
                        if (value != null) {
                            value = value.trim();
                            value = value.replace("\\n", "\n");
                            value = value.replace("\\", "");
                            value = value.replace("&lt;", "<");
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    value = null;
                    attrName = null;
                    name = null;
                }
                if (name != null && name.equals("string") && value != null && attrName != null && value.length() != 0 && attrName.length() != 0) {
                    stringMap.put(attrName, value);
                    name = null;
                    value = null;
                    attrName = null;
                }
                eventType = parser.next();
            }
            return stringMap;
        } catch (Exception ignored) {
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return new HashMap<>();
    }
}
