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
@ModuleSpec
public class TigerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppJoint.get().service(FuncMonkeyRouter.class).startMonkey();
        AppJoint.get().service(FuncMonkeyRouter.class).startMonkeyForResult();
        AppJoint.get().service(FuncMonkeyRouter2.class).startMonkey();
        AppJoint.get().service(FuncMonkeyRouter2.class).startMonkeyForResult();
        AppJoint.get().service(FuncAppRouter.class).callApp();
    }
}
