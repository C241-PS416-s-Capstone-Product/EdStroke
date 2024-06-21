package com.capstone.edstroke.view.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.edstroke.data.pref.UserModel
import com.capstone.edstroke.data.repository.UserRepository
import com.capstone.edstroke.data.request.RegisterRequest
import com.capstone.edstroke.data.response.ErrorResponse
import com.capstone.edstroke.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileViewModel(
    private val repository: UserRepository
) : ViewModel() {
    private val _profileResult = MutableLiveData<RegisterResponse>()
    val profileResult: LiveData<RegisterResponse> = _profileResult


    fun updateProfile(username: String, email: String) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(password = null, email = email, username = username)

                //get success message
                val result = repository.updateProfile(request)
                _profileResult.value = result
                val message = result.msg
                Log.d("Update Profile", message.toString())
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message

                Log.d("Update Profile", errorMessage.toString())
            }


        }


    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }
}