package com.lzx.musiclib.dynamic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.lzx.musiclib.R
import com.lzx.musiclib.dp
import com.lzx.musiclib.loadImage
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import kotlinx.android.synthetic.main.activity_card.tabLayout
import kotlinx.android.synthetic.main.activity_card.viewpager
import kotlinx.android.synthetic.main.activity_dynamic.btnClose
import kotlinx.android.synthetic.main.activity_dynamic.btnNext
import kotlinx.android.synthetic.main.activity_dynamic.btnPlay
import kotlinx.android.synthetic.main.activity_dynamic.btnPro
import kotlinx.android.synthetic.main.activity_dynamic.songName
import kotlinx.android.synthetic.main.activity_dynamic.userHeader
import kotlinx.android.synthetic.main.activity_dynamic.voiceBar

class DynamicActivity : AppCompatActivity() {

    private var categoryList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic)
        categoryList.add("推荐")
        categoryList.add("最新")
        val adapter = DynamicCategoryAdapter(supportFragmentManager, categoryList)
        viewpager.removeAllViews()
        viewpager.removeAllViewsInLayout()
        viewpager.adapter = adapter
        tabLayout.setViewPager(viewpager)

        StarrySky.closeNotification()
        StarrySky.setIsOpenNotification(false)

        StarrySky.with().playbackState().observe(this, {
            when (it.stage) {
                PlaybackStage.BUFFERING -> {
                    btnPro.visibility = View.VISIBLE
                }
                PlaybackStage.PLAYING -> {
                    userHeader.loadImage(it.songInfo?.songCover)
                    songName.text = it.songInfo?.songName
                    btnPro.visibility = View.GONE
                    btnPlay.setImageResource(R.drawable.icon_dynamic_top_stop)
                    showVoiceBar()
                }
                PlaybackStage.ERROR,
                PlaybackStage.PAUSE,
                PlaybackStage.IDLE -> {
                    btnPlay.setImageResource(R.drawable.icon_dynamic_top_play)
                }
            }
        })

        btnPlay.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().pauseMusic()
            } else {
                StarrySky.with().restoreMusic()
            }
        }
        btnNext.setOnClickListener {
            StarrySky.with().skipToNext()
        }
        btnClose.setOnClickListener {
            StarrySky.with().stopMusic()
            hideVoiceBar()
        }
    }

    fun showVoiceBar() {
        if (voiceBar.translationY == 0f) {
            return
        }
        val anim = ObjectAnimator.ofFloat(voiceBar, "translationY", (-50f).dp, 0f)
        anim.duration = 500
        anim?.interpolator = LinearInterpolator()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                voiceBar.visibility = View.VISIBLE
            }
        })
        anim.start()
    }

    private fun hideVoiceBar() {
        if (voiceBar.translationY == -50f) {
            return
        }
        val anim = ObjectAnimator.ofFloat(voiceBar, "translationY", 0f, (-50f).dp)
        anim.duration = 500
        anim?.interpolator = LinearInterpolator()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                voiceBar.visibility = View.GONE
            }
        })
        anim.start()
    }
}

class DynamicCategoryAdapter(
    fm: FragmentManager?,
    private var categoryList: MutableList<String>
) : FragmentStatePagerAdapter(fm!!) {

    private val fragmentMap = hashMapOf<String, Fragment>()

    override fun getItem(position: Int): Fragment {
        val category = categoryList[position]
        if (fragmentMap[category] != null) {
            return fragmentMap[category]!!
        }
        val fragment = DynamicFragment.newInstance(category)
        fragmentMap[category] = fragment
        return fragment
    }

    override fun getCount(): Int = categoryList.size

    override fun getPageTitle(position: Int): CharSequence? {
        return categoryList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        val category = categoryList[position]
        if (fragmentMap[category] != null) {
            fragmentMap.remove(category)
        }
    }

    var currFragment: DynamicFragment? = null

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        if (obj is DynamicFragment) {
            currFragment = obj
        }
        super.setPrimaryItem(container, position, obj)
    }
}