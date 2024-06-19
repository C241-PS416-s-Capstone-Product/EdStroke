package com.capstone.edstroke.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.capstone.edstroke.view.camera.PoseEstimationHelper.Keypoint

class KeypointsOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val keypoints = mutableListOf<Keypoint>()
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    fun updateKeypoints(newKeypoints: List<Keypoint>) {
        keypoints.clear()
        keypoints.addAll(newKeypoints)
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        keypoints.forEach { keypoint ->
            if (keypoint.score > 0.3) {
                canvas.drawCircle(keypoint.x * width, keypoint.y * height, 10f, paint)
            }
        }
    }
}
