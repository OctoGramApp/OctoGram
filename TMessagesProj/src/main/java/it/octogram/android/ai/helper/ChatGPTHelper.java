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
import it.octogram.android.ai.chatgpt.ChatGPTModels;
import it.octogram.android.ai.chatgpt.ChatGPTRequest;
import it.octogram.android.ai.chatgpt.ChatGPTRequestBuilder;
import it.octogram.android.ai.chatgpt.ChatGPTResponse;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;

public class ChatGPTHelper {
    private static final String TAG = "ChatGPTHelper";
    private static final ChatGPTClient chatGPTClient = new ChatGPTClient();

    public static void prompt(AiUtils.AiPrompt aiPrompt, MainAiHelper.OnResultState callback) {
        if (!isAvailable()) {
            callback.onFailed();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    ChatGPTRequest chatGPTRequest = new ChatGPTRequestBuilder()
                            .setUserMessage(aiPrompt.getText())
                            .setDeveloperMessage(aiPrompt.getPrompt())
                            .setModel(ChatGPTModels.GPT_3_5_TURBO)
                            .setTemperature(0.8)
                            .setMaxTokens(200)
                            .setApiKey(getApiKey())
                            .build();
                    ChatGPTResponse response = chatGPTClient.sendMessage(chatGPTRequest);

                    String content = response.getContent();
                    if (content.strip().trim().isEmpty()) {
                        callback.onEmptyResponse();
                    } else {
                        callback.onSuccess(content.strip().trim());
                    }
                } catch (ChatGPTException | StandardHTTPRequest.Http429Exception e) {
                    OctoLogging.e(TAG, "ChatGPT API error: " + e.getMessage(), e);
                    if (e instanceof StandardHTTPRequest.Http429Exception) {
                        callback.onTooManyRequests();
                    } else {
                        callback.onFailed();
                    }
                } catch (Exception e) {
                    OctoLogging.e(TAG, "Generic error during ChatGPT request", e);
                    callback.onFailed();
                }
            }
        }.start();
    }

    private static String getApiKey() {
        return OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIKey.getValue().replaceAll(" ", "").trim();
    }

    public static boolean isAvailable() {
        return OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs.getValue() && !getApiKey().isEmpty();
    }
}
