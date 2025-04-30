/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.chatgpt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;

public class ChatGPTClient {
    static final String TAG = "ChatGPTClient";

    public ChatGPTResponse sendMessage(ChatGPTRequest chatGPTRequest) throws ChatGPTException, StandardHTTPRequest.Http429Exception {
        try {
            JSONObject requestBody = new JSONObject()
                    .put("model", chatGPTRequest.getModel())
                    .put("temperature", chatGPTRequest.getTemperature())
                    .put("max_tokens", chatGPTRequest.getMaxTokens());

            JSONArray messagesArray = new JSONArray()
                    .put(new JSONObject()
                            .put("role", "user")
                            .put("content", chatGPTRequest.getUserMessage())
                    );

            if (!chatGPTRequest.getDeveloperMessage().isEmpty()) {
                messagesArray.put(new JSONObject()
                        .put("role", "system")
                        .put("content", chatGPTRequest.getDeveloperMessage())
                );
            }

            requestBody.put("messages", messagesArray);

            StandardHTTPRequest request = new StandardHTTPRequest.Builder(chatGPTRequest.getApiUrl())
                    .connectTimeout(2000)
                    .method("POST")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + chatGPTRequest.getApiKey())
                    .data(requestBody.toString())
                    .build();


            String rawResponse = request.request();
            HttpURLConnection conn = request.getHttpURLConnection();

            // OctoLogging.d(TAG, "ChatGPT Request: " + requestBody);
            // OctoLogging.d(TAG, "ChatGPT Response: " +rawResponse);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                OctoLogging.e(TAG, "ChatGPT API error: " + conn.getResponseCode() + ": " + rawResponse);
                throw new ChatGPTException("API returned HTTP " + conn.getResponseCode() + ": " + rawResponse);
            }

            JSONObject json = new JSONObject(rawResponse);
            String content = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return new ChatGPTResponse(content);
        } catch (StandardHTTPRequest.Http429Exception e) {
            OctoLogging.e(TAG, "ChatGPT API error: " + e.getMessage(), e);
            throw e;
        } catch (IOException | JSONException e) {
            OctoLogging.e(TAG, "Error processing ChatGPT response", e);
            throw new ChatGPTException("Error sending message to ChatGPT", e);
        }
    }
}