/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai

data class AiPrompt(
    val prompt: String,
    val text: String,
    val filePath: String,
    val mimeType: String,
    val loadAsImage: Boolean
) {
    constructor(prompt: String, content: String) : this(
        prompt = prompt,
        text = content,
        filePath = "",
        mimeType = "",
        loadAsImage = false
    )
}
