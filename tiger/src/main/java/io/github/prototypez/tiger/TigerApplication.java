package io.github.prototypez.tiger;

import android.app.Application;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.appjoint.core.ModuleSpec;
import io.github.prototypez.router.FuncAppRouter;
import io.github.prototypez.router.FuncMonkeyRouter;
import io.github.prototypez.router.FuncMonkeyRouter2;

/**
 * Created by zhounl on 2017/11/28.
 */
@ModuleSpec("tiger")
public class TigerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppJoint.get().getRouter(FuncMonkeyRouter.class).startMonkey();
        AppJoint.get().getRouter(FuncMonkeyRouter.class).startMonkeyForResult();
        AppJoint.get().getRouter(FuncMonkeyRouter2.class).startMonkey();
        AppJoint.get().getRouter(FuncMonkeyRouter2.class).startMonkeyForResult();
        AppJoint.get().getRouter(FuncAppRouter.class).callApp();
    }
}
