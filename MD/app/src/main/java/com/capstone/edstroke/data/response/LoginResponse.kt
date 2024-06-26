package com.capstone.edstroke.data.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("user")
	val userResult: User? = null,

	@field:SerializedName("token")
	val token: String? = null,

	@field:SerializedName("msg")
	val msg: String? = null
)

data class User(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
