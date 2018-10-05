package io.github.prototypez.appjoint.module1;

import android.app.Application;
import android.util.Log;
import io.github.prototypez.appjoint.commons.AppBase;
import io.github.prototypez.appjoint.core.ModuleSpec;

@ModuleSpec public class Module1Application extends AppBase {

  public static Application INSTANCE;

  @Override public void onCreate() {
    super.onCreate();
    INSTANCE = (Application) getApplicationContext();
    // do module1 initialization
    Log.i("module1", "module1 init is called");
  }
}
