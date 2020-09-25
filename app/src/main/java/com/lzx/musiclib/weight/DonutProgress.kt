package com.lzx.musiclib.weight

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.lzx.musiclib.R
import com.lzx.musiclib.dp
import kotlin.math.abs

open class DonutProgress @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val INSTANCE_STATE = "saved_instance"
        private const val INSTANCE_TEXT_COLOR = "text_color"
        private const val INSTANCE_TEXT_SIZE = "text_size"
        private const val INSTANCE_TEXT = "text"
        private const val INSTANCE_INNER_BOTTOM_TEXT_SIZE = "inner_bottom_text_size"
        private const val INSTANCE_INNER_BOTTOM_TEXT = "inner_bottom_text"
        private const val INSTANCE_INNER_BOTTOM_TEXT_COLOR = "inner_bottom_text_color"
        private const val INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color"
        private const val INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color"
        private const val INSTANCE_MAX = "max"
        private const val INSTANCE_PROGRESS = "progress"
        private const val INSTANCE_SUFFIX = "suffix"
        private const val INSTANCE_PREFIX = "prefix"
        private const val INSTANCE_FINISHED_STROKE_WIDTH = "finished_stroke_width"
        private const val INSTANCE_UNFINISHED_STROKE_WIDTH = "unfinished_stroke_width"
        private const val INSTANCE_BACKGROUND_COLOR = "inner_background_color"
        private const val INSTANCE_STARTING_DEGREE = "starting_degree"
        private const val INSTANCE_INNER_DRAWABLE = "inner_drawable"
    }

    private var finishedPaint = Paint()
    private var unfinishedPaint = Paint()
    private var innerCirclePaint = Paint()
    private var textPaint = TextPaint()
    private var innerBottomTextPaint = TextPaint()
    private val finishedOuterRect = RectF()
    private val unfinishedOuterRect = RectF()
    var attributeResourceId = 0
    var attributeBitmap: Bitmap? = null

    var isShowText = false
    private var textSize = 0f
    private var textColor = 0
    private var innerBottomTextColor = 0
    private var progress = 0f
    private var max = 0
    private var finishedStrokeColor = 0
    private var unfinishedStrokeColor = 0
    private var startingDegree = 0
    private var finishedStrokeWidth = 0f
    private var unfinishedStrokeWidth = 0f
    private var innerBackgroundColor = 0
    private var prefixText = ""
    private var suffixText = "%"
    private var text: String? = null
    private var innerBottomTextSize = 0f
    private var innerBottomText: String? = null
    private var innerBottomTextHeight = 0f
    private val defaultStrokeWidth: Float = 10.dp
    private val defaultFinishedColor = Color.rgb(66, 145, 241)
    private val defaultUnfinishedColor = Color.rgb(204, 204, 204)
    private val defaultTextColor = Color.rgb(66, 145, 241)
    private val defaultInnerBottomTextColor = Color.rgb(66, 145, 241)
    private val defaultInnerBackgroundColor = Color.TRANSPARENT
    private val defaultMax = 100
    private val defaultStartingDegree = 0
    private val defaultTextSize: Float = 18.dp
    private val defaultInnerBottomTextSize: Float = 18.dp
    private val minSize: Int = 100.dp.toInt()

    init {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.DonutProgress, defStyleAttr, 0)
        initByAttributes(attributes)
        attributes.recycle()
        initPainters()
    }

    private fun initPainters() {
        if (isShowText) {
            textPaint.apply {
                color = textColor
                textSize = textSize
                isAntiAlias = true
            }
            innerBottomTextPaint.apply {
                color = innerBottomTextColor
                textSize = innerBottomTextSize
                isAntiAlias = true
            }
        }
        finishedPaint.apply {
            color = finishedStrokeColor
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeWidth = finishedStrokeWidth
        }
        unfinishedPaint.apply {
            color = unfinishedStrokeColor
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = unfinishedStrokeWidth
        }
        innerCirclePaint.apply {
            color = innerBackgroundColor
            isAntiAlias = true
        }
    }

    private fun initByAttributes(attributes: TypedArray) {
        finishedStrokeColor = attributes.getColor(R.styleable.DonutProgress_donut_finished_color, defaultFinishedColor)
        unfinishedStrokeColor = attributes.getColor(R.styleable.DonutProgress_donut_unfinished_color, defaultUnfinishedColor)
        isShowText = attributes.getBoolean(R.styleable.DonutProgress_donut_show_text, true)
        attributeResourceId = attributes.getResourceId(R.styleable.DonutProgress_donut_inner_drawable, 0)
        if (attributeResourceId != 0) {
            attributeBitmap = BitmapFactory.decodeResource(resources, attributeResourceId)
        }
        setMax(attributes.getInt(R.styleable.DonutProgress_donut_max, defaultMax))
        setProgress(attributes.getFloat(R.styleable.DonutProgress_donut_progress, 0f))
        finishedStrokeWidth = attributes.getDimension(R.styleable.DonutProgress_donut_finished_stroke_width, defaultStrokeWidth)
        unfinishedStrokeWidth = attributes.getDimension(R.styleable.DonutProgress_donut_unfinished_stroke_width, defaultStrokeWidth)
        if (isShowText) {
            if (attributes.getString(R.styleable.DonutProgress_donut_prefix_text) != null) {
                prefixText = attributes.getString(R.styleable.DonutProgress_donut_prefix_text) ?: ""
            }
            if (attributes.getString(R.styleable.DonutProgress_donut_suffix_text) != null) {
                suffixText = attributes.getString(R.styleable.DonutProgress_donut_suffix_text) ?: ""
            }
            if (attributes.getString(R.styleable.DonutProgress_donut_text) != null) {
                text = attributes.getString(R.styleable.DonutProgress_donut_text)
            }
            textColor = attributes.getColor(R.styleable.DonutProgress_donut_text_color, defaultTextColor)
            textSize = attributes.getDimension(R.styleable.DonutProgress_donut_text_size, defaultTextSize)
            innerBottomTextSize = attributes.getDimension(R.styleable.DonutProgress_donut_inner_bottom_text_size, defaultInnerBottomTextSize)
            innerBottomTextColor = attributes.getColor(R.styleable.DonutProgress_donut_inner_bottom_text_color, defaultInnerBottomTextColor)
            innerBottomText = attributes.getString(R.styleable.DonutProgress_donut_inner_bottom_text)
        }
        innerBottomTextSize = attributes.getDimension(R.styleable.DonutProgress_donut_inner_bottom_text_size, defaultInnerBottomTextSize)
        innerBottomTextColor = attributes.getColor(R.styleable.DonutProgress_donut_inner_bottom_text_color, defaultInnerBottomTextColor)
        innerBottomText = attributes.getString(R.styleable.DonutProgress_donut_inner_bottom_text)
        startingDegree = attributes.getInt(R.styleable.DonutProgress_donut_circle_starting_degree, defaultStartingDegree)
        innerBackgroundColor = attributes.getColor(R.styleable.DonutProgress_donut_background_color, defaultInnerBackgroundColor)
    }

    override fun invalidate() {
        initPainters()
        super.invalidate()
    }

    fun getFinishedStrokeWidth(): Float {
        return finishedStrokeWidth
    }

    fun setFinishedStrokeWidth(finishedStrokeWidth: Float) {
        this.finishedStrokeWidth = finishedStrokeWidth
        this.invalidate()
    }

    fun getUnfinishedStrokeWidth(): Float {
        return unfinishedStrokeWidth
    }

    fun setUnfinishedStrokeWidth(unfinishedStrokeWidth: Float) {
        this.unfinishedStrokeWidth = unfinishedStrokeWidth
        this.invalidate()
    }

    private val progressAngle: Float
        get() = getProgress() / max.toFloat() * 360f

    fun getProgress(): Float {
        return progress
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        if (this.progress > getMax()) {
            this.progress %= getMax().toFloat()
        }
        invalidate()
    }

    fun getMax(): Int {
        return max
    }

    fun setMax(max: Int) {
        if (max > 0) {
            this.max = max
            invalidate()
        }
    }

    fun getTextSize(): Float {
        return textSize
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        this.invalidate()
    }

    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        this.invalidate()
    }

    fun getFinishedStrokeColor(): Int {
        return finishedStrokeColor
    }

    fun setFinishedStrokeColor(finishedStrokeColor: Int) {
        this.finishedStrokeColor = finishedStrokeColor
        this.invalidate()
    }

    fun getUnfinishedStrokeColor(): Int {
        return unfinishedStrokeColor
    }

    fun setUnfinishedStrokeColor(unfinishedStrokeColor: Int) {
        this.unfinishedStrokeColor = unfinishedStrokeColor
        this.invalidate()
    }

    fun getText(): String? {
        return text
    }

    fun setText(text: String?) {
        this.text = text
        this.invalidate()
    }

    fun getSuffixText(): String {
        return suffixText
    }

    fun setSuffixText(suffixText: String) {
        this.suffixText = suffixText
        this.invalidate()
    }

    fun getPrefixText(): String {
        return prefixText
    }

    fun setPrefixText(prefixText: String) {
        this.prefixText = prefixText
        this.invalidate()
    }

    fun getInnerBackgroundColor(): Int {
        return innerBackgroundColor
    }

    fun setInnerBackgroundColor(innerBackgroundColor: Int) {
        this.innerBackgroundColor = innerBackgroundColor
        this.invalidate()
    }

    fun getInnerBottomText(): String? {
        return innerBottomText
    }

    fun setInnerBottomText(innerBottomText: String?) {
        this.innerBottomText = innerBottomText
        this.invalidate()
    }

    fun getInnerBottomTextSize(): Float {
        return innerBottomTextSize
    }

    fun setInnerBottomTextSize(innerBottomTextSize: Float) {
        this.innerBottomTextSize = innerBottomTextSize
        this.invalidate()
    }

    fun getInnerBottomTextColor(): Int {
        return innerBottomTextColor
    }

    fun setInnerBottomTextColor(innerBottomTextColor: Int) {
        this.innerBottomTextColor = innerBottomTextColor
        this.invalidate()
    }

    fun getStartingDegree(): Int {
        return startingDegree
    }

    fun setStartingDegree(startingDegree: Int) {
        this.startingDegree = startingDegree
        this.invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec))
        innerBottomTextHeight = height - height * 3 / 4.toFloat()
    }

    private fun measure(measureSpec: Int): Int {
        var result: Int
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = minSize
            if (mode == MeasureSpec.AT_MOST) {
                result = result.coerceAtMost(size)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val delta = finishedStrokeWidth.coerceAtLeast(unfinishedStrokeWidth)
        finishedOuterRect[delta, delta, width - delta] = height - delta
        unfinishedOuterRect[delta, delta, width - delta] = height - delta
        val innerCircleRadius = (width - finishedStrokeWidth.coerceAtMost(unfinishedStrokeWidth) + abs(finishedStrokeWidth - unfinishedStrokeWidth)) / 2f
        canvas.drawCircle(width / 2.0f, height / 2.0f, innerCircleRadius, innerCirclePaint)
        canvas.drawArc(unfinishedOuterRect, getStartingDegree() + progressAngle, 360 - progressAngle, false, unfinishedPaint)
        canvas.drawArc(finishedOuterRect, getStartingDegree().toFloat(), progressAngle, false, finishedPaint)
        if (isShowText) {
            val text = if (text != null) text else prefixText + progress + suffixText
            if (!text.isNullOrEmpty()) {
                val textHeight = textPaint.descent() + textPaint.ascent()
                canvas.drawText(text, (width - textPaint.measureText(text)) / 2.0f, (width - textHeight) / 2.0f, textPaint)
            }
            if (!getInnerBottomText().isNullOrEmpty()) {
                innerBottomTextPaint.textSize = innerBottomTextSize
                val bottomTextBaseline = height - innerBottomTextHeight - (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(getInnerBottomText() ?: "",
                    (width - innerBottomTextPaint.measureText(getInnerBottomText())) / 2.0f, bottomTextBaseline, innerBottomTextPaint)
            }
        }
        if (attributeBitmap != null) {
            canvas.drawBitmap(attributeBitmap!!, (width - attributeBitmap!!.width) / 2.0f,
                (height - attributeBitmap!!.height) / 2.0f, null)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor())
        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize())
        bundle.putFloat(INSTANCE_INNER_BOTTOM_TEXT_SIZE, getInnerBottomTextSize())
        bundle.putFloat(INSTANCE_INNER_BOTTOM_TEXT_COLOR, getInnerBottomTextColor().toFloat())
        bundle.putString(INSTANCE_INNER_BOTTOM_TEXT, getInnerBottomText())
        bundle.putInt(INSTANCE_INNER_BOTTOM_TEXT_COLOR, getInnerBottomTextColor())
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedStrokeColor())
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedStrokeColor())
        bundle.putInt(INSTANCE_MAX, getMax())
        bundle.putInt(INSTANCE_STARTING_DEGREE, getStartingDegree())
        bundle.putFloat(INSTANCE_PROGRESS, getProgress())
        bundle.putString(INSTANCE_SUFFIX, getSuffixText())
        bundle.putString(INSTANCE_PREFIX, getPrefixText())
        bundle.putString(INSTANCE_TEXT, getText())
        bundle.putFloat(INSTANCE_FINISHED_STROKE_WIDTH, getFinishedStrokeWidth())
        bundle.putFloat(INSTANCE_UNFINISHED_STROKE_WIDTH, getUnfinishedStrokeWidth())
        bundle.putInt(INSTANCE_BACKGROUND_COLOR, getInnerBackgroundColor())
        bundle.putInt(INSTANCE_INNER_DRAWABLE, attributeResourceId)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            textColor = state.getInt(INSTANCE_TEXT_COLOR)
            textSize = state.getFloat(INSTANCE_TEXT_SIZE)
            innerBottomTextSize = state.getFloat(INSTANCE_INNER_BOTTOM_TEXT_SIZE)
            innerBottomText = state.getString(INSTANCE_INNER_BOTTOM_TEXT)
            innerBottomTextColor = state.getInt(INSTANCE_INNER_BOTTOM_TEXT_COLOR)
            finishedStrokeColor = state.getInt(INSTANCE_FINISHED_STROKE_COLOR)
            unfinishedStrokeColor = state.getInt(INSTANCE_UNFINISHED_STROKE_COLOR)
            finishedStrokeWidth = state.getFloat(INSTANCE_FINISHED_STROKE_WIDTH)
            unfinishedStrokeWidth = state.getFloat(INSTANCE_UNFINISHED_STROKE_WIDTH)
            innerBackgroundColor = state.getInt(INSTANCE_BACKGROUND_COLOR)
            attributeResourceId = state.getInt(INSTANCE_INNER_DRAWABLE)
            initPainters()
            setMax(state.getInt(INSTANCE_MAX))
            setStartingDegree(state.getInt(INSTANCE_STARTING_DEGREE))
            setProgress(state.getFloat(INSTANCE_PROGRESS))
            prefixText = state.getString(INSTANCE_PREFIX) ?: ""
            suffixText = state.getString(INSTANCE_SUFFIX) ?: ""
            text = state.getString(INSTANCE_TEXT)
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    fun setDonutProgress(percent: String?) {
        if (!percent.isNullOrEmpty()) {
            setProgress(percent.toInt().toFloat())
        }
    }
}