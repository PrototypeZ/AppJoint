package io.github.prototypez.appjoint.module1

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.prototypez.appjoint.commons.T
import kotlinx.android.synthetic.main.fragment_module1_tab.btnCallMethodAsyncOfApp
import kotlinx.android.synthetic.main.fragment_module1_tab.btnCallMethodAsyncOfModule2
import kotlinx.android.synthetic.main.fragment_module1_tab.btnCallMethodSyncOfApp
import kotlinx.android.synthetic.main.fragment_module1_tab.btnCallMethodSyncOfModule2
import kotlinx.android.synthetic.main.fragment_module1_tab.btnGetFragmentOfApp
import kotlinx.android.synthetic.main.fragment_module1_tab.btnGetFragmentOfModule2
import kotlinx.android.synthetic.main.fragment_module1_tab.btnObservableOfApp
import kotlinx.android.synthetic.main.fragment_module1_tab.btnObservableOfModule2
import kotlinx.android.synthetic.main.fragment_module1_tab.btnStartActivityOfApp
import kotlinx.android.synthetic.main.fragment_module1_tab.btnStartActivityOfModule2

class Module1TabFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_module1_tab, container, false)
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    btnStartActivityOfApp.setOnClickListener { Services.sAppService.startActivityOfApp(context) }

    btnGetFragmentOfApp.setOnClickListener {
      childFragmentManager.beginTransaction()
          .replace(R.id.vAppFragmentPlaceholder, Services.sAppService.obtainFragmentOfApp())
          .commitAllowingStateLoss()
    }

    btnCallMethodSyncOfApp.setOnClickListener {
      T.s("From app module: ${Services.sAppService.callMethodSyncOfApp()}")
    }

    btnCallMethodAsyncOfApp.setOnClickListener { _ ->
      Services.sAppService.callMethodAsyncOfApp {
        btnCallMethodAsyncOfApp.post { T.s("From app module: ${it.data}") }
      }
    }

    btnObservableOfApp.setOnClickListener { _ ->
      Services.sAppService.observableOfApp()
          .subscribe { T.s("From app module: ${it.data}") }
    }

    // module2 action setup

    btnStartActivityOfModule2.setOnClickListener {
      Services.sModule2Service.startActivityOfModule2(context)
    }

    btnGetFragmentOfModule2.setOnClickListener {
      childFragmentManager.beginTransaction()
          .replace(R.id.vModule2FragmentPlaceholder,
              Services.sModule2Service.obtainFragmentOfModule2())
          .commitAllowingStateLoss()
    }

    btnCallMethodSyncOfModule2.setOnClickListener {
      T.s("From module2: ${Services.sModule2Service.callMethodSyncOfModule2()}")
    }

    btnCallMethodAsyncOfModule2.setOnClickListener { _ ->
      Services.sModule2Service.callMethodAsyncOfModule2 {
        btnCallMethodAsyncOfModule2.post { T.s("From module2: ${it.data}") }
      }
    }

    btnObservableOfModule2.setOnClickListener { _ ->
      Services.sModule2Service.observableOfModule2()
          .subscribe { T.s("From module2: ${it.data}") }
    }

  }


  companion object {
    fun newInstance(): Module1TabFragment {
      val fragment = Module1TabFragment()
      return fragment
    }
  }
}