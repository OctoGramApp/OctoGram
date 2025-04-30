/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.chatgpt;

public class ChatGPTException extends Exception {
    public ChatGPTException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatGPTException(String message) {
        super(message);
    }
}