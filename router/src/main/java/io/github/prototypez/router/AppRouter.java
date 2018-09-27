package io.github.prototypez.router;


import io.github.prototypez.router.app.Callback;
import io.reactivex.Observable;

public interface AppRouter {

    /**
     * 普通的同步方法调用
     */
    String syncMethodOfApp();

    /**
     * 以 RxJava 形式封装的异步方法
     */
    Observable<String> asyncMethod1OfApp();

    /**
     * 以 Callback 形式封装的异步方法
     */
    void asyncMethod2OfApp(Callback<String> callback);
}
