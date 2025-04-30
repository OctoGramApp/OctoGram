/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.chatgpt;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// ChatGPTModels.java
public final class ChatGPTModels {

    private ChatGPTModels() {
    }

    // -- GPT-4.5 Preview
    public static final String GPT_4_5_PREVIEW = "gpt-4.5-preview";
    public static final String GPT_4_5_PREVIEW_2025_02_27 = "gpt-4.5-preview-2025-02-27";

    // -- GPT-4.1 Series
    public static final String GPT_4_1 = "gpt-4.1";
    public static final String GPT_4_1_2025_04_14 = "gpt-4.1-2025-04-14";
    public static final String GPT_4_1_MINI = "gpt-4.1-mini";
    public static final String GPT_4_1_MINI_2025_04_14 = "gpt-4.1-mini-2025-04-14";
    public static final String GPT_4_1_NANO = "gpt-4.1-nano";
    public static final String GPT_4_1_NANO_2025_04_14 = "gpt-4.1-nano-2025-04-14";

    // -- GPT-4o Series
    public static final String GPT_4O = "gpt-4o";
    public static final String GPT_4O_2024_11_20 = "gpt-4o-2024-11-20";
    public static final String GPT_4O_2024_08_06 = "gpt-4o-2024-08-06";
    public static final String GPT_4O_2024_05_13 = "gpt-4o-2024-05-13";

    // -- GPT-4o Mini
    public static final String GPT_4O_MINI = "gpt-4o-mini";
    public static final String GPT_4O_MINI_2024_07_18 = "gpt-4o-mini-2024-07-18";

    // -- GPT-4o Audio/Speech
    public static final String GPT_4O_AUDIO_PREVIEW = "gpt-4o-audio-preview";
    public static final String GPT_4O_AUDIO_PREVIEW_2024_10_01 = "gpt-4o-audio-preview-2024-10-01";
    public static final String GPT_4O_AUDIO_PREVIEW_2024_12_17 = "gpt-4o-audio-preview-2024-12-17";
    public static final String GPT_4O_MINI_AUDIO_PREVIEW = "gpt-4o-mini-audio-preview";
    public static final String GPT_4O_MINI_AUDIO_PREVIEW_2024_12_17 = "gpt-4o-mini-audio-preview-2024-12-17";
    public static final String GPT_4O_TRANSCRIBE = "gpt-4o-transcribe";
    public static final String GPT_4O_MINI_TRANSCRIBE = "gpt-4o-mini-transcribe";
    public static final String GPT_4O_MINI_TTS = "gpt-4o-mini-tts";

    // -- GPT-4o Search
    public static final String GPT_4O_SEARCH_PREVIEW = "gpt-4o-search-preview";
    public static final String GPT_4O_SEARCH_PREVIEW_2025_03_11 = "gpt-4o-search-preview-2025-03-11";
    public static final String GPT_4O_MINI_SEARCH_PREVIEW = "gpt-4o-mini-search-preview";
    public static final String GPT_4O_MINI_SEARCH_PREVIEW_2025_03_11 = "gpt-4o-mini-search-preview-2025-03-11";

    // -- GPT-3.5 Turbo
    public static final String GPT_3_5_TURBO = "gpt-3.5-turbo";
    public static final String GPT_3_5_TURBO_16K = "gpt-3.5-turbo-16k";
    public static final String GPT_3_5_TURBO_1106 = "gpt-3.5-turbo-1106";
    public static final String GPT_3_5_TURBO_0125 = "gpt-3.5-turbo-0125";

    // -- GPT-3.5 Instruct
    public static final String GPT_3_5_TURBO_INSTRUCT = "gpt-3.5-turbo-instruct";
    public static final String GPT_3_5_TURBO_INSTRUCT_0914 = "gpt-3.5-turbo-instruct-0914";

    // -- O1 Series
    public static final String O1_PREVIEW = "o1-preview";
    public static final String O1_PREVIEW_2024_09_12 = "o1-preview-2024-09-12";
    public static final String O1_MINI = "o1-mini";
    public static final String O1_MINI_2024_09_12 = "o1-mini-2024-09-12";

    // -- Embeddings
    public static final String TEXT_EMBEDDING_3_LARGE = "text-embedding-3-large";
    public static final String TEXT_EMBEDDING_3_SMALL = "text-embedding-3-small";
    public static final String TEXT_EMBEDDING_ADA_002 = "text-embedding-ada-002";

    // -- Image Generation
    public static final String DALL_E_3 = "dall-e-3";
    public static final String DALL_E_2 = "dall-e-2";

    // -- Audio/Speech
    public static final String WHISPER_1 = "whisper-1";
    public static final String TTS_1 = "tts-1";
    public static final String TTS_1_1106 = "tts-1-1106";
    public static final String TTS_1_HD = "tts-1-hd";
    public static final String TTS_1_HD_1106 = "tts-1-hd-1106";

    // -- Base Models (Legacy)
    public static final String BABBAGE_002 = "babbage-002";
    public static final String DAVINCI_002 = "davinci-002";

    // -- Moderation Models
    public static final String OMNI_MODERATION_LATEST = "omni-moderation-latest";
    public static final String OMNI_MODERATION_2024_09_26 = "omni-moderation-2024-09-26";

    @StringDef({GPT_4_5_PREVIEW, GPT_4_5_PREVIEW_2025_02_27, GPT_4_1, GPT_4_1_2025_04_14, GPT_4_1_MINI, GPT_4_1_MINI_2025_04_14, GPT_4_1_NANO, GPT_4_1_NANO_2025_04_14, GPT_4O, GPT_4O_2024_11_20, GPT_4O_2024_08_06, GPT_4O_2024_05_13, GPT_4O_MINI, GPT_4O_MINI_2024_07_18, GPT_4O_AUDIO_PREVIEW, GPT_4O_AUDIO_PREVIEW_2024_10_01, GPT_4O_AUDIO_PREVIEW_2024_12_17, GPT_4O_MINI_AUDIO_PREVIEW, GPT_4O_MINI_AUDIO_PREVIEW_2024_12_17, GPT_4O_TRANSCRIBE, GPT_4O_MINI_TRANSCRIBE, GPT_4O_MINI_TTS, GPT_4O_SEARCH_PREVIEW, GPT_4O_SEARCH_PREVIEW_2025_03_11, GPT_4O_MINI_SEARCH_PREVIEW, GPT_4O_MINI_SEARCH_PREVIEW_2025_03_11, GPT_3_5_TURBO, GPT_3_5_TURBO_16K, GPT_3_5_TURBO_1106, GPT_3_5_TURBO_0125, GPT_3_5_TURBO_INSTRUCT, GPT_3_5_TURBO_INSTRUCT_0914, O1_PREVIEW, O1_PREVIEW_2024_09_12, O1_MINI, O1_MINI_2024_09_12, TEXT_EMBEDDING_3_LARGE, TEXT_EMBEDDING_3_SMALL, TEXT_EMBEDDING_ADA_002, DALL_E_3, DALL_E_2, WHISPER_1, TTS_1, TTS_1_1106, TTS_1_HD, TTS_1_HD_1106, BABBAGE_002, DAVINCI_002, OMNI_MODERATION_LATEST, OMNI_MODERATION_2024_09_26})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChatGPTModel {
    }
}