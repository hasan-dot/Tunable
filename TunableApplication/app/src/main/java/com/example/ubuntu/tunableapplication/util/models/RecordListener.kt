package com.example.ubuntu.tunableapplication.util.models

import android.view.View

interface RecordListener {
    fun onClickPlay(view: View, position: Int)
    fun onClickStop(view: View, position: Int)
}