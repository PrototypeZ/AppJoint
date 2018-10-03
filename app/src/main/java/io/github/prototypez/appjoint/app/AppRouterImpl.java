package io.github.prototypez.appjoint.app;


import io.github.prototypez.appjoint.core.ServiceProvider;
import io.github.prototypez.router.AppRouter;
import io.github.prototypez.router.app.Callback;
import io.reactivex.Observable;

@ServiceProvider
public class AppRouterImpl implements AppRouter {

    @Override
    public String syncMethodOfApp() {
        return "syncMethodResult";
    }

    @Override
    public Observable<String> asyncMethod1OfApp() {
        return Observable.just("asyncMethod1Result");
    }

    @Override
    public void asyncMethod2OfApp(final Callback<String> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.onResult("asyncMethod2Result");
            }
        }).start();
    }
}
