package com.capstone.edstroke.view.risk_exercise

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.capstone.edstroke.databinding.FragmentExercisePrepareBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class ExercisePrepareFragment : Fragment() {

    private lateinit var binding: FragmentExercisePrepareBinding
    private var videoId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExercisePrepareBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedExercise = arguments?.getString("selectedExercise")

        when (selectedExercise) {
            "Shoulder Range of Motion" -> {
                videoId = "rcS-_UI8YPo"
                binding.titleTextView.text = "Shoulder Range of Motion"
                binding.countTextView.text = "Repetitions Needed: 10"
                binding.descriptionTextView.text =
                    "Gently raise arms overhead, then lower. \n" +
                            "Move arms out to sides, then back down. \n" +
                            "Perform arm circles forward and backward. \n" +
                            "Helps maintain flexibility and mobility.\n"
            }
            "Mini-Lunge" -> {
                videoId = "R3YEDs3Y7MI"
                binding.titleTextView.text = "Mini-Lunge"
                binding.countTextView.text = "Repetitions Needed: 8"
                binding.descriptionTextView.text =
                    "Stand with feet hip-width apart. \n" +
                            "Step one foot forward, bending both knees to lower your body. \n" +
                            "Keep front knee over ankle. \n" +
                            "Return to start and switch legs.\n"
            }
        }

        lifecycle.addObserver(binding.youtubePlayerView)
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.cueVideo(videoId, 0f)
            }
        })

        binding.startButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("selectedExercise", selectedExercise)
            }
            (activity as? RehabExerciseActivity)?.loadExerciseStartFragment(bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.youtubePlayerView.release()
    }
}
