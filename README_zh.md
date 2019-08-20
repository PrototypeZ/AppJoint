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

  /**
   * 启动 moduel1 模块的 Activity
   */
  fun startActivityOfModule1(context: Context)

  /**
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
@ModuleSpec( priority = 1 ) // 支持指定初始化的优先级， 如果不指定优先级，模块以不可知的顺序随机初始化
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

**AppJoint** 可以保证，当标记了 `@AppSpec` 的类被系统回调属于 `Application` 的某个生命周期函数（例如 `onCreate`、 `attachBaseContext`）时，那些标记了 `@ModuleSpec` 的类也会被回调相同的生命周期方法。 


## 跨模块接口的多种实现

我们可以为跨模块的接口提供多种实现，只需要在实现类上标记 `@ServiceProvider` 注解的同时指定具体的命名：

```kotlin
@ServiceProvider("anotherImpl")
class Module1ServiceAnotherImpl : Module1Service {
  ...
}
```

然后，我们可以在需要获得 `Module1Service` 接口实例的地方，传入需要具体的实现类的名字：

```kotlin
Module1Service service = AppJoint.service(Module1Service.class, "anotherImpl");
```

## 组件化的其他问题

除了上面介绍的功能，组件化还涉及许多其它问题，但是这些内容已经不属于 **AppJoint** 的范畴了，它们包括：

+ 如何独立编译启动组件化模块，以及切换模块的独立编译模式与全量启动模式。
+ 如何分离模块的独立启动相关逻辑和代码，使 App 处于全量编译时这些逻辑不会被打包进去
+ 如何在模块独立编译模式时，调用其它模块的相关代码也能正常工作。
+ 等等...

如何配合 **AppJoint** 实现一个完整的组件化方案，欢迎阅读：

 [『回归初心：极简 Android 组件化方案 — AppJoint』](https://juejin.im/post/5bb9c0d55188255c7566e1e2)

## FAQ

+ Q: AppJoint 支持 Instant Run 吗？
  
  A: 支持，请放心使用。

+ Q: 需要配置 Proguard 规则吗？

  A: 不需要。

## 常见问题

+ 在编译的过程中报错， `AppJoint class file not found, please check "io.github.prototypez:app-joint-core:{latest_version}" is in your dependency graph.`

  解决方案： 首先确定在应用了 `apply plugin: 'app-joint'` 的模块内，确实可以访问到 `"io.github.prototypez:app-joint-core:{latest_version}"` 这个依赖，然后确保插件应用在其它插件之前。


## LICENSE

    Copyright (c) 2016-present, AppJoint Contributors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.