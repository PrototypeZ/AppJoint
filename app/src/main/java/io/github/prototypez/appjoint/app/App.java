package io.github.prototypez.appjoint.app;

import android.app.Application;
import android.content.Context;

import io.github.prototypez.appjoint.core.AppSpec;

/**
 * Created by zhounl on 2017/11/28.
 */
@AppSpec
public class App extends Application {

    public static Application INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        INSTANCE = (Application) base.getApplicationContext();
    }

}
