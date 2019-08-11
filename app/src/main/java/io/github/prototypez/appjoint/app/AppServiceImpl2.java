package io.github.prototypez.appjoint.app;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import io.github.prototypez.appjoint.core.ServiceProvider;
import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.app.callback.AppCallback;
import io.github.prototypez.service.app.entity.AppEntity;
import io.reactivex.Observable;

/**
 * AppService 的另外一个实现
 */
@ServiceProvider("another") public class AppServiceImpl2 implements AppService {

  @Override public String callMethodSyncOfApp() {
    return "syncMethodResultApp(another implementation)";
  }

  @Override public Observable<AppEntity> observableOfApp() {
    return Observable.just(new AppEntity("rxJavaResultApp(another implementation)"));
  }

  @Override public void callMethodAsyncOfApp(final AppCallback<AppEntity> callback) {
    new Thread(() -> callback.onResult(
        new AppEntity("asyncMethodResultApp(another implementation)"))).start();
  }

  @Override public void startActivityOfApp(Context context) {
    Intent intent = new Intent(context, LegacyActivity.class);
    context.startActivity(intent);
  }

  @Override public Fragment obtainFragmentOfApp() {
    return LegacyFragment.Companion.newInstance();
  }
}
