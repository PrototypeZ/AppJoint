package io.github.prototypez.appjoint.module2.standalone

import android.util.Log

import io.github.prototypez.appjoint.module2.Module2Application
import io.github.prototypez.appjoint.module2.Services
import io.github.prototypez.appjoint.module2.standalone.mock.AppServiceMock
import io.github.prototypez.appjoint.module2.standalone.mock.Module1ServiceMock

class Module2StandaloneApplication : Module2Application() {

  override fun onCreate() {
    // module2 init inside super.onCreate()
    super.onCreate()
    // initialization only used for running module2 standalone
    Log.i("module2Standalone", "module2Standalone init is called")
    Services.sAppService = AppServiceMock()
    Services.sModule1Service = Module1ServiceMock()
  }
}
