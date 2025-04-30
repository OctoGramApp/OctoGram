/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.ai.chatgpt;


public class ChatGPTRequest {

    private final String model;
    private final String userMessage;
    private final String developerMessage;
    private final double temperature;
    private final int maxTokens;
    private final String apiKey;
    private final String apiUrl;


    public ChatGPTRequest(String model, String userMessage, String developerMessage, double temperature, int maxTokens, String apiKey) {
        this.model = model;
        this.userMessage = userMessage;
        this.developerMessage = developerMessage;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.apiKey = apiKey;
        this.apiUrl = "https://api.openai.com/v1/chat/completions";
    }

    public ChatGPTRequest(String model, String userMessage, String developerMessage, double temperature, int maxTokens, String apiKey, String apiUrl) {
        this.model = model;
        this.userMessage = userMessage;
        this.developerMessage = developerMessage;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}