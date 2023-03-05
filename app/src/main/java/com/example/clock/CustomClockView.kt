package com.example.clock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.util.*

@Suppress("DEPRECATION")
class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var hourHandLength = 0f
    private var minuteHandLength = 0f
    private var secondHandLength = 0f

    private var hourHandColor = Color.BLACK
    private var minuteHandColor = Color.BLACK
    private var secondHandColor = Color.RED

    private var calendar: Calendar

    private val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val secondHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ClockView)
            hourHandColor = typedArray.getColor(R.styleable.ClockView_hourHandColor, Color.BLACK)
            minuteHandColor =
                typedArray.getColor(R.styleable.ClockView_minuteHandColor, Color.BLACK)
            secondHandColor = typedArray.getColor(R.styleable.ClockView_secondHandColor, Color.RED)
            typedArray.recycle()
        }

        hourHandPaint.strokeWidth = resources.getDimension(R.dimen.hour_hand_stroke_width)
        hourHandPaint.color = hourHandColor

        minuteHandPaint.strokeWidth = resources.getDimension(R.dimen.minute_hand_stroke_width)
        minuteHandPaint.color = minuteHandColor

        secondHandPaint.strokeWidth = resources.getDimension(R.dimen.second_hand_stroke_width)
        secondHandPaint.color = secondHandColor

        calendar = Calendar.getInstance()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = resources.getDimension(R.dimen.clock_size).toInt()
        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)

        val minSize = minOf(width, height)
        radius = (minSize / 2).toFloat()
        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()

        hourHandLength = (radius * 0.4).toFloat()
        minuteHandLength = (radius * 0.55).toFloat()
        secondHandLength = (radius * 0.7).toFloat()

        setMeasuredDimension(width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTimer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTimer()
    }

    private var timer: Timer? = null

    private fun startTimer() {
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    calendar = Calendar.getInstance()
                    postInvalidate()
                }
            }, 0, 1000)
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.calendar = calendar
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superState)
        savedState?.let {
            calendar = it.calendar ?: Calendar.getInstance()
            postInvalidate()
        }
    }

    internal class SavedState : BaseSavedState {

        var calendar: Calendar? = null

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            calendar = `in`.readSerializable() as? Calendar
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeSerializable(calendar)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private fun drawHourHand(canvas: Canvas, hour: Int, minute: Int) {
        val angle = (hour + minute / 60f) * 30f
        val radians = Math.toRadians(angle.toDouble())
        val startX = centerX
        val startY = centerY
        val stopX = centerX + hourHandLength * kotlin.math.sin(radians).toFloat()
        val stopY = centerY - hourHandLength * kotlin.math.cos(radians).toFloat()

        canvas.drawLine(startX, startY, stopX, stopY, hourHandPaint)
    }

    private fun drawMinuteHand(canvas: Canvas, minute: Int) {
        val angle = minute * 6f
        val radians = Math.toRadians(angle.toDouble())
        val startX = centerX
        val startY = centerY
        val stopX = centerX + minuteHandLength * kotlin.math.sin(radians).toFloat()
        val stopY = centerY - minuteHandLength * kotlin.math.cos(radians).toFloat()

        canvas.drawLine(startX, startY, stopX, stopY, minuteHandPaint)
    }

    private fun drawSecondHand(canvas: Canvas, second: Int) {
        val angle = second * 6f
        val radians = Math.toRadians(angle.toDouble())
        val startX = centerX
        val startY = centerY
        val stopX = centerX + secondHandLength * kotlin.math.sin(radians).toFloat()
        val stopY = centerY - secondHandLength * kotlin.math.cos(radians).toFloat()

        canvas.drawLine(startX, startY, stopX, stopY, secondHandPaint)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        val facePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val strokePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5F
        }

        val centerPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }


        // рисование циферблата
        canvas.drawCircle(radius * 2  / 2f, radius * 2 / 2f, radius, facePaint)
        canvas.drawCircle(radius * 2  / 2f, radius * 2 / 2f, radius, strokePaint)


        // определение размера цифр на циферблате
        val numeralSpacing = radius / 8
        val numeralRadius = radius - numeralSpacing

        // настройка кисти для рисования цифр на циферблате
        val numeralPaint = Paint().apply {
            color = Color.BLACK
            textSize = numeralSpacing
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        // рисование цифр на циферблате
        for (i in 1..12) {
            val angle = Math.PI / 6 * (i - 3)
            val x = (radius * 2  / 2 + kotlin.math.cos(angle) * numeralRadius).toFloat()
            val y = (radius * 2 / 2 + kotlin.math.sin(angle) * numeralRadius).toFloat()
            canvas.drawText(i.toString(), x, y, numeralPaint)
        }

        drawHourHand(canvas, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE))
        drawMinuteHand(canvas, calendar.get(Calendar.MINUTE))
        drawSecondHand(canvas, calendar.get(Calendar.SECOND))
//        // рисуем часовую стрелку
//        val hourRotation =
//            360f / 12 * calendar.get(Calendar.HOUR) + 30f / 60 * calendar.get(Calendar.MINUTE)
//        canvas?.rotate(hourRotation, centerX, centerY)
//        canvas?.drawLine(centerX, centerY, centerX, centerY - hourHandLength, hourHandPaint)
//        canvas?.rotate(-hourRotation, centerX, centerY)
//
//        // рисуем минутную стрелку
//        val minuteRotation =
//            360f / 60 * calendar.get(Calendar.MINUTE) + 6f / 60 * calendar.get(Calendar.SECOND)
//        canvas?.rotate(minuteRotation, centerX, centerY)
//        canvas?.drawLine(centerX, centerY, centerX, centerY - minuteHandLength, minuteHandPaint)
//        canvas?.rotate(-minuteRotation, centerX, centerY)
//
//        // рисуем секундную стрелку
//        val secondRotation = 360f / 60 * calendar.get(Calendar.SECOND)
//        canvas?.rotate(secondRotation, centerX, centerY)
//        canvas?.drawLine(centerX, centerY, centerX, centerY - secondHandLength, secondHandPaint)
//        canvas?.rotate(-secondRotation, centerX, centerY)

        // рисуем центр часов
        val centerDotRadius = 20f
        canvas.drawCircle(centerX, centerY, centerDotRadius, centerPaint)
        invalidate()
    }
}