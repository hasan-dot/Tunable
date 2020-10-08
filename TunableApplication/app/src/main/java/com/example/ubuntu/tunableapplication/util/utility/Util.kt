package com.example.ubuntu.tunableapplication.util.utility

import android.content.Context

object Util {

    private val TOKEN = Util::class.java.name + ".TOKEN"
    private val REMEMBER = Util::class.java.name + ".REMEMBER"

    fun setRemembered(context: Context, remember: Boolean) {
        val sharedPref = context.getSharedPreferences(REMEMBER, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(REMEMBER, remember)
        editor.apply()
    }

    fun getRemembered(context: Context): Boolean {
        val remember = context.getSharedPreferences(REMEMBER, Context.MODE_PRIVATE)
        return remember.getBoolean(REMEMBER, false)
    }



    fun setToken(context: Context, token: String?) {
        val sharedPref = context.getSharedPreferences(TOKEN, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(TOKEN, token)
        editor.apply()
    }

    fun getToken(context: Context): String {
        val language = context.getSharedPreferences(TOKEN, Context.MODE_PRIVATE)
        return language.getString(TOKEN, null)
    }

    fun clear(context: Context) {
        setRemembered(context, false)
        setToken(context, null)
    }
}