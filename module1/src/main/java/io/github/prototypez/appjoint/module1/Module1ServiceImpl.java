package io.github.prototypez.appjoint.module1;

import android.content.Context;
import android.support.v4.app.Fragment;
import io.github.prototypez.appjoint.core.ServiceProvider;
import io.github.prototypez.service.module1.Module1Service;
import io.github.prototypez.service.module1.callback.Module1Callback;
import io.github.prototypez.service.module1.entity.Module1Entity;
import io.reactivex.Observable;

@ServiceProvider
public class Module1ServiceImpl implements Module1Service {
    @Override
    public void startActivityOfModule1(Context context) {
        Module1Activity.Companion.start(context);
    }

    @Override
    public Fragment obtainFragmentOfModule1() {
        return Module1Fragment.Companion.newInstance();
    }

    @Override
    public String callMethodSyncOfModule1() {
        return "syncMethodResultModule1";
    }

    @Override
    public void callMethodAsyncOfModule1(Module1Callback<Module1Entity> callback) {
        new Thread(() -> callback.onResult(new Module1Entity("asyncMethodResultModule1"))).start();
    }

    @Override
    public Observable<Module1Entity> observableOfModule1() {
        return Observable.just(new Module1Entity("rxJavaResultModule1"));
    }
}
