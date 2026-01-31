package com.yoav_s.repository.BASE.AI;

import com.yoav_s.repository.AI.GeminiRepository;

import java.util.concurrent.Executor;

public class AiRepositoryFactory {

    /**
     * Creates and returns a concrete implementation of BaseAiRepository based on the chosen provider.
     *
     * @param provider The AI provider to use (e.g., AiProvider.GEMINI).
     * @param executor The main thread executor for handling callbacks safely.
     * @return An instance of a class that implements BaseAiRepository.
     */
    public static BaseAiRepository createRepository(AiProvider provider, Executor executor) {
        switch (provider) {
            case GEMINI:
                return new GeminiRepository(executor);
            case CHAT_GPT:
                // נניח שה-SDK של ChatGPT צריך גם Context
                // return new ChatGPTRepositoryImpl(executor, applicationContext);
                // כרגע נחזיר null כדוגמה
                throw new UnsupportedOperationException("ChatGPT is not yet implemented.");
            case CLAUDE:
                throw new UnsupportedOperationException("Claude AI is not yet implemented.");
            case QWEN:
                throw new UnsupportedOperationException("QWEN AI is not yet implemented.");
            default:
                throw new IllegalArgumentException("Unknown AI provider: " + provider);
        }
    }
}