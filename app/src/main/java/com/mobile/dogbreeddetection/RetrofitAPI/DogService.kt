package com.mobile.dogbreeddetection.RetrofitAPI

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DogService {
    @GET("/")
    suspend fun getStatus(): Response<Status>

    @GET("dog/{uri}")
    suspend fun getDogBreed(@Path("uri")uri:String): Response<Dog>
}