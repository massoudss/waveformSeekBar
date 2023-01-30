package com.masoudss.lib

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.RawRes
import com.masoudss.lib.utils.*
import com.masoudss.lib.utils.ThreadBlocking
import com.masoudss.lib.utils.WaveformOptions
import java.io.File
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt


open class WaveformTimeline @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0
    private val mWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mWaveRect = RectF()
    private val mMarkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMarkerRect = RectF()
    private val mProgressCanvas = Canvas()
    private var mMaxValue = Utils.dp(context, 2).toInt()
    private var mTouchDownX = 0F
    private var mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var mAlreadyMoved = false
    private var wasPlaying = false
    private var mPlayer = MediaPlayer()
    private var mTimestampPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var progressBitmap: Bitmap
    private lateinit var progressShader: Shader
    private var edit = -1
    private var selectorStart = Selector(500)
    private var selectorEnd = Selector(1500)
    private var selecting = false
    private var mSelectorPaint = Paint()
    private var selectionRect = RectF(0f,0f,0f,getAvailableHeight().toFloat())

    private var roundedCorner = floatArrayOf(
        10f,10f,
        10f,10f,
        10f,10f,
        10f,10f
    )
    var onProgressChanged: TimelineOnProgressChanged? = null

    var sample: IntArray? = null
        set(value) {
            field = value
            try {
                mPlayer.prepare()
            }catch (e: IllegalStateException){
                Log.e("WaveformTimeline","Error Preparing audio")
                e.printStackTrace()
            }
            //OnPrepared gets called
        }

    var progress: Float = 0F
        set(value) {
            field = value
            invalidate()
            onProgressChanged?.onProgressChanged(this, progress, false)
        }

    private var maxProgress: Float = 100F
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

    var wavePaddingTop: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var wavePaddingBottom: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var wavePaddingLeft: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var wavePaddingRight: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var waveWidth: Float = 0.5f
        set(value){
            field = value
            updateSecondDistance(visibleProgress)
            invalidate()
        }

    var waveMinHeight: Float = waveWidth
        set(value) {
            field = value
            invalidate()
        }

    var waveGravity: WaveGravity = WaveGravity.CENTER
        set(value) {
            field = value
            invalidate()
        }

    var marker: HashMap<Float, String>? = null
        set(value) {
            field = value
            invalidate()
        }

    var markerWidth: Float = Utils.dp(context, 1)
        set(value) {
            field = value
            invalidate()
        }

    var markerColor: Int = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }

    var markerTextColor: Int = Color.RED
        set(value) {
            field = value
            invalidate()
        }

    var selectionColor: Int = -16723457
        set(value) {
            field = value
            invalidate()
        }

    var selectionStrokeColor: Int = -16733746
        set(value) {
            field = value
            invalidate()
        }
    var markerTextSize: Float = Utils.dp(context, 12)
        set(value) {
            field = value

            invalidate()
        }

    var markerTextPadding: Float = Utils.dp(context, 2)
        set(value) {
            field = value
            invalidate()
        }

    var visibleProgress: Float = 0F
        set(value) {
            field = value
            updateSecondDistance(value)
            invalidate()
        }

    var timestampColor: Int = Color.GRAY
        set(value) {
            field = value
            invalidate()
        }

    private val timestampTextHeight: Int
        get(){
            val bounds = Rect()
            mTimestampPaint.getTextBounds("0", 0, 1, bounds)
            return bounds.height()
        }

    var timestampTextSize: Float = Utils.dp(context, 12)
        set(value) {
            field = value
            timestampTextHeight
            invalidate()
        }

    var timestampSecondDistance: Int = 1
        set(value){
            field = if(value <= 0)
                1
            else
                value
        }
    var isPlaying: Boolean = false
        set(value) {
            if(value)
                mPlayer.start()
            else
                mPlayer.pause()
            invalidate()
            field = value
        }

    private val onPrepared = OnPreparedListener {
        setMaxValue()
        maxProgress = mPlayer.duration.toFloat()
        isPlaying = true
        isPlaying = false
        invalidate()
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveformTimeline)
        waveWidth = ta.getDimension(R.styleable.WaveformTimeline_wave_width, waveWidth)
        wavePaddingTop = ta.getDimension(R.styleable.WaveformTimeline_wave_padding_top, 0F).toInt()
        wavePaddingBottom = ta.getDimension(R.styleable.WaveformTimeline_wave_padding_Bottom, 0F).toInt()
        wavePaddingLeft = ta.getDimension(R.styleable.WaveformTimeline_wave_padding_left, 0F).toInt()
        wavePaddingRight = ta.getDimension(R.styleable.WaveformTimeline_wave_padding_right, 0F).toInt()
        waveMinHeight = ta.getDimension(R.styleable.WaveformTimeline_wave_min_height, waveMinHeight)
        waveBackgroundColor = ta.getColor(R.styleable.WaveformTimeline_wave_background_color, waveBackgroundColor)
        waveProgressColor = ta.getColor(R.styleable.WaveformTimeline_wave_progress_color, waveProgressColor)
        timestampColor = ta.getColor(R.styleable.WaveformTimeline_timestamp_color, timestampColor)
        selectionColor = ta.getColor(R.styleable.WaveformTimeline_selection_color, selectionColor)
        selectionStrokeColor = ta.getColor(R.styleable.WaveformTimeline_selection_stroke_color, selectionStrokeColor)
        progress = ta.getFloat(R.styleable.WaveformTimeline_wave_progress, progress)
        visibleProgress = ta.getFloat(R.styleable.WaveformTimeline_wave_visible_progress, visibleProgress)
        val gravity = ta.getString(R.styleable.WaveformTimeline_wave_gravity)?.toInt() ?: WaveGravity.CENTER.ordinal
        waveGravity = WaveGravity.values()[gravity]
        markerWidth = ta.getDimension(R.styleable.WaveformTimeline_marker_width, markerWidth)
        markerColor = ta.getColor(R.styleable.WaveformTimeline_marker_color, markerColor)
        markerTextColor = ta.getColor(R.styleable.WaveformTimeline_marker_text_color, markerTextColor)
        markerTextSize = ta.getDimension(R.styleable.WaveformTimeline_marker_text_size, markerTextSize)
        markerTextPadding = ta.getDimension(R.styleable.WaveformTimeline_marker_text_padding, markerTextPadding)
        timestampTextSize = ta.getDimension(R.styleable.WaveformTimeline_timestamp_text_size, timestampTextSize)
        ta.recycle()
        selectionRect = RectF(0f,height.toFloat(),0f,0f)
        mTimestampPaint.isAntiAlias = true
        mTimestampPaint.strokeWidth = 3F
        mTimestampPaint.textSize = timestampTextSize
        mSelectorPaint.strokeWidth = 2f
        mPlayer.setOnPreparedListener(onPrepared)
    }

    private fun setMaxValue() {
        mMaxValue = sample?.maxOrNull() ?: 0
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
        mPlayer.setDataSource(audio)
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }

    @ThreadBlocking
    fun setSampleFrom(@RawRes audio: Int) {
        val file = context.resources.openRawResourceFd(audio)
        mPlayer.setDataSource(file.fileDescriptor)
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }

    @ThreadBlocking
    fun setSampleFrom(audio: Uri) {
        WaveformOptions.getSampleFrom(context, audio) {
            sample = it
        }
    }
    fun reset(){
        mPlayer.reset()
    }

    fun seekTo(mills: Int){
        mPlayer.seekTo(mills)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasWidth = w
        mCanvasHeight = h
        progressBitmap =
            Bitmap.createBitmap(getAvailableWidth(), getAvailableHeight(), Bitmap.Config.ARGB_8888)
        progressShader = BitmapShader(progressBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        sample?.let { waveSample ->
            if (waveSample.isEmpty())
                return

            canvas.clipRect(
                paddingLeft,
                paddingTop,
                mCanvasWidth - paddingRight,
                mCanvasHeight - paddingBottom
            )
            var step = waveSample.size / (getAvailableWidth() / waveWidth)

            var previousWaveRight = paddingLeft.toFloat() + wavePaddingLeft
            var sampleItemPosition: Int

            val barsToDraw = (getAvailableWidth() / waveWidth).toInt()
            val start: Int
            val progressXPosition: Float
            if(mPlayer.isPlaying)   //Avoid redrawing
                progress = mPlayer.currentPosition.toFloat()
            if (zoomed()) {
                // If visibleProgress is > 0, the bars move instead of the blue colored part
                step *= visibleProgress / maxProgress
                val barsForProgress = barsToDraw + 1
                // intFactor is required as depending on whether an equal number of bars must be drawn, the start will switch differently
                val intFactor = (((barsForProgress + 1) % 2))
                // Calculate fixed start change depending
                previousWaveRight += (getAvailableWidth() * 0.5F) % waveWidth
                previousWaveRight += intFactor * 0.5F * waveWidth - waveWidth
                // Calculate start change depending on progress, so that it moves smoothly
                previousWaveRight -= ((progress + intFactor * visibleProgress / barsForProgress * 0.5f) % (visibleProgress / barsForProgress)) / (visibleProgress / barsForProgress) * waveWidth
                start = (progress * barsForProgress / visibleProgress - (barsForProgress / 2F)).roundToInt() - 1
                progressXPosition = getAvailableWidth() * 0.5F
            } else {
                start = 0
                progressXPosition = getAvailableWidth() * progress / maxProgress
            }
            // draw waves
            for (i in start until barsToDraw + start + 3) {
                sampleItemPosition = floor(i * step).roundToInt()
                var waveHeight =
                    if (sampleItemPosition in waveSample.indices && mMaxValue != 0)
                        (getAvailableHeight() - wavePaddingTop - wavePaddingBottom) * (waveSample[sampleItemPosition].toFloat() / mMaxValue) - (timestampTextHeight + 10)*2
                    else 0F

                if (waveHeight < waveMinHeight) waveHeight = waveMinHeight

                val top: Float = when (waveGravity) {
                    WaveGravity.TOP -> paddingTop.toFloat() + wavePaddingTop + (timestampTextHeight + 10)
                    WaveGravity.CENTER -> (paddingTop + wavePaddingTop + getAvailableHeight()) / 2F - waveHeight / 2F
                    WaveGravity.BOTTOM -> mCanvasHeight - paddingBottom - wavePaddingBottom - waveHeight
                }

                mWaveRect.set(
                    previousWaveRight,
                    top,
                    previousWaveRight + waveWidth,
                    top + waveHeight
                )
                when {
                    // if progress is currently in waveRect, color have to be split up
                    mWaveRect.contains(progressXPosition, mWaveRect.centerY()) -> {
                        mProgressCanvas.setBitmap(progressBitmap)
                        mWavePaint.color = waveProgressColor
                        mProgressCanvas.drawRect(
                            0F,
                            0F,
                            progressXPosition,
                            mWaveRect.bottom,
                            mWavePaint
                        )
                        mWavePaint.color = waveBackgroundColor
                        mProgressCanvas.drawRect(
                            progressXPosition,
                            0F,
                            getAvailableWidth().toFloat(),
                            mWaveRect.bottom,
                            mWavePaint
                        )
                        mWavePaint.shader = progressShader
                    }
                    mWaveRect.right <= progressXPosition -> {
                        mWavePaint.color = waveProgressColor
                        mWavePaint.shader = null
                    }
                    else -> {
                        mWavePaint.color = waveBackgroundColor
                        mWavePaint.shader = null
                    }
                }
                canvas.drawRect(mWaveRect,mWavePaint)
                previousWaveRight = mWaveRect.right
            }

            //draw markers
            marker?.forEach {
                // out of progress range
                if (it.key < 0 || it.key > maxProgress) return

                val markerXPosition = getAvailableWidth() * (it.key / maxProgress) + start
                mMarkerRect.set(
                    markerXPosition - (markerWidth / 2),
                    0f,
                    markerXPosition + (markerWidth / 2),
                    getAvailableHeight().toFloat()
                )
                mMarkerPaint.color = markerColor
                canvas.drawRect(mMarkerRect, mMarkerPaint)

                val markerTextDistance = markerTextPadding
                val markerTextXPosition = -markerXPosition - (markerWidth / 2) - markerTextDistance

                mMarkerPaint.textSize = markerTextSize
                mMarkerPaint.color = markerTextColor
                canvas.rotate(90f)
                canvas.drawText(it.value+start, markerTextDistance, markerTextXPosition, mMarkerPaint)
                canvas.rotate(-90f)
            }

            //Draw timestamps
            val nTimestamp = if(zoomed()) {
                visibleProgress / 1000
            } else {
                maxProgress / 1000
            }
            mTimestampPaint.color = timestampColor
            //TODO Fix problems with waveGap > 0 and waveWidth > 0.525
            if(waveWidth <= 0.525f){
                val timeStart: Int =
                    if(zoomed())
                        ( ( ( (progress - (visibleProgress * 0.5f)) / 1000 ).toInt()) /timestampSecondDistance)*timestampSecondDistance
                    else
                        0
                val timeEnd = (timeStart + nTimestamp + timestampSecondDistance)
                var time = timeStart
                while(time < timeEnd){
                    val timeX: Float = (getAvailableWidth().toFloat() / maxProgress) * ( time.toFloat() * (maxProgress / nTimestamp) ) - (start.toFloat() * waveWidth)
                    if(time >= 0){
                        canvas.drawLine(timeX,timestampTextHeight+10f,timeX ,getAvailableHeight().toFloat(),mTimestampPaint)
                        val minutes = (time / 60).toString()
                        val seconds = (time % 60).toString()
                        var sec = seconds
                        if ((time % 60)< 10) {
                            sec = "0$seconds"
                        }

                        val timeCodeStr = "$minutes:$sec"

                        val offset = (0.5f * mTimestampPaint.measureText(timeCodeStr))

                        canvas.drawText(
                            timeCodeStr,
                            timeX - offset,
                            timestampTextHeight.toFloat(),
                            mTimestampPaint
                        )
                    }
                    time+=timestampSecondDistance
                }
            }

            if(selectorStart.mills != selectorEnd.mills) {
                selectionRect.left = if (zoomed()) millsToPixels(selectorStart.mills) else millsToPixelsFlat(selectorStart.mills)
                selectionRect.right = if (zoomed()) millsToPixels(selectorEnd.mills) else millsToPixelsFlat(selectorEnd.mills)

                selectionRect.bottom = height.toFloat()
                mSelectorPaint.style = Paint.Style.FILL
                mSelectorPaint.color = selectionColor
                mSelectorPaint.alpha = 30
                canvas.drawRoundRect(
                    selectionRect,
                    roundedCorner[0],
                    roundedCorner[0],
                    mSelectorPaint
                )

                mSelectorPaint.color = selectionStrokeColor
                mSelectorPaint.style = Paint.Style.STROKE
                mSelectorPaint.alpha = 255
                canvas.drawRoundRect(
                    selectionRect,
                    roundedCorner[0],
                    roundedCorner[0],
                    mSelectorPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (!isEnabled)
            return false

        event!!

        if(selecting){
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(zoomed())
                        selectorStart.mills = pixelsToMills(event.x)
                    else
                        selectorStart.mills = pixelsToMillsFlat(event.x)
                }
                MotionEvent.ACTION_MOVE -> {
                    if(zoomed())
                        selectorEnd.mills = pixelsToMills(event.x)
                    else
                        selectorEnd.mills = pixelsToMillsFlat(event.x)
                    invalidate()
                }
                MotionEvent.ACTION_UP ->{
                    selecting = false
                }
            }
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                /*Yeah, that's horrible to see
                   if visibleProgress > 0 uses isTouching
                   else uses isTouchingFlat
                   Possible `edit` values
                     0: start selector
                     1: end selector
                    -1: just move the progress
                */
                edit = if (zoomed())
                    if(isTouching(selectorStart,event.x))
                        0
                    else if(isTouching(selectorEnd,event.x))
                        1
                    else -1
                else
                    if(isTouchingFlat(selectorStart,event.x))
                        0
                    else if(isTouchingFlat(selectorEnd,event.x))
                        1
                    else -1
            }

            MotionEvent.ACTION_MOVE -> {
                when(edit) {
                    0 -> {
                        selectorStart.mills = if (zoomed()) pixelsToMills(event.x) else pixelsToMillsFlat(event.x)
                    }
                    1 -> {
                        selectorEnd.mills = if (zoomed()) pixelsToMills(event.x) else pixelsToMillsFlat(event.x)
                    }
                }
                invalidate()
            }

        }

        if(edit == -1)
            if (zoomed()) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    wasPlaying = isPlaying
                    isPlaying = false
                    mTouchDownX = event.x
                    mAlreadyMoved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop || mAlreadyMoved) {
                        updateProgress(event)
                        mAlreadyMoved = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mPlayer.seekTo(getProgress(event).toInt())
                    if(wasPlaying){
                        isPlaying = true
                        wasPlaying = false
                    }
                    performClick()
                }
            }
        } else {
                when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isParentScrolling()) {
                        mTouchDownX = event.x
                        wasPlaying = isPlaying
                        isPlaying = false
                    }
                    else {
                        updateProgress(event)
                        wasPlaying = isPlaying
                        isPlaying = false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    updateProgress(event)
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - mTouchDownX) > mScaledTouchSlop) {
                        updateProgress(event)
                        mPlayer.seekTo(getProgress(event).toInt())
                        if(wasPlaying){
                            isPlaying = true
                            wasPlaying = false
                        }
                    }
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
        progress = getProgress(event)
        onProgressChanged?.onProgressChanged(this, progress, true)
    }

    private fun updateSecondDistance(visibleProgress: Float){

        val timestampSize = mTimestampPaint.measureText("0:00")
        timestampSecondDistance =
            if(zoomed())
                ( (visibleProgress/1000) / (width / (timestampSize * 1.5f)) ).toInt()
            else
                ( (maxProgress/1000) / (width / (timestampSize * 1.5f)) ).toInt()
    }
    private fun getProgress(event: MotionEvent): Float {
        return if (zoomed()) {
            (mPlayer.currentPosition - visibleProgress * (event.x - mTouchDownX) / getAvailableWidth()).coerceIn(
                0F,
                maxProgress
            )
        } else {
            maxProgress * event.x / getAvailableWidth()
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun getAvailableWidth(): Int {
        var width = mCanvasWidth - paddingLeft - paddingRight
        if (width <= 0) width = 1
        return width
    }

    private fun getAvailableHeight(): Int {
        var height = mCanvasHeight - paddingTop - paddingBottom
        if (height <= 0) height = 1
        return height
    }

    private fun millsToPixels(mills: Long): Float {
        val offset = (progress - visibleProgress / 2)
        return ((mills-offset) / visibleProgress) * getAvailableWidth()
    }

    private fun pixelsToMills(pixel: Float): Long {
        val offset = (progress - visibleProgress / 2)
        return (visibleProgress * (pixel / getAvailableWidth()) + offset).toLong()
    }

    private fun millsToPixelsFlat(mills: Long): Float{
        return width/maxProgress * mills
    }

    private fun pixelsToMillsFlat(pixel: Float): Long{
        return (maxProgress/width * pixel).toLong()
    }

    private fun isTouching(selector: Selector,touchPosition: Float): Boolean{
        val distance = abs(millsToPixels(selector.mills) - touchPosition)
        return distance < selector.distanceThreshold
    }

    private fun isTouchingFlat(selector: Selector, touchPosition: Float): Boolean{
        return abs(millsToPixelsFlat(selector.mills) - touchPosition) < selector.distanceThreshold
    }

    private fun zoomed(): Boolean{
        return visibleProgress > 0
    }
}
