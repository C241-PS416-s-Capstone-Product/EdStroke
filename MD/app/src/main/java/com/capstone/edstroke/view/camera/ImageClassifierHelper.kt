package com.capstone.edstroke.view.camera//package com.capstone.edstroke.view.camera
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.os.SystemClock
//import android.util.Log
//import android.view.Surface
//import androidx.camera.core.ImageProxy
//import com.capstone.edstroke.R
//import com.google.firebase.ml.modeldownloader.CustomModel
//import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
//import com.google.firebase.ml.modeldownloader.DownloadType
//import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
//import com.google.mediapipe.framework.image.BitmapImageBuilder
//import org.tensorflow.lite.DataType
//import org.tensorflow.lite.support.common.ops.CastOp
//import org.tensorflow.lite.support.image.ImageProcessor
//import org.tensorflow.lite.support.image.TensorImage
//import org.tensorflow.lite.support.image.ops.ResizeOp
//import com.google.mediapipe.tasks.components.containers.Classifications
//import com.google.mediapipe.tasks.core.BaseOptions
//import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
//import com.google.mediapipe.tasks.vision.core.RunningMode
//import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
//import java.io.File
//import java.io.IOException
//
//class ImageClassifierHelper(
//    var threshold: Float = 0.1f,
//    var maxResults: Int = 3,
//    val modelName: String = "mobilenet_v1.tflite",
//    val runningMode: RunningMode = RunningMode.LIVE_STREAM,
//    val context: Context,
//    val classifierListener: ClassifierListener?
//) {
//    private var imageClassifier: ImageClassifier? = null
//    private var modelFile: File? = null
//
//    init {
//        downloadModel()
//    }
//
//    @Synchronized
//    private fun downloadModel() {
//        val conditions = CustomModelDownloadConditions.Builder()
//            .requireWifi()
//            .build()
//        FirebaseModelDownloader.getInstance()
//            .getModel("exercise-trial-1", DownloadType.LOCAL_MODEL, conditions)
//            .addOnSuccessListener { model: CustomModel ->
//                try {
//                    modelFile = model.file
//                    if (modelFile != null) {
//                        setupImageClassifier()
//                    } else {
//                        throw IOException("Model file is null")
//                    }
//                } catch (e: IOException) {
//                    val errorMsg = e.message.toString()
//                    classifierListener?.onError(errorMsg)
//                    Log.e(TAG, errorMsg, e)
//                }
//            }
//            .addOnFailureListener { e: Exception? ->
//                val errorMsg = context.getString(R.string.firebaseml_model_download_failed)
//                classifierListener?.onError(errorMsg)
//                Log.e(TAG, "Model download failed", e)
//            }
//    }
//
//    private fun setupImageClassifier() {
//        if (modelFile == null) return
//
//        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
//            .setScoreThreshold(threshold)
//            .setMaxResults(maxResults)
//            .setRunningMode(runningMode)
//
//        if (runningMode == RunningMode.LIVE_STREAM) {
//            optionsBuilder.setResultListener { result, image ->
//                val finishTimeMs = SystemClock.uptimeMillis()
//                val inferenceTime = finishTimeMs - result.timestampMs()
//                classifierListener?.onResults(
//                    result.classificationResult().classifications(),
//                    inferenceTime
//                )
//            }.setErrorListener { error ->
//                classifierListener?.onError(error.message.toString())
//            }
//        }
//
//        val baseOptionsBuilder = BaseOptions.builder()
//            .setModelAssetPath(modelFile?.absolutePath ?: modelName)
//        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())
//
//        try {
//            imageClassifier = ImageClassifier.createFromOptions(
//                context,
//                optionsBuilder.build()
//            )
//        } catch (e: IllegalStateException) {
//            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
//            Log.e(TAG, e.message.toString())
//        }
//    }
//
//    fun classifyImage(image: ImageProxy) {
//        if (imageClassifier == null) {
//            setupImageClassifier()
//        }
//
//        val mpImage = BitmapImageBuilder(toBitmap(image)).build()
//
//        val imageProcessingOptions = ImageProcessingOptions.builder()
//            .setRotationDegrees(image.imageInfo.rotationDegrees)
//            .build()
//
//        val inferenceTime = SystemClock.uptimeMillis()
//        imageClassifier?.classifyAsync(mpImage, imageProcessingOptions, inferenceTime)
//    }
//
//    private fun toBitmap(image: ImageProxy): Bitmap {
//        val bitmapBuffer = Bitmap.createBitmap(
//            image.width,
//            image.height,
//            Bitmap.Config.ARGB_8888
//        )
//        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
//        image.close()
//        return bitmapBuffer
//    }
//
//    interface ClassifierListener {
//        fun onError(error: String)
//        fun onResults(
//            results: List<Classifications>?,
//            inferenceTime: Long
//        )
//    }
//
//    companion object {
//        private const val TAG = "ImageClassifierHelper"
//    }
//}
