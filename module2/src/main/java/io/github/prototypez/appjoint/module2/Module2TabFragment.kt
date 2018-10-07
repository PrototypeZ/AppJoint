package io.github.prototypez.appjoint.module2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.prototypez.appjoint.commons.T
import io.github.prototypez.service.module1.callback.Module1Callback
import kotlinx.android.synthetic.main.fragment_module2_tab.btnCallMethodAsyncOfApp
import kotlinx.android.synthetic.main.fragment_module2_tab.btnCallMethodAsyncOfModule1
import kotlinx.android.synthetic.main.fragment_module2_tab.btnCallMethodSyncOfApp
import kotlinx.android.synthetic.main.fragment_module2_tab.btnCallMethodSyncOfModule1
import kotlinx.android.synthetic.main.fragment_module2_tab.btnGetFragmentOfApp
import kotlinx.android.synthetic.main.fragment_module2_tab.btnGetFragmentOfModule1
import kotlinx.android.synthetic.main.fragment_module2_tab.btnObservableOfApp
import kotlinx.android.synthetic.main.fragment_module2_tab.btnObservableOfModule1
import kotlinx.android.synthetic.main.fragment_module2_tab.btnStartActivityOfApp
import kotlinx.android.synthetic.main.fragment_module2_tab.btnStartActivityOfModule1

class Module2TabFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_module2_tab, container, false)
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

    // module1 action setup

    btnStartActivityOfModule1.setOnClickListener {
      Services.sModule1Service.startActivityOfModule1(context!!)
    }

    btnGetFragmentOfModule1.setOnClickListener {
      childFragmentManager.beginTransaction()
          .replace(R.id.vModule1FragmentPlaceholder,
              Services.sModule1Service.obtainFragmentOfModule1())
          .commitAllowingStateLoss()
    }

    btnCallMethodSyncOfModule1.setOnClickListener {
      T.s("From module1: ${Services.sModule1Service.callMethodSyncOfModule1()}")
    }

    btnCallMethodAsyncOfModule1.setOnClickListener { _ ->
      Services.sModule1Service.callMethodAsyncOfModule1(Module1Callback {
        btnCallMethodAsyncOfModule1.post { T.s("From module1: ${it.data}") }
      })
    }

    btnObservableOfModule1.setOnClickListener { _ ->
      Services.sModule1Service.observableOfModule1()
          .subscribe { T.s("From module1: ${it.data}") }
    }
  }


  companion object {
    fun newInstance(): Module2TabFragment {
      val fragment = Module2TabFragment()
      return fragment
    }
  }
}