package com.example.ubuntu.tunableapplication.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.ubuntu.tunableapplication.R
import com.example.ubuntu.tunableapplication.util.utility.Util

class SplashActivity : AppCompatActivity() {

    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 3000 //3 seconds

    private val LoginRunnable: Runnable = Runnable {
        if (!isFinishing) {

            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private val MainRunnable: Runnable = Runnable {
        if (!isFinishing) {

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mDelayHandler = Handler()
        if (!Util.getRemembered(this@SplashActivity)){
            mDelayHandler!!.postDelayed(LoginRunnable, SPLASH_DELAY)
        }else{
            mDelayHandler!!.postDelayed(MainRunnable, SPLASH_DELAY)
        }
    }

    public override fun onDestroy() {

        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(LoginRunnable)
        }

        super.onDestroy()
    }
}
