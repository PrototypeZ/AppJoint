package io.github.prototypez.appjoint.module1;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.router.AppRouter;
import io.github.prototypez.router.Module2Router;

public class RouterServices {

    public static AppRouter sAppRouter = AppJoint.service(AppRouter.class);

    public static Module2Router sModule2Router = AppJoint.service(Module2Router.class);
}
