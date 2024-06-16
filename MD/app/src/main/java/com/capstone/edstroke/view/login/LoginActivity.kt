package com.capstone.edstroke.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.capstone.edstroke.R
import com.capstone.edstroke.data.pref.UserModel
import com.capstone.edstroke.databinding.ActivityLoginBinding
import com.capstone.edstroke.view.ViewModelFactory
import com.capstone.edstroke.view.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private val loginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
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

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
//            loginViewModel.saveSession(
//                            UserModel(
//                                "testing",
//                                "10",
//                                "testing@example.com",
//                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTAsImlhdCI6MTcxODQ3OTYzNiwiZXhwIjoxNzE4NDgzMjM2fQ.Gshw9eB-Xgh_Q8xRGYaeD3VslRbciI1iccMrZFP36c4",
//                                true
//                            )
//                        )
//            val intent = Intent(this@LoginActivity, MainActivity::class.java)
//            intent.flags =
//                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            finish()
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            loginViewModel.login(username, password)
            loginViewModel.loginResult.observe(this) { result ->
                result?.let {
                    if (it.msg.isNullOrEmpty()) {
                        val token = it.token
                        val userId = it.userResult?.id.toString()
                        val username = it.userResult?.username
                        val email = it.userResult?.email
                        loginViewModel.saveSession(
                            UserModel(
                                username!!,
                                userId!!,
                                email!!,
                                token!!,
                                true
                            )
                        )
                        if (!isFinishing) {
                            AlertDialog.Builder(this).apply {
                                setTitle(getString(R.string.yeah))
                                setMessage(getString(R.string.login_alert))
                                setPositiveButton(getString(R.string.next)) { _, _ ->
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                create()
                                show()
                            }
                        }
                    } else {
                        if (!isFinishing) {
                            AlertDialog.Builder(this).apply {
                                setTitle(getString(R.string.oops))
                                setMessage(getString(R.string.incorrect_credentials))
                                setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                create()
                                show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val desc =
            ObjectAnimator.ofFloat(binding.descTextView, View.ALPHA, 1f).setDuration(100)
        val username =
            ObjectAnimator.ofFloat(binding.usernameTextView, View.ALPHA, 1f).setDuration(100)
        val usernameedit =
            ObjectAnimator.ofFloat(binding.usernameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val password =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordedit =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(title, desc, username, usernameedit, password, passwordedit, login)
            startDelay = 100
            start()
        }
    }

}