/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai.GenericLLM;

public class GenericLLMException extends Exception {
    public GenericLLMException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenericLLMException(String message) {
        super(message);
    }
}