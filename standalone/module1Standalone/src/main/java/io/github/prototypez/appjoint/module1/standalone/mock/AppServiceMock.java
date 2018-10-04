package io.github.prototypez.appjoint.module1.standalone.mock;


import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.app.callback.AppCallback;
import io.reactivex.Observable;

public class AppServiceMock implements AppService {
    @Override
    public String callMethodSyncOfApp() {
        return "mockSyncMethodOfApp";
    }

    @Override
    public Observable<String> observableOfApp() {
        return Observable.just("mockAsyncMethod1OfApp");
    }

    @Override
    public void callMethodAsync2OfApp(final AppCallback<String> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.onResult("mockAsyncMethod2Result");
            }
        }).start();
    }
}
