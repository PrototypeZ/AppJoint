package io.github.prototypez.appjoint.module1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.app.callback.AppCallback;
import io.github.prototypez.service.app.entity.AppEntity;

public class Module1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module1);

        AppService appService = AppJoint.service(AppService.class);

        String syncResult = appService.callMethodSyncOfApp();
        appService.observableOfApp()
                .subscribe((result) -> {
                    // handle asyncResult
                });
        appService.callMethodAsync2OfApp((AppCallback<AppEntity>) data -> {
            // handle asyncResult
        });
    }
}
