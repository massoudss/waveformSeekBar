package com.masoudss.lib

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.masoudss.lib.utils.ThreadBlocking
import com.masoudss.lib.utils.Utils
import com.masoudss.lib.utils.WaveGravity
import com.masoudss.lib.utils.WaveformOptions
import java.io.File
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

open class WaveformSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWaveRect = RectF()
    private val mProgressCanvas = Canvas()
    private var mMaxValue = Utils.dp(context, 2).toInt()
    private var mTouchDownX = 0F
    private var mProgress = 0f
    private var mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var mAlreadyMoved = false
    private lateinit var progressBitmap: Bitmap
    private lateinit var progressShader: Shader

    var onProgressChanged: SeekBarOnProgressChanged? = null

    var sample: IntArray? = null
        set(value) {
            field = value
            setMaxValue()
            invalidate()
        }

    var progress: Float = 0F
        set(value) {
            field = value
            invalidate()
            onProgressChanged?.onProgressChanged(this, progress, false)
        }

    var maxProgress: Float = 100F
        set(value) {
            field = value
            invalidate()
        }

    var waveBackgroundColor: Int = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }

    var waveProgressColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    var waveGap: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    var waveWidth: Float = Utils.dp(context, 5)
        set(value) {
            field = value
            invalidate()
        }

    var waveMinHeight: Float = waveWidth
        set(value) {
            field = value
            invalidate()
        }

    var waveCornerRadius: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    var waveGravity: WaveGravity = WaveGravity.CENTER
        set(value) {
            field = value
            invalidate()
        }

    var visibleProgress: Float = 0F
        set(value) {
            field = value
            invalidate()
        }

    init  {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveformSeekBar)
        waveWidth = ta.getDimension(R.styleable.WaveformSeekBar_wave_width, waveWidth)
        waveGap = ta.getDimension(R.styleable.WaveformSeekBar_wave_gap, waveGap)
        waveCornerRadius = ta.getDimension(R.styleable.WaveformSeekBar_wave_corner_radius, waveCornerRadius)
        waveMinHeight = ta.getDimension(R.styleable.WaveformSeekBar_wave_min_height, waveMinHeight)
        waveBackgroundColor = ta.getColor(R.styleable.WaveformSeekBar_wave_background_color, waveBackgroundColor)
        waveProgressColor = ta.getColor(R.styleable.WaveformSeekBar_wave_progress_color, waveProgressColor)
        progress = ta.getFloat(R.styleable.WaveformSeekBar_wave_progress, progress)
        maxProgress = ta.getFloat(R.styleable.WaveformSeekBar_wave_max_progress, maxProgress)
        visibleProgress = ta.getFloat(R.styleable.WaveformSeekBar_wave_visible_progress, visibleProgress)
        val gravity = ta.getString(R.styleable.WaveformSeekBar_wave_gravity)?.toInt() ?: WaveGravity.CENTER.ordinal
        waveGravity = WaveGravity.values()[gravity]
        ta.recycle()
    }

    private fun setMaxValue() {
        mMaxValue = sample?.max() ?: 0
    }

    @ThreadBlocking
    fun setSampleFrom(samples: IntArray) {
        this.sample = samples
    }

    @ThreadBlocking
    fun setSampleFrom(audio: File) {
        setSampleFrom(audio.path)
    }

    @ThreadBlocking
    fun setSampleFrom(audio: String) {
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }

    @ThreadBlocking
    fun setSampleFrom(audio: Int) {
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasWidth = w
        mCanvasHeight = h
        progressBitmap = Bitmap.createBitmap(getAvailableWidth(), mCanvasHeight, Bitmap.Config.ARGB_8888)
        progressShader = BitmapShader(progressBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        sample?.let { waveSample ->
            if (waveSample.isEmpty())
                return

            canvas.clipRect(paddingLeft, paddingTop, mCanvasWidth - paddingRight, mCanvasHeight - paddingBottom)
            val totalWaveWidth = waveGap + waveWidth
            var step = waveSample.size / (getAvailableWidth() / totalWaveWidth)

            var lastWaveRight = paddingLeft.toFloat()
            var sampleItemPosition: Int

            val barsToDraw = (getAvailableWidth() / totalWaveWidth).toInt()
            val start: Int
            val progressView: Float
            if (visibleProgress > 0) {
                // If visibleProgress is > 0, the bars move instead of the blue colored part
                step *= visibleProgress / maxProgress
                val barsForProgress = barsToDraw + 1
                // intFactor is required as depending on whether an equal number of bars must be drawn, the start will switch differently
                val intFactor = (((barsForProgress + 1) % 2))
                // Calculate fixed start change depending
                lastWaveRight += (getAvailableWidth() * 0.5F) % totalWaveWidth
                lastWaveRight += intFactor * 0.5F * totalWaveWidth - totalWaveWidth
                // Calculate start change depending on progress, so that it moves smoothly
                lastWaveRight -= ((progress + intFactor * visibleProgress / barsForProgress * 0.5f) % (visibleProgress / barsForProgress)) / (visibleProgress / barsForProgress) * totalWaveWidth
                start = (progress * barsForProgress / visibleProgress - (barsForProgress / 2F)).roundToInt() - 1
                progressView = getAvailableWidth() * 0.5F
            } else {
                start = 0
                progressView = getAvailableWidth() * progress / maxProgress
            }
            for (i in start until barsToDraw + start + 3) {
                sampleItemPosition = floor(i * step).roundToInt()
                var waveHeight = if (sampleItemPosition >= 0 && sampleItemPosition < waveSample.size)
                    getAvailableHeight() * (waveSample[sampleItemPosition].toFloat() / mMaxValue)
                else 0F

                if (waveHeight < waveMinHeight)
                    waveHeight = waveMinHeight

                val top: Float = when (waveGravity) {
                    WaveGravity.TOP -> paddingTop.toFloat()
                    WaveGravity.CENTER -> paddingTop + getAvailableHeight() / 2F - waveHeight / 2F
                    WaveGravity.BOTTOM -> mCanvasHeight - paddingBottom - waveHeight
                }

                mWaveRect.set(lastWaveRight, top, lastWaveRight + waveWidth, top + waveHeight)
                when {
                    mWaveRect.contains(progressView, mWaveRect.centerY()) -> {
                        mProgressCanvas.setBitmap(progressBitmap)
                        mWavePaint.color = waveProgressColor
                        mProgressCanvas.drawRect(0F, 0F, progressView, mWaveRect.bottom, mWavePaint)
                        mWavePaint.color = waveBackgroundColor
                        mProgressCanvas.drawRect(progressView, 0F, getAvailableWidth().toFloat(), mWaveRect.bottom, mWavePaint)
                        mWavePaint.shader = progressShader
                    }
                    mWaveRect.right <= progressView -> {
                        mWavePaint.color = waveProgressColor
                        mWavePaint.shader = null
                    }
                    else -> {
                        mWavePaint.color = waveBackgroundColor
                        mWavePaint.shader = null
                    }
                }
                canvas.drawRoundRect(mWaveRect, waveCornerRadius, waveCornerRadius, mWavePaint)
                lastWaveRight = mWaveRect.right + waveGap
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled)
            return false
        if (visibleProgress > 0) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mTouchDownX = event.x
                    mProgress = progress
                    mAlreadyMoved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop || mAlreadyMoved) {
                        updateProgress(event)
                        mAlreadyMoved = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    performClick()
                }
            }
        } else {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isParentScrolling())
                        mTouchDownX = event.x
                    else
                        updateProgress(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    updateProgress(event)
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop)
                        updateProgress(event)
                    performClick()
                }
            }
        }
        return true
    }

    private fun isParentScrolling(): Boolean {
        var parent = parent as View
        val root = rootView
        while (true) {
            when {
                parent.canScrollHorizontally(1) -> return true
                parent.canScrollHorizontally(-1) -> return true
                parent.canScrollVertically(1) -> return true
                parent.canScrollVertically(-1) -> return true
            }
            if (parent == root)
                return false
            parent = parent.parent as View
        }
    }

    private fun updateProgress(event: MotionEvent) {
        if (visibleProgress > 0) {
            progress = mProgress - visibleProgress * (event.x - mTouchDownX) / getAvailableWidth()
            progress = (progress).coerceIn(0F, maxProgress)
        } else {
            progress = maxProgress * event.x / getAvailableWidth()
        }
        onProgressChanged?.onProgressChanged(this, progress, true)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun getAvailableWidth() = mCanvasWidth - paddingLeft - paddingRight

    private fun getAvailableHeight() = mCanvasHeight - paddingTop - paddingBottom

}