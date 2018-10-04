package io.github.prototypez.appjoint.app;


import android.content.Context;
import android.support.v4.app.Fragment;

import io.github.prototypez.appjoint.core.ServiceProvider;
import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.app.callback.AppCallback;
import io.github.prototypez.service.app.entity.AppEntity;
import io.reactivex.Observable;

@ServiceProvider
public class AppServiceImpl implements AppService {

    @Override
    public String callMethodSyncOfApp() {
        return "syncMethodResult";
    }

    @Override
    public Observable<AppEntity> observableOfApp() {
        return Observable.just(new AppEntity("asyncMethod1Result"));
    }

    @Override
    public void callMethodAsync2OfApp(final AppCallback<AppEntity> callback) {
        new Thread(() -> callback.onResult(new AppEntity("asyncMethod2Result"))).start();
    }

    @Override
    public void startActivityOfApp(Context context) {

    }

    @Override
    public Fragment obtainFragmentOfModule1() {
        return null;
    }
}
