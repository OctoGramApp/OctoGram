/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.helper;

import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.ai.AiUtils;
import it.octogram.android.logs.OctoLogging;

public class MainAiHelper {
    static final String TAG = "MainAiHelper";
    private static boolean frozenState = false;

    public static boolean canUseAiFeatures() {
        return OctoConfig.INSTANCE.aiFeatures.getValue() && (GeminiHelper.isAvailable() || ChatGPTHelper.isAvailable() || OpenRouterHelper.isAvailable());
    }

    public static boolean canTranslateMessages() {
        return canUseAiFeatures() && OctoConfig.INSTANCE.aiFeaturesTranslateMessages.getValue();
    }

    public static int getPreferredProvider() {
        int favoriteProvider = GeminiHelper.isAvailable() ? Providers.GEMINI : Providers.CHATGPT;
        if (!OpenRouterHelper.isAvailable() && !GeminiHelper.isAvailable() && !ChatGPTHelper.isAvailable()) {
            favoriteProvider = -1;
        }

        int recentProvider = OctoConfig.INSTANCE.aiFeaturesRecentProvider.getValue();
        if (recentProvider == Providers.GEMINI && GeminiHelper.isAvailable()) {
            favoriteProvider = Providers.GEMINI;
        } else if (recentProvider == Providers.CHATGPT && ChatGPTHelper.isAvailable()) {
            favoriteProvider = Providers.CHATGPT;
        } else if (recentProvider == Providers.OPENROUTER && OpenRouterHelper.isAvailable()) {
            favoriteProvider = Providers.OPENROUTER;
        }

        return favoriteProvider;
    }

    public static String getProviderName(int preferredProvider) {
        if (preferredProvider == Providers.GEMINI) {
            return "Gemini";
        } else if (preferredProvider == Providers.CHATGPT) {
            return "ChatGPT";
        } else if (preferredProvider == Providers.OPENROUTER) {
            return "OpenRouter";
        } else {
            OctoLogging.e(TAG, "Unknown provider: " + preferredProvider);
            return "Unknown";
        }
    }

    public static boolean isProviderAvailable(int preferredProvider) {
        if (preferredProvider == Providers.GEMINI) {
            return GeminiHelper.isAvailable();
        } else if (preferredProvider == Providers.CHATGPT) {
            return ChatGPTHelper.isAvailable();
        } else if (preferredProvider == Providers.OPENROUTER) {
            return OpenRouterHelper.isAvailable();
        } else {
            OctoLogging.e(TAG, "Unknown provider: " + preferredProvider);
            return false;
        }
    }

    public static void request(AiUtils.AiPrompt aiPrompt, int preferredProvider, OnResultState state) {
        request(aiPrompt, preferredProvider, state, false);
    }

    private static void request(AiUtils.AiPrompt aiPrompt, int preferredProvider, OnResultState state, boolean forced) {
        if (!forced) {
            if (frozenState) {
                return;
            }
            OctoConfig.INSTANCE.aiFeaturesRecentProvider.updateValue(preferredProvider);
        }

        if (preferredProvider == Providers.GEMINI) {
            GeminiHelper.prompt(aiPrompt, state);
        } else if (preferredProvider == Providers.CHATGPT) {
            ChatGPTHelper.prompt(aiPrompt, state);
        } else if (preferredProvider == Providers.OPENROUTER) {
            OpenRouterHelper.prompt(aiPrompt, state);
        } else {
            OctoLogging.e(TAG, "Unknown provider: " + preferredProvider);
            state.onFailed();
        }
    }

    public static void ping(ConfigProperty<Boolean> configProperty, ConfigProperty<String> keyProperty, String key, OnResultState callback) {
        if (configProperty != OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs && configProperty != OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs && configProperty != OctoConfig.INSTANCE.aiFeaturesUseOpenRouterAPIs) {
            callback.onFailed();
            return;
        }

        frozenState = true;

        boolean originalState = configProperty.getValue();
        configProperty.updateValue(true);

        int preferredProvider;

        if (configProperty == OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs) {
            preferredProvider = Providers.GEMINI;
        } else if (configProperty == OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs) {
            preferredProvider = Providers.CHATGPT;
        } else {
            preferredProvider = Providers.OPENROUTER;
        }

        String originalKeyState = keyProperty.getValue();
        keyProperty.updateValue(key);

        request(new AiUtils.AiPrompt("Ping", "Pong?"), preferredProvider, new OnResultState() {
            private void resetState() {
                configProperty.updateValue(originalState);
                keyProperty.updateValue(originalKeyState);
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
    }

    public static class Providers {
        public static int GEMINI = 0;
        public static int CHATGPT = 1;
        public static int OPENROUTER = 2;

        public static List<Integer> availableProviders = List.of(GEMINI, CHATGPT, OPENROUTER);
    }
}
