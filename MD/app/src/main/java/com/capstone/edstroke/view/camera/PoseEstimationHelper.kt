package com.capstone.edstroke.view.camera

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.capstone.edstroke.R
import com.capstone.edstroke.ml.SingleposeMovenetLightning
import com.google.android.gms.tasks.Task
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class PoseEstimationHelper(
    val context: Context,
    val detectorListener: DetectorListener?,
    private val onError: (String) -> Unit,
) {
    private var model: SingleposeMovenetLightning? = null
    private var initializationTask: Task<Void>? = null

    init {
        initializationTask = TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable ->
            val optionsBuilder = TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLiteVision.initialize(context, optionsBuilder.build())
        }.addOnSuccessListener {
            Log.d(TAG, "TfLiteVision initialized successfully")
            downloadModel()
        }.addOnFailureListener {
            val errorMsg = context.getString(R.string.tflitevision_is_not_initialized_yet)
            detectorListener?.onError(errorMsg)
            onError(errorMsg)
            Log.e(TAG, "TfLiteVision initialization failed", it)
        }
    }

    @Synchronized
    private fun downloadModel() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel("singlepose-movenet-lightning", DownloadType.LOCAL_MODEL, conditions)
            .addOnSuccessListener { model: CustomModel ->
                try {
                    val modelFile = model.file
                    if (modelFile != null) {
                        setupModel()
                    } else {
                        throw IOException("Model file is null")
                    }
                } catch (e: IOException) {
                    val errorMsg = e.message.toString()
                    detectorListener?.onError(errorMsg)
                    onError(errorMsg)
                }
            }
            .addOnFailureListener { e: Exception? ->
                val errorMsg = context.getString(R.string.firebaseml_model_download_failed)
                detectorListener?.onError(errorMsg)
                onError(errorMsg)
                Log.e(TAG, "Model download failed", e)
            }
    }

    private fun setupModel() {
        try {
            val options = Model.Options.Builder()
                .setDevice(Model.Device.GPU)
                .setNumThreads(4)
                .build()

            model = SingleposeMovenetLightning.newInstance(context, options)
            Log.d(TAG, "Pose estimation model set up successfully")
        } catch (e: IOException) {
            val errorMsg = context.getString(R.string.image_classifier_failed)
            detectorListener?.onError(errorMsg)
            onError(errorMsg)
            Log.e(TAG, "Pose estimation model setup failed", e)
        } catch (e: IllegalArgumentException) {
            val errorMsg = context.getString(R.string.image_classifier_failed)
            detectorListener?.onError(errorMsg)
            onError(errorMsg)
            Log.e(TAG, "Pose estimation model setup failed", e)
        }
    }

    fun detectPose(image: ImageProxy) {
        initializationTask?.addOnSuccessListener {
            if (!TfLiteVision.isInitialized()) {
                val errorMessage = context.getString(R.string.tflitevision_is_not_initialized_yet)
                Log.e(TAG, errorMessage)
                detectorListener?.onError(errorMessage)
                onError(errorMessage)
                return@addOnSuccessListener
            }

            if (model == null) {
                val errorMsg = "Pose estimation model is not set up yet"
                detectorListener?.onError(errorMsg)
                onError(errorMsg)
                return@addOnSuccessListener
            }

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR))
                .add(Rot90Op(-image.imageInfo.rotationDegrees / 90))
                .add(NormalizeOp(0f, 1f))
                .build()

            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(toBitmap(image)))
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(tensorImage.buffer)

            var inferenceTime = SystemClock.uptimeMillis()
            val outputs = model?.process(inputFeature0)
            inferenceTime = SystemClock.uptimeMillis() - inferenceTime

            outputs?.outputFeature0AsTensorBuffer?.let {
                val keypoints = extractKeypoints(it)
                detectorListener?.onResults(keypoints, inferenceTime)
            } ?: run {
                val errorMsg = "Pose estimation inference failed"
                detectorListener?.onError(errorMsg)
                onError(errorMsg)
            }

        }?.addOnFailureListener {
            val errorMessage = context.getString(R.string.tflitevision_is_not_initialized_yet)
            Log.e(TAG, errorMessage)
            detectorListener?.onError(errorMessage)
            onError(errorMessage)
        }
    }

    private fun extractKeypoints(outputFeature0: TensorBuffer): List<Keypoint> {
        val keypoints = mutableListOf<Keypoint>()
        val scores = outputFeature0.floatArray

        // Assume that each keypoint has 3 values: x, y, and score
        for (i in scores.indices step 3) {
            val x = scores[i]
            val y = scores[i + 1]
            val score = scores[i + 2]
            keypoints.add(Keypoint(x, y, score))
        }
        return keypoints
    }

    private fun toBitmap(image: ImageProxy): Bitmap {
        val bitmapBuffer = Bitmap.createBitmap(
            image.width,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        image.close()
        return bitmapBuffer
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            keypoints: List<Keypoint>?,
            inferenceTime: Long
        )
    }

    data class Keypoint(val x: Float, val y: Float, val score: Float)

    companion object {
        private const val TAG = "PoseEstimationHelper"
    }
}
