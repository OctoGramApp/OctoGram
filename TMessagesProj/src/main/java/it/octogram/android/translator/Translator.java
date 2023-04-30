package it.octogram.android.translator;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import it.octogram.android.AlertController;
import it.octogram.android.OctoConfig;
import it.octogram.android.entities.HTMLKeeper;

public class Translator {

    public static final int PROVIDER_GOOGLE = 1;
    public static final int PROVIDER_YANDEX = 4;
    public static final int PROVIDER_DEEPL = 5;
    public static final int PROVIDER_NIU = 9;
    public static final int PROVIDER_DUCKDUCKGO = 13;
    public static final int PROVIDER_TELEGRAM = 14;

    public static void handleTranslationError(Context context, final Exception e, final Runnable onRetry, Theme.ResourcesProvider resourcesProvider) {
        if (context == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        if (e instanceof UnsupportedTargetLanguageException) {
            builder.setMessage(LocaleController.getString("TranslateApiUnsupported", R.string.TranslateApiUnsupported));
            builder.setPositiveButton(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), (dialog, which) -> showTranslationProviderSelector(context, null, resourcesProvider));
        } else {
            if (e instanceof BaseTranslator.Http429Exception) {
                builder.setTitle(LocaleController.getString("TranslationFailed", R.string.TranslationFailed));
                builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
            } else if (e != null && e.getLocalizedMessage() != null) {
                builder.setTitle(LocaleController.getString("TranslationFailed", R.string.TranslationFailed));
                builder.setMessage(e.getLocalizedMessage());
            } else {
                builder.setMessage(LocaleController.getString("TranslationFailed", R.string.TranslationFailed));
            }
            if (onRetry != null) {
                builder.setPositiveButton(LocaleController.getString("Retry", R.string.Retry), (dialog, which) -> onRetry.run());
            }
            builder.setNeutralButton(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), (dialog, which) -> showTranslationProviderSelector(context, null, resourcesProvider));
        }
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.show();
    }

    public static Pair<ArrayList<String>, ArrayList<Integer>> getProviders() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> types = new ArrayList<>();
        names.add(LocaleController.getString("ProviderDeepLTranslate", R.string.ProviderDeepLTranslate));
        types.add(Translator.PROVIDER_DEEPL);
        names.add(LocaleController.getString("ProviderGoogleTranslate", R.string.ProviderGoogleTranslate));
        types.add(Translator.PROVIDER_GOOGLE);
        names.add(LocaleController.getString("ProviderDuckDuckGo", R.string.ProviderDuckDuckGo));
        types.add(Translator.PROVIDER_DUCKDUCKGO);
        names.add(LocaleController.getString("ProviderYandex", R.string.ProviderYandex));
        types.add(Translator.PROVIDER_YANDEX);
        names.add(LocaleController.getString("ProviderTelegram", R.string.ProviderTelegram));
        types.add(Translator.PROVIDER_TELEGRAM);
        return new Pair<>(names, types);
    }

    public static void showTranslationTargetSelector(Context context, Runnable callback, Theme.ResourcesProvider resourcesProvider) {
        showTranslationTargetSelector(context, false, callback, resourcesProvider);
    }

    public static void showTranslationTargetSelector(Context context, boolean isKeyboard, Runnable callback, Theme.ResourcesProvider resourcesProvider) {
        BaseTranslator translator = Translator.getCurrentTranslator();
        ArrayList<String> targetLanguages = new ArrayList<>(translator.getTargetLanguages());
        ArrayList<CharSequence> names = new ArrayList<>();
        for (String language : targetLanguages) {
            Locale locale = Locale.forLanguageTag(language);
            if (!TextUtils.isEmpty(locale.getScript())) {
                names.add(HtmlCompat.fromHtml(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayScript()), AndroidUtilities.capitalize(locale.getDisplayScript(locale))), HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
            } else {
                names.add(String.format("%s - %s", AndroidUtilities.capitalize(locale.getDisplayName()), AndroidUtilities.capitalize(locale.getDisplayName(locale))));
            }
        }
        AndroidUtilities.selectionSort(names, targetLanguages);

        targetLanguages.add(0, "app");
        names.add(0, LocaleController.getString("Default", R.string.Default));

        AlertController.show(names, LocaleController.getString("TranslationLanguage", R.string.TranslationLanguage), targetLanguages.indexOf(isKeyboard ? OctoConfig.translationKeyboardTarget : OctoConfig.translationTarget), context, i -> {
            if (isKeyboard) {
                OctoConfig.setTranslationKeyboardTarget(targetLanguages.get(i));
            } else {
                OctoConfig.setTranslationTarget(targetLanguages.get(i));
            }
            callback.run();
        }, resourcesProvider);
    }

    public static void showTranslationProviderSelector(Context context, MessagesStorage.BooleanCallback callback) {
        showTranslationProviderSelector(context, callback, null);
    }

    public static void showTranslationProviderSelector(Context context, MessagesStorage.BooleanCallback callback, Theme.ResourcesProvider resourcesProvider) {
        Pair<ArrayList<String>, ArrayList<Integer>> providers = getProviders();
        ArrayList<String> names = providers.first;
        ArrayList<Integer> types = providers.second;
        if (names == null || types == null) {
            return;
        }
        int index = types.indexOf(OctoConfig.translationProvider);
        if (index == -1) {
            index = types.indexOf(Translator.PROVIDER_GOOGLE);
        }
        AlertController.show(names, LocaleController.getString("TranslationProvider", R.string.TranslationProvider), index, context, i -> {
            BaseTranslator translator = getTranslator(types.get(i));
            String targetLanguage = translator.getTargetLanguage(OctoConfig.translationTarget);

            if (translator.supportLanguage(targetLanguage)) {
                OctoConfig.setTranslationProvider(types.get(i));
                if (callback != null) callback.run(true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider)
                        .setMessage(LocaleController.getString("TranslateApiUnsupported", R.string.TranslateApiUnsupported));
                if ("app".equals(OctoConfig.translationTarget)) {
                    builder.setPositiveButton(LocaleController.getString("UseGoogleTranslate", R.string.UseGoogleTranslate), (dialog, which) -> {
                        OctoConfig.setTranslationProvider(Translator.PROVIDER_GOOGLE);
                        if (callback != null) callback.run(false);
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                } else if (translator.supportLanguage(translator.getCurrentAppLanguage())) {
                    builder.setPositiveButton(LocaleController.getString("ResetLanguage", R.string.ResetLanguage), (dialog, which) -> {
                        OctoConfig.setTranslationProvider(types.get(i));
                        OctoConfig.setTranslationTarget("app");
                        if (callback != null) callback.run(false);
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                } else {
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                }
                builder.show();
            }
        }, resourcesProvider);
    }

    public static BaseTranslator getCurrentTranslator() {
        return getTranslator(OctoConfig.translationProvider);
    }

    public static BaseTranslator getTranslator(int type) {
        switch (type) {
            case PROVIDER_YANDEX:
                return YandexTranslator.getInstance();
            case PROVIDER_DEEPL:
                return DeepLTranslator.getInstance();
            case PROVIDER_DUCKDUCKGO:
                return DuckDuckGoTranslator.getInstance();
            case PROVIDER_TELEGRAM:
                return TelegramTranslator.getInstance();
            case PROVIDER_GOOGLE:
            default:
                return GoogleAppTranslator.getInstance();
        }
    }

    public static String translate(TLRPC.TL_textWithEntities query, TranslateCallBack translateCallBack) {
        Object additionalObjectTranslation = query;
        if (TranslatorHelper.isSupportHTMLMode()) {
            additionalObjectTranslation = HTMLKeeper.entitiesToHtml(query.text, query.entities, true);
        } else if (!TranslatorHelper.isSupportMarkdown()) {
            additionalObjectTranslation = query.text;
        }
        return translate(new ArrayList<>(Collections.singletonList(additionalObjectTranslation)), singleTranslateCallback(translateCallBack));
    }

    public static String translate(MessageObject query, TranslateCallBack translateCallBack) {
        return translate(new ArrayList<>(Collections.singletonList((new TranslatorHelper.TranslatorContext(query)).getTranslateObject())), singleTranslateCallback(translateCallBack));
    }

    public static String translate(String query, TranslateCallBack translateCallBack) {
        return translate(new ArrayList<>(Collections.singletonList(query)), singleTranslateCallback(translateCallBack));
    }

    public static String translate(ArrayList<Object> translations, MultiTranslateCallBack translateCallBack) {
        return translate(translations, false, translateCallBack);
    }

    public static String translate(String query, boolean isKeyboard, TranslateCallBack translateCallBack) {
        return translate(new ArrayList<>(Collections.singletonList(query)), isKeyboard, singleTranslateCallback(translateCallBack));
    }

    private static MultiTranslateCallBack singleTranslateCallback(TranslateCallBack callBack) {
        return (e, result) -> {
            if (result != null && !result.isEmpty()) {
                callBack.onSuccess(e, result.get(0));
            } else {
                callBack.onSuccess(e, null);
            }
        };
    }

    public static String translate(ArrayList<Object> translations, boolean isKeyboard, MultiTranslateCallBack translateCallBack) {
        BaseTranslator translator = getCurrentTranslator();
        String language = isKeyboard ? translator.getCurrentTargetKeyboardLanguage() : translator.getCurrentTargetLanguage();
        String token = Utilities.generateRandomString();
        if (!translator.supportLanguage(language)) {
            translateCallBack.onSuccess(new UnsupportedTargetLanguageException(), null);
        } else {
            translator.startTask(translations, language, translateCallBack, token);
        }
        return token;
    }

    public interface MultiTranslateCallBack {
        void onSuccess(Exception e, ArrayList<BaseTranslator.Result> result);
    }

    public interface TranslateCallBack {
        void onSuccess(Exception e, BaseTranslator.Result result);
    }


    private static class UnsupportedTargetLanguageException extends IllegalArgumentException {
    }
}

