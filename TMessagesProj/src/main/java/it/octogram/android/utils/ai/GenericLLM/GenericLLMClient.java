/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai.GenericLLM;

import android.util.Base64;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.utils.OctoLogging;

public class GenericLLMClient {

    private static final String TAG = "GenericLLMClient";

    public GenericLLMResponse sendMessage(GenericLLMRequest request)
            throws GenericLLMException, StandardHTTPRequest.Http429Exception {
        try {
            JSONObject requestBody = buildRequestBody(request);

            StandardHTTPRequest httpRequest = new StandardHTTPRequest.Builder(request.getApiUrl())
                    .connectTimeout(2000)
                    .method("POST")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + request.getApiKey())
                    .data(requestBody.toString())
                    .build();

            String rawResponse = httpRequest.request();
            JSONObject jsonResponse = processResponse(httpRequest, rawResponse);

            OctoLogging.d(TAG, "GenericLLM Request: " + requestBody);
            OctoLogging.d(TAG, "GenericLLM Response: " + rawResponse);

            JSONObject messageObj = extractMessageFromResponse(jsonResponse, false);
            if (!messageObj.has("content")) {
                throw new GenericLLMException("No message content found in the response");
            }

            return new GenericLLMResponse(messageObj.getString("content"));

        } catch (StandardHTTPRequest.Http429Exception e) {
            OctoLogging.e(TAG, "Rate limit hit", e);
            throw e;
        } catch (IOException | JSONException e) {
            OctoLogging.e(TAG, "Error processing response", e);
            throw new GenericLLMException("Error sending message to GenericLLM", e);
        }
    }

    @NonNull
    private JSONObject buildRequestBody(GenericLLMRequest request) throws JSONException {
        JSONObject requestBody = new JSONObject()
                .put("model", request.getModel())
                .put("temperature", request.getTemperature())
                .put("max_tokens", request.getMaxTokens());

        if (request.isStreamOutput()) {
            requestBody.put("stream", true);
        }

        JSONArray inputArray = buildInputArray(request);
        String inputKey = request.isOpenRouter() ? "messages" : "input";

        requestBody.put(inputKey, inputArray);
        return requestBody;
    }

    @NonNull
    private JSONArray buildInputArray(GenericLLMRequest request) throws JSONException {
        JSONArray inputArray = new JSONArray();

        if (!request.getDeveloperMessage().isEmpty()) {
            inputArray.put(new JSONObject()
                    .put("role", "system")
                    .put("content", request.getDeveloperMessage()));
        }

        if (request.isOpenRouter()) {
            inputArray.put(new JSONObject()
                    .put("role", "user")
                    .put("content", request.getUserMessage()));
        } else {
            JSONObject userMessage = new JSONObject();
            JSONArray contentArray = new JSONArray();

            contentArray.put(new JSONObject()
                    .put("type", "input_text")
                    .put("text", request.getUserMessage()));

            if (request.hasMedia()) {
                String dataUri = encodeMediaToDataUri(request);
                contentArray.put(new JSONObject()
                        .put("type", "input_image")
                        .put("image_url", dataUri));
            }

            userMessage.put("role", "user");
            userMessage.put("content", contentArray);
            inputArray.put(userMessage);
        }

        return inputArray;
    }

    private String encodeMediaToDataUri(GenericLLMRequest request) {
        String base64Image = Base64.encodeToString(request.getMediaBlob(), Base64.NO_WRAP);
        return "data:" + request.getMediaMimeType() + ";base64," + base64Image;
    }

    @NonNull
    private JSONObject processResponse(StandardHTTPRequest httpRequest, String rawResponse)
            throws GenericLLMException, IOException, JSONException {
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

            throw new GenericLLMException("API error " + errorCode + ": " + errorMessage);
        }

        if (statusCode >= 400) {
            throw new GenericLLMException("HTTP error " + statusCode + ": " + rawResponse);
        }

        return jsonResponse;
    }

    @NonNull
    private JSONObject extractMessageFromResponse(JSONObject jsonResponse, boolean useReasoningAsFallback)
            throws GenericLLMException, JSONException {
        JSONArray choices = jsonResponse.optJSONArray("choices");

        if (choices == null || choices.length() == 0) {
            return fallbackMessage(jsonResponse);
        }

        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject messageObj = firstChoice.optJSONObject("message");

        if (messageObj == null || messageObj.optString("content").trim().isEmpty()) {
            if (useReasoningAsFallback && messageObj != null) {
                String reasoning = messageObj.optString("reasoning", "").trim();
                if (!reasoning.isEmpty()) {
                    return new JSONObject().put("content", reasoning);
                }
            }
            return fallbackMessage(jsonResponse);
        }

        return messageObj;
    }

    private JSONObject fallbackMessage(JSONObject jsonResponse) throws GenericLLMException, JSONException {
        if (jsonResponse.has("output_text")) {
            return new JSONObject().put("content", jsonResponse.getString("output_text"));
        }
        throw new GenericLLMException("Response JSON does not contain 'choices' or a valid fallback field.");
    }
}
