package com.capstone.edstroke.view.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.capstone.edstroke.R
import com.capstone.edstroke.data.pref.UserModel
import com.capstone.edstroke.databinding.ActivityEditProfileBinding
import com.capstone.edstroke.view.ViewModelFactory
import com.capstone.edstroke.view.dashboard.DashboardActivity
import kotlinx.coroutines.flow.first

class EditProfileActivity : AppCompatActivity() {

    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelFactory(this)
    }

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var user: UserModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { userResult ->
            user = userResult
            binding.edtEmail.setText(userResult.email)
            binding.edtUsername.setText(userResult.username)
        }

        binding.btnEdit.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val email = binding.edtEmail.text.toString()
            viewModel.updateProfile(username, email)
            viewModel.profileResult.observe(this) { result ->
                result?.let {
                    viewModel.saveSession(
                        UserModel(
                            username = username,
                            email = email,
                            token = user.token,
                            isLogin = true,
                            userId = user.userId
                        )
                    )
                    Toast.makeText(this@EditProfileActivity, result.msg, Toast.LENGTH_SHORT)
                        .show()

                    val intent = Intent(this@EditProfileActivity, DashboardActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()

                }
            }


        }

    }
}