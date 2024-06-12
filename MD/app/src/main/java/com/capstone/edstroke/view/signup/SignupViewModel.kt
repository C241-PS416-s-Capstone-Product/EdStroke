package com.capstone.edstroke.view.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.edstroke.data.repository.UserRepository
import com.capstone.edstroke.data.request.RegisterRequest
import com.capstone.edstroke.data.response.ErrorResponse
import com.capstone.edstroke.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignupViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<RegisterResponse>()
    val registerResult: LiveData<RegisterResponse> = _registerResult

    fun register(username: String, password: String, email: String,) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(username, password, email)

                //get success message
                val result = repository.register(request)
                _registerResult.value = result
                val message = result.msg
                Log.d("RegisterSuccess", message.toString())
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                Log.d("RegisterError", errorMessage.toString())
            }
        }
    }
}