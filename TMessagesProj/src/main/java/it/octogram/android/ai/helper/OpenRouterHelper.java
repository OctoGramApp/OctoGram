/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.helper;

import it.octogram.android.OctoConfig;
import it.octogram.android.ai.AiUtils;
import it.octogram.android.ai.chatgpt.ChatGPTClient;
import it.octogram.android.ai.chatgpt.ChatGPTException;
import it.octogram.android.ai.chatgpt.ChatGPTRequestBuilder;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;

public class OpenRouterHelper {
    static final String TAG = "OpenRouterHelper";
    static final ChatGPTClient openRouterClient = new ChatGPTClient();

    public static void prompt(AiUtils.AiPrompt aiPrompt, MainAiHelper.OnResultState callback) {
        if (!isAvailable()) {
            callback.onFailed();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    var openRouterRequest = new ChatGPTRequestBuilder()
                            .setUserMessage(aiPrompt.getText())
                            .setDeveloperMessage(aiPrompt.getPrompt())
                            .setApiUrl("https://openrouter.ai/api/v1/chat/completions")
                            .setModel(OctoConfig.INSTANCE.aiFeaturesOpenRouterSelectedModel.getValue())
                            .setTemperature(0.8)
                            .setMaxTokens(200)
                            .setApiKey(getApiKey())
                            .setStreamOutput(false)
                            .build();
                    var response = openRouterClient.sendMessage(openRouterRequest);

                    String content = response.getContent();
                    OctoLogging.e(TAG, "OpenRouter Content: "+content);
                    if (content.strip().trim().isEmpty()) {
                        callback.onEmptyResponse();
                    } else {
                        callback.onSuccess(content.strip().trim());
                    }
                } catch (ChatGPTException | StandardHTTPRequest.Http429Exception e) {
                    OctoLogging.e(TAG, "OpenRouter API error: " + e.getMessage(), e);
                        if (e instanceof StandardHTTPRequest.Http429Exception) {
                            callback.onTooManyRequests();
                        } else {
                            callback.onFailed();
                        }
                } catch (Exception e) {
                    OctoLogging.e(TAG, "Generic error during OpenRouter request", e);
                    callback.onFailed();
                }
            }
        }.start();
    }

    private static String getApiKey() {
        return OctoConfig.INSTANCE.aiFeaturesOpenRouterAPIKey.getValue().replaceAll(" ", "").trim();
    }

    public static boolean isAvailable() {
        return OctoConfig.INSTANCE.aiFeaturesUseOpenRouterAPIs.getValue() && !getApiKey().isEmpty();
    }
}