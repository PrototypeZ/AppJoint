package io.github.prototypez.monkey;

import android.util.Log;

import io.github.prototypez.appjoint.core.RouterProvider;
import io.github.prototypez.router.FuncMonkeyRouter;
import io.github.prototypez.router.FuncMonkeyRouter2;

/**
 * Created by zhounl on 2017/11/27.
 */
@RouterProvider
public class MonkeyRouterImpl2 implements FuncMonkeyRouter2 {
    @Override
    public void startMonkey() {
        Log.i("AppJoint", "start monkey!");
    }

    @Override
    public void startMonkeyForResult() {
        Log.i("AppJoint", "start monkey for result!");
    }
}
