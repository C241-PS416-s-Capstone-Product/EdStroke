package com.capstone.edstroke.data.retrofit.networking

import com.capstone.edstroke.data.response.ModelResultDetail
import com.capstone.edstroke.data.response.ModelResultNearby
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("/place/nearbysearch/json")
    fun getDataResult(@Query("key") key: String,
                      @Query("keyword") keyword: String,
                      @Query("location") location: String,
                      @Query("rankby") rankby: String): Call<ModelResultNearby>

    @GET("/place/details/json")
    fun getDetailResult(@Query("key") key: String,
                        @Query("placeid") placeid: String): Call<ModelResultDetail>

}