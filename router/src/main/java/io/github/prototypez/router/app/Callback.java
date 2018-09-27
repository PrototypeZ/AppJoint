package io.github.prototypez.router.app;

public interface Callback<T> {
    void onResult(T data);
}
