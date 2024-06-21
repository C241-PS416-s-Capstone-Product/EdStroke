package com.capstone.edstroke.view.risk_exercise

import android.content.Context
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException

class RiskExerciseHelper(
    val exercise: String,
    val context: Context,
    private val onError: (String) -> Unit,
) {
    private var interpreter: Interpreter? = null

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            downloadModel(exercise)
        }
    }

    private suspend fun downloadModel(exercise: String) {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        try {
            when (exercise) {
                "Shoulder Range of Motion" -> {
                    val model = FirebaseModelDownloader.getInstance()
                        .getModel("exercise-trial-1", DownloadType.LOCAL_MODEL, conditions)
                        .await()
                    val modelFile = model.file ?: throw IOException("Model file is null")
                    setupModel(modelFile)
                }
                "Mini-Lunge" -> {
                    val model = FirebaseModelDownloader.getInstance()
                        .getModel("exercise-trial-1", DownloadType.LOCAL_MODEL, conditions)
                        .await()
                    val modelFile = model.file ?: throw IOException("Model file is null")
                    setupModel(modelFile)
                }
                else -> {
                    val errorMsg = "Invalid exercise: $exercise"
                    Log.e(TAG, errorMsg)
                    onError(errorMsg)
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Failed to download exercise-trial-1 model"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
        }
    }

    private fun setupModel(modelFile: File) {
        val options = Interpreter.Options()
        if (CompatibilityList().isDelegateSupportedOnThisDevice) {
            options.addDelegate(GpuDelegate())
            Log.d(TAG, "Using GPU delegate")
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            options.useNNAPI = true
            Log.d(TAG, "Using NNAPI")
        } else {
            options.setNumThreads(4)
            Log.d(TAG, "Using CPU with 4 threads")
        }
        try {
            interpreter = Interpreter(modelFile, options)
            Log.d(TAG, "Exercise trial model set up successfully")
        } catch (e: IOException) {
            val errorMsg = "Failed to set up exercise-trial-1 model"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
        }
    }

    fun analyzeKeypoints(keypoints: List<PoseEstimationHelper.Keypoint>): FloatArray? {
        if (interpreter == null) {
            val errorMsg = "Exercise trial model is not set up yet"
            Log.e(TAG, errorMsg)
            onError(errorMsg)
            return null
        }

        if (keypoints.size != 17) {
            val errorMsg = "Invalid number of keypoints. Expected 17, got ${keypoints.size}"
            Log.e(TAG, errorMsg)
            onError(errorMsg)
            return null
        }

        val input = FloatArray(34) // Each keypoint has x, y -> 17 * 2 = 34
        keypoints.forEachIndexed { index, keypoint ->
            input[index * 2] = keypoint.x
            input[index * 2 + 1] = keypoint.y
        }

        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 34), DataType.FLOAT32)
        inputBuffer.loadArray(input)

        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, OUTPUT_SIZE), DataType.FLOAT32)

        return try {
            interpreter?.run(inputBuffer.buffer, outputBuffer.buffer.rewind())
            outputBuffer.floatArray
        } catch (e: Exception) {
            val errorMsg = "Failed to run inference on exercise-trial-1 model"
            Log.e(TAG, errorMsg, e)
            onError(errorMsg)
            null
        }
    }

    fun close() {
        interpreter?.close()
    }

    companion object {
        private const val TAG = "RiskExerciseHelper"
        private const val OUTPUT_SIZE = 2 // Adjust this according to the model output size
    }
}
