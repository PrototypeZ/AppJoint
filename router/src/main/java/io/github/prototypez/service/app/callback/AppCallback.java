package io.github.prototypez.service.app.callback;

public interface AppCallback<T> {
    void onResult(T data);
}
