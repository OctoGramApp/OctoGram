/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator.localhelper;

import static org.telegram.messenger.LocaleController.getString;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.StoreUtils;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.translator.SingleTranslationsHandler;
import it.octogram.android.utils.translator.providers.BaseTranslator;
import it.octogram.android.utils.translator.providers.GoogleTranslator;

public class LocalTranslator implements BaseTranslator {
    public static final LocalTranslator INSTANCE = new LocalTranslator();

    private static final String TAG = "LocalTranslator";
    private static final List<String> SUPPORTED_TARGET_LANGUAGES = List.of(
            "sq", "ar", "am", "az", "ga", "et", "or", "eu", "be", "bg", "is", "pl", "bs", "fa",
            "af", "tt", "da", "de", "ru", "fr", "tl", "fi", "fy", "km", "ka", "gu",
            "kk", "ht", "ko", "ha", "nl", "ky", "gl", "ca", "cs", "kn", "co", "hr", "ku",
            "la", "lv", "lo", "lt", "lb", "rw", "ro", "mg", "mt", "mr", "ml", "ms", "mk",
            "mi", "mn", "bn", "my", "hmn", "xh", "zu", "ne", "no", "pa", "pt", "ps", "ny",
            "ja", "sv", "sm", "sr", "st", "si", "eo", "sk", "sl", "sw", "gd", "ceb", "so",
            "tg", "te", "ta", "th", "tr", "tk", "cy", "ug", "ur", "uk", "uz", "es", "iw",
            "el", "haw", "sd", "hu", "sn", "hy", "ig", "it", "yi", "hi", "su", "id", "jw",
            "en", "yo", "vi", "zh-TW", "zh-CN", "zh");

    private static void getOriginalLanguage(MessageObject selectedMessage, String text, OnGetLanguageInterface callback) {
        if (selectedMessage != null && selectedMessage.messageOwner != null && selectedMessage.messageOwner.originalLanguage != null && !selectedMessage.messageOwner.originalLanguage.equals(TranslateController.UNKNOWN_LANGUAGE)) {
            callback.onLanguageReceived(selectedMessage.messageOwner.originalLanguage);
        } else if (LanguageDetector.hasSupport()) {
            LanguageDetector.detectLanguage(text, callback::onLanguageReceived, (Exception e) -> {
                OctoLogging.e(TAG, "Language detection failed: " + e.getMessage());
                callback.onLanguageReceived("en");
            });
        } else {
            callback.onLanguageReceived("en");
        }
    }

    @Override
    public String getName() {
        return getString(R.string.TranslatorProviderTelegramDevice);
    }

    @Override
    public int getMaxExecutionPoolSize() {
        return GoogleTranslator.INSTANCE.getMaxExecutionPoolSize();
    }

    @Override
    public void executeTranslation(MessageObject selectedMessage, TLRPC.InputPeer peer, int msgId, String text, ArrayList<TLRPC.MessageEntity> entities, String definedToLanguage, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        if (StoreUtils.isFromPlayStore()) {
            OctoLogging.w(TAG, "Translation not available for Play Store builds.");
            callback.onResponseReceived();
            callback.onError();
            return;
        }

        if (!OnDeviceHelper.isAvailable()) {
            OctoLogging.w(TAG, "Local translator service not available.");
            callback.onResponseReceived();
            callback.onExtensionNeedInstall();
            return;
        }

        OctoLogging.d(TAG, "Request to translate text: " + text);
        getOriginalLanguage(selectedMessage, text, new OnGetLanguageInterface() {
            @Override
            public void onLanguageReceived(String definedFromLanguage) {
                OctoLogging.d(TAG, "Detected source language: " + definedFromLanguage);
                if (definedFromLanguage.contains("_")) {
                    definedFromLanguage = definedFromLanguage.split("_")[0];
                }
                if ("nb".equals(definedFromLanguage)) {
                    definedFromLanguage = "no";
                }
                String _tmpDfTo = definedToLanguage;
                if (_tmpDfTo.contains("_")) {
                    _tmpDfTo = _tmpDfTo.split("_")[0];
                }
                if ("nb".equals(_tmpDfTo)) {
                    _tmpDfTo = "no";
                }

                Message msg = Message.obtain(null, OnDeviceHelper.ID_ACTION_TRANSLATE);
                Bundle data = new Bundle();
                data.putString("from_language", definedFromLanguage);
                data.putString("to_language", _tmpDfTo);
                data.putString("text_to_translate", text);
                msg.setData(data);
                OnDeviceHelper.sendMessage(msg, new OnDeviceHelper.OnMessageReceived() {
                    @Override
                    public boolean onMessageReceived(Message msg) {
                        Bundle bundle = msg.getData();
                        boolean isLanguageError = bundle.getBoolean("is_language_error", false);
                        boolean isDownloading = bundle.getBoolean("is_downloading", false);
                        boolean isFailed = bundle.getBoolean("is_failed", false);
                        int progress = bundle.getInt("progress", 0);

                        String result = bundle.getString("result");
                        OctoLogging.d(TAG, "Received translation result:");
                        OctoLogging.d(TAG, "Is Language Error: " + isLanguageError);
                        OctoLogging.d(TAG, "Is Downloading: " + isDownloading);
                        OctoLogging.d(TAG, "Is Failed: " + isFailed);
                        OctoLogging.d(TAG + "_PROGRESS", "Progress: " + progress);
                        OctoLogging.d(TAG, "Result: " + result);

                        if ((result == null && !isLanguageError && !isFailed && !isDownloading)) {
                            return true;
                        }

                        if (isLanguageError) {
                            OctoLogging.w(TAG, "Translation failed due to unsupported language.");
                            callback.onResponseReceived();
                            callback.onUnavailableLanguage();
                            return true;
                        } else if (isFailed) {
                            OctoLogging.w(TAG, "Translation failed.");
                            callback.onResponseReceived();
                            callback.onError();
                            return true;
                        } else if (isDownloading) {
                            OctoLogging.d(TAG, "Translation model is downloading.");
                            callback.onDownloadingModel(progress);
                            return false;
                        } else {
                            OctoLogging.d(TAG, "Translation successful: " + result);
                            callback.onResponseReceived();
                            TLRPC.TL_textWithEntities translatedText = new TLRPC.TL_textWithEntities();
                            translatedText.text = result.trim();
                            callback.onSuccess(translatedText);
                            return true;
                        }
                    }

                    @Override
                    public void onDeclareTotalFailure() {
                        callback.onResponseReceived();
                        callback.onExtensionError();
                    }
                });
            }

            @Override
            public void onFailed() {
                OctoLogging.w(TAG, "Failed to detect original language for translation.");
                callback.onError();
            }
        });
    }

    public static void listDownloadedModels(OnModelsListCallback callback) {
        Message msg = Message.obtain(null, OnDeviceHelper.ID_ACTION_LIST_MODELS);
        OnDeviceHelper.sendMessage(msg, new OnDeviceHelper.OnMessageReceived() {
            @Override
            public boolean onMessageReceived(Message msg) {
                Bundle bundle = msg.getData();
                if (bundle.containsKey("models_list")) {
                    ArrayList<String> models = bundle.getStringArrayList("models_list");
                    if (models == null) {
                        callback.onError();
                        return true;
                    }
                    OctoLogging.d(TAG, "Received models list with " + models.size() + " items.");
                    callback.onModelsReceived(models);
                    return true;
                }
                return false;
            }

            @Override
            public void onDeclareTotalFailure() {
                callback.onError();
            }
        });
    }

    public static void deleteDownloadedModel(String language, Utilities.Callback<Boolean> callback) {
        if (language == null || language.isEmpty()) {
            callback.run(false);
            return;
        }

        Message msg = Message.obtain(null, OnDeviceHelper.ID_ACTION_DELETE_MODEL);
        Bundle data = new Bundle();
        data.putString("language", language);
        msg.setData(data);

        OnDeviceHelper.sendMessage(msg, new OnDeviceHelper.OnMessageReceived() {
            @Override
            public boolean onMessageReceived(Message msg) {
                Bundle bundle = msg.getData();
                boolean isDeleted = bundle.getBoolean("is_deleted", false);
                OctoLogging.d(TAG, "Received deletion result for " + language + ": " + isDeleted);
                callback.run(isDeleted);
                return true;
            }

            @Override
            public void onDeclareTotalFailure() {
                callback.run(false);
            }
        });
    }

    public static void deleteAllModels() {
        Message msg = Message.obtain(null, OnDeviceHelper.ID_ACTION_DELETE_ALL_MODELS);
        OnDeviceHelper.sendMessageWithoutResponse(msg);
    }

    @Override
    public boolean isUnsupportedLanguage(String language) {
        return TextUtils.isEmpty(language) || !SUPPORTED_TARGET_LANGUAGES.contains(language);
    }

    public interface OnModelsListCallback {
        void onModelsReceived(ArrayList<String> models);

        void onError();
    }

    private interface OnGetLanguageInterface {
        void onLanguageReceived(String language);

        void onFailed();
    }


}