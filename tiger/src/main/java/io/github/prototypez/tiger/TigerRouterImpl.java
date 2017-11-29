package io.github.prototypez.tiger;

import android.util.Log;

import io.github.prototypez.appjoint.core.RouterProvider;
import io.github.prototypez.router.FuncTigerRouter;

/**
 * Created by zhounl on 2017/11/27.
 */
@RouterProvider
public class TigerRouterImpl implements FuncTigerRouter {
    @Override
    public void startTiger() {
        Log.i("AppJoint", "start tiger!");
    }

    @Override
    public void startTigerForResult() {
        Log.i("AppJoint", "start tiger for result!");
    }
}
