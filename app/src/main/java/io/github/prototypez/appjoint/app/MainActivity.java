package io.github.prototypez.appjoint.app;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.router.FuncMonkeyRouter;
import io.github.prototypez.router.FuncMonkeyRouter2;
import io.github.prototypez.router.FuncTigerRouter;
import io.github.prototypez.router.FuncTigerRouter2;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppJoint.get().getRouter(FuncMonkeyRouter.class).startMonkey();
                AppJoint.get().getRouter(FuncMonkeyRouter.class).startMonkeyForResult();
                AppJoint.get().getRouter(FuncMonkeyRouter2.class).startMonkey();
                AppJoint.get().getRouter(FuncMonkeyRouter2.class).startMonkeyForResult();

                AppJoint.get().getRouter(FuncTigerRouter.class).startTiger();
                AppJoint.get().getRouter(FuncTigerRouter.class).startTigerForResult();
                AppJoint.get().getRouter(FuncTigerRouter2.class).startTiger();
                AppJoint.get().getRouter(FuncTigerRouter2.class).startTigerForResult();
            }
        });


    }

}
