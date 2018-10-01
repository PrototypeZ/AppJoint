package io.github.prototypez.module2.standalone;

import android.util.Log;

import io.github.prototypez.appjoint.module2.Module2Application;

public class Module2StandaloneApplication extends Module2Application {

    @Override
    public void onCreate() {
        // module1 init inside super.onCreate()
        super.onCreate();
        // initialization only used for running module1 standalone
        Log.i("module2Standalone", "module2Standalone init is called");
    }
}
