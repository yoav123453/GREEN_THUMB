package com.yoav_s.repository.BASE.AI;

import android.graphics.Bitmap;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * An interface that defines the basic capabilities of a Generative AI repository.
 * Any specific AI provider implementation (like Gemini or OpenAI) should implement this interface.
 */
public interface BaseAiRepository {

    /**
     * Generates a text response from a simple text prompt.
     * This is the most common use case.
     *
     * @param prompt The text query to send to the AI model.
     * @return A ListenableFuture that will resolve to the AI's string response.
     */
    ListenableFuture<String> generateText(String prompt);

    /**
     * Generates a text response from a multimodal prompt (text and image).
     *
     * @param prompt The text query accompanying the image.
     * @param image The image to be analyzed by the model.
     * @return A ListenableFuture that will resolve to the AI's string response.
     */
    ListenableFuture<String> generateTextFromImage(String prompt, Bitmap image);

    /**
     * Sends a message as part of an ongoing conversation.
     * The repository is responsible for maintaining the chat history.
     *
     * @param prompt The new message to send in the chat.
     * @return A ListenableFuture that will resolve to the AI's response in the conversation.
     */
    ListenableFuture<String> continueChat(String prompt);

    /**
     * Sends a message as part of an ongoing multimodal conversation.
     * The repository is responsible for maintaining the chat history.
     *
     * @param prompt The new message to send in the chat.
     * @param image The image to be analyzed by the model.
     * @return A ListenableFuture that will resolve to the AI's response in the conversation.
     */
    ListenableFuture<String> continueChatWithImage(String prompt, Bitmap image);
}