package io.github.prototypez.appjoint.module1.standalone.mock

import android.content.Context
import android.support.v4.app.Fragment
import io.github.prototypez.appjoint.commons.T

import io.github.prototypez.service.module1.callback.Module1Callback
import io.github.prototypez.service.module2.Module2Service
import io.github.prototypez.service.module2.entity.Module2Entity
import io.reactivex.Observable

class Module2ServiceMock : Module2Service {
  override fun module2TabFragment(): Fragment? {
    return null
  }

  override fun startActivityOfModule2(context: Context?) {
    T.s("Mock: startActivityOfModule2 called")
  }

  override fun obtainFragmentOfModule2(): Fragment {
    T.s("Mock: obtainFragmentOfModule2 called")
    return Fragment()
  }

  override fun callMethodSyncOfModule2(): String {
    return "Mock<syncMethodResultModule2>"
  }

  override fun callMethodAsyncOfModule2(callback: Module1Callback<Module2Entity>) {
    Thread { callback.onResult(Module2Entity("Mock<asyncMethodResultModule2>")) }.start()
  }

  override fun observableOfModule2(): Observable<Module2Entity> {
    return Observable.just(Module2Entity("Mock<rxJavaResultModule2>"))
  }
}
