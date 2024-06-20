package com.capstone.edstroke.view.risk_exercise

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.capstone.edstroke.R
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.firebase.ml.modeldownloader.CustomModel
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
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class PoseEstimationHelper(
    val context: Context,
    val detectorListener: DetectorListener?,
    private val onError: (String) -> Unit,
) {
    private var interpreter: Interpreter? = null

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            initializeTfLite()
            downloadModel()
        }
    }

    private suspend fun initializeTfLite() {
        val gpuAvailable = TfLiteGpu.isGpuDelegateAvailable(context).await()
        val optionsBuilder = TfLiteInitializationOptions.builder()
        if (gpuAvailable) {
            optionsBuilder.setEnableGpuDelegateSupport(true)
            Log.d(TAG, "GPU delegate support enabled")
        }
        TfLiteVision.initialize(context, optionsBuilder.build()).await()
        Log.d(TAG, "TfLiteVision initialized successfully")
    }

    private suspend fun downloadModel() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        try {
            val model = FirebaseModelDownloader.getInstance()
                .getModel("singlepose-movenet-lightning", DownloadType.LOCAL_MODEL, conditions)
                .await()
            val modelFile = model.file ?: throw IOException("Model file is null")
            setupModel(modelFile)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.firebaseml_model_download_failed)
            detectorListener?.onError(errorMsg)
            onError(errorMsg)
            Log.e(TAG, "Model download failed", e)
        }
    }

    private fun setupModel(modelFile: File) {
        try {
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
            interpreter = Interpreter(modelFile, options)
            Log.d(TAG, "Pose estimation model set up successfully")
        } catch (e: IOException) {
            val errorMsg = context.getString(R.string.image_classifier_failed)
            detectorListener?.onError(errorMsg)
            onError(errorMsg)
            Log.e(TAG, "Pose estimation model setup failed", e)
        }
    }

    fun detectPose(image: ImageProxy) {
        Log.d(TAG, "Detect pose called")

        if (interpreter == null) {
            val errorMsg = "Pose estimation model is not set up yet"
            Log.d(TAG, errorMsg)
            detectorListener?.onError(errorMsg)
            onError(errorMsg)
            image.close()
            return
        }

        try {
            // Convert ImageProxy to Bitmap
            val bitmap = toBitmap(image)
            Log.d(TAG, "Converted ImageProxy to Bitmap")

            // Check if the image is black
            if (isBlackImage(bitmap)) {
                Log.d(TAG, "Black image detected, skipping pose detection")
                image.close()
                return
            }

            // Prepare TensorImage
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            Log.d(TAG, "TensorImage loaded with Bitmap")

            // Process image with ImageProcessor
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(192, 192, ResizeOp.ResizeMethod.BILINEAR))
                .add(Rot90Op(-image.imageInfo.rotationDegrees / 90))
                .add(NormalizeOp(0f, 1f)) // Normalizing to [0, 1]
                .build()

            val processedImage = imageProcessor.process(tensorImage)
            Log.d(TAG, "Image processed")

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 192, 192, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(processedImage.buffer)
            Log.d(TAG, "Input feature loaded")

            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1, 17, 3), DataType.FLOAT32)

            var inferenceTime = SystemClock.uptimeMillis()
            interpreter?.run(inputFeature0.buffer, outputBuffer.buffer.rewind())
            inferenceTime = SystemClock.uptimeMillis() - inferenceTime
            Log.d(TAG, "Inference time: $inferenceTime ms")

            val keypoints = extractKeypoints(outputBuffer)
            if (keypoints.isEmpty()) {
                Log.d(TAG, "No valid keypoints detected")
            } else {
                Log.d(TAG, "Keypoints extracted: $keypoints")
                detectorListener?.onResults(keypoints, inferenceTime)
            }
        } catch (e: IllegalArgumentException) {
            val errorMessage = e.message ?: "Unknown error"
            Log.e(TAG, "Error during pose detection: $errorMessage")
            detectorListener?.onError(errorMessage)
            onError(errorMessage)
        } finally {
            image.close()
            Log.d(TAG, "ImageProxy closed")
        }
    }

    private fun toBitmap(image: ImageProxy): Bitmap {
        Log.d(TAG, "Converting ImageProxy to Bitmap")
        val nv21 = yuv420888ToNv21(image)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        Log.d(TAG, "Bitmap created from ImageProxy")
        return bitmap
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        Log.d(TAG, "Converting YUV_420_888 to NV21")
        val nv21: ByteArray
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        Log.d(TAG, "NV21 byte array created from YUV_420_888")
        return nv21
    }

    private fun isBlackImage(bitmap: Bitmap): Boolean {
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (bitmap.getPixel(x, y) != Color.BLACK) {
                    return false
                }
            }
        }
        return true
    }

    private fun extractKeypoints(outputBuffer: TensorBuffer?): List<Keypoint> {
        val keypoints = mutableListOf<Keypoint>()
        if (outputBuffer == null) return keypoints

        val scores = outputBuffer.floatArray

        // Assume that each keypoint has 3 values: x, y, and score
        for (i in scores.indices step 3) {
            val x = scores[i]
            val y = scores[i + 1]
            val score = scores[i + 2]
            keypoints.add(Keypoint(x, y, score))
        }
        return keypoints
    }

    fun close() {
        interpreter?.close()
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
