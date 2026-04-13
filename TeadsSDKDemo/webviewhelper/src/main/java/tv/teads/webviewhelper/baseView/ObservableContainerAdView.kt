package tv.teads.webviewhelper.baseView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

open class ObservableContainerAdView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var touchForwardTarget: View? = null

    // Scroll detection state for onInterceptTouchEvent.
    // Mirrors the pattern used by ScrollView: track the initial touch position
    // and intercept once displacement exceeds the system touch slop.
    // Interception sends ACTION_CANCEL to children, preventing performClick().
    private var interceptDownX = 0f
    private var interceptDownY = 0f
    private var isScrolling = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    fun setTouchForwardTarget(target: View) {
        this.touchForwardTarget = target
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        forwardTouchEvent(event)

        when (event.actionMasked) {
            // Record initial finger position and reset scroll state for this gesture
            MotionEvent.ACTION_DOWN -> {
                interceptDownX = event.x
                interceptDownY = event.y
                isScrolling = false
            }
            // Once finger moves beyond touch slop, intercept to send ACTION_CANCEL to children
            MotionEvent.ACTION_MOVE -> {
                if (!isScrolling) {
                    val dx = abs(event.x - interceptDownX)
                    val dy = abs(event.y - interceptDownY)
                    if (dx > touchSlop || dy > touchSlop) {
                        isScrolling = true
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isScrolling = false
            }
        }
        forwardTouchEvent(event)
        return true
    }

    private fun forwardTouchEvent(event: MotionEvent) {
        touchForwardTarget?.let { target ->
            // Create a copy of the event
            val forwardEvent = MotionEvent.obtain(event)

            // Get screen positions of both views
            val thisLocation = IntArray(2)
            val targetLocation = IntArray(2)
            this.getLocationOnScreen(thisLocation)
            target.getLocationOnScreen(targetLocation)

            // Translate coordinates: from ad view space to WebView space
            forwardEvent.offsetLocation(
                (thisLocation[0] - targetLocation[0]).toFloat(),
                (thisLocation[1] - targetLocation[1]).toFloat()
            )

            target.dispatchTouchEvent(forwardEvent)
            forwardEvent.recycle()
        }
    }
}
