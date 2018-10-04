package io.github.prototypez.appjoint.module2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class Module2Activity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_module2)
  }

  companion object {
    fun start(context: Context) {
      val intent = Intent(context, Module2Activity::class.java)
      context.startActivity(intent)
    }
  }
}