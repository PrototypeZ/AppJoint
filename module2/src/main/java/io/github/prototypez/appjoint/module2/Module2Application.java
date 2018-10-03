package io.github.prototypez.appjoint.module2;

import android.app.Application;
import android.util.Log;

import io.github.prototypez.appjoint.core.ModuleSpec;

@ModuleSpec
public class Module2Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // do module2 initialization
        Log.i("module2", "module2 init is called");
    }
}
