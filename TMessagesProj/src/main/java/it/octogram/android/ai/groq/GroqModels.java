/*
 * This file is AUTO-GENERATED — DO NOT EDIT MANUALLY.
 * To update it, modify the Go template source and regenerate.
 *
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.groq;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

public class GroqModels {

    // -- Allam-2-7b Series
    public static final String ALLAM_2_7B = "allam-2-7b";

    // -- Compound-beta Series
    public static final String COMPOUND_BETA = "compound-beta";

    // -- Compound-beta-mini Series
    public static final String COMPOUND_BETA_MINI = "compound-beta-mini";

    // -- Deepseek-r1-distill-llama-70b Series
    public static final String DEEPSEEK_R1_DISTILL_LLAMA_70B = "deepseek-r1-distill-llama-70b";

    // -- Distil-whisper-large-v3-en Series
    public static final String DISTIL_WHISPER_LARGE_V3_EN = "distil-whisper-large-v3-en";

    // -- Gemma2-9b-it Series
    public static final String GEMMA2_9B_IT = "gemma2-9b-it";

    // -- Llama-3.1-8b-instant Series
    public static final String LLAMA_3_1_8B_INSTANT = "llama-3.1-8b-instant";

    // -- Llama-3.3-70b-versatile Series
    public static final String LLAMA_3_3_70B_VERSATILE = "llama-3.3-70b-versatile";

    // -- Llama-guard-3-8b Series
    public static final String LLAMA_GUARD_3_8B = "llama-guard-3-8b";

    // -- Llama3-70b-8192 Series
    public static final String LLAMA3_70B_8192 = "llama3-70b-8192";

    // -- Llama3-8b-8192 Series
    public static final String LLAMA3_8B_8192 = "llama3-8b-8192";

    // -- Meta-llama Series
    public static final String META_LLAMA_LLAMA_PROMPT_GUARD_2_22M = "meta-llama/llama-prompt-guard-2-22m";
    public static final String META_LLAMA_LLAMA_PROMPT_GUARD_2_86M = "meta-llama/llama-prompt-guard-2-86m";
    public static final String META_LLAMA_LLAMA_GUARD_4_12B = "meta-llama/llama-guard-4-12b";
    public static final String META_LLAMA_LLAMA_4_MAVERICK_17B_128E_INSTRUCT = "meta-llama/llama-4-maverick-17b-128e-instruct";
    public static final String META_LLAMA_LLAMA_4_SCOUT_17B_16E_INSTRUCT = "meta-llama/llama-4-scout-17b-16e-instruct";

    // -- Mistral-saba-24b Series
    public static final String MISTRAL_SABA_24B = "mistral-saba-24b";

    // -- Playai-tts Series
    public static final String PLAYAI_TTS = "playai-tts";

    // -- Playai-tts-arabic Series
    public static final String PLAYAI_TTS_ARABIC = "playai-tts-arabic";

    // -- Qwen-qwq-32b Series
    public static final String QWEN_QWQ_32B = "qwen-qwq-32b";

    // -- Whisper-large-v3 Series
    public static final String WHISPER_LARGE_V3 = "whisper-large-v3";

    // -- Whisper-large-v3-turbo Series
    public static final String WHISPER_LARGE_V3_TURBO = "whisper-large-v3-turbo";

    @StringDef({
        ALLAM_2_7B,
        COMPOUND_BETA,
        COMPOUND_BETA_MINI,
        DEEPSEEK_R1_DISTILL_LLAMA_70B,
        DISTIL_WHISPER_LARGE_V3_EN,
        GEMMA2_9B_IT,
        LLAMA_3_1_8B_INSTANT,
        LLAMA_3_3_70B_VERSATILE,
        LLAMA_GUARD_3_8B,
        LLAMA3_70B_8192,
        LLAMA3_8B_8192,
        META_LLAMA_LLAMA_PROMPT_GUARD_2_22M,
        META_LLAMA_LLAMA_PROMPT_GUARD_2_86M,
        META_LLAMA_LLAMA_GUARD_4_12B,
        META_LLAMA_LLAMA_4_MAVERICK_17B_128E_INSTRUCT,
        META_LLAMA_LLAMA_4_SCOUT_17B_16E_INSTRUCT,
        MISTRAL_SABA_24B,
        PLAYAI_TTS,
        PLAYAI_TTS_ARABIC,
        QWEN_QWQ_32B,
        WHISPER_LARGE_V3,
        WHISPER_LARGE_V3_TURBO
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface GroqModel {
    }

    public static final List<String> ALL_MODELS = Arrays.asList(
        ALLAM_2_7B,
        COMPOUND_BETA,
        COMPOUND_BETA_MINI,
        DEEPSEEK_R1_DISTILL_LLAMA_70B,
        DISTIL_WHISPER_LARGE_V3_EN,
        GEMMA2_9B_IT,
        LLAMA_3_1_8B_INSTANT,
        LLAMA_3_3_70B_VERSATILE,
        LLAMA_GUARD_3_8B,
        LLAMA3_70B_8192,
        LLAMA3_8B_8192,
        META_LLAMA_LLAMA_PROMPT_GUARD_2_22M,
        META_LLAMA_LLAMA_PROMPT_GUARD_2_86M,
        META_LLAMA_LLAMA_GUARD_4_12B,
        META_LLAMA_LLAMA_4_MAVERICK_17B_128E_INSTRUCT,
        META_LLAMA_LLAMA_4_SCOUT_17B_16E_INSTRUCT,
        MISTRAL_SABA_24B,
        PLAYAI_TTS,
        PLAYAI_TTS_ARABIC,
        QWEN_QWQ_32B,
        WHISPER_LARGE_V3,
        WHISPER_LARGE_V3_TURBO
    );
}