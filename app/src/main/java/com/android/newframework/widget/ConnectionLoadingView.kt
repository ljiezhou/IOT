package com.android.newframework.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

class ConnectionLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val mainColor = Color.parseColor("#4A7189")

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = mainColor
        alpha = 150
    }

    private val middlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = mainColor
        alpha = 80
    }

    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#2D4A5E")
    }

    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = mainColor
    }

    private var pulseScale = 1f
    private var ripple1 = 0f
    private var ripple2 = 0.5f

    private var pulseAnimator: ValueAnimator? = null
    private var rippleAnimator: ValueAnimator? = null


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val baseRadius = min(width, height) * 0.35f

        // ① 背景基底圆（最外层最小范围）
        canvas.drawCircle(cx, cy, baseRadius, backgroundPaint)

        // ② 脉冲外圈（缩放）
        canvas.save()
        canvas.scale(pulseScale, pulseScale, cx, cy)
        canvas.drawCircle(cx, cy, baseRadius, outerPaint)
        canvas.restore()

        // ③ 中圈装饰
        canvas.drawCircle(cx, cy, baseRadius * 0.85f, middlePaint)

        // ④ 波纹
        drawRipple(canvas, cx, cy * 0.9f, baseRadius * ripple1, 1 - ripple1)
        drawRipple(canvas, cx, cy * 0.9f, baseRadius * ripple2, 1 - ripple2)
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#2D4A5E") // 你现在用的工业蓝
    }

    private fun drawRipple(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        alphaFactor: Float
    ) {
        if (radius <= 0f) return
        ripplePaint.alpha = (alphaFactor * 80).toInt()
        canvas.drawCircle(cx, cy, radius, ripplePaint)
    }

    fun startWaiting() {
        startPulse()
        startRipple()
    }

    fun showConnected() {
        stop()
        pulseScale = 1f
        invalidate()
    }

    fun stop() {
        pulseAnimator?.cancel()
        rippleAnimator?.cancel()
    }

    private fun startPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.05f, 1f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                pulseScale = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startRipple() {
        rippleAnimator?.cancel()
        rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                ripple1 = it.animatedValue as Float
                ripple2 = (ripple1 + 0.5f) % 1f
                invalidate()
            }
            start()
        }
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density
}