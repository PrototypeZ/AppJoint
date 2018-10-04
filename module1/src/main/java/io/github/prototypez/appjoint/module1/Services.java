package io.github.prototypez.appjoint.module1;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.module2.Module2Service;

public class Services {

    public static AppService sAppService = AppJoint.service(AppService.class);

    public static Module2Service sModule2Service = AppJoint.service(Module2Service.class);

    //    /**
    //     * Service of Module1 self, there's no need to call AppJoint.service
    //     */
    //    public static Module1Service sModule1Service = new Module1ServiceImpl();
}
