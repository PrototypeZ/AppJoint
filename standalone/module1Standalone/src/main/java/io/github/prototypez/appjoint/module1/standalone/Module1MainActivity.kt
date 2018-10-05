package io.github.prototypez.appjoint.module1.standalone

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.prototypez.appjoint.module1.Module1TabFragment

class Module1MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_module1_main)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .add(R.id.container, Module1TabFragment.newInstance())
          .commit()
    }
  }
}
