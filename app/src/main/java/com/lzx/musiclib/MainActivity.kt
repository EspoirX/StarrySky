package com.lzx.musiclib

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.tab.FlacFragment
import com.lzx.musiclib.tab.M3u8Fragment
import com.lzx.musiclib.tab.RecommendFragment
import com.lzx.musiclib.tab.RecordFragment
import com.lzx.musiclib.tab.RtmpFragment
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.basecode.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.playback.PlaybackStage
import kotlinx.android.synthetic.main.activity_main.donutProgress
import kotlinx.android.synthetic.main.activity_main.floatLayout
import kotlinx.android.synthetic.main.activity_main.songCover
import kotlinx.android.synthetic.main.activity_main.tabLayout
import kotlinx.android.synthetic.main.activity_main.viewPager


class MainActivity : AppCompatActivity() {

    private var timerTaskManager = com.lzx.basecode.TimerTaskManager()
    private var viewModel: MusicViewModel? = null
    private var rotationAnim: ObjectAnimator? = null
    private var localBroadcastManager: LocalBroadcastManager? = null
    private var connectedReceiver: ServiceConnectedReceiver? = null


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val songInfo = intent?.getParcelableExtra<SongInfo?>("songInfo")
        val bundleInfo = intent?.getBundleExtra("bundleInfo")
        songInfo?.let {
            showToast("从通知栏点击进来,songName = " + it.songName)
        }
        bundleInfo?.let {
            val notifyKey = bundleInfo.getString("notifyKey") ?: "null"
            showToast("从通知栏点击进来,自定义参数 = $notifyKey")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        connectedReceiver = ServiceConnectedReceiver()
        val intentFilter = IntentFilter("onServiceConnectedSuccessAction")
        localBroadcastManager?.registerReceiver(connectedReceiver!!, intentFilter)

        songCover?.loadImage("https://cdn2.ettoday.net/images/4031/d4031158.jpg")

        val list = mutableListOf<String>()
        list.add("精品推荐")
        list.add("FLAC无损")
        list.add("电台(m3u8 HLS)")
        list.add("RTMP 流")
        list.add("录音")
        val adapter = ViewPagerAdapter(supportFragmentManager, list)
        viewPager?.adapter = adapter
        tabLayout?.setViewPager(viewPager)
        viewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                floatLayout?.visibility = if (position == 4) View.GONE else View.VISIBLE
            }
        })

        timerTaskManager.bindLifecycle(this.lifecycle)

        rotationAnim = ObjectAnimator.ofFloat(songCover, "rotation", 0f, 359f)
        rotationAnim?.interpolator = LinearInterpolator()
        rotationAnim?.duration = 20000
        rotationAnim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                rotationAnim?.start()
            }
        })

        StarrySky.playbackState().observe(this, Observer {
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    rotationAnim?.start()
                    songCover?.loadImage(it.songInfo?.songCover)
                    timerTaskManager.startToUpdateProgress()
                }
                PlaybackStage.IDEA,
                PlaybackStage.ERROR,
                PlaybackStage.PAUSE,
                PlaybackStage.STOP -> {
                    rotationAnim?.cancel()
                    timerTaskManager.stopToUpdateProgress()
                    if (it.stage == PlaybackStage.ERROR) {
                        showToast("播放失败，请查看log了解原因")
                    }
                }
            }
        })
        timerTaskManager.setUpdateProgressTask(Runnable {
            val position = StarrySky.with().getPlayingPosition()
            val duration = StarrySky.with().getDuration()
            if (donutProgress.getMax().toLong() != duration) {
                donutProgress.setMax(duration.toInt())
            }
            donutProgress.setProgress(position.toFloat())
        })
        songCover?.setOnClickListener {
            StarrySky.with().getNowPlayingSongInfo()?.let {
                navigationTo<PlayDetailActivity>(
                    "songId" to it.songId,
                    "type" to "other")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rotationAnim?.cancel()
        rotationAnim?.removeAllListeners()
        rotationAnim = null
        connectedReceiver?.let { localBroadcastManager?.unregisterReceiver(it) }
    }

    inner class ServiceConnectedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "onServiceConnectedSuccessAction") {
//                viewModel?.playWhenStartApp()
            }
        }
    }
}

class ViewPagerAdapter(fm: FragmentManager, private val list: MutableList<String>) : FragmentStatePagerAdapter(fm) {

    private val fragmentMap = hashMapOf<String, Fragment>()
    override fun getItem(position: Int): Fragment {
        val value = list.getOrNull(position)
        if (fragmentMap[value] != null) {
            return fragmentMap[value]!!
        }
        val fragment = when (position) {
            0 -> RecommendFragment.newInstance()
//            1 -> HotFragment.newInstance()
            1 -> FlacFragment.newInstance()
            2 -> M3u8Fragment.newInstance()
            3 -> RtmpFragment.newInstance()
            4 -> RecordFragment.newInstance()
            else -> throw IllegalArgumentException()
        }
        fragmentMap[value!!] = fragment
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