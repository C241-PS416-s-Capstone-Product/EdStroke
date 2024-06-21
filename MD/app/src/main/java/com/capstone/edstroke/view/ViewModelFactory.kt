package com.capstone.edstroke.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.capstone.edstroke.data.repository.UserRepository
import com.capstone.edstroke.di.Injection
import com.capstone.edstroke.view.advice.AdviceViewModel
import com.capstone.edstroke.view.login.LoginViewModel
import com.capstone.edstroke.view.main.MainViewModel
import com.capstone.edstroke.view.profile.ProfileViewModel
import com.capstone.edstroke.view.risk_screening.RiskViewModel
import com.capstone.edstroke.view.signup.SignupViewModel

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when{

                modelClass.isAssignableFrom(MainViewModel::class.java) ->{
                    MainViewModel(Injection.provideUserRepository(context)) as T
                }
                modelClass.isAssignableFrom(SignupViewModel::class.java) ->{
                    SignupViewModel(Injection.provideUserRepository(context)) as T
                }
                modelClass.isAssignableFrom(LoginViewModel::class.java) ->{
                    LoginViewModel(Injection.provideUserRepository(context)) as T
                }
                modelClass.isAssignableFrom(ProfileViewModel::class.java) ->{
                    ProfileViewModel(Injection.provideUserRepository(context)) as T
                }
                modelClass.isAssignableFrom(RiskViewModel::class.java) ->{
                    RiskViewModel(Injection.provideRiskRepository(context)) as T
                }
                modelClass.isAssignableFrom(AdviceViewModel::class.java) ->{
                    AdviceViewModel(Injection.provideRiskRepository(context)) as T
                }

                else -> throw IllegalArgumentException("unknown viewmodel class")
            }

    }
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return when {
//            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
//                MainViewModel(userRepository) as T
//            }
//
//            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
//                LoginViewModel(userRepository) as T
//            }
//
//            modelClass.isAssignableFrom(SignupViewModel::class.java) -> {
//                SignupViewModel(userRepository) as T
//            }
//            // Add StoryViewModel here if needed
//            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
//        }
//    }
//
//    companion object {
//        @Volatile
//        private var INSTANCE: ViewModelFactory? = null
//        fun getInstance(context: Context): ViewModelFactory {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE ?: ViewModelFactory(
//                    Injection.provideUserRepository(context),
//                ).also { INSTANCE = it }
//            }
//        }
//    }
}
