package io.github.prototypez.appjoint.app

import android.util.Log
import io.github.prototypez.appjoint.commons.AppBase
import io.github.prototypez.appjoint.core.AppSpec

/**
 * Created by zhounl on 2017/11/28.
 */
@AppSpec
class App : AppBase() {
  override fun onCreate() {
    super.onCreate()
    Log.i("app", "app init is called")
  }
}
