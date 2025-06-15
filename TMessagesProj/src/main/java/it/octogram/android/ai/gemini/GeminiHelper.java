/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.gemini;

import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

import it.octogram.android.OctoConfig;
import it.octogram.android.ai.AiPrompt;
import it.octogram.android.ai.MainAiHelper;
import it.octogram.android.logs.OctoLogging;

public class GeminiHelper {
    private static final String TAG = "GeminiHelper";
    private static GenerativeModelFutures model;
    private static String lastModelApiKey;

    public static void prompt(AiPrompt aiPrompt, MainAiHelper.OnResultState callback) {
        if (!isAvailable()) {
            callback.onFailed();
            return;
        }

        if (model == null || !lastModelApiKey.equals(getApiKey())) {
            GenerativeModel gm = new GenerativeModel(GeminiModels.GEMINI_2_0_FLASH, getApiKey());
            lastModelApiKey = getApiKey();
            model = GenerativeModelFutures.from(gm);
        }

        var content = new Content.Builder();
        content.setRole("system");
        content.addText(aiPrompt.getPrompt());

        content.setRole("user");
        content.addText(aiPrompt.getText());
        if (!aiPrompt.getFilePath().isEmpty()) {
            File file = new File(aiPrompt.getFilePath());
            if (file.exists()) {
                if (aiPrompt.getLoadAsImage()) {
                    content.addImage(BitmapFactory.decodeFile(file.getAbsolutePath()));
                } else {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] bytes = new byte[(int) file.length()];
                        int readBytes = fis.read(bytes);

                        if (readBytes != bytes.length) {
                            callback.onFailed();
                            return;
                        }

                        content.addBlob(aiPrompt.getMimeType(), bytes);
                    } catch (IOException ignored) {
                        callback.onFailed();
                        return;
                    }
                }
            } else {
                callback.onFailed();
                return;
            }
        }
        content.build();
        Executor executor = ContextCompat.getMainExecutor(LaunchActivity.instance);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content.build());

        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result.getText() == null && result.getText().isEmpty()) {
                    OctoLogging.e(TAG, "Error: Empty response" + result.getText());
                    callback.onEmptyResponse();
                    return;
                }
                callback.onSuccess(result.getText().strip().trim());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                String stacktrace = OctoLogging.parseStackTrace(throwable.getMessage(), throwable);
                OctoLogging.e(TAG, "Error: " + stacktrace);
                if (stacktrace.contains("The model is overloaded.")) {
                    callback.onModelOverloaded();
                } else {
                    callback.onFailed();
                }
            }
        }, executor);
    }

    private static String getApiKey() {
        return OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIKey.getValue().replaceAll(" ", "").trim();
    }

    public static boolean isAvailable() {
        return OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs.getValue() && !getApiKey().isEmpty();
    }
}
