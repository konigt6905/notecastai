package com.notecastai.integration.ai.provider.openrouter.dto;

import lombok.Getter;

/**
 * Supported OpenRouter models.
 * maxTokens is the requested completion tokens, not the context window.
 * Prices on OpenRouter drift, check their site if you care about exact numbers.
 */
@Getter
public enum OpenRouterModel {

        // current models, prefer these

        // --- Anthropic Claude 4.6 (1M context) ---
        CLAUDE_OPUS_4_6("anthropic/claude-opus-4.6", 32000, 0.6, true),
        CLAUDE_SONNET_4_6("anthropic/claude-sonnet-4.6", 64000, 0.6, true),

        // --- Anthropic Claude 4.5 ---
        CLAUDE_OPUS_4_5("anthropic/claude-opus-4.5", 32000, 0.6, true),
        CLAUDE_SONNET_4_5("anthropic/claude-sonnet-4.5", 64000, 0.6, true),
        CLAUDE_HAIKU_4_5("anthropic/claude-haiku-4.5", 16384, 0.6, true),

        // --- OpenAI GPT-5 ---
        GPT_5_1("openai/gpt-5.1", 16384, 0.6, true),
        GPT_5_MINI("openai/gpt-5-mini", 16384, 0.6, true),

        // --- Google Gemini 3 ---
        GEMINI_3_PRO("google/gemini-3-pro-preview", 16384, 0.7, true),
        GEMINI_3_FLASH("google/gemini-3-flash-preview", 16384, 0.7, true),

        // --- DeepSeek ---
        DEEPSEEK_V3_2("deepseek/deepseek-v3.2", 16384, 0.7, true),

        // legacy, kept for compatibility

        // --- Anthropic ---
        CLAUDE_3_7_SONNET("anthropic/claude-3.7-sonnet", 64000, 0.6, true),
        CLAUDE_3_OPUS("anthropic/claude-3-opus", 8191, 0.6, true),
        CLAUDE_3_HAIKU("anthropic/claude-3-haiku", 8191, 0.7, true),

        // --- OpenAI ---
        GPT_4O("openai/gpt-4o", 8191, 0.6, true),
        GPT_4O_mini("openai/gpt-4o-mini", 16000, 0.6, true),
        GPT_4_TURBO("openai/gpt-4-turbo", 8191, 0.6, true),

        // --- Google ---
        GEMINI_1_5_PRO("google/gemini-pro-1.5", 16384, 0.7, true),
        GEMINI_1_5_FLASH("google/gemini-flash-1.5", 16384, 0.7, true),
        GEMINI_2_5_FLASH("google/gemini-2.5-flash-preview", 66000, 0.7, true),
        GEMINI_2_5_PRO("google/gemini-2.5-pro-exp-03-25", 30000, 0.7, true),

        // --- Meta ---
        LLAMA_3_1_70B_INSTRUCT("meta-llama/llama-3.1-70b-instruct", 16384, 0.7, true),
        LLAMA_3_1_8B_INSTRUCT("meta-llama/llama-3.1-8b-instruct", 8191, 0.7, true),

        // --- Mistral ---
        MIXTRAL_8X22B_INSTRUCT("mistralai/mixtral-8x22b-instruct", 8191, 0.7, true),
        MISTRAL_LARGE_LATEST("mistralai/mistral-large-latest", 8191, 0.7, true),

        // --- Cohere ---
        COMMAND_R_PLUS("cohere/command-r-plus", 16384, 0.5, true),

        // --- DeepSeek ---
        DEEPSEEK_V2_CHAT("deepseek/deepseek-chat", 16384, 0.7, true),

        // --- Qwen (Alibaba) ---
        QWEN_2_72B_INSTRUCT("qwen/qwen-2-72b-instruct", 8191, 0.7, true),
        QWEN_3_32B("qwen/qwen-3-32b", 8191, 0.7, true),
        QWEN_3_235b("qwen/qwen3-235b-a22b", 20000, 0.7, true),

        // --- Databricks ---
        DBRX_INSTRUCT("databricks/dbrx-instruct", 8191, 0.7, true),

        GEMINI_2_5_FLASH_THINKING("google/gemini-2.5-flash-preview:thinking", 66000, 0.7, true),
        GROK_FAST_1("x-ai/grok-code-fast-1", 66000, 0.7, true),
        GROK_3_MINI("x-ai/grok-3-mini-beta", 30000, 0.7, true),
        CLAUDE_3_5_SONNET("anthropic/claude-3.5-sonnet", 8191, 0.6, true);

        private final String modelId;
        private final int maxTokens;
        private final double temperature;
        private final boolean supportsJsonMode;

        OpenRouterModel(String modelId, int maxTokens, double temperature, boolean supportsJsonMode) {
                this.modelId = modelId;
                this.maxTokens = maxTokens;
                this.temperature = temperature;
                this.supportsJsonMode = supportsJsonMode;
        }

        @Override
        public String toString() {
                return String.format("%s (%s)", name(), modelId);
        }

}