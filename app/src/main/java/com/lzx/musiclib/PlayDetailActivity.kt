package com.lzx.musiclib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PlayDetailActivity : AppCompatActivity() {

    private var songId: String? = null
    private var type: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)
        songId = intent.getStringExtra("songId")
        type = intent.getStringExtra("type")
        val fragment = PlayDetailFragment.newInstance(songId, type)
        fragment.addFragmentToActivity(supportFragmentManager, R.id.container)
    }
}
