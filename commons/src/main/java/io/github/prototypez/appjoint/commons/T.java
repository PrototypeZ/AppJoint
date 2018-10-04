package io.github.prototypez.appjoint.commons;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class T {

  public static void s(Context context, String msg) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
  }

  public static void s(Context context, @StringRes int resourceId) {
    Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show();
  }
}
