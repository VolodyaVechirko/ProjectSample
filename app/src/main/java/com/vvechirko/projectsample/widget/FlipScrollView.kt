package com.vvechirko.projectsample.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.OverScroller
import android.widget.Toast
import kotlin.math.abs

class FlipScrollView(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val scroller = OverScroller(context)

    private var scrollRange = 0
    private var isBeingDragged = false
    private var lastMotionX = 0
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    init {
        isFocusable = true
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val click = View.OnClickListener {
            Toast.makeText(it.context, "Click!", Toast.LENGTH_SHORT).show()
        }
        for (i in 0 until childCount) {
            getChildAt(i).setOnClickListener(click)
        }
    }

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

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE && isBeingDragged) {
            return true
        }
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> run {
                /*
                 * isBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */
                /*
                 * Locally do absolute value. lastMotionX is set to the x value
                 * of the down event.
                 */
                val activePointerId = activePointerId
                if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    return@run
                }
                val pointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointerIndex).toInt()
                val xDiff = abs(x - lastMotionX)
                if (xDiff > touchSlop) {
                    isBeingDragged = true
                    lastMotionX = x
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val x = event.x.toInt()
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                lastMotionX = x
                activePointerId = event.getPointerId(0)
                isBeingDragged = false
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                /* Release the drag */
                isBeingDragged = false
                activePointerId = MotionEvent.INVALID_POINTER_ID
                onDragFinished()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                lastMotionX = event.getX(index).toInt()
                activePointerId = event.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                lastMotionX = event.getX(event.findPointerIndex(activePointerId)).toInt()
            }
        }
        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return isBeingDragged
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Log.d("onTouchEvent", "onTouchEvent $event")
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isBeingDragged = false
                if (isBeingDragged) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                // Remember where the motion event started
                lastMotionX = event.x.toInt()
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(activePointerIndex).toInt()
                var deltaX = lastMotionX - x
                if (!isBeingDragged && abs(deltaX) > touchSlop) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    isBeingDragged = true
                    if (deltaX > 0) {
                        deltaX -= touchSlop
                    } else {
                        deltaX += touchSlop
                    }
                }
                if (isBeingDragged) {
                    // Scroll to follow the motion event
                    lastMotionX = x
                    val oldX = scrollX
                    val oldY = scrollY
                    overScrollBy(
                        deltaX, 0, scrollX, 0, scrollRange, 0,
                        0, 0, true
                    )
                    onScrollChanged(scrollX, scrollY, oldX, oldY)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (isBeingDragged) {
                activePointerId = MotionEvent.INVALID_POINTER_ID
                isBeingDragged = false
                onDragFinished()
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            lastMotionX = ev.getX(newPointerIndex).toInt()
            activePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun onDragFinished() {
        scroller.forceFinished(true)
        val toX = if (scrollX > scrollRange / 2) scrollRange else 0
        scroller.startScroll(scrollX, scrollY, toX - scrollX, 0)
        postInvalidateOnAnimation()
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
//            Log.d("GestureDetector", "computeScroll $currX")

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

//        Log.d("GestureDetector", "scrollX $scrollX, scrollRange $scrollRange")
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        super.scrollTo(scrollX, scrollY)
    }

    override fun shouldDelayChildPressedState(): Boolean = true

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