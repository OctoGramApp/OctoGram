/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.chatgpt;

public class ChatGPTRequestBuilder {

    private String model = ChatGPTModels.GPT_3_5_TURBO;
    private String userMessage;
    private double temperature = 0.7;
    private int maxTokens = 150;
    private String apiKey;
    private String apiUrl;
    private String developerMessage;

    public ChatGPTRequestBuilder setModel(String model) {
        this.model = model;
        return this;
    }

    public ChatGPTRequestBuilder setUserMessage(String userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    public ChatGPTRequestBuilder setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public ChatGPTRequestBuilder setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public ChatGPTRequestBuilder setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public ChatGPTRequest build() {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        return new ChatGPTRequest(model, userMessage, developerMessage, temperature, maxTokens, apiKey, apiUrl);
    }

    public ChatGPTRequestBuilder setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
        return this;
    }

    public ChatGPTRequestBuilder setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }
}