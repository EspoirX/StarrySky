package com.lzx.musiclib

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.lzx.musiclib.base.BaseFragment
import kotlinx.android.synthetic.main.activity_main.tabLayout
import kotlinx.android.synthetic.main.activity_main.viewPager

class PlayDetailActivity : AppCompatActivity() {

    private var songId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)
        songId = intent.getStringExtra("songId")
        val list = mutableListOf<String>()
        list.add("详情")
        list.add("声音")
        val adapter = DetailAdapter(supportFragmentManager, list, songId)
        viewPager?.adapter = adapter
        tabLayout?.setViewPager(viewPager)
        tabLayout?.setCurrentTabOnly(1)
    }
}

class DetailAdapter(fm: FragmentManager, private val list: MutableList<String>, private val songId: String?) : FragmentStatePagerAdapter(fm) {

    private val fragmentMap = hashMapOf<String, Fragment>()
    override fun getItem(position: Int): Fragment {
        val value = list.getOrNull(position)
        if (fragmentMap[value] != null) {
            return fragmentMap[value]!!
        }
        var fragment: BaseFragment? = null
        when (position) {
            0 -> fragment = SongDetailFragment()
            1 -> fragment = PlayDetailFragment.newInstance(songId)
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