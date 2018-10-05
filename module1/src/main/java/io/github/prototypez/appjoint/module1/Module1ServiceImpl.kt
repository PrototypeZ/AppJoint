package io.github.prototypez.appjoint.module1

import android.content.Context
import android.support.v4.app.Fragment
import io.github.prototypez.appjoint.core.ServiceProvider
import io.github.prototypez.service.module1.Module1Service
import io.github.prototypez.service.module1.callback.Module1Callback
import io.github.prototypez.service.module1.entity.Module1Entity
import io.reactivex.Observable

@ServiceProvider
class Module1ServiceImpl : Module1Service {
  override fun startActivityOfModule1(context: Context) {
    Module1Activity.start(context)
  }

  override fun obtainFragmentOfModule1(): Fragment {
    return Module1Fragment.newInstance()
  }

  override fun callMethodSyncOfModule1(): String {
    return "syncMethodResultModule1"
  }

  override fun callMethodAsyncOfModule1(callback: Module1Callback<Module1Entity>) {
    Thread { callback.onResult(Module1Entity("asyncMethodResultModule1")) }.start()
  }

  override fun observableOfModule1(): Observable<Module1Entity> {
    return Observable.just(Module1Entity("rxJavaResultModule1"))
  }
}
