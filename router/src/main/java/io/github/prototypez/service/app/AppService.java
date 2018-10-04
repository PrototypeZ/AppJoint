package io.github.prototypez.service.app;


import android.content.Context;
import android.support.v4.app.Fragment;

import io.github.prototypez.service.app.callback.AppCallback;
import io.github.prototypez.service.app.entity.AppEntity;
import io.reactivex.Observable;

public interface AppService {

    /**
     * 普通的同步方法调用
     */
    String callMethodSyncOfApp();

    /**
     * 以 RxJava 形式封装的异步方法
     */
    Observable<AppEntity> observableOfApp();

    /**
     * 以 Callback 形式封装的异步方法
     */
    void callMethodAsync2OfApp(AppCallback<AppEntity> callback);

    /*
     * 启动 App 模块的 Activity
     */
    void startActivityOfApp(Context context);

    /*
     * 调用 App 模块的 Fragment
     */
    Fragment obtainFragmentOfModule1();
}
