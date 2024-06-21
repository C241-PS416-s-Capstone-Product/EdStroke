package com.capstone.edstroke.view.risk_exercise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.capstone.edstroke.R
import com.capstone.edstroke.databinding.ActivityRehabExerciseBinding

class RehabExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRehabExerciseBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRehabExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        initFragment()
    }

    private fun initFragment() {
        val fragmentManager = supportFragmentManager
        val exerciseFragment = ExerciseFragment()
        val fragment = fragmentManager.findFragmentByTag(ExerciseFragment::class.java.simpleName)
        if (fragment !is ExerciseFragment) {
            Log.d("MyFlexibleFragment", "Fragment Name :" + ExerciseFragment::class.java.simpleName)
            fragmentManager
                .beginTransaction()
                .add(R.id.frame_container, exerciseFragment, ExerciseFragment::class.java.simpleName)
                .commit()
        }
    }

    fun loadExercisePrepareFragment(bundle: Bundle) {
        val fragmentManager = supportFragmentManager
        val exercisePrepareFragment = ExercisePrepareFragment().apply {
            arguments = bundle
        }
        val fragment = fragmentManager.findFragmentByTag(ExercisePrepareFragment::class.java.simpleName)
        if (fragment !is ExercisePrepareFragment) {
            Log.d("MyFlexibleFragment", "Fragment Name :" + ExercisePrepareFragment::class.java.simpleName)
            fragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, exercisePrepareFragment, ExercisePrepareFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    fun loadExerciseStartFragment(bundle: Bundle) {
        val fragmentManager = supportFragmentManager
        val exerciseStartFragment = ExerciseStartFragment().apply {
            arguments = bundle
        }
        val fragment = fragmentManager.findFragmentByTag(ExerciseStartFragment::class.java.simpleName)
        if (fragment !is ExerciseStartFragment) {
            Log.d("MyFlexibleFragment", "Fragment Name :" + ExerciseStartFragment::class.java.simpleName)
            fragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, exerciseStartFragment, ExerciseStartFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    fun loadExerciseEndFragment(bundle: Bundle) {
        val fragmentManager = supportFragmentManager
        val exerciseEndFragment = ExerciseEndFragment().apply {
            arguments = bundle
        }
        val fragment = fragmentManager.findFragmentByTag(ExerciseEndFragment::class.java.simpleName)
        if (fragment !is ExerciseEndFragment) {
            Log.d("MyFlexibleFragment", "Fragment Name :" + ExerciseEndFragment::class.java.simpleName)
            fragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, exerciseEndFragment, ExerciseEndFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
