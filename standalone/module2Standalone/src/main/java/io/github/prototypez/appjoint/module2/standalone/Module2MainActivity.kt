package io.github.prototypez.appjoint.module2.standalone

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.prototypez.appjoint.module2.Module2TabFragment

class Module2MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_module2_main)

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .add(R.id.container, Module2TabFragment.newInstance())
          .commit()
    }
  }
}
