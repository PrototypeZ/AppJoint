package io.github.prototypez.appjoint.app;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import io.github.prototypez.appjoint.core.ServiceProvider;
import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.app.callback.AppCallback;
import io.github.prototypez.service.app.entity.AppEntity;
import io.reactivex.Observable;

/**
 * 组件化过程不可能一蹴而就，组件化的过程是 App 模块渐渐 “瘦身”的过程
 * 最终 App 模块仅仅作为整个应用的一个 “application 壳”，但是组件化
 * 的过程是缓慢的，我们要允许在组件化的过程中，子模块依然可以调用 App
 * 模块内的方法
 */
@ServiceProvider
public class AppServiceImpl implements AppService {

    @Override public String callMethodSyncOfApp() {
        return "syncMethodResultApp";
    }

    @Override public Observable<AppEntity> observableOfApp() {
        return Observable.just(new AppEntity("rxJavaResultApp"));
    }

    @Override public void callMethodAsyncOfApp(final AppCallback<AppEntity> callback) {
        new Thread(() -> callback.onResult(new AppEntity("asyncMethodResultApp"))).start();
    }

    @Override public void startActivityOfApp(Context context) {
        Intent intent = new Intent(context, LegacyActivity.class);
        context.startActivity(intent);
    }

    @Override public Fragment obtainFragmentOfApp() {
        return LegacyFragment.Companion.newInstance();
    }
}
