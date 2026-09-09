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

    private var isShowing = false
    private var animator: ValueAnimator? = null

    private var activeIndices: IntArray? = null
    private var activeColor: Int = 0

    init {
        paint.strokeWidth = resources.displayMetrics.density * 8f
    }

    fun startWinAnimation(winningIndices: IntArray?, lineColor: Int) {
        if (winningIndices == null || winningIndices.size < 3) return
        activeIndices = winningIndices.clone()
        activeColor = lineColor

        post {
            animator?.cancel()
            if (!calculateCoordinates(winningIndices, lineColor)) return@post

            currentEndX = startX
            currentEndY = startY
            isShowing = true

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

    fun showWinningLine(winningIndices: IntArray?, lineColor: Int) {
        if (winningIndices == null || winningIndices.size < 3) return
        activeIndices = winningIndices.clone()
        activeColor = lineColor

        post {
            animator?.cancel()
            if (!calculateCoordinates(winningIndices, lineColor)) return@post

            currentEndX = targetEndX
            currentEndY = targetEndY
            isShowing = true
            invalidate()
        }
    }

    private fun calculateCoordinates(winningIndices: IntArray, lineColor: Int): Boolean {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return false

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
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val indices = activeIndices
        if (isShowing && indices != null) {
            calculateCoordinates(indices, activeColor)
            currentEndX = targetEndX
            currentEndY = targetEndY
            invalidate()
        }
    }

    fun clear() {
        animator?.cancel()
        animator = null
        activeIndices = null
        isShowing = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isShowing) {
            canvas.drawLine(startX, startY, currentEndX, currentEndY, paint)
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}
