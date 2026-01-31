package com.yoav_s.repository.BASE.DB;

public class TaskResult<T> {
    private final T result;

    public TaskResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }
}