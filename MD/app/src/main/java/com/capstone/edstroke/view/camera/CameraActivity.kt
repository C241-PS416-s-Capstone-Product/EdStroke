package com.capstone.edstroke.view.camera

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.capstone.edstroke.databinding.ActivityCameraBinding
import com.capstone.edstroke.view.riskexercise.RiskExerciseHelper
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var poseEstimationHelper: PoseEstimationHelper
    private lateinit var riskExerciseHelper: RiskExerciseHelper
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var progressDialog: ProgressDialog

    private var repetitionCount = 0
    private var currentState = "Other"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        poseEstimationHelper = PoseEstimationHelper(
            context = this,
            detectorListener = object : PoseEstimationHelper.DetectorListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResults(keypoints: List<PoseEstimationHelper.Keypoint>?, inferenceTime: Long) {
                    runOnUiThread {
                        keypoints?.let {
                            // Update keypoints on the overlay view
                            var filteredKeypoint = keypointsFilter(it)
                            binding.keypointsOverlay.updateKeypoints(filteredKeypoint)
                            // Analyze keypoints using risk exercise helper
                            val analysisResults = riskExerciseHelper.analyzeKeypoints(filteredKeypoint)
                            if (analysisResults != null) {
                                Log.d("RiskAnalysis", "Results: ${analysisResults.joinToString()}")
                                // Convert FloatArray to List<Float>
                                val analysisResultsList = analysisResults.toList()
                                updateRepetitionCount(analysisResultsList)
                            }
                        }
                    }
                }
            },
            onError = {
                runOnUiThread {
                    Toast.makeText(this@CameraActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        )

        riskExerciseHelper = RiskExerciseHelper(this) { errorMsg ->
            runOnUiThread {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
        scope.launch {
            riskExerciseHelper.initialize()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        scope.launch {
            initializeAndStartCamera()
        }
    }

    private suspend fun initializeAndStartCamera() {
        withContext(Dispatchers.Main) {
            showLoadingDialog("Initializing Model...")
        }
        withContext(Dispatchers.IO) {
            poseEstimationHelper.initialize()
        }
        withContext(Dispatchers.Main) {
            dismissLoadingDialog()
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
        val imageAnalyzer = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
        imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
            poseEstimationHelper.detectPose(image)
        }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (exc: Exception) {
            Toast.makeText(
                this@CameraActivity,
                "Failed to start the camera.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e(TAG, "startCamera: ${exc.message}")
        }
    }

    private fun updateRepetitionCount(analysisResults: List<Float>) {
        val currentExerciseState = if (analysisResults[0] > 0.5) "Exercise" else "Other"
        if (currentState == "Other" && currentExerciseState == "Exercise") {
            repetitionCount++
            Log.d("RepetitionCount", "Repetition count: $repetitionCount")
            // Update UI with repetition count if needed
        }
        currentState = currentExerciseState
    }

    private fun keypointsFilter(keypoints: List<PoseEstimationHelper.Keypoint>): List<PoseEstimationHelper.Keypoint> {
        val defaultKeypoint = PoseEstimationHelper.Keypoint(0.0f, 0.0f, 0.0f)
        val filteredKeypoints = keypoints.map {
            if (it.score > 0.4) it else defaultKeypoint
        }
        return filteredKeypoints
    }

    private fun showLoadingDialog(message: String) {
        progressDialog = ProgressDialog(this).apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }

    private fun dismissLoadingDialog() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        poseEstimationHelper.close()
    }

    companion object {
        private const val TAG = "CameraActivity"
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        const val CAMERAX_RESULT = 200
    }
}
