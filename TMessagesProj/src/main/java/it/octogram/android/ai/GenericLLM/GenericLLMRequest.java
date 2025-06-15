/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.ai.GenericLLM;


import androidx.annotation.NonNull;

public class GenericLLMRequest {

    private final String model;
    private final String userMessage;
    private final String developerMessage;
    private final double temperature;
    private final int maxTokens;
    private final String apiKey;
    private final String apiUrl;
    private final boolean streamOutput;
    private final byte[] mediaBlob;
    private final String mediaMimeType;
    private final Boolean isOpenRouter;

    public GenericLLMRequest(String model, String userMessage, String developerMessage, double temperature, int maxTokens, boolean streamOutput, String apiKey, String apiUrl, byte[] mediaBlob, String mediaMimeType, boolean isOpenRouter) {
        this.model = model;
        this.userMessage = userMessage;
        this.developerMessage = developerMessage;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.streamOutput = streamOutput;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.mediaBlob = mediaBlob;
        this.mediaMimeType = mediaMimeType;
        this.isOpenRouter = isOpenRouter;
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

    public boolean isStreamOutput() {
        return streamOutput;
    }

    public byte[] getMediaBlob() {
        return mediaBlob;
    }

    public String getMediaMimeType() {
        return mediaMimeType;
    }

    public boolean hasMedia() {
        return mediaBlob != null && mediaBlob.length > 0 && mediaMimeType != null && !mediaMimeType.isEmpty();
    }

    public boolean isOpenRouter() {
        return isOpenRouter;
    }

    @NonNull
    @Override
    public String toString() {
        return "GenericLLMRequest{" +
                "model='" + model + '\'' +
                ", userMessage='" + userMessage + '\'' +
                ", developerMessage='" + developerMessage + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", apiKey='***'" +
                ", apiUrl='" + apiUrl + '\'' +
                ", streamOutput=" + streamOutput +
                ", hasMedia=" + hasMedia() +
                ", mediaMimeType='" + mediaMimeType + '\'' +
                ", isOpenRouter=" + isOpenRouter +
                '}';
    }
}