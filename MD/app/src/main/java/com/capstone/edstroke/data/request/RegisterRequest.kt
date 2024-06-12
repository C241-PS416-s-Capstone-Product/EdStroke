package com.capstone.edstroke.data.request

import com.google.gson.annotations.SerializedName

data class RegisterRequest(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
