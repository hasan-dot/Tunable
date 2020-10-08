package com.example.ubuntu.tunableapplication.util.network

import com.example.ubuntu.tunableapplication.util.models.BearerToken
import com.example.ubuntu.tunableapplication.util.models.User
import com.google.gson.JsonObject
import retrofit2.http.*
import rx.Observable

interface WebServiceInterface {
    @FormUrlEncoded
    @POST("login")
    fun login(@Field("email") email: String, @Field("password") password: String): Observable<BearerToken>

    @GET("me")
    fun getDetails(@Header("x-access-token") authHeader: String): Observable<User>

    @FormUrlEncoded
    @POST("register")
    fun register(@Field("first_name") first_name: String,
                 @Field("last_name") last_name: String,
                 @Field("email") email: String,
                 @Field("password") password: String,
                 @Field("c_password") c_password: String
                 ): Observable<BearerToken>

    @Headers("Accept: application/json")
    @GET("getmusic")
    fun getMusic(@Header("x-access-token") authHeader: String): Observable<JsonObject>

}
