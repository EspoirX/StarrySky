package com.lzx.musiclib.weight

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 频谱动画控件
 */
class SpectrumDrawView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private var isStartAnim = false
    private var spectrumCount = 3 // 频谱条列数
    private val spectrumList = mutableListOf<DrawSpectrumRect>()
    private var bgColor = Color.TRANSPARENT // 圆形背景颜色
    private var spectrumColor = Color.WHITE // 频谱条颜色

    // 创建画笔
    private val paint = Paint()
    private var maxWH = 50f

    init {
        initView()
    }

    private fun initView() {
        setSpectrumCount(3)
    }

    fun setMaxWH(maxWH: Float) {
        this.maxWH = maxWH
    }

    /**
     * 开始动画
     */
    fun startAnim() {
        isStartAnim = true
        updateEnergyHandler.removeMessages(MSG_UPDATE_ENERGY)
        updateEnergyHandler.sendEmptyMessage(MSG_UPDATE_ENERGY)
    }

    /**
     * 停止动画
     */
    fun stopAnim() {
        isStartAnim = false
        updateEnergyHandler.removeMessages(MSG_UPDATE_ENERGY)
    }

    private fun destroyView() {
        stopAnim()
        spectrumList.clear()
    }

    /**
     * 设置频谱柱状条个数
     *
     */
    fun setSpectrumCount(spectrumCount: Int) {
        if (spectrumCount < 1) {
            return
        }
        this.spectrumCount = spectrumCount
        spectrumList.clear()
        paint.isAntiAlias = true
        val randList = mutableListOf<Float>()
        val nRandCount = spectrumCount
        for (i in 0 until nRandCount) {
            randList.add(1.0f * i / nRandCount)
        }
        for (i in 0 until spectrumCount) {
            spectrumList.add(DrawSpectrumRect(i, nextFloatNotRe(randList)))
        }
    }

    private fun nextFloatNotRe(randList: MutableList<Float>): Float {
        val rand = Random(System.currentTimeMillis())
        if (randList.size == 0) {
            return rand.nextFloat()
        }
        val nId = abs(rand.nextInt(Int.MAX_VALUE)) % randList.size
        if (nId >= 0 && nId < randList.size) {
            val randNum = randList[nId]
            randList.removeAt(nId)
            return randNum
        }
        return rand.nextFloat()
    }

    /**
     * 设置动画单方向缓动总时间（柱形条高度单次从0->maxHeight渐变的时间）
     */
    fun setTweenTime(time: Int) {
        mTweenTime = time
        computerRefeshFrameTime()
    }

    /**
     * 设置圆形背景颜色
     */
    fun setBgColor(color: Int) {
        bgColor = color
    }

    /**
     * 设置频谱柱状条颜色
     */
    fun setSpectrumColor(color: Int) {
        spectrumColor = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSpectrum(canvas)
    }

    internal class DrawSpectrumRect(var index: Int, var deltT: Float) {
        fun nextFloat(): Float {
            var h: Float = tweenInOut(deltT)
            if (h <= 0.0f) {
                h = 0.0f
            } else if (h >= 1.0f) {
                h = 1.0f
            }
            return h
        }

        var bOut = true

        private fun tweenIn(t: Float): Float {
            return (-cos(t * (PI / 2).toDouble())).toFloat() + 1
        }

        private fun tweenOut(t: Float): Float {
            return sin(t * (PI / 2).toDouble()).toFloat()
        }

        private fun tweenInOut(t: Float): Float {
            return if (bOut) tweenOut(t) else tweenIn(1 - t)
        }

        fun deltT(t: Float) {
            deltT += t
            if (deltT >= 1.0f) {
                deltT -= 1.0f
                bOut = !bOut
            }
        }
    }

    private val borderCount = 0
    private fun drawSpectrum(canvas: Canvas) {
        // 这里检测一下spectrumCount和spectrumList数量上是否一致,不一致的话进行纠错
        // 因为在2.3手机上会莫名其妙走detach,把spectrumList清空,导致画不出东西
        if (spectrumCount != spectrumList.size) {
            setSpectrumCount(spectrumCount)
        }

        // Rect region=new Rect(0,0,this.getMeasuredWidth(),this.getMeasuredHeight());
        val center = Point(this.measuredWidth / 2, this.measuredHeight / 2)
        val radis = this.measuredWidth / 2.toFloat()
        if (radis <= 0) {
            return
        }
        paint.color = bgColor
        canvas.drawCircle(center.x.toFloat(), center.y.toFloat(), radis, paint) // 大圆
        var maxW = maxWH
        val spaceCount = spectrumCount - 1 + borderCount
        var space = maxW / (spectrumCount + spaceCount) // 间隔与频谱条等宽
        space *= 1.5f  //产品需求，柱子细一点，所以把间隔变大1.5倍
        maxW -= space * borderCount
        val maxH = maxW // 宽高相等
        val w = (maxW - space * (spectrumCount - 1)) / spectrumCount
        val startX = center.x - maxW / 2
        // float startY=center.y-maxH/2;
        paint.color = spectrumColor
        for (rct in spectrumList) {
            val left = startX + rct.index * (w + space)
            val bottom = center.y + maxH / 2
            canvas.drawRect(left, bottom - maxH * rct.nextFloat(), left + w, bottom, paint)
        }
    }

    private var mTweenTime = 1000
    private var mUpdateEnergyTime = 100
    private fun computerRefeshFrameTime() {
        mUpdateEnergyTime = mTweenTime / UPDATE_ENERGY_FRAME
        if (mUpdateEnergyTime < 30) {
            mUpdateEnergyTime = 30
        }
        if (mUpdateEnergyTime > 330) {
            mUpdateEnergyTime = 330
        }
    }

    private var updateEnergyHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_UPDATE_ENERGY) {
                this.removeMessages(MSG_UPDATE_ENERGY)
                if (isStartAnim) {
                    for (rct in spectrumList) {
                        rct.deltT(mUpdateEnergyTime * 1.0f / mTweenTime)
                    }
                    invalidate()
                    sendEmptyMessageDelayed(MSG_UPDATE_ENERGY, mUpdateEnergyTime.toLong())
                }
            }
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (spectrumList.size == 0 && spectrumCount > 0) {
            setSpectrumCount(spectrumCount)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyView()
    }

    companion object {
        private const val MSG_UPDATE_ENERGY = 120
        private const val PI = 3.14159265f
        private const val UPDATE_ENERGY_FRAME = 10
    }
}