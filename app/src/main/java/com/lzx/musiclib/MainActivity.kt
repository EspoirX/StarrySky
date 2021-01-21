package com.lzx.musiclib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.itemClicked
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import kotlinx.android.synthetic.main.activity_main.recycleView

class MainActivity : AppCompatActivity() {

    private var viewModel: MusicViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = getSelfViewModel {
            val list = getHomeMusic()
            initRecycleView(list)
        }
    }

    private fun initRecycleView(list: MutableList<SongInfo>) {
        recycleView?.setup<SongInfo> {
            dataSource(list)
            adapter {
                addItem(R.layout.item_home_music) {
                    bindViewHolder { info, position, holder ->
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(info?.songCover)
                        setText(R.id.title to info?.songName, R.id.desc to info?.songName)
                        itemClicked {
                            StarrySky.with().playMusic(list, position)
                            navigationTo<PlayDetailActivity>("songId" to info?.songId)
                        }
                    }
                }
            }
        }
    }
}
//class MainActivity : AppCompatActivity() {
//
//    private var viewModel: MusicViewModel? = null
//    private var rotationAnim: ObjectAnimator? = null
//
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        val songInfo = intent?.getParcelableExtra<SongInfo?>("songInfo")
//        val bundleInfo = intent?.getBundleExtra("bundleInfo")
//        songInfo?.let {
//            showToast("从通知栏点击进来,songName = " + it.songName)
//        }
//        bundleInfo?.let {
//            val notifyKey = bundleInfo.getString("notifyKey") ?: "null"
//            showToast("从通知栏点击进来,自定义参数 = $notifyKey")
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
//        songCover?.loadImage("https://cdn2.ettoday.net/images/4031/d4031158.jpg")
//
//        val list = mutableListOf<String>()
//        list.add("精品推荐")
//        list.add("FLAC无损")
//        list.add("电台(m3u8 HLS)")
//        list.add("RTMP 流")
//        val adapter = ViewPagerAdapter(supportFragmentManager, list)
//        viewPager?.adapter = adapter
//        tabLayout?.setViewPager(viewPager)
//        viewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                floatLayout?.visibility = if (position == 4) View.GONE else View.VISIBLE
//            }
//        })
//
//        rotationAnim = ObjectAnimator.ofFloat(songCover, "rotation", 0f, 359f)
//        rotationAnim?.interpolator = LinearInterpolator()
//        rotationAnim?.duration = 20000
//        rotationAnim?.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator?) {
//                super.onAnimationEnd(animation)
//                rotationAnim?.start()
//            }
//        })
//
//        StarrySky.with().playbackState().observe(this, Observer {
//            when (it.stage) {
//                PlaybackStage.PLAYING -> {
//                    rotationAnim?.start()
//                    songCover?.loadImage(it.songInfo?.songCover)
//                }
//                PlaybackStage.IDEA,
//                PlaybackStage.ERROR,
//                PlaybackStage.PAUSE -> {
//                    rotationAnim?.cancel()
//                    if (it.stage == PlaybackStage.ERROR) {
//                        showToast("播放失败，请查看log了解原因")
//                    }
//                }
//            }
//        })
//        StarrySky.with().setOnPlayProgressListener(lifecycle, object : OnPlayProgressListener {
//            override fun onPlayProgress(currPos: Long, duration: Long) {
//                if (donutProgress.getMax().toLong() != duration) {
//                    donutProgress.setMax(duration.toInt())
//                }
//                donutProgress.setProgress(currPos.toFloat())
//            }
//        })
//        songCover?.setOnClickListener {
//            StarrySky.with().getNowPlayingSongInfo()?.let {
//                navigationTo<PlayDetailActivity>(
//                    "songId" to it.songId,
//                    "type" to "other")
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        rotationAnim?.cancel()
//        rotationAnim?.removeAllListeners()
//        rotationAnim = null
//    }
//}
//
//class ViewPagerAdapter(fm: FragmentManager, private val list: MutableList<String>) : FragmentStatePagerAdapter(fm) {
//
//    private val fragmentMap = hashMapOf<String, Fragment>()
//    override fun getItem(position: Int): Fragment {
//        val value = list.getOrNull(position)
//        if (fragmentMap[value] != null) {
//            return fragmentMap[value]!!
//        }
//        val fragment = when (position) {
//            0 -> RecommendFragment.newInstance()
//            1 -> FlacFragment.newInstance()
//            2 -> M3u8Fragment.newInstance()
//            3 -> RtmpFragment.newInstance()
//            else -> throw IllegalArgumentException()
//        }
//        fragmentMap[value!!] = fragment
//        return fragment
//    }
//
//    override fun getCount(): Int = list.size
//
//    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
//        super.destroyItem(container, position, obj)
//        val classifyId = list.getOrNull(position) ?: 0
//        if (fragmentMap.containsKey(classifyId)) {
//            fragmentMap.remove(classifyId)
//        }
//    }
//
//    var currFragment: BaseFragment? = null
//
//    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
//        currFragment = obj as BaseFragment
//        super.setPrimaryItem(container, position, obj)
//    }
//
//    override fun getPageTitle(position: Int): CharSequence? = list.getOrNull(position) ?: ""
//}