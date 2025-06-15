/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import it.octogram.android.AiModelMessagesState;
import it.octogram.android.AiModelType;
import it.octogram.android.OctoConfig;
import it.octogram.android.ai.icons.AiFeatureIcons;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.utils.OctoUtils;

public class CustomModelsHelper {
    private static final String TAG = "CustomModelsHelper";
    private static final HashMap<String, CustomModel> modelsList = new HashMap<>();
    private static boolean loadedModels = false;
    private static boolean frozenSave = false;

    public static final int DEFAULT_MESSAGES_TO_PASS = 15;
    public static final String VIRTUAL_CHAT_CONTEXT_MODEL_ID = "vcm";
    public static final String VIRTUAL_ASK_ON_MEDIA_MODEL_ID = "aom";

    public static List<Pair<String, String>> getAvailableHashtags(int currentModelType) {
        ArrayList<Pair<String, String>> list = new ArrayList<>();

        if (currentModelType == AiModelType.RELATED_TO_MESSAGES.getId() || currentModelType == AiModelType.RELATED_TO_CHATS.getId()) {
            list.add(new Pair<>("chat_title", "Title of the chat"));
            list.add(new Pair<>("chat_username", "Username of the chat"));
            list.add(new Pair<>("chat_description", "Description of the chat"));
            list.add(new Pair<>("chat_members_count", "Members count of the chat"));
        }

        if (currentModelType == AiModelType.RELATED_TO_MESSAGES.getId() || currentModelType == AiModelType.RELATED_TO_INPUT.getId()) {
            list.add(new Pair<>("message_text", "Text of the message or caption of the media"));
        }

        if (currentModelType == AiModelType.RELATED_TO_INPUT.getId()) {
            list.add(new Pair<>("reply_to_author", "Author of the replied message"));
            list.add(new Pair<>("reply_to_text", "Text of the replied message"));
        }

        return list;
    }

    public static List<Pair<String, String>> getSpecialHashtags() {
        ArrayList<Pair<String, String>> list = new ArrayList<>();
        list.add(new Pair<>("formality", "Formality of the response"));
        list.add(new Pair<>("length", "Length of the response"));
        list.add(new Pair<>("language", "Language of the response"));

        return list;
    }

    private static void loadModels() {
        if (loadedModels) {
            return;
        }
        loadedModels = true;
        modelsList.clear();
        String data = OctoConfig.INSTANCE.aiFeaturesCustomModels.getValue();
        loadFromString(data);
    }

    public static void loadFromString(String data) {
        try {
            JSONObject array = new JSONObject(new JSONTokener(data));
            Iterator<String> keys = array.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    JSONObject keyData = array.getJSONObject(key);
                    if (isValidModel(keyData)) {
                        CustomModel model = new CustomModel();
                        model.saveFromJsonObject(keyData);
                        modelsList.put(key, model);
                    }
                } catch (JSONException ignored) {
                }
            }
        } catch (JSONException ignored) {
        }
        loadedModels = true;
    }

    public static CustomModel getModelById(String id) {
        if (id != null && (id.equals(VIRTUAL_CHAT_CONTEXT_MODEL_ID) || id.equals(VIRTUAL_ASK_ON_MEDIA_MODEL_ID))) {
            CustomModel model = new CustomModel();
            model.saveFromVirtualID(id);
            return model;
        }

        loadModels();
        if (modelsList.containsKey(id)) {
            return modelsList.get(id);
        }
        return null;
    }

    public static CustomModel getModelFromMessage(MessageObject messageObject) {
        File downloadedFile = OctoUtils.getFileContentFromMessage(messageObject);

        if (downloadedFile != null && downloadedFile.length() <= 30 * 1024) { // 30 kB limit
            try {
                String jsonContent = readFileContent(downloadedFile);

                JSONObject jsonObject = new JSONObject(new JSONTokener(jsonContent));

                if (isValidModel(jsonObject)) {
                    CustomModel model = new CustomModel();
                    model.saveFromJsonObject(jsonObject);
                    return model;
                }

            } catch (IOException e) {
                OctoLogging.e(TAG, "I/O error while reading model file", e);
            } catch (JSONException e) {
                OctoLogging.e(TAG, "Invalid JSON format in model file", e);
            }
        } else {
            OctoLogging.w(TAG, "File is null or exceeds size limit (30kB)");
        }

        return null;
    }

    private static String readFileContent(File file) throws IOException {
        StringBuilder jsonStringBuilder = new StringBuilder();
        FileInputStream downloadedFileStream = new FileInputStream(file);
        InputStreamReader reader = new InputStreamReader(downloadedFileStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            jsonStringBuilder.append(line);
        }

        bufferedReader.close();
        return jsonStringBuilder.toString();
    }

    public static boolean isValidModel(JSONObject object) {
        try {
            if (object.has("title") && object.getString("title").trim().length() > 40) {
                return false;
            }
            if (object.has("icon") && !AiFeatureIcons.getAiIcons().containsKey(object.getString("icon"))) {
                return false;
            }
            if (object.getString("prompt").trim().isBlank()) {
                return false;
            }
            if (object.has("modelType") && !AiModelType.Companion.hasState(object.getInt("modelType"))) {
                return false;
            }
            if (object.has("messages") && (object.getInt("messages") < 5 || object.getInt("messages") > 100)) {
                return false;
            }
            if (object.has("appearsInList")) {
                JSONArray array = object.getJSONArray("appearsInList");
                for (int i = 0; i < array.length(); i++) {
                    String data = array.getString(i);
                    if (!AiModelMessagesState.Companion.hasState(data)) {
                        return false;
                    }
                }
            }
            if (object.has("uploadMedia")) {
                object.getBoolean("uploadMedia");
            }
            return true;
        } catch (JSONException ignored) {
        }
        return false;
    }

    public static String createNewModel(String title, String icon, String prompt, int modelType, ArrayList<AiModelMessagesState> appearsInList, int messages, boolean uploadMedia) {
        String modelID = OctoUtils.generateRandomString().replace("-", "");
        updateModel(modelID, title, icon, prompt, modelType, appearsInList, messages, uploadMedia);
        return modelID;
    }

    public static String updateModel(String modelID, String title, String icon, String prompt, int modelType, ArrayList<AiModelMessagesState> appearsInList, int messages, boolean uploadMedia) {
        loadModels();
        CustomModel model;
        if (modelsList.containsKey(modelID)) {
            model = Objects.requireNonNull(modelsList.get(modelID));
        } else {
            model = new CustomModel();
        }

        model.title = title;
        model.icon = icon == null ? "" : icon;
        model.prompt = prompt;
        model.modelType = AiModelType.Companion.find(modelType);
        model.appearsInList = appearsInList;
        model.uploadMedia = uploadMedia;
        model.messagesToPass = messages;

        modelsList.put(modelID, model);
        saveLocally();

        return modelID;
    }

    public static HashMap<String, CustomModel> getModelsList() {
        loadModels();
        return modelsList;
    }

    public static void deleteModel(String modelID) {
        loadModels();
        modelsList.remove(modelID);
        saveLocally();
    }

    private static void saveLocally() {
        if (frozenSave) {
            return;
        }
        try {
            JSONObject object = new JSONObject();
            for (String key : modelsList.keySet()) {
                object.put(key, Objects.requireNonNull(modelsList.get(key)).convertAsObject());
            }
            OctoConfig.INSTANCE.aiFeaturesCustomModels.updateValue(object.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void freezeSaving() {
        frozenSave = true;
    }

    public static class CustomModel {
        public String title = "";
        public String icon = "";
        public String prompt;
        public AiModelType modelType = AiModelType.RELATED_TO_MESSAGES;
        public ArrayList<AiModelMessagesState> appearsInList = new ArrayList<>();
        public boolean uploadMedia = false;
        public int messagesToPass = DEFAULT_MESSAGES_TO_PASS;

        private boolean isVirtualModel = false;

        public boolean isVirtualModel() {
            return isVirtualModel;
        }

        public void saveFromVirtualID(String id) {
            if (id.equals(VIRTUAL_CHAT_CONTEXT_MODEL_ID)) {
                isVirtualModel = true;
                title = LocaleController.getString(R.string.AiFeatures_Features_ChatContext);
                prompt = "Recap these messages in #language";
                modelType = AiModelType.RELATED_TO_CHATS;
                messagesToPass = 60;
            } else if (id.equals(VIRTUAL_ASK_ON_MEDIA_MODEL_ID)) {
                isVirtualModel = true;
                title = LocaleController.getString(R.string.AiFeatures_Features_AskOnPhoto);
                prompt = null;
                modelType = AiModelType.RELATED_TO_MESSAGES;
                appearsInList.add(AiModelMessagesState.PHOTOS);
                appearsInList.add(AiModelMessagesState.STICKERS);
                appearsInList.add(AiModelMessagesState.MUSIC);
                appearsInList.add(AiModelMessagesState.VOICE_MESSAGES);
                appearsInList.add(AiModelMessagesState.VIDEOS);
                appearsInList.add(AiModelMessagesState.GIFS);
                uploadMedia = true;
            }
        }

        public void saveFromJsonObject(JSONObject object) {
            if (isVirtualModel) {
                return;
            }

            try {
                if (object.has("title")) {
                    title = object.getString("title").trim();
                }
                if (object.has("icon")) {
                    icon = object.getString("icon").trim();
                }
                if (object.has("modelType")) {
                    modelType = AiModelType.Companion.find(object.getInt("modelType"));
                }
                prompt = object.getString("prompt").trim();
                appearsInList.clear();
                if (modelType == AiModelType.RELATED_TO_MESSAGES && object.has("appearsInList")) {
                    JSONArray array = object.getJSONArray("appearsInList");
                    for (int i = 0; i < array.length(); i++) {
                        String data = array.getString(i);
                        if (AiModelMessagesState.Companion.hasState(data)) {
                            appearsInList.add(AiModelMessagesState.Companion.find(data));
                        }
                    }
                }
                if (modelType == AiModelType.RELATED_TO_MESSAGES && object.has("uploadMedia")) {
                    uploadMedia = object.getBoolean("uploadMedia");
                }
                if (modelType == AiModelType.RELATED_TO_CHATS && object.has("messages")) {
                    messagesToPass = object.getInt("messages");
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public JSONObject convertAsObject() {
            if (isVirtualModel) {
                return null;
            }

            try {
                JSONObject object = new JSONObject();
                if (!title.isEmpty()) {
                    object.put("title", title.trim());
                }
                if (!icon.isEmpty()) {
                    object.put("icon", icon.trim());
                }
                object.put("prompt", prompt.trim());
                if (modelType != AiModelType.RELATED_TO_MESSAGES) {
                    object.put("modelType", modelType.getId());
                }
                if (uploadMedia && modelType == AiModelType.RELATED_TO_MESSAGES) {
                    object.put("uploadMedia", true);
                }
                if (!appearsInList.isEmpty() && modelType == AiModelType.RELATED_TO_MESSAGES) {
                    JSONArray appearsInListData = new JSONArray();
                    for (AiModelMessagesState state : appearsInList) {
                        appearsInListData.put(state.getId());
                    }
                    object.put("appearsInList", appearsInListData);
                }
                if (messagesToPass != DEFAULT_MESSAGES_TO_PASS && modelType == AiModelType.RELATED_TO_CHATS) {
                    object.put("messages", messagesToPass);
                }
                return object;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
