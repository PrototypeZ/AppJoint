package io.github.prototypez.appjoint.module1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
        Intent intent = new Intent(context, Module1Activity.class);
        context.startActivity(intent);
    }

    @Override
    public Fragment obtainFragmentOfModule1() {
        Fragment fragment = new Module1Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("param1", "value1");
        bundle.putString("param2", "value2");
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public String callMethodSyncOfModule1() {
        return null;
    }

    @Override
    public void callMethodAsyncOfModule1(Module1Callback<Module1Entity> callback) {

    }

    @Override
    public Observable<Module1Entity> observableOfModule1() {
        return null;
    }
}
