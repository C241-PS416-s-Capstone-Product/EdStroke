package com.capstone.edstroke.view.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.edstroke.data.repository.UserRepository
import com.capstone.edstroke.data.pref.UserModel
import com.capstone.edstroke.data.request.LoginRequest
import com.capstone.edstroke.data.request.RegisterRequest
import com.capstone.edstroke.data.response.ErrorResponse
import com.capstone.edstroke.data.response.LoginResponse
import com.capstone.edstroke.data.retrofit.ApiConfig
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResponse>()
    val loginResult: LiveData<LoginResponse> = _loginResult

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(username, password)
                //get success message
                val result = userRepository.login(request)
                _loginResult.value = result
                val message = result.msg
                Log.d("LoginSuccess", message.toString())

                // Set token and update ApiService
                val token = result.token ?: return@launch
                ApiConfig.setToken(token)
                userRepository.updateApiService(ApiConfig.getUserApiService())

            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                Log.d("LoginError", errorMessage.toString())
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            userRepository.saveSession(user)
        }
    }
}