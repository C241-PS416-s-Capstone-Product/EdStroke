package com.capstone.edstroke.data.pref

data class UserModel(
    val username: String,
    val userId: String,
    val email: String,
    val token: String,
    val isLogin: Boolean = false
)