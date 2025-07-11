/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.groq;

import it.octogram.android.OctoConfig;
import it.octogram.android.ai.AiPrompt;
import it.octogram.android.ai.GenericLLM.GenericLLMClient;
import it.octogram.android.ai.GenericLLM.GenericLLMException;
import it.octogram.android.ai.GenericLLM.GenericLLMRequest;
import it.octogram.android.ai.GenericLLM.GenericLLMRequestBuilder;
import it.octogram.android.ai.GenericLLM.GenericLLMResponse;
import it.octogram.android.ai.MainAiHelper;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;

public class GroqHelper {
    private static final String TAG = "GroqHelper";
    private static final GenericLLMClient groqClient = new GenericLLMClient();

    public static void prompt(AiPrompt aiPrompt, MainAiHelper.OnResultState callback) {
        if (!isAvailable()) {
            callback.onFailed();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    GenericLLMRequest groqRequest = new GenericLLMRequestBuilder()
                            .setUserMessage(aiPrompt.getText())
                            .setDeveloperMessage(aiPrompt.getPrompt())
                            .setModel(OctoConfig.INSTANCE.aiFeaturesGroqSelectedModel.getValue())
                            .setTemperature(0.8)
                            .setMaxTokens(200)
                            .setApiKey(getApiKey())
                            .setStreamOutput(false)
                            .build();
                    GenericLLMResponse response = groqClient.sendMessage(groqRequest);

                    String content = response.getContent();
                    if (content.strip().trim().isEmpty()) {
                        callback.onEmptyResponse();
                    } else {
                        callback.onSuccess(content.strip().trim());
                    }
                } catch (GenericLLMException | StandardHTTPRequest.Http429Exception e) {
                    OctoLogging.e(TAG, "Groq API error: " + e.getMessage(), e);
                    if (e instanceof StandardHTTPRequest.Http429Exception) {
                        callback.onTooManyRequests();
                    } else {
                        callback.onFailed();
                    }
                } catch (Exception e) {
                    OctoLogging.e(TAG, "Generic error during Groq request" + "(" + e.getMessage() + ")", e);
                    callback.onFailed();
                }
            }
        }.start();
    }

    private static String getApiKey() {
        return OctoConfig.INSTANCE.aiFeaturesUseGroqAPIKey.getValue().replaceAll(" ", "").trim();
    }

    public static boolean isAvailable() {
        return OctoConfig.INSTANCE.aiFeaturesUseGroqAPIs.getValue() && !getApiKey().isEmpty();
    }

}
