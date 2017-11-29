package io.github.prototypez.appjoint;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhounl on 2017/11/15.
 */

public class AppJoint {

    private List<Application> moduleApplications;

    private Map<Class, Class> routersMap;

    private Map<Class, Object> routerInstanceMap = new HashMap<>();

    private AppJoint() {

        moduleApplications = new ArrayList<>();

        try {
            Class appJointResultClass = Class.forName("io.github.prototypez.appjoint.AppJointResult");
            Field appField = appJointResultClass.getField("INSTANCES");
            moduleApplications = (List<Application>) appField.get(appJointResultClass);
            Field routersField = appJointResultClass.getField("ROUTERS_PROVIDER_MAP");
            routersMap = (Map<Class, Class>) routersField.get(appJointResultClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public void attachBaseContext(Context context) {
        for (Application app : moduleApplications) {
            try {
                // invoke each application's attachBaseContext
                Method attachBaseContext = ContextWrapper.class.getDeclaredMethod("attachBaseContext", Context.class);
                attachBaseContext.setAccessible(true);
                attachBaseContext.invoke(app, context);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void initCallbackMethods(Application delegateApp) {
        for (Application app : moduleApplications) {
            initCallbackMethods(delegateApp, app);
        }
    }

    private void initCallbackMethods(Application delegateApp, Application targetApp) {

        try {
            Field mActivityLifecycleCallbacksField = Application.class.getDeclaredField("mActivityLifecycleCallbacks");
            mActivityLifecycleCallbacksField.setAccessible(true);
            mActivityLifecycleCallbacksField.set(targetApp, mActivityLifecycleCallbacksField.get(delegateApp));

            Field mComponentCallbacksField = Application.class.getDeclaredField("mComponentCallbacks");
            mComponentCallbacksField.setAccessible(true);
            mComponentCallbacksField.set(targetApp, mComponentCallbacksField.get(delegateApp));

            Field mAssistCallbacksField = Application.class.getDeclaredField("mAssistCallbacks");
            mAssistCallbacksField.setAccessible(true);
            mAssistCallbacksField.set(targetApp, mAssistCallbacksField.get(delegateApp));

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void onCreate() {
        for (Application app : moduleApplications) {
            app.onCreate();
        }

    }

    public void onConfigurationChanged(Configuration configuration) {
        for (Application app : moduleApplications) {
            app.onConfigurationChanged(configuration);
        }
    }

    public void onLowMemory() {
        for (Application app : moduleApplications) {
            app.onLowMemory();
        }
    }

    public void onTerminate() {
        for (Application app : moduleApplications) {
            app.onTerminate();
        }

    }

    public void onTrimMemory(int level) {
        for (Application app : moduleApplications) {
            app.onTrimMemory(level);
        }
    }

    public synchronized <T> T getRouter(Class<T> routerType) {
        T requiredRouter = null;
        if (!routerInstanceMap.containsKey(routerType)) {
            try {
                requiredRouter = (T) routersMap.get(routerType).newInstance();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            requiredRouter = (T) routerInstanceMap.get(routerType);
        }
        return requiredRouter;
    }

    public static AppJoint get() {
        return SingletonHolder.INSTANCE;
    }

    static class SingletonHolder {
        static AppJoint INSTANCE = new AppJoint();
    }
}
