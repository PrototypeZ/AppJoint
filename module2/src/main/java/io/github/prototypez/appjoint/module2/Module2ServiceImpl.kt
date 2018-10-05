package io.github.prototypez.appjoint.module2

import android.content.Context
import android.support.v4.app.Fragment
import io.github.prototypez.appjoint.core.ServiceProvider
import io.github.prototypez.service.module2.Module2Service
import io.github.prototypez.service.module2.callback.Module2Callback
import io.github.prototypez.service.module2.entity.Module2Entity
import io.reactivex.Observable

@ServiceProvider
class Module2ServiceImpl : Module2Service {
  override fun module2TabFragment(): Fragment {
    return Module2TabFragment.newInstance()
  }

  override fun startActivityOfModule2(context: Context?) {
    Module2Activity.start(context!!)
  }

  override fun obtainFragmentOfModule2(): Fragment {
    return Module2Fragment.newInstance()
  }

  override fun callMethodSyncOfModule2(): String {
    return "syncMethodResultModule2"
  }

  override fun callMethodAsyncOfModule2(callback: Module2Callback<Module2Entity>) {
    Thread { callback.onResult(Module2Entity("asyncMethodResultModule2")) }.start()
  }

  override fun observableOfModule2(): Observable<Module2Entity> {
    return Observable.just(Module2Entity("rxJavaResultModule2"))
  }
}