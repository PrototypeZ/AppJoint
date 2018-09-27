package io.github.prototypez.appjoint.module1;

import android.content.Context;
import android.content.Intent;

import io.github.prototypez.router.Module1Router;

public class Module1RouterImpl implements Module1Router {
    @Override
    public void startModule1Activity(Context context) {
        Intent intent = new Intent(context, Module1Activity.class);
        context.startActivity(intent);
    }
}
