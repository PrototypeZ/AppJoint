package io.github.prototypez.appjoint.module2;

import io.github.prototypez.appjoint.AppJoint;
import io.github.prototypez.service.app.AppService;
import io.github.prototypez.service.module1.Module1Service;

public class Services {

  public static AppService sAppService = AppJoint.service(AppService.class);

  public static Module1Service sModule1Service = AppJoint.service(Module1Service.class);
}
