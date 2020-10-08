package com.example.ubuntu.tunableapplication.util.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiServiceNetwork private constructor()//hide the public constructor
    : Interceptor {
    private var token: String? = null
    private val logger: okhttp3.logging.HttpLoggingInterceptor
        get() {
            val logging = okhttp3.logging.HttpLoggingInterceptor()
            logging.level = okhttp3.logging.HttpLoggingInterceptor.Level.HEADERS
            logging.level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
            logging.level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            return logging
        }

    fun getNetworkService(url: String): WebServiceInterface {
        return createRetrofitInstance(url).create(WebServiceInterface::class.java)
    }

    private fun createRetrofitInstance(url: String): Retrofit {
        val okHttpClient = OkHttpClient().newBuilder()
        okHttpClient.addInterceptor(this)
        okHttpClient.addInterceptor(logger)
        retrofit.baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.connectTimeout(20,
                        TimeUnit.SECONDS).writeTimeout(20,
                        TimeUnit.SECONDS).readTimeout(20,
                        TimeUnit.SECONDS).build())
        return retrofit.build()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder: Request.Builder
        builder = request.newBuilder()
        val request1 = builder.build()
        if (!token.isNullOrEmpty()){
            builder.addHeader("x-access-token", ""+this.token)
        }
        return chain.proceed(request1)
    }

    companion object {
        private val retrofit = Retrofit.Builder()

        fun getInstance(): ApiServiceNetwork {
            return ApiServiceNetwork()
        }
    }
}
