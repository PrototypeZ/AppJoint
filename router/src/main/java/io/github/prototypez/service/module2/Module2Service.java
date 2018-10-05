package io.github.prototypez.service.module2;

import android.content.Context;
import android.support.v4.app.Fragment;
import io.github.prototypez.service.module2.callback.Module2Callback;
import io.github.prototypez.service.module2.entity.Module2Entity;
import io.reactivex.Observable;

public interface Module2Service {

    /*
     * 启动 module2 模块的 Activity
     */
    void startActivityOfModule2(Context context);

    /*
     * 调用 module2 模块的 Fragment
     */
    Fragment obtainFragmentOfModule2();

    /**
     * 普通的同步方法调用
     */
    String callMethodSyncOfModule2();

    /**
     * 以 Callback 形式封装的异步方法
     */
    void callMethodAsyncOfModule2(Module2Callback<Module2Entity> callback);

    /**
     * 以 RxJava 形式封装的异步方法
     */
    Observable<Module2Entity> observableOfModule2();

    Fragment module2TabFragment();
}
