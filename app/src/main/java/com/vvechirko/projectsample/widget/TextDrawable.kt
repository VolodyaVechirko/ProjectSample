package com.vvechirko.projectsample.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.StyleRes
import com.vvechirko.projectsample.setTextAppearance

class TextDrawable(context: Context, val text: String, @StyleRes textAppearance: Int = 0) : Drawable() {

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
    }

    private var intrinsicWidth: Int = 0
    private var intrinsicHeight: Int = 0

    init {
        paint.setTextAppearance(context, textAppearance)

        intrinsicWidth = paint.measureText(text).toInt()
        val fm = paint.fontMetrics
        intrinsicHeight = (fm.bottom - fm.top).toInt()
    }

    override fun draw(canvas: Canvas) {
        val baseline = bounds.bottom.toFloat() - paint.fontMetrics.bottom
        canvas.drawText(text, bounds.left.toFloat(), baseline, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getIntrinsicHeight(): Int {
        return intrinsicHeight
    }

    override fun getIntrinsicWidth(): Int {
        return intrinsicWidth
    }
}