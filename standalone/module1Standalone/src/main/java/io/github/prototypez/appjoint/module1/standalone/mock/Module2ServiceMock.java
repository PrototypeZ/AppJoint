package io.github.prototypez.appjoint.module1.standalone.mock;

import android.content.Context;
import android.support.v4.app.Fragment;

import io.github.prototypez.service.module1.callback.Module1Callback;
import io.github.prototypez.service.module2.Module2Service;
import io.github.prototypez.service.module2.entity.Module2Entity;
import io.reactivex.Observable;

public class Module2ServiceMock implements Module2Service {
    @Override
    public void startActivityOfModule2(Context context) {

    }

    @Override
    public Fragment obtainFragmentOfModule2() {
        return null;
    }

    @Override
    public String callMethodSyncOfModule2() {
        return null;
    }

    @Override
    public void callMethodAsyncOfModule2(Module1Callback<Module2Entity> callback) {

    }

    @Override
    public Observable<Module2Entity> observableOfModule2() {
        return null;
    }
}
