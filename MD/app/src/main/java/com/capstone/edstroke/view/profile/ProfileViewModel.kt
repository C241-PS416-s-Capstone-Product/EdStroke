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
import com.capstone.edstroke.data.response.HistoryResponse
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
    private val _historyResult = MutableLiveData<HistoryResponse>()
    val historyResult: LiveData<HistoryResponse> = _historyResult

    private val _isError = MutableLiveData<String>()
    val isError: LiveData<String> = _isError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun updateProfile(username: String, email: String) {
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                val request = RegisterRequest(password = null, email = email, username = username)
                //get success message
                val result = repository.updateProfile(request)
                _profileResult.value = result
                _isLoading.postValue(false)
                val message = result.msg
                Log.d("Update Profile", message.toString())
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                _isLoading.postValue(false)
                _isError.postValue(errorMessage.toString())
                Log.d("Update Profile", errorMessage.toString())
            }catch (e: Exception) {
                _isLoading.postValue(false)
                _isError.postValue(e.message ?: "An unexpected error occurred")
            } finally {
                _isLoading.value = false
            }


        }


    }

    fun getHistoryRisk() {
        viewModelScope.launch {
            try {

                val result = repository.history()
                _historyResult.value = result
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                _isLoading.postValue(false)
                _isError.postValue(errorMessage.toString())
                Log.d("Update Profile", errorMessage.toString())
            }catch (e: Exception) {
                _isLoading.postValue(false)
                _isError.postValue(e.message ?: "An unexpected error occurred")
            } finally {
                _isLoading.value = false
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