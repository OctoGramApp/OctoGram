/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Build;
import android.text.style.URLSpan;

import androidx.annotation.ChecksSdkIntAtLeast;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.ui.OctoChatsTranslatorProviderUI;
import it.octogram.android.utils.translator.localhelper.LocalTranslator;
import it.octogram.android.utils.translator.providers.BaiduTranslator;
import it.octogram.android.utils.translator.providers.BaseTranslator;
import it.octogram.android.utils.translator.providers.DeepLTranslator;
import it.octogram.android.utils.translator.providers.GoogleTranslator;
import it.octogram.android.utils.translator.providers.LingoTranslator;
import it.octogram.android.utils.translator.providers.NewGoogleTranslator;
import it.octogram.android.utils.translator.providers.TelegramTranslation;
import it.octogram.android.utils.translator.providers.YandexTranslator;

public class MainTranslationsHandler {
    public static void initTranslationItem(Context context, BaseFragment fragment, MessageObject selectedMessage, int currentAccount, TLRPC.InputPeer peer, int msgId, String fromLanguage, String toLanguage, CharSequence text, ArrayList<TLRPC.MessageEntity> entities, boolean noforwards, Utilities.CallbackReturn<URLSpan, Boolean> onLinkPress, Runnable onDismiss) {
        SingleTranslationsHandler manager = new SingleTranslationsHandler();
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
        manager.noForwards = noforwards;
        manager.onLinkPress = onLinkPress;
        manager.onDismiss = onDismiss;

        manager.init();
    }

    public static void hideTranslationItem(int currentAccount, MessageObject selectedMessage) {
        SingleTranslationsHandler manager = new SingleTranslationsHandler();
        manager.currentAccount = currentAccount;
        manager.selectedMessage = selectedMessage;
        manager.hideTranslationItem();
    }

    public static void translate(int currentAccount, TLRPC.InputPeer peer, int msgId, String toLanguage, CharSequence text, ArrayList<TLRPC.MessageEntity> entities, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        SingleTranslationsHandler manager = new SingleTranslationsHandler();
        manager.currentAccount = currentAccount;
        manager.peer = peer;
        manager.msgId = msgId;
        manager.toLanguage = toLanguage;
        manager.text = text;
        manager.entities = entities;

        manager.initTranslationProcess(callback);
    }

    public static void translate(int currentAccount, String toLanguage, CharSequence text, ArrayList<TLRPC.MessageEntity> entities, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        translate(currentAccount, null, 0, toLanguage, text, entities, callback);
    }

    public static void translate(int currentAccount, String toLanguage, CharSequence text, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        translate(currentAccount, null, 0, toLanguage, text, null, callback);
    }

    public static BaseTranslator getInstanceForTranslation(int currentProvider) {
        if (currentProvider == TranslatorProvider.GOOGLE.getValue()) {
            return GoogleTranslator.INSTANCE;
        } else if (currentProvider == TranslatorProvider.YANDEX.getValue()) {
            return YandexTranslator.INSTANCE;
        } else if (currentProvider == TranslatorProvider.DEEPL.getValue()) {
            return DeepLTranslator.INSTANCE;
        } else if (currentProvider == TranslatorProvider.BAIDU.getValue()) {
            return BaiduTranslator.INSTANCE;
        } else if (currentProvider == TranslatorProvider.LINGO.getValue()) {
            return LingoTranslator.INSTANCE;
        } else if (currentProvider == TranslatorProvider.GOOGLE_CLOUD.getValue()) {
            return NewGoogleTranslator.INSTANCE;
        } else if (currentProvider == TranslatorProvider.DEVICE_TRANSLATION.getValue()) {
            return LocalTranslator.INSTANCE;
        } else {
            return TelegramTranslation.INSTANCE;
        }
    }

    public static boolean isLanguageUnavailable(String currentLanguage) {
        return isLanguageUnavailable(currentLanguage, OctoConfig.INSTANCE.translatorProvider.getValue());
    }

    public static boolean isLanguageUnavailable(String currentLanguage, int translationProvider) {
        return getInstanceForTranslation(translationProvider).isUnsupportedLanguage(currentLanguage);
    }

    public static String getProviderName() {
        return getProviderName(OctoConfig.INSTANCE.translatorProvider.getValue());
    }

    public static String getProviderName(int translationProvider) {
        return getInstanceForTranslation(translationProvider).getName();
    }

    public static int getMaxExecutionPoolSize() {
        return getMaxExecutionPoolSize(OctoConfig.INSTANCE.translatorProvider.getValue());
    }

    public static int getMaxExecutionPoolSize(int translationProvider) {
        return getInstanceForTranslation(translationProvider).getMaxExecutionPoolSize();
    }

    public static void suggestProviderUpdate(Context context, BaseFragment fragment, Runnable providerChanged) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(getString(R.string.Warning));
        alertDialogBuilder.setMessage(getString(R.string.TranslatorUnsupportedLanguage));
        alertDialogBuilder.setPositiveButton(getString(R.string.TranslatorUnsupportedLanguageChange), (dialog, which1) -> {
            dialog.dismiss();
            OctoChatsTranslatorProviderUI ui = new OctoChatsTranslatorProviderUI();
            ui.setOnChangedRunnable(providerChanged);
            fragment.presentFragment(new PreferencesFragment(ui));
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.Cancel), null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    public static boolean canUseExternalApp() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
