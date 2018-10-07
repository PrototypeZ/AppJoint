package io.github.prototypez.appjoint.module2.standalone.mock

import android.content.Context
import android.support.v4.app.Fragment
import io.github.prototypez.appjoint.commons.T
import io.github.prototypez.service.module1.Module1Service
import io.github.prototypez.service.module1.callback.Module1Callback
import io.github.prototypez.service.module1.entity.Module1Entity
import io.reactivex.Observable

class Module1ServiceMock : Module1Service {

  override fun startActivityOfModule1(context: Context) {
    T.s("Mock: startActivityOfModule1 called")
  }

  override fun obtainFragmentOfModule1(): Fragment {
    T.s("Mock: obtainFragmentOfModule1 called")
    return Fragment()
  }

  override fun callMethodSyncOfModule1(): String {
    return "Mock<syncMethodResultModule1>"
  }

  override fun callMethodAsyncOfModule1(callback: Module1Callback<Module1Entity>) {
    Thread { callback.onResult(Module1Entity("Mock<asyncMethodResultModule1>")) }.start()
  }

  override fun observableOfModule1(): Observable<Module1Entity> {
    return Observable.just(Module1Entity("Mock<rxJavaResultModule1>"))
  }
}
