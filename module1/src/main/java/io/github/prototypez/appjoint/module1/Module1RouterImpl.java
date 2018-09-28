package io.github.prototypez.appjoint.module1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import io.github.prototypez.appjoint.core.RouterProvider;
import io.github.prototypez.router.Module1Router;

@RouterProvider
public class Module1RouterImpl implements Module1Router {
    @Override
    public void startModule1Activity(Context context) {
        Intent intent = new Intent(context, Module1Activity.class);
        context.startActivity(intent);
    }

    @Override
    public Fragment obtainModule1Fragment() {
        Fragment fragment = new Module1Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("param1", "value1");
        bundle.putString("param2", "value2");
        fragment.setArguments(bundle);
        return fragment;
    }
}
