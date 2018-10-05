package io.github.prototypez.appjoint.module1

import android.util.Log
import io.github.prototypez.appjoint.commons.AppBase
import io.github.prototypez.appjoint.core.ModuleSpec

@ModuleSpec
open class Module1Application : AppBase() {

  override fun onCreate() {
    super.onCreate()
    // do module1 initialization
    Log.i("module1", "module1 init is called")
  }
}
