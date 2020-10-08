package com.example.ubuntu.tunableapplication.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.ubuntu.tunableapplication.R
import com.example.ubuntu.tunableapplication.util.models.BaseModel
import com.example.ubuntu.tunableapplication.util.models.BearerToken
import com.example.ubuntu.tunableapplication.util.network.ApiProvider
import com.example.ubuntu.tunableapplication.util.network.ApiResult
import com.example.ubuntu.tunableapplication.util.utility.Util
import com.google.gson.JsonObject


class LoginActivity : AppCompatActivity() {
    private var valid: Boolean = true
    private val TAG = "Login Activity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        intOnClickListeners()
    }

    private fun intOnClickListeners() {
        btn_login.setOnClickListener { _: View -> attemptLogin() }
        txt_register.setOnClickListener { _: View -> startRegister() }
    }

    private fun startRegister() {
        val registerActivityIntent = Intent(this, RegisterActivity::class.java)
        startActivity(registerActivityIntent)
    }

    private fun attemptLogin() {
        val email = edt_email.text.toString()
        val password = edt_password.text.toString()
        if(loginValidator(email, password)) {
            btn_login.visibility = View.GONE
            loginRequest(email, password)
        }
        else{
            val myToast = Toast.makeText(this, "Email or Password input is wrong", Toast.LENGTH_SHORT)
            myToast.show()
            clearInput()
        }

    }

    private fun clearInput() {
        edt_password.setText("")
        edt_email.requestFocus()
    }

    private fun loginRequest(email: String, password: String) {
        ApiProvider().loginApi(object : ApiResult {
            override fun onError(e: Exception) {
                Log.e(TAG, e.message)
            }

            override fun onModel(baseModel: BaseModel) {
                if (baseModel is BearerToken) {
                    val auth = baseModel.auth
                    val token = baseModel.token
                    btn_login.visibility = View.VISIBLE
                    Util.setRemembered(this@LoginActivity, cbx_remember_me.isChecked )
                    Util.setToken(this@LoginActivity, token)
                    statMainActivity()
                }
            }

            override fun onJson(jsonObject: JsonObject) {
                btn_login.visibility = View.VISIBLE
                Toast.makeText(this@LoginActivity, "Wrong credentials, Please Try again", Toast.LENGTH_LONG).show()
                clearInput()
            }

            override fun onAPIFail() {
                btn_login.visibility = View.VISIBLE
                Toast.makeText(this@LoginActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            }

        }, email, password)
    }

    private fun statMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    private fun loginValidator(email: String, password: String) : Boolean{
        this.setValidator(true)

        this.isEmailValid(email)
        this.isPasswordValid(password)

        return this.valid
    }

    private fun isEmailValid(email: String): LoginActivity {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.setValidator(false)
        }
        return this
    }

    private fun isPasswordValid(password: String): LoginActivity {
        if (TextUtils.isEmpty(password) || password.length <= 5) {
            this.setValidator(false)
        }
        return this
    }

    private fun setValidator(valid: Boolean) {
        this.valid = valid
    }
}
