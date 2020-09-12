package com.lzx.musiclib

import android.view.View
import com.lzx.musiclib.base.BaseFragment

class HotFragment : BaseFragment() {
    companion object {
        fun newInstance(): HotFragment {
            return HotFragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recomment

    override fun initView(view: View?) {
    }

    override fun unInitView() {
    }
}