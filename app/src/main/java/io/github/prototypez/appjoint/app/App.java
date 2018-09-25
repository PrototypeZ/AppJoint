package io.github.prototypez.appjoint.app;

import android.app.Application;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.appjoint.core.ModulesSpec;

/**
 * Created by zhounl on 2017/11/28.
 */
@ModulesSpec({"module1", "module2"})
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppJoint.get().onCreate();
    }


}
