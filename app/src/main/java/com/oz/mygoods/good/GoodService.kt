package com.oz.mygoods.good

import retrofit2.Call
import retrofit2.http.*

interface GoodService {
    @GET("api/goods")
    fun all(): Call<List<Good>>

    @GET("api/goods/needed")
    fun needed(@Header("Authorization") token:String): Call<List<Good>>

    @GET("api/goods/{id}")
    operator fun get(@Path("id") id: Long): Call<Good?>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("api/goods/new")
    fun create(@Body book: Good): Call<Good?>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @PUT("api/goods/{id}")
    fun update(@Path("id") id: Long, @Body book: Good): Call<Good?>
}
