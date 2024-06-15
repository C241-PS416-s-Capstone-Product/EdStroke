package com.capstone.edstroke.view.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.io.File
import java.io.IOException

class PoseLandmarkerHelper(
    var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE,
    var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE,
    var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE,
    var currentModel: Int = MODEL_MOVENET_SINGLEPOSE_LIGHTNING,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    // this listener is only used when running in RunningMode.LIVE_STREAM
    val poseLandmarkerHelperListener: LandmarkerListener? = null
) {

    // For this example this needs to be a var so it can be reset on changes.
    // If the Pose Landmarker will not change, a lazy val would be preferable.
    private var poseLandmarker: PoseLandmarker? = null

    init {
        downloadModel()
    }

    fun clearPoseLandmarker() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    // Return running status of PoseLandmarkerHelper
    fun isClose(): Boolean {
        return poseLandmarker == null
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
                        setupObjectDetector(modelFile)
                    } else {
                        throw IOException("Model file is null")
                    }
                } catch (e: IOException) {
                    val errorMsg = e.message.toString()
                    poseLandmarkerHelperListener?.onError(errorMsg)
                    Log.e(TAG, errorMsg, e)
                }
            }
            .addOnFailureListener { e: Exception? ->
                val errorMsg = "Model download failed"
                poseLandmarkerHelperListener?.onError(errorMsg)
                Log.e(TAG, errorMsg, e)
            }
    }

    private fun setupObjectDetector(modelFile: File) {
        // Set general pose landmarker options
        val baseOptionBuilder = BaseOptions.builder()

        // Use the specified hardware for running the model. Default to CPU
        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionBuilder.setDelegate(Delegate.CPU)
            }
            DELEGATE_GPU -> {
                baseOptionBuilder.setDelegate(Delegate.GPU)
            }
        }

        baseOptionBuilder.setModelAssetPath(modelFile.absolutePath)

        // Check if runningMode is consistent with poseLandmarkerHelperListener
        when (runningMode) {
            RunningMode.LIVE_STREAM -> {
                if (poseLandmarkerHelperListener == null) {
                    throw IllegalStateException(
                        "poseLandmarkerHelperListener must be set when runningMode is LIVE_STREAM."
                    )
                }
            }
            else -> {
                // no-op
            }
        }

        try {
            val baseOptions = baseOptionBuilder.build()
            // Create an option builder with base options and specific
            // options only use for Pose Landmarker.
            val optionsBuilder =
                PoseLandmarker.PoseLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                    .setMinTrackingConfidence(minPoseTrackingConfidence)
                    .setMinPosePresenceConfidence(minPosePresenceConfidence)
                    .setRunningMode(runningMode)

            // The ResultListener and ErrorListener only use for LIVE_STREAM mode.
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            poseLandmarker =
                PoseLandmarker.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            poseLandmarkerHelperListener?.onError(
                "Pose Landmarker failed to initialize. See error logs for " +
                        "details"
            )
            Log.e(
                TAG, "MediaPipe failed to load the task with error: " + e
                    .message
            )
        } catch (e: RuntimeException) {
            // This occurs if the model being used does not support GPU
            poseLandmarkerHelperListener?.onError(
                "Pose Landmarker failed to initialize. See error logs for " +
                        "details", GPU_ERROR
            )
            Log.e(
                TAG,
                "Image classifier failed to load model with error: " + e.message
            )
        }
    }

    // Convert the ImageProxy to MP Image and feed it to PoselandmakerHelper.
    fun detectLiveStream(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean
    ) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException(
                "Attempting to call detectLiveStream" +
                        " while not using RunningMode.LIVE_STREAM"
            )
        }
        val frameTime = SystemClock.uptimeMillis()

        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer =
            Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )

        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // flip image if user use front camera
            if (isFrontCamera) {
                postScale(
                    -1f,
                    1f,
                    imageProxy.width.toFloat(),
                    imageProxy.height.toFloat()
                )
            }
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        detectAsync(mpImage, frameTime)
    }

    // Run pose landmark using MediaPipe Pose Landmarker API
    @VisibleForTesting
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        poseLandmarker?.detectAsync(mpImage, frameTime)
        // As we're using running mode LIVE_STREAM, the landmark result will
        // be returned in returnLivestreamResult function
    }

    // Accepts the URI for a video file loaded from the user's gallery and attempts to run
    // pose landmarker inference on the video. This process will evaluate every
    // frame in the video and attach the results to a bundle that will be
    // returned.
    fun detectVideoFile(
        videoUri: Uri,
        inferenceIntervalMs: Long
    ): ResultBundle? {
        if (runningMode != RunningMode.VIDEO) {
            throw IllegalArgumentException(
                "Attempting to call detectVideoFile" +
                        " while not using RunningMode.VIDEO"
            )
        }

        // Inference time is the difference between the system time at the start and finish of the
        // process
        val startTime = SystemClock.uptimeMillis()

        var didErrorOccurred = false

        // Load frames from the video and run the pose landmarker.
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val videoLengthMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()

        // Note: We need to read width/height from frame instead of getting the width/height
        // of the video directly because MediaRetriever returns frames that are smaller than the
        // actual video size when the video is too large. This causes the subsequent conversion
        // to MPImage to fail.
        val firstFrame = retriever.getFrameAtTime(0)
        val frameWidth = firstFrame?.width
        val frameHeight = firstFrame?.height
        val numberOfFrameToRead =
            videoLengthMs?.div(inferenceIntervalMs) ?: 0

        val resultList = mutableListOf<PoseLandmarkerResult>()
        val mpImageList = mutableListOf<MPImage>()
        val inferenceStartTimeList = mutableListOf<Long>()

        for (i in 0..numberOfFrameToRead) {
            val timestampMs = i * inferenceIntervalMs * 1000

            val bitmap =
                retriever.getFrameAtTime(
                    timestampMs,
                    MediaMetadataRetriever.OPTION_CLOSEST
                )?.copy(Bitmap.Config.ARGB_8888, false)

            if (bitmap == null) {
                Log.e(TAG, "Skipping frame at $timestampMs")
                continue
            }

            val mpImage = BitmapImageBuilder(bitmap).build()
            try {
                val inferenceStartTime = SystemClock.uptimeMillis()
                val result = poseLandmarker?.detect(mpImage)

                result?.let {
                    resultList.add(it)
                    mpImageList.add(mpImage)
                    inferenceStartTimeList.add(inferenceStartTime)
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "MP Pose Landmarker failed to process video frame$e")
                didErrorOccurred = true
                break
            }
        }
        retriever.release()

        val inferenceTimePerImageMs =
            if (resultList.isNotEmpty()) {
                (SystemClock.uptimeMillis() - startTime) / resultList.size.toLong()
            } else {
                0
            }

        if (frameWidth != null) {
            return if (didErrorOccurred) {
                null
            } else {
                ResultBundle(
                    results = resultList,
                    inputImages = mpImageList,
                    inferenceTimings = inferenceStartTimeList,
                    inferenceTime = inferenceTimePerImageMs,
                    videoFps = frameWidth * frameHeight!!
                )
            }
        }
        return null
    }

    // Return the result of the inference performed on a frame in the live stream
    private fun returnLivestreamResult(result: PoseLandmarkerResult, input: MPImage) {
        this.poseLandmarkerHelperListener?.onResults(result, input)
    }

    // Return errors thrown during detection, for example when processing
    // a frame in the live stream
    private fun returnLivestreamError(error: RuntimeException) {
        this.poseLandmarkerHelperListener?.onError(error.message ?: "An unknown error occurred")
    }

    companion object {
        const val TAG = "PoseLandmarkerHelper"
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5f
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5f
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5f

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1

        const val OTHER_ERROR = 0

        const val MODEL_MOVENET_SINGLEPOSE_LIGHTNING = 0
        const val MODEL_MOVENET_SINGLEPOSE_THUNDER = 1

        const val GPU_ERROR = "GPU is not supported on this device"
    }


    data class ResultBundle(
        val results: List<PoseLandmarkerResult>,
        val inputImages: List<MPImage>,
        val inferenceTimings: List<Long>,
        val videoFps: Int,
        val inferenceTime: Long,
    )

    interface LandmarkerListener {
        fun onError(error: String, errorCode: String = OTHER_ERROR.toString())
        fun onResults(resultBundle: PoseLandmarkerResult, inputImage: MPImage)
    }
}