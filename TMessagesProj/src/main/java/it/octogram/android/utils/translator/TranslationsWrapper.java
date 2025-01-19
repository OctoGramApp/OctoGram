/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.style.URLSpan;

import androidx.annotation.ChecksSdkIntAtLeast;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.Bulletin;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.preferences.ui.OctoTranslatorUI;
import it.octogram.android.utils.PopupChoiceDialogUtils;

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

    public static void translate(int currentAccount, String toLanguage, CharSequence text, ArrayList<TLRPC.MessageEntity> entities, SingleTranslationManager.OnTranslationResultCallback callback) {
        translate(currentAccount, null, 0, toLanguage, text, entities, callback);
    }

    public static void translate(int currentAccount, String toLanguage, CharSequence text, SingleTranslationManager.OnTranslationResultCallback callback) {
        translate(currentAccount, null, 0, toLanguage, text, null, callback);
    }

    public static boolean isLanguageUnavailable(String currentLanguage) {
        return isLanguageUnavailable(currentLanguage, OctoConfig.INSTANCE.translatorProvider.getValue());
    }

    public static boolean isLanguageUnavailable(String currentLanguage, int translationProvider) {
        if (translationProvider == TranslatorProvider.GOOGLE.getValue()) {
            return GoogleTranslator.isUnsupportedLanguage(currentLanguage);
        } else if (translationProvider == TranslatorProvider.YANDEX.getValue()) {
            return YandexTranslator.isUnsupportedLanguage(currentLanguage);
        } else if (translationProvider == TranslatorProvider.DEEPL.getValue()) {
            return DeepLTranslator.isUnsupportedLanguage(currentLanguage);
        } else if (translationProvider == TranslatorProvider.BAIDU.getValue()) {
            return BaiduTranslator.isUnsupportedLanguage(currentLanguage);
        } else if (translationProvider == TranslatorProvider.LINGO.getValue()) {
            return LingoTranslator.isUnsupportedLanguage(currentLanguage);
        } else if (translationProvider == TranslatorProvider.EMOJIS.getValue()) {
            return EmojisTranslator.isUnsupportedLanguage(currentLanguage);
        } else return translationProvider != TranslatorProvider.DEFAULT.getValue();
    }

    public static void suggestProviderUpdate(Context context, BaseFragment fragment, Runnable providerChanged) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(LocaleController.getString(R.string.Warning));
        alertDialogBuilder.setMessage(LocaleController.getString(R.string.TranslatorUnsupportedLanguage));
        alertDialogBuilder.setPositiveButton(LocaleController.getString(R.string.TranslatorUnsupportedLanguageChange), (dialog, which1) -> {
            dialog.dismiss();
            Dialog selectNewProviderDialog = PopupChoiceDialogUtils.createChoiceDialog(
                    fragment.getParentActivity(),
                    OctoTranslatorUI.getProvidersPopupOptions(),
                    LocaleController.getString(R.string.TranslatorProvider),
                    OctoConfig.INSTANCE.translatorProvider.getValue(),
                    (dialogInterface, sel) -> {
                        OctoConfig.INSTANCE.translatorProvider.updateValue(sel);
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.showBulletin, Bulletin.TYPE_SUCCESS, LocaleController.getString(R.string.TranslatorUnsupportedLanguageChangeDone));

                        if (providerChanged != null) {
                            providerChanged.run();
                        }
                    }
            );

            fragment.setVisibleDialog(selectNewProviderDialog);
            selectNewProviderDialog.show();
        });
        alertDialogBuilder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    public static boolean canUseExternalApp() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }
}
