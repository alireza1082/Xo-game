package com.example.android.xo.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.sqrt

class WinningLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var startX = 0f
    private var startY = 0f
    private var targetEndX = 0f
    private var targetEndY = 0f
    private var currentEndX = 0f
    private var currentEndY = 0f

    private var isAnimating = false
    private var animator: ValueAnimator? = null

    init {
        // Line thickness: 7dp
        paint.strokeWidth = resources.displayMetrics.density * 7f
    }

    fun startWinAnimation(winningIndices: IntArray?, lineColor: Int) {
        if (winningIndices == null || winningIndices.size < 3) return

        post {
            animator?.cancel()

            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return@post

            paint.color = lineColor

            val firstIdx = winningIndices[0]
            val lastIdx = winningIndices[winningIndices.size - 1]

            val colA = firstIdx % 3
            val rowA = firstIdx / 3
            val centerAX = (colA + 0.5f) * (w / 3f)
            val centerAY = (rowA + 0.5f) * (h / 3f)

            val colB = lastIdx % 3
            val rowB = lastIdx / 3
            val centerBX = (colB + 0.5f) * (w / 3f)
            val centerBY = (rowB + 0.5f) * (h / 3f)

            val dx = centerBX - centerAX
            val dy = centerBY - centerAY
            val dist = sqrt(dx * dx + dy * dy)

            val nx = if (dist > 0f) dx / dist else 0f
            val ny = if (dist > 0f) dy / dist else 0f
            val ext = (w / 3f) * 0.32f

            startX = centerAX - nx * ext
            startY = centerAY - ny * ext
            targetEndX = centerBX + nx * ext
            targetEndY = centerBY + ny * ext

            currentEndX = startX
            currentEndY = startY
            isAnimating = true

            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 320L
                interpolator = DecelerateInterpolator(1.5f)
                addUpdateListener { va ->
                    val fraction = va.animatedValue as Float
                    currentEndX = startX + (targetEndX - startX) * fraction
                    currentEndY = startY + (targetEndY - startY) * fraction
                    invalidate()
                }
                start()
            }
        }
    }

    fun clear() {
        animator?.cancel()
        animator = null
        isAnimating = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isAnimating) {
            canvas.drawLine(startX, startY, currentEndX, currentEndY, paint)
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}
