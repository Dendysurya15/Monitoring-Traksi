package com.cbi.monitoring_traksi.ui.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.LoginModel
import com.cbi.monitoring_traksi.data.repository.LoginRepository
import com.cbi.monitoring_traksi.ui.viewModel.LoginViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_login.idPassword
import kotlinx.android.synthetic.main.activity_login.idUsername
import kotlinx.android.synthetic.main.activity_login.loadingLogin
import kotlinx.android.synthetic.main.activity_login.mbFinger
import kotlinx.android.synthetic.main.activity_login.mbLogin
import kotlinx.android.synthetic.main.activity_login.mcbRemember

class LoginActivity : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    private var username = ""
    private var pass = ""

    private lateinit var viewModel: LoginViewModel
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        prefManager = PrefManager(this)

        if (AppUtils.checkBiometricSupport(this) && prefManager!!.name.toString().isNotEmpty()) {
            mbFinger.visibility = View.VISIBLE
            biometricPrompt()
        }
        loadingLogin.visibility = View.VISIBLE
        val repository = LoginRepository(this, window, loadingLogin)
        viewModel = ViewModelProvider(this, LoginViewModel.Factory(repository))[LoginViewModel::class.java]
        viewModel.loginModel.observe(this) { result ->
            handleLoginResult(result)
        }

        setTampilan()
        clickButtonLogin()

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleLoginResult(result: LoginModel) {
        when (result.statusCode) {
            1 -> {
                val intent: Intent

                intent = Intent(this, MainActivity::class.java)

                startActivity(intent)
                finishAffinity()
            }
            else -> {
                AppUtils.closeLoadingLayout(loadingLogin)
                AlertDialogUtility.alertDialog(
                    this,
                    "Caution",
                    result.message ?: "Login failed",
                    "warning.json"
                )
            }
        }
    }
    private fun clickButtonLogin(){

        AppUtils.handleTextChanges(idUsername) {
            username = it
        }
        AppUtils.handleTextChanges(idPassword) {
            pass = it
        }

        mbLogin.setOnClickListener {


            if (username.isEmpty() || pass.isEmpty()) {
                showAlertDialog("Error", "Username or password cannot be empty.")
            }else{
//                displayToasty(this,email + ' ' + pass)
                loadingLogin.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingLogin)
                viewModel.loginUser(username, pass)
            }
        }

        mbFinger.setOnClickListener {
            biometricPrompt()
        }

        mcbRemember.setOnCheckedChangeListener { _, isChecked ->
            prefManager!!.remember = isChecked
        }

    }

    private fun biometricPrompt() {
        AppUtils.showBiometricPrompt(this, prefManager!!.name.toString()) {
            runOnUiThread {
                loadingLogin.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingLogin)
            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
    private fun setTampilan() {
        if (prefManager!!.remember) {
            mcbRemember.isChecked = true
            if (prefManager!!.username.toString().isNotEmpty() && prefManager!!.password.toString().isNotEmpty()) {
                idUsername.setText(prefManager!!.username, TextView.BufferType.SPANNABLE)
                idPassword.setText(prefManager!!.password, TextView.BufferType.SPANNABLE)
                username = prefManager!!.username.toString()
                pass = prefManager!!.password.toString()
            }
        } else {
            mcbRemember.isChecked = false
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun displayToasty(context: Context, message: String){

        Toasty.success(context, message, Toast.LENGTH_LONG).show()
    }

}



