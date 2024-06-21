package com.capstone.edstroke.view.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.edstroke.R
import com.capstone.edstroke.databinding.ActivityMainBinding
import com.capstone.edstroke.databinding.ActivityProfileBinding
import com.capstone.edstroke.view.ViewModelFactory
import com.capstone.edstroke.view.login.LoginActivity
import com.capstone.edstroke.view.main.MainViewModel
import com.capstone.edstroke.view.risk_screening.RiskScreeningActivity
import com.capstone.edstroke.view.welcome.WelcomeActivity

class ProfileActivity : AppCompatActivity() {
    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            binding.txtEmail.text = user.email
            binding.txtUsername.text = user.username
            binding.txtProfileUsername.text = user.username
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.exit_confirm))
                setMessage(getString(R.string.exit))
                setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    dialog.dismiss()
                    viewModel.logout()

                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()

                }
                create()
                show()
            }
        }


    }
}