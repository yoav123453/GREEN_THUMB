package com.yoav_s.viewmodel.BASE.AI;

import android.app.Application;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * A base ViewModel for screens that interact with a Generative AI model.
 * It handles the common logic of managing UI state (loading, success, error)
 * and processing asynchronous responses from an AI repository.
 *
 * @param <T> The type of the result expected from the AI (e.g., String).
 */
public abstract class BaseAiViewModel<T> extends AndroidViewModel {

    // --- UI State Management ---
    // Protected so that child classes can access them if needed.

    // Mutable LiveData, private to this base class. Only we can change the state.
    protected final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    // Public, immutable LiveData for the UI to observe.
    public final LiveData<Boolean> isLoading = _isLoading;

    // LiveData for successful results.
    protected final MutableLiveData<T> _successResult = new MutableLiveData<>();
    public final LiveData<T> successResult = _successResult;

    // LiveData for error messages.
    protected final MutableLiveData<String> _errorResult = new MutableLiveData<>();
    public final LiveData<String> errorResult = _errorResult;

    public BaseAiViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * A protected helper method that child ViewModels will use to execute an AI task.
     * It handles setting the loading state and attaching the success/error callback.
     *
     * @param futureTask The ListenableFuture returned from the AI repository.
     */
    protected void executeAiTask(ListenableFuture<T> futureTask) {
        // 1. Set the UI to loading state
        _isLoading.postValue(true);

        // 2. Add the callback to handle the future result
        Futures.addCallback(futureTask, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                // On success, post the result and stop loading
                _successResult.postValue(result);
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                // On failure, post the error message and stop loading
                _errorResult.postValue(t.getMessage());
                _isLoading.postValue(false);
            }
        }, ContextCompat.getMainExecutor(getApplication()));
    }
}