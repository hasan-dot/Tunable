package com.example.ubuntu.tunableapplication.util.network

import android.util.Log
import com.example.ubuntu.tunableapplication.util.models.BearerToken
import com.example.ubuntu.tunableapplication.util.models.User
import com.google.gson.JsonObject
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class ApiProvider {
    private val TAG = "ApiProvider"

    private val mApiServiceNetwork = ApiServiceNetwork.getInstance()

    fun loginApi(apiResult: ApiResult, email: String, password: String) {
        try {
            mApiServiceNetwork.getNetworkService(Constants.API_ENDPOINT)
                    .login(email, password)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<BearerToken>() {
                        override fun onCompleted() {
                            //
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, "onError" + Log.getStackTraceString(e))
                            apiResult.onAPIFail()
                        }

                        override fun onNext(user: BearerToken) {
                            Log.i(TAG, "Operation performed successfully")
                            if(!user.auth){
                                apiResult.onJson(JsonObject())
                            } else {
                                apiResult.onModel(user)
                            }

                        }
                    })
        } catch (e: Exception) {
            Log.e(TAG, "Exception" + Log.getStackTraceString(e))
            apiResult.onError(e)
        }

    }

    fun getUser(apiResult: ApiResult, token: String) {
        try {
            mApiServiceNetwork.getNetworkService(Constants.API_ENDPOINT)
                    .getDetails("$token")
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<User>() {
                        override fun onCompleted() {
                            //Do nothing for now
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, "onError" + Log.getStackTraceString(e))
                            apiResult.onAPIFail()
                        }

                        override fun onNext(user: User) {
                            Log.i(TAG, user.email)
                            apiResult.onModel(user)
                        }
                    })
        } catch (e: Exception) {
            Log.e(TAG, "Exception" + Log.getStackTraceString(e))
            apiResult.onError(e)
        }

    }

    fun registerApi(apiResult: ApiResult, email: String,
                    first_name: String, last_name: String,
                    password: String, c_password: String) {
        try {
            mApiServiceNetwork.getNetworkService(Constants.API_ENDPOINT)
                    .register(first_name, last_name, email, password, c_password)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<BearerToken>() {
                        override fun onCompleted() {
                            //
                        }

                        override fun onError(e: Throwable) {
                            Log.e(TAG, "onError" + Log.getStackTraceString(e))
                            apiResult.onAPIFail()
                        }

                        override fun onNext(user: BearerToken) {
                            Log.i(TAG, "Operation performed successfully")
                            if(!user.auth){
                                val message = JsonObject()
                                message.addProperty("error", user.token)
                                apiResult.onJson(message)
                            } else {
                                apiResult.onModel(user)
                            }

                        }
                    })
        } catch (e: Exception) {
            Log.e(TAG, "Exception" + Log.getStackTraceString(e))
            apiResult.onError(e)
        }

    }
    fun getRecordings(apiResult: ApiResult, token: String) {
        try {
            mApiServiceNetwork.getNetworkService(Constants.API_ENDPOINT)
                    .getMusic("$token")
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<JsonObject>() {
                        override fun onCompleted() {
                            //
                        }

                        override fun onError(e: Throwable) {
//                            Log.e(TAG, "onError" + Log.getStackTraceString(e))
//                            apiResult.onAPIFail()
                        }

                        override fun onNext(recordings: JsonObject) {
                            apiResult.onJson(recordings)
                        }
                    })
        } catch (e: Exception) {
            Log.e(TAG, "Exception" + Log.getStackTraceString(e))
            apiResult.onError(e)
        }

    }

}