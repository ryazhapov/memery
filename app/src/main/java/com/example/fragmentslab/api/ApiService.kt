package com.example.fragmentslab.api

import com.example.fragmentslab.model.Post
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/random?json=true")
    fun getPost(): Call<Post>
}