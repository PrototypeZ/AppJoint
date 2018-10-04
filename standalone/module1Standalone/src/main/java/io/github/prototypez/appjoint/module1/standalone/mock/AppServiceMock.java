package io.github.prototypez.appjoint.module1.standalone.mock;

import android.content.Context;
import android.support.v4.app.Fragment;
import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.app.callback.AppCallback;
import io.github.prototypez.service.app.entity.AppEntity;
import io.reactivex.Observable;

public class AppServiceMock implements AppService {

    @Override
    public String callMethodSyncOfApp() {
      return null;
    }

    @Override public Observable<AppEntity> observableOfApp() {
      return null;
    }

    @Override public void callMethodAsyncOfApp(AppCallback<AppEntity> callback) {

    }

  @Override public void startActivityOfApp(Context context) {

  }

  @Override public Fragment obtainFragmentOfApp() {
    return null;
    }
}
