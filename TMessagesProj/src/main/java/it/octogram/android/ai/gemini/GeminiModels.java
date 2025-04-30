/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.gemini;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GeminiModels {

    // -- Gemini Series 1
    public static final String GEMINI_1_0_PRO_VISION_LATEST = "gemini-1.0-pro-vision-latest";
    public static final String GEMINI_PRO_VISION = "gemini-pro-vision";
    public static final String GEMINI_1_5_PRO_LATEST = "gemini-1.5-pro-latest";
    public static final String GEMINI_1_5_PRO_001 = "gemini-1.5-pro-001";
    public static final String GEMINI_1_5_PRO_002 = "gemini-1.5-pro-002";
    public static final String GEMINI_1_5_PRO = "gemini-1.5-pro";
    public static final String GEMINI_1_5_FLASH_LATEST = "gemini-1.5-flash-latest";
    public static final String GEMINI_1_5_FLASH_001 = "gemini-1.5-flash-001";
    public static final String GEMINI_1_5_FLASH_001_TUNING = "gemini-1.5-flash-001-tuning";
    public static final String GEMINI_1_5_FLASH = "gemini-1.5-flash";
    public static final String GEMINI_1_5_FLASH_002 = "gemini-1.5-flash-002";
    public static final String GEMINI_1_5_FLASH_8B = "gemini-1.5-flash-8b";
    public static final String GEMINI_1_5_FLASH_8B_001 = "gemini-1.5-flash-8b-001";
    public static final String GEMINI_1_5_FLASH_8B_LATEST = "gemini-1.5-flash-8b-latest";
    public static final String GEMINI_1_5_FLASH_8B_EXP_0827 = "gemini-1.5-flash-8b-exp-0827";
    public static final String GEMINI_1_5_FLASH_8B_EXP_0924 = "gemini-1.5-flash-8b-exp-0924";

    // -- Gemini Series 2 & 2.5
    public static final String GEMINI_2_5_PRO_EXP_03_25 = "gemini-2.5-pro-exp-03-25";
    public static final String GEMINI_2_5_PRO_PREVIEW_03_25 = "gemini-2.5-pro-preview-03-25";
    public static final String GEMINI_2_5_FLASH_PREVIEW_04_17 = "gemini-2.5-flash-preview-04-17";
    public static final String GEMINI_2_0_FLASH_EXP = "gemini-2.0-flash-exp";
    public static final String GEMINI_2_0_FLASH = "gemini-2.0-flash";
    public static final String GEMINI_2_0_FLASH_001 = "gemini-2.0-flash-001";
    public static final String GEMINI_2_0_FLASH_LITE_001 = "gemini-2.0-flash-lite-001";
    public static final String GEMINI_2_0_FLASH_LITE = "gemini-2.0-flash-lite";
    public static final String GEMINI_2_0_FLASH_LITE_PREVIEW_02_05 = "gemini-2.0-flash-lite-preview-02-05";
    public static final String GEMINI_2_0_FLASH_LITE_PREVIEW = "gemini-2.0-flash-lite-preview";
    public static final String GEMINI_2_0_PRO_EXP = "gemini-2.0-pro-exp";
    public static final String GEMINI_2_0_PRO_EXP_02_05 = "gemini-2.0-pro-exp-02-05";
    public static final String GEMINI_EXP_1206 = "gemini-exp-1206";
    public static final String GEMINI_2_0_FLASH_THINKING_EXP_01_21 = "gemini-2.0-flash-thinking-exp-01-21";
    public static final String GEMINI_2_0_FLASH_THINKING_EXP = "gemini-2.0-flash-thinking-exp";
    public static final String GEMINI_2_0_FLASH_THINKING_EXP_1219 = "gemini-2.0-flash-thinking-exp-1219";

    // -- LearnLM Series
    public static final String LEARNLM_1_5_PRO_EXPERIMENTAL = "learnlm-1.5-pro-experimental";
    public static final String LEARNLM_2_0_FLASH_EXPERIMENTAL = "learnlm-2.0-flash-experimental";

    // -- Gemma Series
    public static final String GEMMA_3_1B_IT = "gemma-3-1b-it";
    public static final String GEMMA_3_4B_IT = "gemma-3-4b-it";
    public static final String GEMMA_3_12B_IT = "gemma-3-12b-it";
    public static final String GEMMA_3_27B_IT = "gemma-3-27b-it";

    // -- Embedding Models
    public static final String EMBEDDING_001 = "embedding-001";
    public static final String TEXT_EMBEDDING_004 = "text-embedding-004";
    public static final String GEMINI_EMBEDDING_EXP_03_07 = "gemini-embedding-exp-03-07";
    public static final String GEMINI_EMBEDDING_EXP = "gemini-embedding-exp";

    // -- AQA Model
    public static final String AQA = "aqa";

    // -- Imagen Models
    public static final String IMAGEN_3_0_GENERATE_002 = "imagen-3.0-generate-002";

    // -- Gemini Live Flash Series
    public static final String GEMINI_2_0_FLASH_LIVE_001 = "gemini-2.0-flash-live-001";

    @StringDef({GEMINI_1_0_PRO_VISION_LATEST, GEMINI_PRO_VISION,
            GEMINI_1_5_PRO_LATEST, GEMINI_1_5_PRO_001, GEMINI_1_5_PRO_002, GEMINI_1_5_PRO,
            GEMINI_1_5_FLASH_LATEST, GEMINI_1_5_FLASH_001, GEMINI_1_5_FLASH_001_TUNING, GEMINI_1_5_FLASH, GEMINI_1_5_FLASH_002,
            GEMINI_1_5_FLASH_8B, GEMINI_1_5_FLASH_8B_001, GEMINI_1_5_FLASH_8B_LATEST, GEMINI_1_5_FLASH_8B_EXP_0827, GEMINI_1_5_FLASH_8B_EXP_0924,
            GEMINI_2_5_PRO_EXP_03_25, GEMINI_2_5_PRO_PREVIEW_03_25, GEMINI_2_5_FLASH_PREVIEW_04_17, // Added
            GEMINI_2_0_FLASH_EXP, GEMINI_2_0_FLASH, GEMINI_2_0_FLASH_001,
            GEMINI_2_0_FLASH_LITE_001, GEMINI_2_0_FLASH_LITE, GEMINI_2_0_FLASH_LITE_PREVIEW_02_05, GEMINI_2_0_FLASH_LITE_PREVIEW,
            GEMINI_2_0_PRO_EXP, GEMINI_2_0_PRO_EXP_02_05, GEMINI_EXP_1206,
            GEMINI_2_0_FLASH_THINKING_EXP_01_21, GEMINI_2_0_FLASH_THINKING_EXP, GEMINI_2_0_FLASH_THINKING_EXP_1219,
            LEARNLM_1_5_PRO_EXPERIMENTAL, LEARNLM_2_0_FLASH_EXPERIMENTAL,
            GEMMA_3_1B_IT, GEMMA_3_4B_IT, GEMMA_3_12B_IT, GEMMA_3_27B_IT,
            EMBEDDING_001, TEXT_EMBEDDING_004, GEMINI_EMBEDDING_EXP_03_07, GEMINI_EMBEDDING_EXP,
            AQA, IMAGEN_3_0_GENERATE_002,
            GEMINI_2_0_FLASH_LIVE_001
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface GeminiModel {
    }
}
