package com.capstone.edstroke.data.response

import com.capstone.edstroke.data.model.nearby.ModelResults
import com.google.gson.annotations.SerializedName

class ModelResultNearby {
    @SerializedName("results")
    lateinit var modelResults: List<ModelResults>
}