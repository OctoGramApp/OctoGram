/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai.chatgpt;

import it.octogram.android.OctoConfig;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.ai.AiPrompt;
import it.octogram.android.utils.ai.GenericLLM.GenericLLMClient;
import it.octogram.android.utils.ai.GenericLLM.GenericLLMException;
import it.octogram.android.utils.ai.GenericLLM.GenericLLMRequest;
import it.octogram.android.utils.ai.GenericLLM.GenericLLMRequestBuilder;
import it.octogram.android.utils.ai.GenericLLM.GenericLLMResponse;
import it.octogram.android.utils.ai.MainAiHelper;

public class ChatGPTHelper {
    private static final String TAG = "ChatGPTHelper";
    private static final GenericLLMClient chatGPTClient = new GenericLLMClient();

    public static void prompt(AiPrompt aiPrompt, MainAiHelper.OnResultState callback) {
        if (!isAvailable()) {
            callback.onFailed();
            return;
        }

        if (!aiPrompt.getFilePath().isEmpty()) {
            callback.onMediaUploadUnavailable();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    GenericLLMRequest chatGPTRequest = new GenericLLMRequestBuilder()
                            .setApiUrl("https://api.openai.com/v1/chat/completions")
                            .setUserMessage(aiPrompt.getText())
                            .setDeveloperMessage(aiPrompt.getPrompt())
                            .setModel(ChatGPTModels.GPT_3_5_TURBO)
                            .setTemperature(0.8)
                            .setMaxTokens(200)
                            .setApiKey(getApiKey())
                            .build();
                    GenericLLMResponse response = chatGPTClient.sendMessage(chatGPTRequest);

                    String content = response.getContent();
                    if (content.strip().trim().isEmpty()) {
                        callback.onEmptyResponse();
                    } else {
                        callback.onSuccess(content.strip().trim());
                    }
                } catch (GenericLLMException | StandardHTTPRequest.Http429Exception e) {
                    OctoLogging.e(TAG, "ChatGPT API error: " + e.getMessage(), e);
                    if (e instanceof StandardHTTPRequest.Http429Exception) {
                        callback.onTooManyRequests();
                    } else {
                        callback.onFailed();
                    }
                } catch (Exception e) {
                    OctoLogging.e(TAG, "Generic error during ChatGPT request" + "(" + e.getMessage() + ")", e);
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
