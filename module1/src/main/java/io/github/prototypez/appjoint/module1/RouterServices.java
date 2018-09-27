package io.github.prototypez.appjoint.module1;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.router.AppRouter;
import io.github.prototypez.router.Module2Router;

public class RouterServices {

    public static AppRouter sAppRouter = AppJoint.getRouter(AppRouter.class);

    public static Module2Router sModule2Router = AppJoint.getRouter(Module2Router.class);
}
