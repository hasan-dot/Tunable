package com.example.ubuntu.tunableapplication.util.network

import com.example.ubuntu.tunableapplication.util.models.BaseModel
import com.google.gson.JsonObject

interface ApiResult {

    fun onError(e: Exception)

    fun onModel(baseModel: BaseModel)

    fun onJson(jsonObject: JsonObject)

    fun onAPIFail()
}
