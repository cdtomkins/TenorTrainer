package com.cdtomkins.tenortrainer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.cdtomkins.tenortrainer.game.PanNote
import com.cdtomkins.tenortrainer.game.TenorPan

/** Shows the pan photo, scaled to fit, and reports taps in image pixel coordinates. */
class PanView(context: Context) : View(context) {
    private val bitmap: Bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.tenor_pan,
        BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 },
    )

    /** Called with the tap position in image pixels. */
    var onTap: ((x: Float, y: Float) -> Unit)? = null

    /** Notes whose printed letter is covered, so the player has to recognise them. */
    var coveredNotes: List<PanNote> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.rgb(22, 20, 18)
    }

    private val imageRect = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.dp(4).toFloat()
    }
    private var scale = 1f
    private var feedbackX = 0f
    private var feedbackY = 0f
    private var feedbackColor: Int? = null
    private val clearFeedback = Runnable {
        feedbackColor = null
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scale = minOf(w.toFloat() / bitmap.width, h.toFloat() / bitmap.height)
        val drawnW = bitmap.width * scale
        val drawnH = bitmap.height * scale
        val left = (w - drawnW) / 2f
        val top = (h - drawnH) / 2f
        imageRect.set(left, top, left + drawnW, top + drawnH)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(bitmap, null, imageRect, null)

        val coverRadius = COVER_RADIUS_PX * scale
        coveredNotes.forEach { note ->
            canvas.drawCircle(
                imageRect.left + note.x * scale,
                imageRect.top + note.y * scale,
                coverRadius,
                coverPaint,
            )
        }

        feedbackColor?.let { color ->
            paint.color = color
            val cx = imageRect.left + feedbackX * scale
            val cy = imageRect.top + feedbackY * scale
            canvas.drawCircle(cx, cy, context.dp(28).toFloat(), paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            val x = (event.x - imageRect.left) / scale
            val y = (event.y - imageRect.top) / scale
            if (x in 0f..TenorPan.IMAGE_SIZE.toFloat() && y in 0f..TenorPan.IMAGE_SIZE.toFloat()) {
                onTap?.invoke(x, y)
            }
            performClick()
        }
        return true
    }

    /** Briefly marks a tap at image coordinates ([x], [y]): green for a hit, red for a miss. */
    fun showFeedback(x: Float, y: Float, hit: Boolean) {
        feedbackX = x
        feedbackY = y
        feedbackColor = if (hit) Color.GREEN else Color.RED
        removeCallbacks(clearFeedback)
        postDelayed(clearFeedback, FEEDBACK_MS)
        invalidate()
    }

    private companion object {
        const val FEEDBACK_MS = 450L
        /** Radius in image pixels; large enough to cover a printed note name. */
        const val COVER_RADIUS_PX = 46f
    }
}
