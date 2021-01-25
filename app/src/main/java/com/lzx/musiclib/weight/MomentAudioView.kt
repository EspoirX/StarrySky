package com.lzx.musiclib.weight

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gcssloop.widget.RCRelativeLayout
import com.lzx.musiclib.LifecycleUtils
import com.lzx.musiclib.R
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky

class MomentAudioView : RCRelativeLayout, LifecycleObserver {

    private lateinit var ivPlay: ImageView
    private lateinit var title: TextView
    private lateinit var desc: TextView

    //    private var isPlaying: Boolean = false
    private var songInfo: SongInfo? = null
    private var dynamicId: String = ""

    constructor(context: Context, ivPlay: ImageView) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        LifecycleUtils.addObserver(context, this)
        LayoutInflater.from(context).inflate(R.layout.layout_moment_audio_view, this, true)
        ivPlay = findViewById(R.id.img_play)
        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        OnClickListener {
            songInfo?.let { playVoice() }
        }.let {
            ivPlay.setOnClickListener(it)
            this.setOnClickListener(it)
        }
    }

    fun setVoiceInfo(info: SongInfo?) {
        this.songInfo = info
        this.dynamicId = info?.songId.orEmpty()
        if (!StarrySky.with().isCurrMusicIsPlaying(info?.songId)) {
            ivPlay.setImageResource(R.drawable.moment_audio_view_pause)
        } else {
            ivPlay.setImageResource(R.drawable.moment_audio_view_play)
        }
        title.text = songInfo?.songName
        desc.text = songInfo?.artist
    }

    fun setUIState(isPlaying: Boolean) {
        if (isPlaying) {
            ivPlay.setImageResource(R.drawable.moment_audio_view_pause)
        } else {
            ivPlay.setImageResource(R.drawable.moment_audio_view_play)
        }
    }

    private fun playVoice() {
        if (StarrySky.with().isCurrMusicIsPlaying(dynamicId)) {
            StarrySky.with().pauseMusic()
        } else {
            StarrySky.with().playMusicByInfo(songInfo)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        LifecycleUtils.removeObserver(context, this)
        StarrySky.with().stopMusic()
    }
}