package com.lzx.musiclib

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.lzx.musiclib.base.BaseFragment
import com.lzx.starrysky.utils.TimerTaskManager
import kotlinx.android.synthetic.main.activity_main.tabLayout
import kotlinx.android.synthetic.main.activity_main.viewPager

class MainActivity : AppCompatActivity() {


    private var viewModel: MusicViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
//        viewModel?.login()
        val list = mutableListOf<String>()
        list.add("精品推荐")
        list.add("热门")
        list.add("最新")
        list.add("歌手")
        val adapter = ViewPagerAdapter(supportFragmentManager, list)
        viewPager?.adapter = adapter
        tabLayout?.setViewPager(viewPager)
    }
}

class ViewPagerAdapter(fm: FragmentManager, private val list: MutableList<String>) : FragmentStatePagerAdapter(fm) {

    private val fragmentMap = hashMapOf<String, Fragment>()
    override fun getItem(position: Int): Fragment {
        val value = list.getOrNull(position)
        if (fragmentMap[value] != null) {
            return fragmentMap[value]!!
        }
        var fragment: BaseFragment? = null
        when (position) {
            0 -> fragment = RecommendFragment.newInstance()
            1 -> fragment = NewFragment.newInstance()
            2 -> fragment = HotFragment.newInstance()
            3 -> fragment = SingerFragment.newInstance()
        }
        fragmentMap[value!!] = fragment!!
        return fragment
    }

    override fun getCount(): Int = list.size

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        super.destroyItem(container, position, obj)
        val classifyId = list.getOrNull(position) ?: 0
        if (fragmentMap.containsKey(classifyId)) {
            fragmentMap.remove(classifyId)
        }
    }

    var currFragment: BaseFragment? = null

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        currFragment = obj as BaseFragment
        super.setPrimaryItem(container, position, obj)
    }

    override fun getPageTitle(position: Int): CharSequence? = list.getOrNull(position) ?: ""
}