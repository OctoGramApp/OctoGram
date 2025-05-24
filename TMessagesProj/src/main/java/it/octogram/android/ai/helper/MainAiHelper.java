/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.helper;

import it.octogram.android.AiProvidersDetails;
import it.octogram.android.OctoConfig;
import it.octogram.android.ai.AiPrompt;
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

    public static AiProvidersDetails getPreferredProvider() {
        AiProvidersDetails favoriteProvider = GeminiHelper.isAvailable() ? AiProvidersDetails.GEMINI : (ChatGPTHelper.isAvailable() ? AiProvidersDetails.CHATGPT : AiProvidersDetails.OPENROUTER);
        if (!OpenRouterHelper.isAvailable() && !GeminiHelper.isAvailable() && !ChatGPTHelper.isAvailable()) {
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
}
