package io.github.prototypez.appjoint.module1;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import io.github.prototypez.appjoint.core.ModuleSpec;

@ModuleSpec("module1")
public class Module1Application extends Application {

    public static Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        // do module1 initialization
        Log.i("module1", "module1 init is called");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        INSTANCE = (Application) base.getApplicationContext();
    }
}
