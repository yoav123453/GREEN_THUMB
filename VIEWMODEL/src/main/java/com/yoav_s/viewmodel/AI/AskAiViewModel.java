package com.yoav_s.viewmodel.AI;

import android.app.Application;
import android.graphics.Bitmap;

import com.google.common.util.concurrent.ListenableFuture;
import com.yoav_s.helper.StringUtil;
import com.yoav_s.repository.BASE.AI.AiProvider;
import com.yoav_s.repository.BASE.AI.AiRepositoryFactory;
import com.yoav_s.repository.BASE.AI.BaseAiRepository;
import com.yoav_s.viewmodel.BASE.AI.BaseAiViewModel;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * A concrete ViewModel for a screen that interacts with a Generative AI.
 * It inherits all the state management logic (loading, success, error) from BaseAiViewModel
 * and provides specific public methods for the UI to call for each action.
 */
public class AskAiViewModel extends BaseAiViewModel<String> {

    private final BaseAiRepository aiRepository;

    public AskAiViewModel(@NonNull Application application) {
        super(application);

        // Define which AI provider to use. This can be configured elsewhere in a real app.
        AiProvider currentProvider = AiProvider.GEMINI;

        // Use the factory to create the correct repository instance.
        // The ViewModel doesn't know or care that it's a GeminiRepositoryImpl.
        this.aiRepository = AiRepositoryFactory.createRepository(
                currentProvider,
                ContextCompat.getMainExecutor(application)
        );
    }

    // --- Public "Action" Methods for the UI (Activity/Fragment) to call ---

    /**
     * 1. Performs a simple text-to-text generation.
     * This is for one-shot questions, not part of an ongoing conversation.
     *
     * @param prompt The user's text question.
     */
    public void generateText(String prompt) {
        if (StringUtil.isNullOrEmpty(prompt.trim()) /*prompt == null || prompt.trim().isEmpty()*/ ) {
            _errorResult.postValue("Prompt cannot be empty.");
            return;
        }
        // Get the "Future" from the repository
        ListenableFuture<String> futureResponse = aiRepository.generateText(prompt);
        // Let the BaseViewModel handle the rest
        executeAiTask(futureResponse);
    }

    /**
     * 2. Performs a multimodal (text + image) generation.
     * This is for one-shot questions about an image.
     *
     * @param prompt The text prompt accompanying the image.
     * @param image  The image to be analyzed.
     */
    public void generateTextFromImage(String prompt, Bitmap image) {
        if (prompt == null || prompt.trim().isEmpty() || image == null) {
            _errorResult.postValue("Prompt and image cannot be empty.");
            return;
        }
        ListenableFuture<String> futureResponse = aiRepository.generateTextFromImage(prompt, image);
        executeAiTask(futureResponse);
    }

    /**
     * 3. Sends a new message in a text-only conversation.
     * The repository will remember the previous turns of the chat.
     *
     * @param prompt The new message from the user.
     */
    public void continueChat(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            _errorResult.postValue("Message cannot be empty.");
            return;
        }
        ListenableFuture<String> futureResponse = aiRepository.continueChat(prompt);
        executeAiTask(futureResponse);
    }

    /**
     * 4. Sends a new message with an image in a conversation.
     * The repository will remember the previous turns of the chat.
     *
     * @param prompt The new text message.
     * @param image  The accompanying image.
     */
    public void continueChatWithImage(String prompt, Bitmap image) {
        if (prompt == null || prompt.trim().isEmpty() || image == null) {
            _errorResult.postValue("Message and image cannot be empty.");
            return;
        }
        ListenableFuture<String> futureResponse = aiRepository.continueChatWithImage(prompt, image);
        executeAiTask(futureResponse);
    }
}