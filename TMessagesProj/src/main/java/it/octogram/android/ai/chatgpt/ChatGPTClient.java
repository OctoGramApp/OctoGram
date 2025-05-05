/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.chatgpt;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;

public class ChatGPTClient {

    private static final String TAG = "ChatGPTClient";

    public ChatGPTResponse sendMessage(ChatGPTRequest request) throws ChatGPTException, StandardHTTPRequest.Http429Exception {
        try {
            JSONObject requestBody = new JSONObject()
                    .put("model", request.getModel())
                    .put("temperature", request.getTemperature())
                    .put("max_tokens", request.getMaxTokens());
            if (request.isStreamOutput()) {
                requestBody.put("stream", true);
            }

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", request.getUserMessage()));
            if (!request.getDeveloperMessage().isEmpty()) {
                messages.put(new JSONObject()
                        .put("role", "system")
                        .put("content", request.getDeveloperMessage()));
            }
            requestBody.put("messages", messages);

            StandardHTTPRequest httpRequest = new StandardHTTPRequest.Builder(request.getApiUrl())
                    .connectTimeout(2000)
                    .method("POST")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + request.getApiKey())
                    .data(requestBody.toString())
                    .build();

            String rawResponse = httpRequest.request();
            JSONObject jsonResponse = processResponse(httpRequest, rawResponse);

            // OctoLogging.d(TAG, "ChatGPT Request: " + requestBody);
            // OctoLogging.d(TAG, "ChatGPT Response: " +rawResponse);
            JSONObject messageObj = extractMessageFromResponse(jsonResponse);
            if (!messageObj.has("content")) {
                throw new ChatGPTException("No message content found in the response");
            }

            return new ChatGPTResponse(messageObj.getString("content"));

        } catch (StandardHTTPRequest.Http429Exception e) {
            OctoLogging.e(TAG, "Rate limit hit", e);
            throw e;
        } catch (IOException | JSONException e) {
            OctoLogging.e(TAG, "Error processing response", e);
            throw new ChatGPTException("Error sending message to ChatGPT", e);
        }
    }

    @NonNull
    private JSONObject processResponse(StandardHTTPRequest httpRequest, String rawResponse)
            throws ChatGPTException, IOException, JSONException {
        HttpURLConnection connection = httpRequest.getHttpURLConnection();
        int statusCode = connection.getResponseCode();
        JSONObject jsonResponse = new JSONObject(rawResponse);

        if (jsonResponse.has("error")) {
            JSONObject error = jsonResponse.getJSONObject("error");
            int errorCode = error.optInt("code", statusCode);
            String errorMessage = error.optString("message", "Unknown error");
            if (errorCode == 429 || statusCode == 429) {
                throw new StandardHTTPRequest.Http429Exception(errorMessage);
            }
            throw new ChatGPTException("API error " + errorCode + ": " + errorMessage);
        }

        if (statusCode >= 400) {
            throw new ChatGPTException("HTTP error " + statusCode + ": " + rawResponse);
        }

        return jsonResponse;
    }

    @NonNull
    private JSONObject extractMessageFromResponse(JSONObject jsonResponse) throws ChatGPTException, JSONException {
        JSONArray choices = jsonResponse.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new ChatGPTException("No choices found in the response");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject messageObj = firstChoice.optJSONObject("message");

        if (messageObj == null) {
            throw new ChatGPTException("Message not found in the response");
        }

        return messageObj;
    }
}
