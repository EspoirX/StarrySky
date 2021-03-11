package com.lzx.musiclib.effect

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.lzx.musiclib.dp
import com.lzx.musiclib.parseColor
import com.lzx.musiclib.sp
import com.lzx.starrysky.StarrySky
import kotlin.math.roundToInt

class BandChartView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private var mMinBandLevel = 0
    private var mBandRange = 0
    private lateinit var mAllBandLevel: ShortArray
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private var mLinePath = Path()
    private var mCornerPathEffect: CornerPathEffect? = null
    private var mDashPathEffect: DashPathEffect? = null
    private var mContentRect = Rect()
    private var mHintTextRect = Rect()
    private var mHintText = "未初始化"
    private var lineColor = "#FF7043".parseColor()
    private var lineDisableColor = "#E0E0E0".parseColor()
    private var lineWidth = 2.dp
    private var hintTextSize = 14.sp
    private var hintTextColor = "#FF7043".parseColor()
    private var gridLineWidth = 1.dp
    private var gridLineColor = "#E0E0E0".parseColor()
    private var mBandSpace = 0

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // 因为 DashPathEffect 在低于 API 28 的版本中不支持硬件加速，因此需要关闭硬件加速才能生效
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
        val dashLength = 8.dp
        mDashPathEffect = DashPathEffect(floatArrayOf(dashLength.toFloat(), dashLength.toFloat()), 0f)
        initData()
    }

    fun initData() {
        val bandLevelRange = StarrySky.effect().getEqualizerBandLevelRange()
        val numberOfBands = StarrySky.effect().getEqualizerNumberOfBands().toInt()


        mMinBandLevel = bandLevelRange.getOrNull(0)?.toInt() ?: 0
        mBandRange = bandLevelRange.getOrNull(1)?.toInt() ?: 0 - mMinBandLevel
        mAllBandLevel = ShortArray(numberOfBands)
        updateAllBandLevelData()
        invalidate()
    }

    private fun updateAllBandLevelData() {
        val numberOfBands = StarrySky.effect().getEqualizerNumberOfBands().toInt()
        if (numberOfBands == 0) return
        for (band in 0 until numberOfBands) {
            mAllBandLevel[band] = StarrySky.effect().getEqualizerBandLevel(band.toShort())
        }
    }

    fun notifyEqualizerSettingChanged() {

        updateAllBandLevelData()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        updateContentRect()
//        if (!mInitialized) {
        drawHintText(canvas)
//            return
//        }
        drawBackgroundGrid(canvas)
        drawBandLine(canvas)
    }

    private fun updateContentRect() {
        mContentRect.top = paddingTop
        mContentRect.left = paddingLeft
        mContentRect.right = width - paddingRight
        mContentRect.bottom = height - paddingBottom
    }

    private fun drawHintText(canvas: Canvas) {
        mPaint.textSize = hintTextSize
        mPaint.getTextBounds(mHintText, 0, mHintText.length, mHintTextRect)
        Gravity.apply(Gravity.CENTER, mHintTextRect.width(), mHintTextRect.height(), mContentRect, mHintTextRect)
        mPaint.color = hintTextColor
        mPaint.style = Paint.Style.FILL

        canvas.drawText(mHintText, mHintTextRect.left.toFloat(), mHintTextRect.top.toFloat(), mPaint)

    }

    private fun drawBackgroundGrid(canvas: Canvas) {
        val halfGridLineWith = gridLineWidth / 2

        // 绘制外框
        mPaint.pathEffect = null
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = gridLineWidth.toFloat()
        mPaint.color = gridLineColor
        canvas.drawRect(halfGridLineWith.toFloat(), halfGridLineWith.toFloat(), (width - halfGridLineWith).toFloat(), (height - halfGridLineWith).toFloat(), mPaint)

        // 绘制垂直线条
        val space = mContentRect.width() / (mAllBandLevel.size - 1)
        val lineCount = mAllBandLevel.size - 2
        for (i in 1..lineCount) {
            canvas.drawLine((space * i).toFloat(), 0f, (space * i).toFloat(), height.toFloat(), mPaint)
        }

        // 绘制中央水平线条
        val centerY = height / 2
        mPaint.pathEffect = mDashPathEffect
        canvas.drawLine(0f, centerY.toFloat(), width.toFloat(), centerY.toFloat(), mPaint)
    }

    private fun drawBandLine(canvas: Canvas) {
        mLinePath.rewind()
        val numberOfBands = mAllBandLevel.size
        val space = mContentRect.width() / (numberOfBands - 1)
        if (space != mBandSpace) {
            mBandSpace = space
            mCornerPathEffect = CornerPathEffect((space / 2.0).roundToInt().toFloat())
        }
        mLinePath.moveTo(mContentRect.left.toFloat(), getBandLevelY(0).toFloat())
        for (band in 1 until numberOfBands) {
            val x = (band * space).toFloat()
            val y = getBandLevelY(band).toFloat()
            mLinePath.lineTo(x, y)
        }
        mPaint.pathEffect = mCornerPathEffect
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = lineWidth.toFloat()
        mPaint.color = if (isEnabled) lineColor else lineDisableColor
        canvas.drawPath(mLinePath, mPaint)
    }

    private fun getBandLevelY(band: Int): Int {
        val height = mContentRect.height()
        val percent = (mAllBandLevel[band] - mMinBandLevel) * 1.0 / mBandRange
        val result = height - (height * percent).roundToInt()
        val halfLineWith = (lineWidth / 2.0).toFloat()
        return halfLineWith.coerceAtLeast(result.toFloat()).coerceAtMost((mContentRect.height() - lineWidth).toFloat()).toInt()
    }
}