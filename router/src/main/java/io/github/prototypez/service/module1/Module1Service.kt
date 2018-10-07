package io.github.prototypez.service.module1

import android.content.Context
import android.support.v4.app.Fragment

import io.github.prototypez.service.module1.callback.Module1Callback
import io.github.prototypez.service.module1.entity.Module1Entity
import io.reactivex.Observable

interface Module1Service {

  /*
     * 启动 moduel1 模块的 Activity
     */
  fun startActivityOfModule1(context: Context)

  /*
     * 调用 module1 模块的 Fragment
     */
  fun obtainFragmentOfModule1(): Fragment

  /**
   * 普通的同步方法调用
   */
  fun callMethodSyncOfModule1(): String

  /**
   * 以 Callback 形式封装的异步方法
   */
  fun callMethodAsyncOfModule1(callback: Module1Callback<Module1Entity>)

  /**
   * 以 RxJava 形式封装的异步方法
   */
  fun observableOfModule1(): Observable<Module1Entity>
}
