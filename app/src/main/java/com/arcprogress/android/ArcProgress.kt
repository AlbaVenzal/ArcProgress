package com.arcprogress.android
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View

import java.text.DecimalFormat

/**
 * Created by bruce on 11/6/14.
 */
class ArcProgress @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    private var paint: Paint? = null
    private var textPaint: Paint? = null

    private val rectF = RectF()

    var strokeWidth = 0f
        set(strokeWidth) {
            field = strokeWidth
            this.invalidate()
        }

    var bottomTextSize: Float = 0f
        set(bottomTextSize) {
            field = bottomTextSize
            this.invalidate()
        }

    var bottomText: String? = null
        set(bottomText) {
            field = bottomText
            this.invalidate()
        }

    var progressTextSize: Float = 0f
        set(textSize) {
            field = textSize
            this.invalidate()
        }

    var textColor: Int = 0
        set(textColor) {
            field = textColor
            this.invalidate()
        }

    var progressText: String? = null
        set(text) {
            field = text
            invalidate()
        }

    var suffixText: String? = null
        set(text) {
            field = text
            invalidate()
        }

    var progress = 0
        set(progress) {
            field = progress

            if (this.progress > max) {
                field = max
            }
            invalidate()
        }
    var max: Int = 0
        set(max) {
            if (max > 0) {
                field = max
                invalidate()
            }
        }
    var finishedStrokeColor: Int = 0
        set(finishedStrokeColor) {
            field = finishedStrokeColor
            this.invalidate()
        }

    var unfinishedStrokeColor: Int = 0
        set(unfinishedStrokeColor) {
            field = unfinishedStrokeColor
            this.invalidate()
        }

    var arcAngle: Float = 0f
        set(arcAngle) {
            field = arcAngle
            this.invalidate()
        }

    private var arcBottomHeight: Float = 0f

    private val default_finished_color = Color.WHITE
    private val default_unfinished_color = Color.rgb(72, 106, 176)
    private val default_text_color = Color.rgb(66, 145, 241)
    private val default_bottom_text_size: Float
    private val default_stroke_width: Float
    private val default_suffix_text: String
    private val default_max = 100
    private val default_arc_angle = 360 * 0.8f
    private var default_text_size: Float = 0f
    val min_size: Float

    init {

        default_text_size = sp2px(resources, 18)
        min_size = dp2px(resources, 100)
        default_text_size = sp2px(resources, 40)
        default_suffix_text = "%"
        default_bottom_text_size = sp2px(resources, 10)
        default_stroke_width = dp2px(resources, 4)

        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.ArcProgress, defStyleAttr, 0)
        initByAttributes(attributes)
        attributes.recycle()

        initPainters()
    }

    protected fun initByAttributes(attributes: TypedArray) {
        finishedStrokeColor = attributes.getColor(R.styleable.ArcProgress_finished_color, default_finished_color)
        unfinishedStrokeColor =
            attributes.getColor(R.styleable.ArcProgress_unfinished_color, default_unfinished_color)
        textColor = attributes.getColor(R.styleable.ArcProgress_text_color, default_text_color)
        progressTextSize = attributes.getDimension(R.styleable.ArcProgress_progress_text_size, default_text_size)
        arcAngle = attributes.getFloat(R.styleable.ArcProgress_angle, default_arc_angle)
        max = attributes.getInt(R.styleable.ArcProgress_max, default_max)
        progress = attributes.getInt(R.styleable.ArcProgress_progress, 0)
        strokeWidth = attributes.getDimension(R.styleable.ArcProgress_stroke_width, default_stroke_width)
        bottomTextSize = attributes.getDimension(R.styleable.ArcProgress_bottom_text_size, default_bottom_text_size)
        bottomText = attributes.getString(R.styleable.ArcProgress_bottom_text)
        progressText = attributes.getString(R.styleable.ArcProgress_progress_text)
        suffixText = attributes.getString(R.styleable.ArcProgress_suffix_text)
    }

    private fun initPainters() {
        textPaint = TextPaint().apply {
            color = textColor
            isAntiAlias = true
        }

        paint = Paint().apply {
            color = default_unfinished_color
            isAntiAlias = true
            strokeWidth = this@ArcProgress.strokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun invalidate() {
        initPainters()
        super.invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        rectF.set(
            strokeWidth / 2f,
            strokeWidth / 2f,
            width - strokeWidth / 2f,
            View.MeasureSpec.getSize(heightMeasureSpec) - strokeWidth / 2f
        )
        val radius = width / 2f
        val angle = (360 - arcAngle) / 2f
        arcBottomHeight = radius * (1 - Math.cos(angle / 180 * Math.PI)).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startAngle = 270 - arcAngle / 2f
        val finishedSweepAngle = this.progress / max.toFloat() * arcAngle
        var finishedStartAngle = startAngle
        if (this.progress == 0) finishedStartAngle = 0.01f
        paint?.let {paint ->
            paint.color = unfinishedStrokeColor
            canvas.drawArc(rectF, startAngle, arcAngle, false, paint)
            paint.color = finishedStrokeColor
            canvas.drawArc(rectF, finishedStartAngle, finishedSweepAngle, false, paint)
        }
        textPaint?.let { textPaint ->
            if (!TextUtils.isEmpty(progressText)) {
                progressText?.let { progressText ->
                    var completeText = progressText
                    suffixText?.let { completeText += it }
                    textPaint.textSize = progressTextSize
                    val textHeight = textPaint.descent() + textPaint.ascent()
                    val textBaseline = (height - textHeight) / 2f
                    canvas.drawText(completeText, (width - textPaint.measureText(completeText)) / 2.0f, textBaseline, textPaint)
                }
            }

            if (arcBottomHeight == 0f) {
                val radius = width / 2f
                val angle = (360 - arcAngle) / 2f
                arcBottomHeight = radius * (1 - Math.cos(angle / 180 * Math.PI)).toFloat()
            }

            if (!TextUtils.isEmpty(bottomText)) {
                bottomText?.let { bottomText ->
                    textPaint.textSize = bottomTextSize
                    val bottomTextHeight = textPaint.descent() + textPaint.ascent()
                    val bottomTextBaseline = height - arcBottomHeight - bottomTextHeight / 2
                    canvas.drawText(bottomText, (width - textPaint.measureText(bottomText))/2f, bottomTextBaseline, textPaint)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putFloat(INSTANCE_STROKE_WIDTH, strokeWidth)
        bundle.putFloat(INSTANCE_BOTTOM_TEXT_SIZE, bottomTextSize)
        bundle.putString(INSTANCE_BOTTOM_TEXT, bottomText)
        bundle.putString(INSTANCE_PROGRESS_TEXT, progressText)
        bundle.putFloat(INSTANCE_PROGRESS_TEXT_SIZE, progressTextSize)
        bundle.putInt(INSTANCE_TEXT_COLOR, textColor)
        bundle.putInt(INSTANCE_PROGRESS, progress)
        bundle.putInt(INSTANCE_MAX, max)
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, finishedStrokeColor)
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, unfinishedStrokeColor)
        bundle.putFloat(INSTANCE_ARC_ANGLE, arcAngle)
        bundle.putString(INSTANCE_SUFFIX_TEXT, suffixText)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            strokeWidth = state.getFloat(INSTANCE_STROKE_WIDTH)
            bottomTextSize = state.getFloat(INSTANCE_BOTTOM_TEXT_SIZE)
            bottomText = state.getString(INSTANCE_BOTTOM_TEXT)
            progressText = state.getString(INSTANCE_PROGRESS_TEXT)
            progressTextSize = state.getFloat(INSTANCE_PROGRESS_TEXT_SIZE)
            textColor = state.getInt(INSTANCE_TEXT_COLOR)
            max = state.getInt(INSTANCE_MAX)
            progress = state.getInt(INSTANCE_PROGRESS)
            finishedStrokeColor = state.getInt(INSTANCE_FINISHED_STROKE_COLOR)
            unfinishedStrokeColor = state.getInt(INSTANCE_UNFINISHED_STROKE_COLOR)
            suffixText = state.getString(INSTANCE_SUFFIX_TEXT)
            initPainters()
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    companion object {

        private val INSTANCE_STATE = "saved_instance"
        private val INSTANCE_STROKE_WIDTH = "stroke_width"
        private val INSTANCE_BOTTOM_TEXT_SIZE = "bottom_text_size"
        private val INSTANCE_BOTTOM_TEXT = "bottom_text"
        private val INSTANCE_PROGRESS_TEXT = "progress_text"
        private val INSTANCE_SUFFIX_TEXT = "progress_text"
        private val INSTANCE_PROGRESS_TEXT_SIZE = "text_size"
        private val INSTANCE_TEXT_COLOR = "text_color"
        private val INSTANCE_PROGRESS = "progress"
        private val INSTANCE_MAX = "max"
        private val INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color"
        private val INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color"
        private val INSTANCE_ARC_ANGLE = "arc_angle"

        fun dp2px(resources: Resources, dp: Int): Float {
            val scale = resources.getDisplayMetrics().density
            return dp * scale + 0.5f
        }

        fun sp2px(resources: Resources, sp: Int): Float {
            val scale = resources.getDisplayMetrics().scaledDensity
            return sp * scale
        }
    }
}