package com.capstone.edstroke.view.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.edstroke.BuildConfig
import com.capstone.edstroke.data.model.details.ModelDetail
import com.capstone.edstroke.data.model.nearby.ModelResults
import com.capstone.edstroke.data.response.ModelResultDetail
import com.capstone.edstroke.data.response.ModelResultNearby
import com.capstone.edstroke.data.retrofit.networking.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MapViewModel : ViewModel() {

    private val modelResultsMutableLiveData = MutableLiveData<ArrayList<ModelResults>>()
    private val modelDetailMutableLiveData = MutableLiveData<ModelDetail>()

    companion object {
        var strApiKey = BuildConfig.MAPS_API_KEY
    }

    fun setMarkerLocation(strLocation: String) {
        val apiService = ApiClient.getClient()
        val call = apiService.getDataResult(strApiKey, "Rumah Sakit", strLocation, "distance")
        call.enqueue(object : Callback<ModelResultNearby> {
            override fun onResponse(call: Call<ModelResultNearby>, response: Response<ModelResultNearby>) {
                if (!response.isSuccessful) {
                    Log.e("response", response.toString())
                } else if (response.body() != null) {
                    val items = ArrayList(response.body()!!.modelResults)
                    modelResultsMutableLiveData.postValue(items)
                }
            }

            override fun onFailure(call: Call<ModelResultNearby>, t: Throwable) {
                Log.e("failure", t.toString())
            }
        })
    }

    fun setDetailLocation(strPlaceID: String) {
        val apiService = ApiClient.getClient()
        val call = apiService.getDetailResult(strApiKey, strPlaceID)
        call.enqueue(object : Callback<ModelResultDetail> {
            override fun onResponse(call: Call<ModelResultDetail>, response: Response<ModelResultDetail>) {
                if (!response.isSuccessful) {
                    Log.e("response", response.toString())
                } else if (response.body() != null) {
                    modelDetailMutableLiveData.postValue(response.body()!!.modelDetail)
                }
            }

            override fun onFailure(call: Call<ModelResultDetail>, t: Throwable) {
                Log.e("failure", t.toString())
            }
        })
    }

    fun getMarkerLocation(): LiveData<ArrayList<ModelResults>> = modelResultsMutableLiveData

    fun getDetailLocation(): LiveData<ModelDetail> = modelDetailMutableLiveData

}
