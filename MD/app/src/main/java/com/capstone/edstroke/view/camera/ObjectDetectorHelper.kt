//package com.capstone.edstroke.view.camera
//import android.graphics.Bitmap
//import android.os.Build
//import android.os.SystemClock
//import android.util.Log
//import androidx.camera.core.ImageProxy
//import com.capstone.edstroke.R
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.tflite.client.TfLiteInitializationOptions
//import com.google.android.gms.tflite.gpu.support.TfLiteGpu
//import com.google.firebase.ml.modeldownloader.CustomModel
//import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
//import com.google.firebase.ml.modeldownloader.DownloadType
//import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
//import org.tensorflow.lite.gpu.CompatibilityList
//import org.tensorflow.lite.support.image.ImageProcessor
//import org.tensorflow.lite.support.image.TensorImage
//import org.tensorflow.lite.support.image.ops.ResizeOp
//import org.tensorflow.lite.support.image.ops.Rot90Op
//import com.google.mediapipe.tasks.components.containers.Classifications
//import com.google.mediapipe.tasks.core.BaseOptions
//import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
//import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
//import java.io.File
//import java.io.IOException
//
//class ObjectDetectorHelper(
//    var threshold: Float = 0.5f,
//    var maxResults: Int = 5,
//    val context: Context,
//    val detectorListener: DetectorListener?,
//    private val onError: (String) -> Unit,
//) {
//    private var objectDetector: ObjectDetector? = null
//    private var initializationTask: Task<Void>? = null
//
//    init {
//        initializationTask = TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable ->
//            val optionsBuilder = TfLiteInitializationOptions.builder()
//            if (gpuAvailable) {
//                optionsBuilder.setEnableGpuDelegateSupport(true)
//            }
//            TfLiteVision.initialize(context, optionsBuilder.build())
//        }.addOnSuccessListener {
//            Log.d(TAG, "TfLiteVision initialized successfully")
//            downloadModel()
//        }.addOnFailureListener {
//            val errorMsg = context.getString(R.string.tflitevision_is_not_initialized_yet)
//            detectorListener?.onError(errorMsg)
//            onError(errorMsg)
//            Log.e(TAG, "TfLiteVision initialization failed", it)
//        }
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
//                    val modelFile = model.file
//                    if (modelFile != null) {
//                        setupObjectDetector(modelFile)
//                    } else {
//                        throw IOException("Model file is null")
//                    }
//                } catch (e: IOException) {
//                    val errorMsg = e.message.toString()
//                    detectorListener?.onError(errorMsg)
//                    onError(errorMsg)
//                }
//            }
//            .addOnFailureListener { e: Exception? ->
//                val errorMsg = context.getString(R.string.firebaseml_model_download_failed)
//                detectorListener?.onError(errorMsg)
//                onError(errorMsg)
//                Log.e(TAG, "Model download failed", e)
//            }
//    }
//
//    private fun setupObjectDetector(modelFile: File) {
//        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
//            .setScoreThreshold(threshold)
//            .setMaxResults(maxResults)
//        val baseOptionsBuilder = BaseOptions.builder()
//        if (CompatibilityList().isDelegateSupportedOnThisDevice) {
//            baseOptionsBuilder.useGpu()
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//            baseOptionsBuilder.useNnapi()
//        } else {
//            // Menggunakan CPU
//            baseOptionsBuilder.setNumThreads(4)
//        }
//        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())
//
//        try {
//            objectDetector = ObjectDetector.createFromFileAndOptions(
//                modelFile,
//                optionsBuilder.build()
//            )
//            Log.d(TAG, "Object detector set up successfully")
//        } catch (e: IllegalStateException) {
//            val errorMsg = context.getString(R.string.image_classifier_failed)
//            detectorListener?.onError(errorMsg)
//            onError(errorMsg)
//            Log.e(TAG, "Object detector setup failed", e)
//        }
//    }
//
//    fun detectObject(image: ImageProxy) {
//        initializationTask?.addOnSuccessListener {
//            if (!TfLiteVision.isInitialized()) {
//                val errorMessage = context.getString(R.string.tflitevision_is_not_initialized_yet)
//                Log.e(TAG, errorMessage)
//                detectorListener?.onError(errorMessage)
//                onError(errorMessage)
//                return@addOnSuccessListener
//            }
//
//            if (objectDetector == null) {
//                val errorMsg = "Object detector is not set up yet"
//                detectorListener?.onError(errorMsg)
//                onError(errorMsg)
//                return@addOnSuccessListener
//            }
//
//            val imageProcessor = ImageProcessor.Builder()
//                .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
//                .add(Rot90Op(-image.imageInfo.rotationDegrees / 90))
//                .build()
//
//            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(toBitmap(image)))
//
//            var inferenceTime = SystemClock.uptimeMillis()
//            val results = objectDetector?.detect(tensorImage)
//            inferenceTime = SystemClock.uptimeMillis() - inferenceTime
//            detectorListener?.onResults(
//                results,
//                inferenceTime,
//                tensorImage.height,
//                tensorImage.width
//            )
//        }?.addOnFailureListener {
//            val errorMessage = context.getString(R.string.tflitevision_is_not_initialized_yet)
//            Log.e(TAG, errorMessage)
//            detectorListener?.onError(errorMessage)
//            onError(errorMessage)
//        }
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
//    interface DetectorListener {
//        fun onError(error: String)
//        fun onResults(
//            results: MutableList<Detection>?,
//            inferenceTime: Long,
//            imageHeight: Int,
//            imageWidth: Int
//        )
//    }
//
//    companion object {
//        private const val TAG = "ObjectDetectorHelper"
//    }
//}
