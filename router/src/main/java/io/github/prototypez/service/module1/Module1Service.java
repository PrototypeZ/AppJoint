package io.github.prototypez.service.module1;

import android.content.Context;
import android.support.v4.app.Fragment;

import io.github.prototypez.service.module1.callback.Module1Callback;
import io.github.prototypez.service.module1.entity.Module1Entity;
import io.reactivex.Observable;

public interface Module1Service {

    /*
     * 启动 moduel1 模块的 Activity
     */
    void startActivityOfModule1(Context context);

    /*
     * 调用 module1 模块的 Fragment
     */
    Fragment obtainFragmentOfModule1();

    /**
     * 普通的同步方法调用
     */
    String callMethodSyncOfModule1();

    /**
     * 以 Callback 形式封装的异步方法
     */
    void callMethodAsyncOfModule1(Module1Callback<Module1Entity> callback);

    /**
     * 以 RxJava 形式封装的异步方法
     */
    Observable<Module1Entity> observableOfModule1();
}
