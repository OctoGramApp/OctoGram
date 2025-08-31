/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai;

import org.telegram.ui.Components.TranslateAlert2;

import it.octogram.android.AiProvidersDetails;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.ai.chatgpt.ChatGPTHelper;
import it.octogram.android.utils.ai.gemini.GeminiHelper;
import it.octogram.android.utils.ai.openrouter.OpenRouterHelper;

public class MainAiHelper {
    public static final String systemInstructions = "Use only plain text or Telegram-supported MarkdownV2.\n\nAllowed formatting (Telegram MarkdownV2):\n- **bold** → wrap text with double asterisks (**text**)\n- __italic__ → wrap text with double underscores (__text__)\n- `inline code` → wrap text with single backticks (`text`)\n- ```code block``` → wrap text with triple backticks (```code```)\n- Lists → start each item with a dash and a space (e.g. '- item')\n\nDo NOT use:\n- Lists with '*' or '+'\n- Headings with '#' (e.g. ###)\n- Blockquotes with '>'\n- Links, tables, or any unsupported Markdown features\n\nExamples:\n✅ **bold text**\n✅ __italic text__\n✅ `inline code`\n✅ ```\ncode block\n```\n✅ - item one\n✅ - item two\n❌ * item one\n❌ + item two\n❌ ### heading\n❌ > quote\n\nOnly the rules above are valid.";
    private static final String TAG = "MainAiHelper";
    private static boolean frozenState = false;

    public static boolean canUseAiFeatures() {
        return OctoConfig.INSTANCE.aiFeaturesAcceptedTerms.getValue() && hasAvailableProviders();
    }

    public static int getEnabledProvidersCount() {
        int i = 0;
        if (GeminiHelper.isAvailable()) i++;
        if (ChatGPTHelper.isAvailable()) i++;
        if (OpenRouterHelper.isAvailable()) i++;
        return i;
    }

    public static boolean hasAvailableProviders() {
        return getEnabledProvidersCount() > 0;
    }

    public static boolean canTranslateMessages() {
        return canUseAiFeatures() && OctoConfig.INSTANCE.aiFeaturesTranslateMessages.getValue();
    }

    public static AiProvidersDetails getPreferredProvider() {
        AiProvidersDetails favoriteProvider = GeminiHelper.isAvailable() ? AiProvidersDetails.GEMINI : ChatGPTHelper.isAvailable() ? AiProvidersDetails.CHATGPT : AiProvidersDetails.OPENROUTER;
        if (!hasAvailableProviders()) {
            favoriteProvider = null;
        }

        int recentProvider = OctoConfig.INSTANCE.aiFeaturesRecentProvider.getValue();
        if (recentProvider == AiProvidersDetails.GEMINI.getId() && GeminiHelper.isAvailable()) {
            favoriteProvider = AiProvidersDetails.GEMINI;
        } else if (recentProvider == AiProvidersDetails.CHATGPT.getId() && ChatGPTHelper.isAvailable()) {
            favoriteProvider = AiProvidersDetails.CHATGPT;
        } else if (recentProvider == AiProvidersDetails.OPENROUTER.getId() && OpenRouterHelper.isAvailable()) {
            favoriteProvider = AiProvidersDetails.OPENROUTER;
        }

        return favoriteProvider;
    }

    public static boolean isProviderAvailable(AiProvidersDetails preferredProvider) {
        if (preferredProvider == AiProvidersDetails.GEMINI) {
            return GeminiHelper.isAvailable();
        } else if (preferredProvider == AiProvidersDetails.CHATGPT) {
            return ChatGPTHelper.isAvailable();
        } else if (preferredProvider == AiProvidersDetails.OPENROUTER) {
            return OpenRouterHelper.isAvailable();
        } else {
            OctoLogging.e(TAG, "Unknown provider: " + preferredProvider);
            return false;
        }
    }

    public static void request(AiPrompt aiPrompt, AiProvidersDetails preferredProvider, OnResultState state) {
        request(aiPrompt, preferredProvider, state, false);
    }

    private static void request(AiPrompt aiPrompt, AiProvidersDetails preferredProvider, OnResultState state, boolean forced) {
        if (!forced) {
            if (frozenState) {
                return;
            }
            OctoConfig.INSTANCE.aiFeaturesRecentProvider.updateValue(preferredProvider.getId());
        }

        if (preferredProvider == AiProvidersDetails.GEMINI) {
            GeminiHelper.prompt(aiPrompt, state);
        } else if (preferredProvider == AiProvidersDetails.CHATGPT) {
            ChatGPTHelper.prompt(aiPrompt, state);
        } else if (preferredProvider == AiProvidersDetails.OPENROUTER) {
            OpenRouterHelper.prompt(aiPrompt, state);
        } else {
            OctoLogging.e(TAG, "Unknown provider: " + preferredProvider);
            state.onFailed();
        }
    }

    public static void ping(AiProvidersDetails provider, String key, OnResultState callback) {
        frozenState = true;

        boolean originalState = provider.getStatusProperty().getValue();
        provider.getStatusProperty().updateValue(true);

        String originalKeyState = provider.getKeyProperty().getValue();
        provider.getKeyProperty().updateValue(key);

        request(new AiPrompt("Ping", "Pong?"), provider, new OnResultState() {
            private void resetState() {
                provider.getStatusProperty().updateValue(originalState);
                provider.getKeyProperty().updateValue(originalKeyState);
                frozenState = false;
            }

            @Override
            public void onSuccess(String result) {
                resetState();
                callback.onSuccess(result);
            }

            @Override
            public void onFailed() {
                resetState();
                callback.onFailed();
            }
        }, true);
    }

    public static String getDestinationLanguage() {
        String toLanguage = TranslateAlert2.getToLanguage();
        if (!OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.getValue().isEmpty()) {
            String temp = TranslateAlert2.languageName(OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.getValue());
            if (temp != null && !temp.isEmpty()) {
                toLanguage = OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.getValue();
            }
        }
        return toLanguage;
    }

    public interface OnResultState {
        void onSuccess(String result);

        void onFailed();

        default void onTooManyRequests() {
            onFailed();
        }

        default void onEmptyResponse() {
            onFailed();
        }

        default void onModelOverloaded() {
            onFailed();
        }

        default void onMediaUploadUnavailable() {
            onFailed();
        }
    }
}
