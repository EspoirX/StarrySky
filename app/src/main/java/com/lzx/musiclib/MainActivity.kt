package com.lzx.musiclib

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.lzx.musiclib.example.ListPlayExampleActivity
import com.lzx.musiclib.example.PlayDetailActivity
import com.lzx.musiclib.example.RequestBeforePlayActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn1).setOnClickListener { v: View? -> startListPlayExampleActivity() }
        findViewById<View>(R.id.btn2).setOnClickListener { v: View? -> startRequestBeforePlayActivity() }
        findViewById<View>(R.id.btn3).setOnClickListener { v: View? -> startPlayDetailActivity() }
    }

    private fun startListPlayExampleActivity() {
        startActivity(Intent(this, ListPlayExampleActivity::class.java))
    }

    private fun startRequestBeforePlayActivity() {
        startActivity(Intent(this, RequestBeforePlayActivity::class.java))
    }

    private fun startPlayDetailActivity() {
        startActivity(Intent(this, PlayDetailActivity::class.java))
    }
}