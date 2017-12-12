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
@ModuleSpec("monkey")
public class MonkeyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppJoint.get().getRouter(FuncTigerRouter.class).startTiger();
        AppJoint.get().getRouter(FuncTigerRouter.class).startTigerForResult();
        AppJoint.get().getRouter(FuncTigerRouter2.class).startTiger();
        AppJoint.get().getRouter(FuncTigerRouter2.class).startTigerForResult();
        AppJoint.get().getRouter(FuncAppRouter.class).callApp();
    }
}
