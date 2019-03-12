package io.github.prototypez.appjoint.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zhounl on 2017/11/14.
 */
@Retention(RetentionPolicy.CLASS)
public @interface ModuleSpec {
    int priority() default 1000;
}
