/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.GenericLLM;

import androidx.annotation.NonNull;

public class GenericLLMRequestBuilder {

    private String model;
    private String userMessage;
    private double temperature = 0.7;
    private int maxTokens = 150;
    private String apiKey;
    private String apiUrl;
    private String developerMessage;
    private boolean streamOutput = false;
    private byte[] mediaBlob;
    private String mediaMimeType;
    private Boolean isOpenRouter = false;

    public GenericLLMRequestBuilder setModel(String model) {
        this.model = model;
        return this;
    }

    public GenericLLMRequestBuilder setUserMessage(String userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    public GenericLLMRequestBuilder setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public GenericLLMRequestBuilder setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public GenericLLMRequestBuilder setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public GenericLLMRequestBuilder setStreamOutput(boolean streamOutput) {
        this.streamOutput = streamOutput;
        return this;
    }

    public GenericLLMRequestBuilder setMediaBlob(byte[] mediaBlob, String mediaMimeType) {
        this.mediaBlob = mediaBlob;
        this.mediaMimeType = mediaMimeType;
        return this;
    }

    public GenericLLMRequestBuilder setMediaBlob(byte[] mediaBlob) {
        this.mediaBlob = mediaBlob;
        return this;
    }

    public GenericLLMRequestBuilder setMediaMimeType(String mediaMimeType) {
        this.mediaMimeType = mediaMimeType;
        return this;
    }

    public GenericLLMRequestBuilder setOpenRouter(Boolean isOpenRouter) {
        this.isOpenRouter = isOpenRouter;
        return this;
    }

    public GenericLLMRequest build() {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("User message cannot be null or empty");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("apiKey cannot be null or empty");
        }
        return new GenericLLMRequest(model, userMessage, developerMessage, temperature, maxTokens, streamOutput, apiKey, apiUrl, mediaBlob, mediaMimeType, isOpenRouter);
    }

    public GenericLLMRequestBuilder setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
        return this;
    }

    public GenericLLMRequestBuilder setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "GenericLLMRequestBuilder{" +
                "model='" + model + '\'' +
                ", userMessage='" + userMessage + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", apiKey='" + apiKey + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", developerMessage='" + developerMessage + '\'' +
                ", streamOutput=" + streamOutput +
                ", mediaBlob=" + (mediaBlob != null ? mediaBlob.length + " bytes" : "null") +
                ", mediaMimeType='" + mediaMimeType + '\'' +
                ", hasMedia=" + (mediaBlob != null && mediaBlob.length > 0 && mediaMimeType != null && !mediaMimeType.isEmpty()) +
                '}';
    }
}