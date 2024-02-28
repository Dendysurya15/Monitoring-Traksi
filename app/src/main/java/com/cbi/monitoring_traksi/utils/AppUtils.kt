package com.cbi.monitoring_traksi.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.cbi.monitoring_traksi.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.loading_view.view.blurLoadView
import kotlinx.android.synthetic.main.loading_view.view.lottieLoadAnimate
import kotlinx.android.synthetic.main.loading_view.view.overlayLoadView
import java.util.Locale
import java.util.concurrent.Executors

object AppUtils {

    const val mainServer = "https://srs-ssms.com/"
    const val serverMp = "https://mobilepro.srs-ssms.com/"
    const val serverPs = "https://palmsentry.srs-ssms.com/"
    const val TAG_SUCCESS = "success"
    const val TAG_MESSAGE = "message"

    const val TAG_USERID = "user_id"
    const val TAG_NAMA = "nama_lengkap"
    const val TAG_DEPARTEMEN = "departemen"
    const val TAG_LOKASIKERJA = "lokasi_kerja"
    const val TAG_JABATAN = "jabatan"
    const val TAG_NOHP = "no_hp"
    const val TAG_EMAIL = "email"
    const val TAG_PASSWORD = "password"
    const val TAG_LOKASI = "lokasi_kerja"
    const val TAG_AKSES = "akses_level"
    const val TAG_AFDELING = "afdeling"


    fun closeLoadingLayout(loaderView: View) {
        Handler(Looper.getMainLooper()).postDelayed({
            loaderView.visibility = View.GONE
        }, 500)
    }
    private fun blurViewLayout(context: Context, window: Window, blurView: BlurView) {
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground: Drawable? = decorView.background
        blurView.setupWith(rootView, RenderScriptBlur(context))
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(1f)
    }

    fun setupInputLayout(
        context: Context,
        layout: TextInputLayout,
//        hintResId: Int,
//        iconResId: Int,
        inputType: Int = InputType.TYPE_TEXT_VARIATION_PERSON_NAME or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
        imeType: Int = EditorInfo.IME_ACTION_NEXT
    ) {
//        layout.hint = context.getString(hintResId)
        layout.editText?.inputType = inputType
        layout.editText?.imeOptions = imeType
    }

    fun showLoadingLayout(context: Context, window: Window, loadingView: View) {
        loadingView.lottieLoadAnimate.visibility = View.VISIBLE
        loadingView.overlayLoadView.visibility = View.VISIBLE
        loadingView.overlayLoadView.setOnTouchListener { _, _ -> true }
        blurViewLayout(context, window, loadingView.blurLoadView)
    }

    fun checkBiometricSupport(context: Context): Boolean {
        when (BiometricManager.from(context).canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                return BiometricManager.from(context)
                    .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                return false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                return false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                return false
            }

            else -> {
                return false
            }
        }
    }
    fun showBiometricPrompt(context: Context, nameUser: String, successCallback: () -> Unit) {
        val executor = Executors.newSingleThreadExecutor()

        val biometricPrompt = BiometricPrompt(
            context as AppCompatActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    successCallback.invoke()
                }
            })

        val textWelcome = context.getString(R.string.welcome_back)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(
                "${
                    textWelcome.substring(0, 1)
                        .toUpperCase(Locale.getDefault()) + textWelcome.substring(1).toLowerCase(
                        Locale.getDefault()
                    )
                } $nameUser."
            )
            .setSubtitle(context.getString(R.string.subtitle_prompt))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
    fun checkConnectionDevice(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val con = try {
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state
        } catch (e: Exception) {
            NetworkInfo.State.DISCONNECTED
        }

        return con === NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state === NetworkInfo.State.CONNECTED
    }
    fun handleTextChanges(
        editText: TextInputEditText,
        onDataChange: (String) -> Unit
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable?) {
                onDataChange.invoke(editable?.toString() ?: "")
            }
        })
    }
}