package io.github.prototypez.appjoint.module1.standalone;

import android.util.Log;

import io.github.prototypez.appjoint.module1.Module1Application;

public class Module1StandaloneApplication extends Module1Application {

    @Override
    public void onCreate() {
        // module1 init inside super.onCreate()
        super.onCreate();
        // initialization only used for running module1 standalone
        Log.i("module1Standalone", "module1Standalone init is called");
    }
}
