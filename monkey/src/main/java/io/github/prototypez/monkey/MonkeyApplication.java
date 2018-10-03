package io.github.prototypez.monkey;

import android.app.Application;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.appjoint.core.ModuleSpec;
import io.github.prototypez.router.FuncAppRouter;
import io.github.prototypez.router.FuncTigerRouter;
import io.github.prototypez.router.FuncTigerRouter2;

/**
 * Created by zhounl on 2017/11/28.
 */
@ModuleSpec
public class MonkeyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppJoint.get().service(FuncTigerRouter.class).startTiger();
        AppJoint.get().service(FuncTigerRouter.class).startTigerForResult();
        AppJoint.get().service(FuncTigerRouter2.class).startTiger();
        AppJoint.get().service(FuncTigerRouter2.class).startTigerForResult();
        AppJoint.get().service(FuncAppRouter.class).callApp();
    }
}
