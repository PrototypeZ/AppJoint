package io.github.prototypez.appjoint.module1

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.tlHead
import kotlinx.android.synthetic.main.activity_main.vpContent

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    vpContent.adapter = object : FragmentPagerAdapter(supportFragmentManager) {

      override fun getItem(position: Int): Fragment {
        return when (position) {
          0 -> Module1TabFragment.newInstance()
          1 -> Services.sModule2Service.module2TabFragment()
          else -> throw IllegalArgumentException("Illegal adapter index $position")
        }
      }

      override fun getCount(): Int {
        return 2
      }

      override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
          0 -> getString(R.string.module1)
          1 -> getString(R.string.module2)
          else -> throw IllegalArgumentException("Illegal adapter index $position")
        }
      }
    }

    tlHead.setupWithViewPager(vpContent)
  }
}
