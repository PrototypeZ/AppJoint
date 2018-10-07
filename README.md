# AppJoint
 [ ![Download](https://api.bintray.com/packages/prototypez/maven/app-joint/images/download.svg) ](https://bintray.com/prototypez/maven/app-joint/_latestVersion)

![](https://rawcdn.githack.com/PrototypeZ/AppJoint/master/app-joint-logo.png)

极简 Android 组件化方案。仅包含 **3** 个注解加 **1** 个 API，超低学习成本，支持渐进式组件化。

## 开始接入

1. 在项目根目录的 `build.gradle` 文件中添加 **AppJoint插件** 依赖：

```groovy
buildscript {
    ...
    dependencies {
        ...
        classpath 'io.github.prototypez:app-joint:{latest_version}'
    }
}
```

2. 在主 App 模块和每个组件化的模块添加 **AppJoint** 依赖：

```groovy
dependencies {
    ...
    implementation "io.github.prototypez:app-joint-core:{latest_version}"
}
```
> 代码中的 `{latest_version}` 即最新版本，当前为： [ ![Download](https://api.bintray.com/packages/prototypez/maven/app-joint/images/download.svg) ](https://bintray.com/prototypez/maven/app-joint/_latestVersion)

3. 在主 App 模块应用 **AppJoint插件**： 

```groovy
apply plugin: 'com.android.application'
apply plugin: 'app-joint'
```

## 跨模块方法调用

假设 `router` 模块是所有模块都依赖的公共模块，我们可以在 `router` 模块内定义每个业务模块对外暴露的接口，例如我们定义 `module1` 对外暴露的接口 `Module1Service` ：

```kotlin
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
```

然后我们可以在 `module1` 内实现该接口：

```kotlin
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
```

需要注意的一点是： 需要在实现类上标记 `@ServiceProvider` 注解。

最后，我们可以在其他任何模块获得 `Module1Service` 接口的实例，调用里面的所有方法：

```kotlin
Module1Service service = AppJoint.service(Module1Service.class);
```

## 多模块 Application 逻辑合并

如果您需要您的每个组件化模块可以独立运行, 您可以为每个组件化的模块创建属于该模块的自定义 `Application` 对象，例如：

```kotlin
@ModuleSpec
class Module1Application : Application() {

  override fun onCreate() {
    super.onCreate()
    // do module1 initialization
    Log.i("module1", "module1 init is called")
  }
}
```

需要注意的是，需要在模块的自定义 `Application` 上标记 `@ModuleSpec` 注解。

同时，在主 App 模块的自定义 `Application` 上标记 `@AppSpec` 注解：

```kotlin
@AppSpec
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    Log.i("app", "app init is called")
  }
}
```

## 其他问题

除了上面介绍的功能，组件化还涉及许多其它问题，但是这些内容已经不属于 **AppJoint** 的范畴了，它们包括：

+ 如何独立编译启动组件化模块，以及切换模块的独立编译模式与全量启动模式。
+ 如何分离模块的独立启动相关逻辑和代码，使 App 处于全量编译时这些逻辑不会被打包进去
+ 如何在模块独立编译模式时，调用其它模块的相关代码也能正常工作。

## LICENSE

    Copyright (c) 2016-present, SateState Contributors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.