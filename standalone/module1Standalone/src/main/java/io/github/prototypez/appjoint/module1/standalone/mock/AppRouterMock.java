package io.github.prototypez.appjoint.module1.standalone.mock;


import io.github.prototypez.router.AppRouter;
import io.github.prototypez.router.app.Callback;
import io.reactivex.Observable;

public class AppRouterMock implements AppRouter {
    @Override
    public String syncMethodOfApp() {
        return "mockSyncMethodOfApp";
    }

    @Override
    public Observable<String> asyncMethod1OfApp() {
        return Observable.just("mockAsyncMethod1OfApp");
    }

    @Override
    public void asyncMethod2OfApp(final Callback<String> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.onResult("mockAsyncMethod2Result");
            }
        }).start();
    }
}
