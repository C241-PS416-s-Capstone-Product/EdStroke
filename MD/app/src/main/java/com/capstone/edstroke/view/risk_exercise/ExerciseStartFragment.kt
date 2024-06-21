package com.capstone.edstroke.view.risk_exercise

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.capstone.edstroke.databinding.FragmentExerciseStartBinding
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import com.capstone.edstroke.R

class ExerciseStartFragment : Fragment() {

    private lateinit var binding: FragmentExerciseStartBinding
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var poseEstimationHelper: PoseEstimationHelper
    private lateinit var riskExerciseHelper: RiskExerciseHelper
    private lateinit var progressDialog: ProgressDialog

    private var repetitionNeeded = 0
    private var repetitionCount = 0
    private var currentState = "Other"

    private lateinit var tvRepetitions: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExerciseStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvRepetitions = view.findViewById(R.id.tvRepetitions)
        val selectedExercise = arguments?.getString("selectedExercise")

        binding.cancelButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        poseEstimationHelper = PoseEstimationHelper(
            context = requireContext(),
            detectorListener = object : PoseEstimationHelper.DetectorListener {
                override fun onError(error: String) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }

                override fun onResults(keypoints: List<PoseEstimationHelper.Keypoint>?, inferenceTime: Long) {
                    keypoints?.let {
                        // Update keypoints on the overlay view
                        val filteredKeypoint = keypointsFilter(it)
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
            },
            onError = {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        )

        when (selectedExercise) {
            "Shoulder Range of Motion" -> {
                repetitionNeeded = 10
                riskExerciseHelper = RiskExerciseHelper("Shoulder Range of Motion", requireContext()) { errorMsg ->
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
            "Mini-Lunge" -> {
                repetitionNeeded = 8
                riskExerciseHelper = RiskExerciseHelper("Mini-Lunge", requireContext()) { errorMsg ->
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        scope.launch {
            showLoadingDialog("Initializing Model...")
            riskExerciseHelper.initialize()
            poseEstimationHelper.initialize()
            dismissLoadingDialog()
            initializeAndStartCamera()
        }
    }

    private suspend fun initializeAndStartCamera() {
        withContext(Dispatchers.Main) {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
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
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        } catch (exc: Exception) {
            Toast.makeText(requireContext(), "Failed to start the camera.", Toast.LENGTH_SHORT).show()
            Log.e("CameraFragment", "startCamera: ${exc.message}")
        }
    }

    private fun updateRepetitionCount(analysisResults: List<Float>) {
        if (repetitionCount < repetitionNeeded) {
            val currentExerciseState = if (analysisResults[0] > 0.5) "Exercise" else "Other"
            if (currentState == "Other" && currentExerciseState == "Exercise") {
                repetitionCount++
                Log.d("RepetitionCount", "Repetition count: $repetitionCount")
                updateRepetitionText()
            }
            currentState = currentExerciseState
        } else {
            navigateToEndFragment()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateRepetitionText() {
        tvRepetitions.text = "Repetitions: $repetitionCount"
    }

    private fun keypointsFilter(keypoints: List<PoseEstimationHelper.Keypoint>): List<PoseEstimationHelper.Keypoint> {
        val defaultKeypoint = PoseEstimationHelper.Keypoint(0.0f, 0.0f, 0.0f)
        return keypoints.map {
            if (it.score > 0.4) it else defaultKeypoint
        }
    }

    private fun showLoadingDialog(message: String) {
        progressDialog = ProgressDialog(requireContext()).apply {
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

    private fun closeCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun navigateToEndFragment() {
        val activity = requireActivity() as RehabExerciseActivity
        activity.loadExerciseEndFragment(Bundle())
    }

    override fun onDestroyView() {
        scope.launch {
            closeCamera()
            poseEstimationHelper.close()
            riskExerciseHelper.close()
        }
        job.cancel()
        super.onDestroyView()
    }
}
