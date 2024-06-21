package com.capstone.edstroke.view.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.capstone.edstroke.databinding.ActivityDashboardBinding
import com.capstone.edstroke.view.ViewModelFactory
import com.capstone.edstroke.view.main.MainViewModel
import com.capstone.edstroke.view.maps.MapsActivity
import com.capstone.edstroke.view.profile.ProfileActivity
import com.capstone.edstroke.view.risk_exercise.RehabExerciseActivity
import com.capstone.edstroke.view.risk_screening.RiskScreeningActivity
import com.capstone.edstroke.view.welcome.WelcomeActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory(this)
    }
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

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        observeSession()

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(DashboardActivity.REQUIRED_PERMISSION)
        }

        binding.llViewProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.llRiskScreening.setOnClickListener {
            val intent = Intent(this, RiskScreeningActivity::class.java)
            startActivity(intent)
        }
        binding.startExerciseButton.setOnClickListener {
            val intent = Intent(this, RehabExerciseActivity::class.java)
            startActivity(intent)
        }

    }


    private fun setupView() {
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
    private fun observeSession() {
        mainViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                binding.tvUsername.text = user.username
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}