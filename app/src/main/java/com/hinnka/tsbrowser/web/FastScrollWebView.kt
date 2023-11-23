package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * https://github.com/Mixiaoxiao/FastScroll-Everywhere FastScrollWebView
 *
 * @author Mixiaoxiao 2016-08-31
 */
open class FastScrollWebView : LollipopFixWebView, FastScrollDelegate.FastScrollable {
    var mFastScrollDelegate: FastScrollDelegate? = null

    // ===========================================================
    // Constructors
    // ===========================================================
    constructor(context: Context) : super(context) {
        createFastScrollDelegate(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        createFastScrollDelegate(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        createFastScrollDelegate(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        createFastScrollDelegate(context)
    }

    // ===========================================================
    // createFastScrollDelegate
    // ===========================================================
    private fun createFastScrollDelegate(context: Context) {
        mFastScrollDelegate = FastScrollDelegate.Builder(this).build()
        // 以下这行是自己添加的
        density = context.resources.displayMetrics.density
    }

    // ===========================================================
    // Delegate
    // ===========================================================
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (mFastScrollDelegate!!.onInterceptTouchEvent(ev)) {
            true
        } else super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mFastScrollDelegate!!.onTouchEvent(event)) {
            true
        } else super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFastScrollDelegate!!.onAttachedToWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (mFastScrollDelegate != null) {
            mFastScrollDelegate!!.onVisibilityChanged(changedView, visibility)
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        mFastScrollDelegate!!.onWindowVisibilityChanged(visibility)
    }

    override fun awakenScrollBars(): Boolean {
        return mFastScrollDelegate!!.awakenScrollBars()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        mFastScrollDelegate!!.dispatchDrawOver(canvas)
    }

    // ===========================================================
    // FastScrollable IMPL, ViewInternalSuperMethods
    // ===========================================================
    override fun superOnTouchEvent(event: MotionEvent?) {
        super.onTouchEvent(event)
    }

    override fun superComputeVerticalScrollExtent(): Int {
        return super.computeVerticalScrollExtent()
    }

    override fun superComputeVerticalScrollOffset(): Int {
        return super.computeVerticalScrollOffset()
    }

    override fun superComputeVerticalScrollRange(): Int {
        return super.computeVerticalScrollRange()
    }

    override fun getFastScrollableView(): View? {
        return this
    }


    override fun getFastScrollDelegate(): FastScrollDelegate? {
        return mFastScrollDelegate
    }

    override fun setNewFastScrollDelegate(newDelegate: FastScrollDelegate?) {
        requireNotNull(newDelegate) { "setNewFastScrollDelegate must NOT be NULL." }
        mFastScrollDelegate!!.onDetachedFromWindow()
        mFastScrollDelegate = newDelegate
        newDelegate.onAttachedToWindow()
    }

    // 以下是自己添加的
    private var lastContentHeight = 0
    private var density = 1f
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        post {
            if (lastContentHeight != contentHeight) {
                lastContentHeight = contentHeight
            }
        }
    }

    val actualContentHeight: Int
        get() = (lastContentHeight * density).toInt()
}