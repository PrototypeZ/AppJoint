package io.github.prototypez.appjoint.module2;

import android.util.Log;

import io.github.prototypez.appjoint.commons.AppBase;
import io.github.prototypez.appjoint.core.ModuleSpec;

@ModuleSpec(priority = 1)
public class Module2Application extends AppBase {

    @Override
    public void onCreate() {
        super.onCreate();
        // do module2 initialization
        Log.i("module2", "module2 init is called");
    }
}
