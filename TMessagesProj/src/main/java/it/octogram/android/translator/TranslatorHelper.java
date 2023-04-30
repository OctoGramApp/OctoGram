package it.octogram.android.translator;

import android.text.TextUtils;

import androidx.core.text.HtmlCompat;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.Locale;

import it.octogram.android.MessageHelper;
import it.octogram.android.OctoConfig;
import it.octogram.android.entities.HTMLKeeper;

public class TranslatorHelper {

    public static void resetTranslatedMessage(MessageObject messageObject) {
        MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().hideTranslation(messageObject);
        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.messageTranslated, messageObject);
    }

    public static boolean isSupportHTMLMode() {
        return isSupportHTMLMode(OctoConfig.translationProvider);
    }

    public static boolean isSupportHTMLMode(int provider) {
        return provider == Translator.PROVIDER_GOOGLE ||
                provider == Translator.PROVIDER_YANDEX ||
                provider == Translator.PROVIDER_DEEPL;
    }

    public static boolean isSupportMarkdown(int provider) {
        return (isSupportHTMLMode(provider) || provider == Translator.PROVIDER_TELEGRAM) && showPremiumFeatures(provider);
    }

    public static boolean isSupportMarkdown() {
        return isSupportMarkdown(OctoConfig.translationProvider);
    }

    public static boolean showPremiumFeatures() {
        return showPremiumFeatures(OctoConfig.translationProvider);
    }

    public static boolean showPremiumFeatures(int provider) {
        UserConfig userConfig = UserConfig.getInstance(UserConfig.selectedAccount);
        MessagesController messagesController = AccountInstance.getInstance(UserConfig.selectedAccount).getMessagesController();
        return provider != Translator.PROVIDER_TELEGRAM || userConfig.isPremium() || !messagesController.premiumLocked;
    }

    public static boolean isSupportAutoTranslate() {
        return isSupportAutoTranslate(OctoConfig.translationProvider);
    }

    public static boolean isSupportAutoTranslate(int provider) {
        return (provider == Translator.PROVIDER_GOOGLE ||
                provider == Translator.PROVIDER_YANDEX ||
                provider == Translator.PROVIDER_DEEPL ||
                provider == Translator.PROVIDER_TELEGRAM) &&
                showPremiumFeatures();
    }

    public static class TranslatorContext {
        private final Object translateObject;
        private int symbolsCount = 0;

        public TranslatorContext(MessageObject messageObject) {
            BaseTranslator.AdditionalObjectTranslation additionalObjectTranslation = new BaseTranslator.AdditionalObjectTranslation();
            if (messageObject.type == MessageObject.TYPE_POLL) {
                if (messageObject.messageOwner.originalPoll == null) {
                    messageObject.messageOwner.originalPoll = new MessageHelper.PollTexts(((TLRPC.TL_messageMediaPoll) messageObject.messageOwner.media).poll);
                }
                additionalObjectTranslation.translation = messageObject.messageOwner.originalPoll.copy();
                additionalObjectTranslation.messagesCount = messageObject.messageOwner.originalPoll.size();
            } else {
                additionalObjectTranslation.translation = messageObject.messageOwner.message;
                additionalObjectTranslation.messagesCount = 1;
            }
            if (messageObject.messageOwner.reply_markup != null && messageObject.messageOwner.reply_markup.rows.size() > 0) {
                if (messageObject.messageOwner.originalReplyMarkupRows == null) {
                    messageObject.messageOwner.originalReplyMarkupRows = new MessageHelper.ReplyMarkupButtonsTexts(messageObject.messageOwner.reply_markup.rows);
                }
                additionalObjectTranslation.additionalInfo = messageObject.messageOwner.originalReplyMarkupRows.copy();
                additionalObjectTranslation.messagesCount += messageObject.messageOwner.originalReplyMarkupRows.size();
            }
            if (additionalObjectTranslation.translation instanceof String) {
                symbolsCount = ((String) additionalObjectTranslation.translation).length();
            } else if (additionalObjectTranslation.translation instanceof MessageHelper.PollTexts) {
                MessageHelper.PollTexts poll = (MessageHelper.PollTexts) additionalObjectTranslation.translation;
                for (int a = 0; a < poll.getTexts().size(); a++) {
                    symbolsCount += poll.getTexts().get(a).length();
                }
            }
            if (messageObject.messageOwner.entities != null && additionalObjectTranslation.translation instanceof String) {
                if (OctoConfig.keepTranslationMarkdown) {
                    if (isSupportHTMLMode()) {
                        additionalObjectTranslation.translation = HTMLKeeper.entitiesToHtml((String) additionalObjectTranslation.translation, messageObject.messageOwner.entities, false);
                    } else if (isSupportMarkdown()) { // Use Telegram entities
                        TLRPC.TL_textWithEntities source = new TLRPC.TL_textWithEntities();
                        source.text = messageObject.messageOwner.message;
                        source.entities = messageObject.messageOwner.entities;
                        additionalObjectTranslation.translation = source;
                    }
                }
            }
            translateObject = additionalObjectTranslation;
        }

        public int getSymbolsCount() {
            return symbolsCount;
        }

        public Object getTranslateObject() {
            return translateObject;
        }
    }

    public static boolean isTranslating(MessageObject messageObject) {
        return MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().isTranslating(messageObject);
    }

    public static String languageName(String code) {
        if (TextUtils.isEmpty(code)) {
            return null;
        }
        if (TextUtils.equals(code, "app")) {
            return LocaleController.getString("Default", R.string.Default);
        }
        Locale language = Locale.forLanguageTag(code);
        String fromString = !TextUtils.isEmpty(language.getScript()) ? String.valueOf(HtmlCompat.fromHtml(language.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY)) : language.getDisplayName();
        if (TextUtils.isEmpty(fromString)) {
            return null;
        }
        return AndroidUtilities.capitalize(fromString);
    }
}
