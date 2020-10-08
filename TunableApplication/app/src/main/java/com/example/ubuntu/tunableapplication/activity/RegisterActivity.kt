package com.example.ubuntu.tunableapplication.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.ubuntu.tunableapplication.R
import com.example.ubuntu.tunableapplication.util.models.BaseModel
import com.example.ubuntu.tunableapplication.util.models.BearerToken
import com.example.ubuntu.tunableapplication.util.network.ApiProvider
import com.example.ubuntu.tunableapplication.util.network.ApiResult
import com.example.ubuntu.tunableapplication.util.utility.Util
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private var valid: Boolean = true
    private val TAG = "Register Activity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        intOnClickListeners()

    }

    private fun intOnClickListeners() {
        btn_register.setOnClickListener { _: View -> attemptRegister() }    }

    private fun attemptRegister() {
        val email = edt_email.text.toString()
        val first_name = edt_first_name.text.toString()
        val last_name = edt_last_name.text.toString()
        val password = edt_password.text.toString()
        val c_password = edt_cpassword.text.toString()
        if(registerValidator(email, first_name, last_name, password, c_password)) {
            btn_register.visibility = View.GONE
            registerRequest(email, first_name, last_name, password, c_password)
        }
        else{
            val myToast = Toast.makeText(this, "Input is not valid", Toast.LENGTH_SHORT)
            myToast.show()
            clearInput()
        }    
    }

    private fun registerRequest(email: String, first_name: String, last_name: String, password: String, c_password: String) {
            ApiProvider().registerApi(object : ApiResult {
                override fun onError(e: Exception) {
                    Log.e(TAG, e.message)
                }

                override fun onModel(baseModel: BaseModel) {
                    if (baseModel is BearerToken) {
                        val token = baseModel.token
                        btn_register.visibility = View.VISIBLE
                        Util.setToken(this@RegisterActivity, token)
                        statMainActivity()
                    }
                }



                override fun onJson(jsonObject: JsonObject) {
                   Toast.makeText(this@RegisterActivity, jsonObject["error"].asString, Toast.LENGTH_SHORT).show()
                    btn_register.visibility = View.VISIBLE
                    clearInput()
                }

                override fun onAPIFail() {
                    btn_register.visibility = View.VISIBLE
                    Toast.makeText(this@RegisterActivity, "Connection Error", Toast.LENGTH_SHORT).show()
                }

            },email, first_name, last_name, password, c_password)
        }


    private fun statMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun registerValidator(email: String, first_name: String, last_name: String, password: String, c_password: String): Boolean {
        this.setValidator(true)

        this.isEmailValid(email)
        this.isPasswordValid(password, c_password)
        this.isNameValid(first_name, last_name)


        return this.valid
    }

    private fun isNameValid(first_name: String, last_name: String) {
        if (TextUtils.isEmpty(first_name) || first_name.length <= 2
            || TextUtils.isEmpty(last_name) || last_name.length <= 2) {
            this.setValidator(false)
        }
    }

    private fun isEmailValid(email: String): RegisterActivity {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.setValidator(false)
        }
        return this
    }

    private fun isPasswordValid(password: String, cpassword: String): RegisterActivity {
        if (TextUtils.isEmpty(password) || password.length <= 5 || password != cpassword) {
            this.setValidator(false)
        }
        return this
    }

    private fun setValidator(valid: Boolean) {
        this.valid = valid
    }

    private fun clearInput() {
        edt_password.setText("")
        edt_cpassword.setText("")
        edt_email.requestFocus()
    }
}
