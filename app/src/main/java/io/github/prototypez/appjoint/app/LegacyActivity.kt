package io.github.prototypez.appjoint.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class LegacyActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_legacy)
  }
}