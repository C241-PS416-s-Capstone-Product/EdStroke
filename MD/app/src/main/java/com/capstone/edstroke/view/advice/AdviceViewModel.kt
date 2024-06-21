package com.capstone.edstroke.view.advice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.edstroke.data.repository.RiskRepository
import com.capstone.edstroke.data.request.LoginRequest
import com.capstone.edstroke.data.request.RiskScreeningRequest
import com.capstone.edstroke.data.response.AdviceResponse
import com.capstone.edstroke.data.response.ErrorResponse
import com.capstone.edstroke.data.response.PredictResponse
import com.capstone.edstroke.data.retrofit.ApiConfig
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AdviceViewModel(
    private val riskRepository: RiskRepository
) : ViewModel() {

    private val _adviceResult = MutableLiveData<List<AdviceResponse>>()
    val adviceResult: LiveData<List<AdviceResponse>> = _adviceResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<String>()
    val isError: LiveData<String> = _isError

    fun adviceResult() {
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                //get success message
                val result = riskRepository.getRiskAdvice()
                _adviceResult.postValue(listOf(result))
                _isLoading.postValue(false)
                Log.d("AdviceGetSuccess", result.toString())
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                _isLoading.postValue(false)
                _isError.postValue(errorMessage.toString())
                Log.d("AdviceGetError", errorMessage.toString())
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _isError.postValue(e.message ?: "An unexpected error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }
}