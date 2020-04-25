package com.lzx.musiclib.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lzx.musiclib.R

/**
 * 这里演示播放音频前需要先请求接口获取url这类需求的解决方案，只需要按照下面代码那样实现
 * 自己的 StarrySkyConfig 然后在初始化时设置进去即可。有关于 StarrySkyConfig 的相关
 * 信息请看文档
 */
class RequestBeforePlayActivity : AppCompatActivity() {
    private val mListPlayAdapter: ListPlayAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_before_play)
    }
}