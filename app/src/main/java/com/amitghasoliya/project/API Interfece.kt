package com.amitghasoliya.project

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/b/6HBE")
    fun fetchData(): Call<ApiResponse>
}