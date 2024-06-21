package com.capstone.edstroke.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.capstone.edstroke.view.risk_exercise.PoseEstimationHelper

class KeypointsOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val keypoints = mutableListOf<PoseEstimationHelper.Keypoint>()
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    fun updateKeypoints(newKeypoints: List<PoseEstimationHelper.Keypoint>) {
        keypoints.clear()
        keypoints.addAll(newKeypoints)
        Log.d("KeypointsOverlayView", "Updated keypoints: $keypoints")
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("KeypointsOverlayView", "Drawing keypoints: $keypoints")
        keypoints.forEach { keypoint ->
            val x = keypoint.x * width
            val y = keypoint.y * height
            Log.d("KeypointsOverlayView", "Drawing keypoint at ($x, $y) with score ${keypoint.score}")
            canvas.drawCircle(x, y, 10f, paint)
        }
    }
}
