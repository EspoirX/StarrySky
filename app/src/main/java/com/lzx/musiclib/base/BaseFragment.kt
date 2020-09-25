package com.lzx.musiclib.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    var mContext: Context? = null
    var mActivity: Activity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getResourceId(), container, false)
        mActivity = activity
        mContext = mActivity
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }


    open fun findViewById(@IdRes id: Int): View? {
        return view?.findViewById(id)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
        mContext = context
    }

    override fun onDetach() {
        mActivity = null
        super.onDetach()
    }

    override fun onDestroyView() {
        unInitView()
        super.onDestroyView()
    }

    abstract fun getResourceId(): Int

    abstract fun initView(view: View?)

    abstract fun unInitView()
}