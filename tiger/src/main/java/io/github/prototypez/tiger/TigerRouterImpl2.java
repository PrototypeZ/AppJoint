package io.github.prototypez.tiger;

import android.util.Log;

import io.github.prototypez.appjoint.core.ServiceProvider;
import io.github.prototypez.router.FuncTigerRouter2;

/**
 * Created by zhounl on 2017/11/27.
 */
@ServiceProvider
public class TigerRouterImpl2 implements FuncTigerRouter2 {
    @Override
    public void startTiger() {
        Log.i("AppJoint", "start tiger!");
    }

    @Override
    public void startTigerForResult() {
        Log.i("AppJoint", "start tiger for result!");
    }
}
