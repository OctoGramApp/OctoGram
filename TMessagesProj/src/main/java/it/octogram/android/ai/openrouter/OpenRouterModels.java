/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.ai.openrouter;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

public class OpenRouterModels {

    // -- ChatGPT Series
    public static final String OPENROUTER_GPT_3_5_TURBO = "openai/gpt-3.5-turbo";

    // -- ArliAI Series
    public static final String ARLIAI_QWQ_32B_RPR_V1 = "arliai/qwq-32b-arliai-rpr-v1:free";

    // -- Agentica Series
    public static final String AGENTICA_DEEPCODER_14B_PREVIEW = "agentica-org/deepcoder-14b-preview:free";

    // -- Moonshot AI Series
    public static final String MOONSHOT_KIMI_VL_A3B_THINKING = "moonshotai/kimi-vl-a3b-thinking:free";
    public static final String MOONSHOT_MOONLIGHT_16B_A3B_INSTRUCT = "moonshotai/moonlight-16b-a3b-instruct:free";

    // -- NVIDIA Nemotron Series
    public static final String NVIDIA_LLAMA_3_1_NEMOTRON_NANO_8B_V1 = "nvidia/llama-3.1-nemotron-nano-8b-v1:free";
    public static final String NVIDIA_LLAMA_3_3_NEMOTRON_SUPER_49B_V1 = "nvidia/llama-3.3-nemotron-super-49b-v1:free";
    public static final String NVIDIA_LLAMA_3_1_NEMOTRON_ULTRA_253B_V1 = "nvidia/llama-3.1-nemotron-ultra-253b-v1:free";
    public static final String NVIDIA_LLAMA_3_1_NEMOTRON_70B_INSTRUCT = "nvidia/llama-3.1-nemotron-70b-instruct:free";

    // -- Meta LLaMA Series
    public static final String META_LLAMA_4_MAVERICK = "meta-llama/llama-4-maverick:free";
    public static final String META_LLAMA_4_SCOUT = "meta-llama/llama-4-scout:free";
    public static final String META_LLAMA_3_3_70B_INSTRUCT = "meta-llama/llama-3.3-70b-instruct:free";
    public static final String META_LLAMA_3_2_3B_INSTRUCT = "meta-llama/llama-3.2-3b-instruct:free";
    public static final String META_LLAMA_3_2_1B_INSTRUCT = "meta-llama/llama-3.2-1b-instruct:free";
    public static final String META_LLAMA_3_2_11B_VISION_INSTRUCT = "meta-llama/llama-3.2-11b-vision-instruct:free";
    public static final String META_LLAMA_3_1_8B_INSTRUCT = "meta-llama/llama-3.1-8b-instruct:free";

    // -- DeepSeek Series
    public static final String DEEPSEEK_V3_BASE = "deepseek/deepseek-v3-base:free";
    public static final String DEEPSEEK_V3_0324 = "deepseek/deepseek-chat-v3-0324:free";
    public static final String DEEPSEEK_R1 = "deepseek/deepseek-r1:free";
    public static final String DEEPSEEK_R1_ZERO = "deepseek/deepseek-r1-zero:free";
    public static final String DEEPSEEK_R1_DISTILL_QWEN_32B = "deepseek/deepseek-r1-distill-qwen-32b:free";
    public static final String DEEPSEEK_R1_DISTILL_QWEN_14B = "deepseek/deepseek-r1-distill-qwen-14b:free";
    public static final String DEEPSEEK_R1_DISTILL_LLAMA_70B = "deepseek/deepseek-r1-distill-llama-70b:free";
    public static final String DEEPSEEK_V3 = "deepseek/deepseek-chat:free";

    // -- Google Gemini Series
    public static final String GOOGLE_GEMINI_2_0_FLASH_EXP = "google/gemini-2.0-flash-exp:free";

    // -- Qwen Series
    public static final String QWEN_QWQ_32B = "qwen/qwq-32b:free";
    public static final String QWEN_QWQ_32B_PREVIEW = "qwen/qwq-32b-preview:free";
    public static final String QWEN2_5_VL_3B_INSTRUCT = "qwen/qwen2.5-vl-3b-instruct:free";
    public static final String QWEN2_5_VL_32B_INSTRUCT = "qwen/qwen2.5-vl-32b-instruct:free";
    public static final String QWEN2_5_VL_72B_INSTRUCT = "qwen/qwen2.5-vl-72b-instruct:free";
    public static final String QWEN2_5_VL_7B_INSTRUCT = "qwen/qwen-2.5-vl-7b-instruct:free";
    public static final String QWEN2_5_CODER_32B_INSTRUCT = "qwen/qwen-2.5-coder-32b-instruct:free";
    public static final String QWEN2_5_7B_INSTRUCT = "qwen/qwen-2.5-7b-instruct:free";
    public static final String QWEN2_5_72B_INSTRUCT = "qwen/qwen-2.5-72b-instruct:free";

    @StringDef({OPENROUTER_GPT_3_5_TURBO, ARLIAI_QWQ_32B_RPR_V1, AGENTICA_DEEPCODER_14B_PREVIEW, MOONSHOT_KIMI_VL_A3B_THINKING, MOONSHOT_MOONLIGHT_16B_A3B_INSTRUCT, NVIDIA_LLAMA_3_1_NEMOTRON_NANO_8B_V1, NVIDIA_LLAMA_3_3_NEMOTRON_SUPER_49B_V1, NVIDIA_LLAMA_3_1_NEMOTRON_ULTRA_253B_V1, NVIDIA_LLAMA_3_1_NEMOTRON_70B_INSTRUCT, META_LLAMA_4_MAVERICK, META_LLAMA_4_SCOUT, META_LLAMA_3_3_70B_INSTRUCT, META_LLAMA_3_2_3B_INSTRUCT, META_LLAMA_3_2_1B_INSTRUCT, META_LLAMA_3_2_11B_VISION_INSTRUCT, META_LLAMA_3_1_8B_INSTRUCT, DEEPSEEK_V3_BASE, DEEPSEEK_V3_0324, DEEPSEEK_R1, DEEPSEEK_R1_ZERO, DEEPSEEK_R1_DISTILL_QWEN_32B, DEEPSEEK_R1_DISTILL_QWEN_14B, DEEPSEEK_R1_DISTILL_LLAMA_70B, DEEPSEEK_V3, GOOGLE_GEMINI_2_0_FLASH_EXP, QWEN_QWQ_32B, QWEN_QWQ_32B_PREVIEW, QWEN2_5_VL_3B_INSTRUCT, QWEN2_5_VL_32B_INSTRUCT, QWEN2_5_VL_72B_INSTRUCT, QWEN2_5_VL_7B_INSTRUCT, QWEN2_5_CODER_32B_INSTRUCT, QWEN2_5_7B_INSTRUCT, QWEN2_5_72B_INSTRUCT})

    @Retention(RetentionPolicy.SOURCE)
    public @interface ChatGPTModel {
    }

    public static final List<String> ALL_MODELS = Arrays.asList(
            OPENROUTER_GPT_3_5_TURBO,
            ARLIAI_QWQ_32B_RPR_V1,
            AGENTICA_DEEPCODER_14B_PREVIEW,
            MOONSHOT_KIMI_VL_A3B_THINKING,
            MOONSHOT_MOONLIGHT_16B_A3B_INSTRUCT,
            NVIDIA_LLAMA_3_1_NEMOTRON_NANO_8B_V1,
            NVIDIA_LLAMA_3_3_NEMOTRON_SUPER_49B_V1,
            NVIDIA_LLAMA_3_1_NEMOTRON_ULTRA_253B_V1,
            NVIDIA_LLAMA_3_1_NEMOTRON_70B_INSTRUCT,
            META_LLAMA_4_MAVERICK,
            META_LLAMA_4_SCOUT,
            META_LLAMA_3_3_70B_INSTRUCT,
            META_LLAMA_3_2_3B_INSTRUCT,
            META_LLAMA_3_2_1B_INSTRUCT,
            META_LLAMA_3_2_11B_VISION_INSTRUCT,
            META_LLAMA_3_1_8B_INSTRUCT,
            DEEPSEEK_V3_BASE,
            DEEPSEEK_V3_0324,
            DEEPSEEK_R1,
            DEEPSEEK_R1_ZERO,
            DEEPSEEK_R1_DISTILL_QWEN_32B,
            DEEPSEEK_R1_DISTILL_QWEN_14B,
            DEEPSEEK_R1_DISTILL_LLAMA_70B,
            DEEPSEEK_V3,
            GOOGLE_GEMINI_2_0_FLASH_EXP,
            QWEN_QWQ_32B,
            QWEN_QWQ_32B_PREVIEW,
            QWEN2_5_VL_3B_INSTRUCT,
            QWEN2_5_VL_32B_INSTRUCT,
            QWEN2_5_VL_72B_INSTRUCT,
            QWEN2_5_VL_7B_INSTRUCT,
            QWEN2_5_CODER_32B_INSTRUCT,
            QWEN2_5_7B_INSTRUCT,
            QWEN2_5_72B_INSTRUCT
    );
}

