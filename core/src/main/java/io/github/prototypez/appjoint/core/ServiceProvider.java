package io.github.prototypez.appjoint.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.github.prototypez.appjoint.AppJoint;

/**
 * Created by zhounl on 2017/11/14.
 */
@Retention(RetentionPolicy.CLASS)
public @interface ServiceProvider {
    String value() default AppJoint.DEFAULT_NAME;
}
