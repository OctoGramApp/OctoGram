/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.translator;

import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Components.TranslateAlert2.preprocess;

import android.content.Context;
import android.content.Intent;
import android.text.style.URLSpan;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorMode;
import it.octogram.android.TranslatorProvider;

public class SingleTranslationManager {
    private Integer reqId;

    Context context;
    BaseFragment fragment;
    MessageObject selectedMessage;
    int currentAccount;
    TLRPC.InputPeer peer;
    int msgId;
    String fromLanguage;
    String toLanguage;
    CharSequence text;
    ArrayList<TLRPC.MessageEntity> entities;
    boolean noforwards;
    Utilities.CallbackReturn<URLSpan, Boolean> onLinkPress;
    Runnable onDismiss;

    public void init() {
        int translatorMode = OctoConfig.INSTANCE.translatorMode.getValue();

        if (translatorMode == TranslatorMode.INLINE.getValue() && selectedMessage != null && TranslateController.isTranslatableViaInlineMode(selectedMessage)) {

            TranslateController controller = MessagesController.getInstance(currentAccount).getTranslateController();

            fixMessageProvider();

            if (selectedMessage.messageOwner.translatedText != null && Objects.equals(selectedMessage.messageOwner.translatedToLanguage, toLanguage)) {
                // translation for this message is already available in that case
                controller.removeAsTranslatingItem(selectedMessage);
                controller.addAsManualTranslate(selectedMessage);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslating, selectedMessage);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslated, selectedMessage);
            } else {
                controller.addAsManualTranslate(selectedMessage);
                controller.addAsTranslatingItem(selectedMessage);
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslating, selectedMessage);
                translateAsInlineMode();
            }

            return;
        } else if (translatorMode == TranslatorMode.EXTERNAL.getValue()) {
            if (TranslationsWrapper.canUseExternalApp()) {
                Intent intent = new Intent(Intent.ACTION_TRANSLATE);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                LaunchActivity.instance.startActivity(intent);
                return;
            } else {
                OctoConfig.INSTANCE.translatorMode.updateValue(TranslatorMode.DEFAULT.getValue());
            }
        }

        fixMessageProvider();

        AndroidUtilities.runOnUIThread(() -> {
            TranslateAlert2 alert = TranslateAlert2.showAlert(context, fragment, currentAccount, peer, msgId, fromLanguage, toLanguage, text, entities, noforwards, onLinkPress, onDismiss, selectedMessage);
            alert.setDimBehind(false);
        });
    }

    private void fixMessageProvider() {
        if (selectedMessage == null) {
            return;
        }

        if (selectedMessage.messageOwner.translatedText == null) {
            return;
        }

        if (selectedMessage.messageOwner.translatedProviderId != OctoConfig.INSTANCE.translatorProvider.getValue()) {
            selectedMessage.messageOwner.translatedToLanguage = null;
            selectedMessage.messageOwner.translatedText = null;
            selectedMessage.messageOwner.translatedProviderId = -1;
        }
    }

    private void translateAsInlineMode() {
        if (reqId != null) {
            ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true);
            reqId = null;
        }

        int provider = OctoConfig.INSTANCE.translatorProvider.getValue();
        initTranslationProcess(new OnTranslationResultCallback() {
            @Override
            public void onGotReqId(int reqId2) {
                reqId = reqId2;
            }

            @Override
            public void onResponseReceived() {
                TranslateController controller = MessagesController.getInstance(currentAccount).getTranslateController();
                controller.removeAsTranslatingItem(selectedMessage);
            }

            @Override
            public void onSuccess(TLRPC.TL_textWithEntities finalText) {
                selectedMessage.messageOwner.translatedToLanguage = toLanguage;
                selectedMessage.messageOwner.translatedText = finalText;
                selectedMessage.messageOwner.translatedProviderId = provider;

                MessagesStorage.getInstance(currentAccount).updateMessageCustomParams(selectedMessage.getDialogId(), selectedMessage.messageOwner);
                AndroidUtilities.runOnUIThread(() -> {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslated, selectedMessage);
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.updateInterfaces, 0);
                });
            }

            @Override
            public void onError() {
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_ERROR, getString(R.string.TranslatorFailed)));
            }

            @Override
            public void onUnavailableLanguage() {
                AndroidUtilities.runOnUIThread(() -> NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_ERROR, getString(R.string.TranslatorUnsupportedLanguage)));
            }
        });
    }

    public void initTranslationProcess(OnTranslationResultCallback callback) {
        int translationProvider = OctoConfig.INSTANCE.translatorProvider.getValue();

        if (TranslationsWrapper.isLanguageUnavailable(toLanguage)) {
            callback.onResponseReceived();
            callback.onUnavailableLanguage();
            return;
        }

        if (translationProvider == TranslatorProvider.DEFAULT.getValue()) {
            translateWithDefault(callback);
        } else if (translationProvider == TranslatorProvider.GOOGLE.getValue()) {
            GoogleTranslator.executeTranslation(text.toString(), entities, toLanguage, callback);
        } else if (translationProvider == TranslatorProvider.YANDEX.getValue()) {
            YandexTranslator.executeTranslation(text.toString(), entities, toLanguage, callback);
        } else if (translationProvider == TranslatorProvider.DEEPL.getValue()) {
            DeepLTranslator.executeTranslation(text.toString(), entities, toLanguage, OctoConfig.INSTANCE.translatorFormality.getValue(), callback);
        } else if (translationProvider == TranslatorProvider.BAIDU.getValue()) {
            BaiduTranslator.executeTranslation(text.toString(), entities, toLanguage, callback);
        } else if (translationProvider == TranslatorProvider.LINGO.getValue()) {
            LingoTranslator.executeTranslation(text.toString(), toLanguage, callback);
        } else if (translationProvider == TranslatorProvider.EMOJIS.getValue()) {
            EmojisTranslator.executeTranslation(text.toString(), callback);
        } else {
            callback.onResponseReceived();
            callback.onError();
        }
    }

    private void translateWithDefault(OnTranslationResultCallback callback) {
        TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
        TLRPC.TL_textWithEntities textWithEntities = new TLRPC.TL_textWithEntities();
        textWithEntities.text = text == null ? "" : text.toString();
        if (entities != null) {
            textWithEntities.entities = entities;
        }
        if (peer != null) {
            req.flags |= 1;
            req.peer = peer;
            req.id.add(msgId);
        } else {
            req.flags |= 2;
            req.text.add(textWithEntities);
        }
        String lang = toLanguage;
        if (lang != null) {
            lang = lang.split("_")[0];
        }
        if ("nb".equals(lang)) {
            lang = "no";
        }
        req.to_lang = lang;

        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, (res, err) -> {
            callback.onResponseReceived();

            if (res instanceof TLRPC.TL_messages_translateResult &&
                    !((TLRPC.TL_messages_translateResult) res).result.isEmpty() &&
                    ((TLRPC.TL_messages_translateResult) res).result.get(0) != null &&
                    ((TLRPC.TL_messages_translateResult) res).result.get(0).text != null
            ) {
                callback.onSuccess(preprocess(textWithEntities, ((TLRPC.TL_messages_translateResult) res).result.get(0)));
            } else {
                callback.onError();
            }
        });

        callback.onGotReqId(reqId);
    }

    public void hideTranslationItem() {
        TranslateController controller = MessagesController.getInstance(currentAccount).getTranslateController();
        controller.removeAsTranslatingItem(selectedMessage);
        controller.removeAsManualTranslate(selectedMessage);

        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslated, selectedMessage));
    }

    public interface OnTranslationResultCallback {
        void onGotReqId(int reqId);
        void onResponseReceived();
        void onSuccess(TLRPC.TL_textWithEntities finalText);
        void onError();
        void onUnavailableLanguage();
    }
}
