package com.vvechirko.projectsample.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.OverScroller

class FlipView(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs),
    GestureDetector.OnGestureListener {

    var scrollRange = 0

    private val gestureDetector = GestureDetector(context, this)
    private val scroller = OverScroller(context)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (MeasureSpec.getSize(widthMeasureSpec) * 0.8).toInt()
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, mode)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
//            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            measureChildWithMargins(child, childWidthMeasureSpec, 0, heightMeasureSpec, 0)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val parentLeft = paddingLeft
        val parentRight = right - left - paddingRight
        val parentTop = paddingTop
        val parentBottom = bottom - top - paddingBottom

        var left = parentLeft
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams
            val childLeft = left + lp.leftMargin
            val childTop = parentTop + lp.topMargin
            val childRight = childLeft + child.measuredWidth
            val childBottom = childTop + child.measuredHeight

            left = childRight + lp.rightMargin
            child.layout(childLeft, childTop, childRight, childBottom)
        }

        val total = left + paddingRight
        scrollRange = if (total > width) total - width else 0
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Log.d("onTouchEvent", "onTouchEvent $event")
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDown(event: MotionEvent): Boolean {
        Log.d("GestureDetector", "onDown")
        scroller.forceFinished(true)
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d("GestureDetector", "onLongPress")
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d("GestureDetector", "onShowPress")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d("GestureDetector", "onSingleTapUp")
        return false
    }

    override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        Log.d("GestureDetector", "onScroll $distanceX, $distanceY")
        val dx = distanceX.toInt()
        updateScroll(scrollX + dx)
        return true
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        Log.d("GestureDetector", "onFling $velocityX, $velocityY")
        scroller.forceFinished(true)
        scroller.fling(
            scrollX, 0, -velocityX.toInt(), -velocityY.toInt(),
            0, scrollRange, 0, 0
        )
        postInvalidateOnAnimation()
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            Log.d("GestureDetector", "computeScroll $currX")

            if (currX != scroller.finalX) {
                updateScroll(currX)
            }
        }
    }

    private fun updateScroll(value: Int) {
        val validValue = when {
            value < 0 -> 0
            value > scrollRange -> scrollRange
            else -> value
        }

        if (scrollX != validValue) {
            scrollX = validValue
        }

        Log.d("GestureDetector", "scrollX $scrollX, scrollRange $scrollRange")
    }

    override fun generateDefaultLayoutParams(): MarginLayoutParams {
        return MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): MarginLayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): MarginLayoutParams {
        return MarginLayoutParams(lp)
    }
}