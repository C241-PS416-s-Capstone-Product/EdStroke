package com.capstone.edstroke.data.response

import com.capstone.edstroke.data.model.details.ModelDetail
import com.google.gson.annotations.SerializedName


class ModelResultDetail {
    @SerializedName("result")
    lateinit var modelDetail: ModelDetail
}