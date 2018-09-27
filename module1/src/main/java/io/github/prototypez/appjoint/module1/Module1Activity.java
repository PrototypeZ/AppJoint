package io.github.prototypez.appjoint.module1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.router.AppRouter;
import io.github.prototypez.router.app.Callback;

public class Module1Activity extends AppCompatActivity {

    AppRouter appRouter = AppJoint.getRouter(AppRouter.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module1);

        AppRouter appRouter = AppJoint.getRouter(AppRouter.class);

        String syncResult = appRouter.syncMethodOfApp();
        appRouter.asyncMethod1OfApp()
                .subscribe((result) -> {
                    // handle asyncResult
                });
        appRouter.asyncMethod2OfApp(new Callback<String>() {
            @Override
            public void onResult(String data) {
                // handle asyncResult
            }
        });
    }
}
