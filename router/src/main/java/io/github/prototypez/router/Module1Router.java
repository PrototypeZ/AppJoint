package io.github.prototypez.router;

import android.content.Context;
import android.support.v4.app.Fragment;

public interface Module1Router {

    void startModule1Activity(Context context);

    Fragment obtainModule1Fragment();
}
