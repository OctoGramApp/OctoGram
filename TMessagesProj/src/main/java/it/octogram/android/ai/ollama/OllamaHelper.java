/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.ollama;

import it.octogram.android.ai.AiPrompt;
import it.octogram.android.ai.MainAiHelper;

public class OllamaHelper {

    private static final String TAG = "OllamaHelper";

    public static void prompt(AiPrompt aiPrompt, MainAiHelper.OnResultState callback) {
        // TODO: Implement the Ollama prompt logic
    }

    public static void getModels(MainAiHelper.OnResultState callback) {
        // TODO: Implement the logic to retrieve models from Ollama
    }

    public static boolean isAvailable() {
        return true; // Placeholder for actual availability check
    }

    public static String getApiKey() {
        return ""; // Placeholder for actual API key retrieval logic
    }
}