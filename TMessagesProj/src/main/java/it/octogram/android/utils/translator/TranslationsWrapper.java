package it.octogram.android.utils.translator;

import android.content.Context;
import android.os.Build;
import android.text.style.URLSpan;

import androidx.annotation.ChecksSdkIntAtLeast;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorProvider;

public class TranslationsWrapper {
    public static void initTranslationItem(Context context, BaseFragment fragment, MessageObject selectedMessage, int currentAccount, TLRPC.InputPeer peer, int msgId, String fromLanguage, String toLanguage, CharSequence text, ArrayList<TLRPC.MessageEntity> entities, boolean noforwards, Utilities.CallbackReturn<URLSpan, Boolean> onLinkPress, Runnable onDismiss) {
        SingleTranslationManager manager = new SingleTranslationManager();
        manager.context = context;
        manager.fragment = fragment;
        manager.selectedMessage = selectedMessage;
        manager.currentAccount = currentAccount;
        manager.peer = peer;
        manager.msgId = msgId;
        manager.fromLanguage = fromLanguage;
        manager.toLanguage = toLanguage;
        manager.text = text;
        manager.entities = entities;
        manager.noforwards = noforwards;
        manager.onLinkPress = onLinkPress;
        manager.onDismiss = onDismiss;

        manager.init();
    }

    public static void hideTranslationItem(int currentAccount, MessageObject selectedMessage) {
        SingleTranslationManager manager = new SingleTranslationManager();
        manager.currentAccount = currentAccount;
        manager.selectedMessage = selectedMessage;

        manager.hideTranslationItem();
    }

    public static void translate(int currentAccount, TLRPC.InputPeer peer, int msgId, String toLanguage, CharSequence text, ArrayList<TLRPC.MessageEntity> entities, SingleTranslationManager.OnTranslationResultCallback callback) {
        SingleTranslationManager manager = new SingleTranslationManager();
        manager.currentAccount = currentAccount;
        manager.peer = peer;
        manager.msgId = msgId;
        manager.toLanguage = toLanguage;
        manager.text = text;
        manager.entities = entities;

        manager.initTranslationProcess(callback);
    }

    public static boolean isLanguageUnavailable(String currentLanguage) {
        int translationProvider = OctoConfig.INSTANCE.translatorProvider.getValue();

        if (translationProvider == TranslatorProvider.GOOGLE.getValue()) {
            return GoogleTranslator.isUnsupportedLanguage(currentLanguage);
        } else if (translationProvider == TranslatorProvider.YANDEX.getValue()) {
            return YandexTranslator.isUnsupportedLanguage(currentLanguage);
        } else if (translationProvider == TranslatorProvider.DEEPL.getValue()) {
            return DeeplTranslator.isUnsupportedLanguage(currentLanguage);
        } else return translationProvider != TranslatorProvider.DEFAULT.getValue();
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    public static boolean canUseExternalApp() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
