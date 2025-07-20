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
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorMode;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.app.ui.bottomsheets.TranslatorConfigBottomSheet;
import it.octogram.android.utils.translator.localhelper.OnDeviceHelper;

public class SingleTranslationsHandler {
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
    boolean noForwards;
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
            if (MainTranslationsHandler.canUseExternalApp()) {
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
            TranslateAlert2 alert = TranslateAlert2.showAlert(context, fragment, currentAccount, peer, msgId, fromLanguage, toLanguage, text, entities, noForwards, onLinkPress, onDismiss, selectedMessage);
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

        if (MainTranslationsHandler.isLanguageUnavailable(toLanguage)) {
            callback.onResponseReceived();
            callback.onUnavailableLanguage();
            return;
        }

        String method = MessagesController.getInstance(currentAccount).translationsManualEnabled;

        if (translationProvider == TranslatorProvider.DEFAULT.getValue()) {
            if (method.equalsIgnoreCase("alternative") || method.equalsIgnoreCase("system")) {
                translationProvider = TranslatorProvider.GOOGLE.getValue();
            } else if (method.equalsIgnoreCase("system")) {
                translationProvider = TranslatorProvider.DEVICE_TRANSLATION.getValue();
            }
        } else if (translationProvider == TranslatorProvider.DEVICE_TRANSLATION.getValue() && callback.isFromQueue()) {
            translationProvider = TranslatorProvider.GOOGLE.getValue();
        }

        MainTranslationsHandler.getInstanceForTranslation(translationProvider).executeTranslation(selectedMessage, peer, msgId, text.toString(), entities, toLanguage, callback);
    }

    public void hideTranslationItem() {
        TranslateController controller = MessagesController.getInstance(currentAccount).getTranslateController();
        controller.removeAsTranslatingItem(selectedMessage);
        controller.removeAsManualTranslate(selectedMessage);

        AndroidUtilities.runOnUIThread(() -> NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.messageTranslated, selectedMessage));
    }

    public interface OnTranslationResultCallback {
        default boolean isFromQueue() {
            return false;
        }

        default void onGotReqId(int reqId) {

        }

        default void onDownloadingModel(int percent) {

        }

        default void onResponseReceived() {

        }

        default void onExtensionNeedInstall() {
            BaseFragment lastFragment = LaunchActivity.getSafeLastFragment();
            if (lastFragment != null) {
                new TranslatorConfigBottomSheet(lastFragment.getContext(), lastFragment, true, new TranslatorConfigBottomSheet.TranslatorConfigInterface() {
                }).show();
            }
        }

        default void onExtensionNeedUpdate() {
            BaseFragment fragment = LaunchActivity.getSafeLastFragment();
            if (fragment != null) {
                if (!OnDeviceHelper.checkVersionCode(fragment.getContext(), OnDeviceHelper.MIN_ONDEVICE_VERSION_CODE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                    builder.setTitle(getString(R.string.Warning));
                    builder.setMessage(AndroidUtilities.replaceTags(String.format(Locale.US, getString(R.string.OnDeviceTranslationModels_UnsupportedVersion), OnDeviceHelper.MIN_ONDEVICE_VERSION, OnDeviceHelper.MIN_ONDEVICE_VERSION_CODE)));
                    builder.setPositiveButton(getString(R.string.OK), null);
                    fragment.showDialog(builder.create());
                }
            }
        }

        void onSuccess(TLRPC.TL_textWithEntities finalText);

        void onError();

        default void onUnavailableLanguage() {
            onError();
        }

        default void onExtensionError() {
            onError();
        }
    }
}
