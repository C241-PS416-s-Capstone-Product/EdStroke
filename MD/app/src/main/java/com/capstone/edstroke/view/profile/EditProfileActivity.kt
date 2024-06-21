package com.capstone.edstroke.view.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.capstone.edstroke.R
import com.capstone.edstroke.data.pref.UserModel
import com.capstone.edstroke.databinding.ActivityEditProfileBinding
import com.capstone.edstroke.view.ViewModelFactory
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
                Log.d("update result", "${result}")
                result?.let {
                    if (it.msg.isNullOrEmpty()) {

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
                    } else {
                        Toast.makeText(this@EditProfileActivity, result.msg, Toast.LENGTH_SHORT)
                            .show()

                    }

                }
            }


        }

    }
}