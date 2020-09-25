package com.lzx.musiclib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PlayDetailActivity : AppCompatActivity() {

    private var songId: String? = null
    private var type: String? = null
    private var position: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)
        songId = intent.getStringExtra("songId")
        type = intent.getStringExtra("type")
        position = intent.getIntExtra("position", 0)
        val fragment = PlayDetailFragment.newInstance(songId, type, position)
        fragment.addFragmentToActivity(supportFragmentManager, R.id.container)
    }
}
