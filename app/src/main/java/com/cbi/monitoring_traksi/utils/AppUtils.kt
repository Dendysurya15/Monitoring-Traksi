package com.cbi.monitoring_traksi.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LifecycleOwner
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.loading_view.view.blurLoadView
import kotlinx.android.synthetic.main.loading_view.view.lottieLoadAnimate
import kotlinx.android.synthetic.main.loading_view.view.overlayLoadView
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

object AppUtils {

    const val mainServer = "https://srs-ssms.com/"
    const val serverMp = "https://mobilepro.srs-ssms.com/"
    const val serverPs = "https://palmsentry.srs-ssms.com/"
    const val serverListJenisAndKodeUnit = "https://srs-ssms.com/aplikasi_traksi/getListJenisAndKodeUnit.php"
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

    fun synchronizeDBJnsDanKodeUnit(
        context: Context,
        prefManager: PrefManager,
        unitViewModel: UnitViewModel,
        loaderView: View,
        update: String? = ""
    ) {

        if (update!!.isNotEmpty()) {
            AlertDialogUtility.alertDialog(
                context,
                context.getString(R.string.caution),
                context.getString(R.string.desc_info1),
                "warning.json"
            )
            Log.d("testing","sudah pernah update")
        }


        val strReq: StringRequest =
            @SuppressLint("SetTextI18n")
            object : StringRequest(
                Method.POST,
                serverListJenisAndKodeUnit,
                Response.Listener { response ->
                    try {
                        val jObj = JSONObject(response)

                        val jenisUnitsArray = jObj.getJSONArray("jenis_unit")
                        val kodeUnitsArray = jObj.getJSONArray("kode_unit")


                        unitViewModel.deleteDataJenisUnit()
                        unitViewModel.deleteDataKodeUnit()
                        for (i in 0 until jenisUnitsArray.length()) {
                            val jenisUnitObject = jenisUnitsArray.getJSONObject(i)
                            val idJenisUnit = jenisUnitObject.getInt("id")
                            val namaJenisUnit = jenisUnitObject.getString("nama_unit")

                            unitViewModel.insertDataJenisUnit(
                                id = idJenisUnit,
                                nama_unit = namaJenisUnit,
                            )
                        }

                        for (i in 0 until kodeUnitsArray.length()) {
                            val kodeUnitObject = kodeUnitsArray.getJSONObject(i)
                            val idKodeUnit = kodeUnitObject.getInt("id")
                            val nama = kodeUnitObject.getString("nama")
                            val unit_kerja = kodeUnitObject.getString("unit_kerja")
                            val type_unit = kodeUnitObject.getString("type_unit")
                            val id_jenis_unit = kodeUnitObject.getInt("id_jenis_unit")



                            unitViewModel.insertDataKodeUnit(
                                id = idKodeUnit,
                                nama = nama,
                                unit_kerja = unit_kerja,
                                type_unit = type_unit,
                                id_jenis_unit = id_jenis_unit,
                            )
                        }

                        val successArrayInsert = mutableListOf<Boolean>()

                        unitViewModel.insertResultJnsUnit.observe(
                            context as LifecycleOwner
                        ) { isSuccess ->

                            if (isSuccess) {
                                Log.d("testing", "Sukses insert data jenis Unit !")
                                closeLoadingLayout(loaderView)
                                successArrayInsert.add(true)
//
                            } else {
                                successArrayInsert.add(false)
                                Log.d("testing", "Terjadi kesalahan, mengunduh data jenis unit")

                            }
                        }

                        unitViewModel.insertResultKodeUnit.observe(
                            context as LifecycleOwner
                        ) { isSuccess ->
                            Log.d("testing", isSuccess.toString())
                            if (isSuccess) {
                                successArrayInsert.add(true)
                                Log.d("testing", "Sukses insert data kode Unit !")
                            } else {
                                Log.d("testing", "Terjadi kesalahan, mengunduh data kode unit")
                                successArrayInsert.add(false)
                            }
                        }

                        val allSuccess = successArrayInsert.all { it }
                        if (allSuccess) {
                            prefManager.isFirstTimeLaunch = false
                            AlertDialogUtility.alertDialog(
                                context,
                                "Sukses",
                                "nais",
                                "success.json"
                            )
                        } else {
                            closeLoadingLayout(loaderView)
                            Log.d("testing", "At least one operation insert is failed.")
                        }
//
                    } catch (e: JSONException) {
                        Log.d(
                            "testing", "${
                                context.getString(
                                    R.string.error_volley1
                                )
                            }: $e"
                        )
                        e.printStackTrace()

                        AlertDialogUtility.withSingleAction(
                            context,
                            context.getString(R.string.try_again),
                            context.getString(R.string.failed),
                            context.getString(R.string.desc_failed_download),
                            "error.json"
                        ) {
                            synchronizeDBJnsDanKodeUnit(
                                context,
                                prefManager,
                                unitViewModel,
                                loaderView,
                                update
                            )
                        }
                    }
                },
                Response.ErrorListener { error ->
                    Log.d(
                        "testing",
                        "${context.getString(R.string.error_volley2)}: $error"
                    )

                    AlertDialogUtility.withSingleAction(
                        context,
                        context.getString(R.string.try_again),
                        context.getString(R.string.failed),
                        context.getString(R.string.desc_failed_download),
                        "error.json"
                    ) {
                        synchronizeDBJnsDanKodeUnit(
                            context,
                            prefManager,
                            unitViewModel,
                            loaderView,
                            update
                        )
                    }
                }) {

            }

        strReq.retryPolicy = DefaultRetryPolicy(
            90000,  // Socket timeout in milliseconds (30 seconds)
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(context).add(strReq)
    }


    fun dialogErrorDownload(
        context: Context,
        window: Window,
        message: String,
        loaderView: View,
        function: () -> Unit
    ) {
        AlertDialogUtility.withSingleAction(
            context, "Ulang", "Peringatan", message, "warning.json"
        ) {
            loaderView.visibility = View.VISIBLE
            showLoadingLayout(context, window, loaderView)

            function()
        }
    }


//    private fun synchronizeUnit(
//        context: Context,
//        prefManager: PrefManager,
//        UnitViewModel: UnitViewModel,
//        loaderView: View,
//        update: String? = ""
//    ) {
//        val strReq: StringRequest =
//            @SuppressLint("SimpleDateFormat")
//            object : StringRequest(
//                Method.POST,
//                apiServer,
//                Response.Listener { response ->
//                    try {
//                        val jObj = JSONObject(response)
//                        val success = jObj.getInt(TAG_SUCCESSCODE)
//
//                        if (success == 1) {
//                            Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
//
//                            if (update!!.isNotEmpty()) {
//                                AlertDialogUtility.alertDialog(
//                                    context,
//                                    context.getString(R.string.success),
//                                    context.getString(R.string.desc_info2),
//                                    "success.json"
//                                )
//                            }
//                        } else if (success == 2) {
//                            Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
//
//                            dataUnitVm.deleteDataUnit()
//                            val dataListUnit = jObj.getJSONArray("data")
//                            for (i in 0 until dataListUnit.length()) {
//                                val jsonObject = dataListUnit.getJSONObject(i)
//                                dataUnitVm.insertDataUnit(
//                                    id = jsonObject.getInt("id_satuan"),
//                                    nama = jsonObject.getString("nm_satuan")
//                                )
//                            }
//
//                            dataUnitVm.insertionResult.observe(
//                                context as LifecycleOwner
//                            ) { isSuccess ->
//                                if (isSuccess) {
//                                    Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
//                                    prefManager.md5Unit = jObj.getString(TAG_MD5)
//                                } else {
//                                    Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
//                                    dataUnitVm.deleteDataUnit()
//                                }
//                            }
//
//                            if (update!!.isNotEmpty()) {
//                                AlertDialogUtility.alertDialog(
//                                    context,
//                                    context.getString(R.string.success),
//                                    context.getString(R.string.desc_info3),
//                                    "success.json"
//                                )
//                            }
//                        } else {
//                            Log.d(LOG_DATA_UNIT, jObj.getString(TAG_MESSAGE))
//                        }
//
//                        val dateNow =
//                            SimpleDateFormat("dd MMM yyyy HH:mm").format(Calendar.getInstance().time)
//                                .toString()
//                        prefManager.lastUpdate = dateNow
//
//                        if (prefManager.isFirstTimeLaunch) {
//                            prefManager.isFirstTimeLaunch = false
//                        }
//
//                        closeLoadingLayout(loaderView)
//                    } catch (e: JSONException) {
//                        Log.d(
//                            LOG_DATA_UNIT, "${
//                                context.getString(
//                                    R.string.error_volley1
//                                )
//                            }: $e"
//                        )
//                        e.printStackTrace()
//
//                        AlertDialogUtility.withSingleAction(
//                            context,
//                            context.getString(R.string.try_again),
//                            context.getString(R.string.failed),
//                            context.getString(R.string.desc_failed_download),
//                            "error.json"
//                        ) {
//                            synchronizeUnit(context, prefManager, dataUnitVm, loaderView, update)
//                        }
//                    }
//                },
//                Response.ErrorListener { error ->
//                    Log.d(
//                        LOG_DATA_UNIT,
//                        "${context.getString(R.string.error_volley2)}: $error"
//                    )
//
//                    AlertDialogUtility.withSingleAction(
//                        context,
//                        context.getString(R.string.try_again),
//                        context.getString(R.string.failed),
//                        context.getString(R.string.desc_failed_download),
//                        "error.json"
//                    ) {
//                        synchronizeUnit(context, prefManager, dataUnitVm, loaderView, update)
//                    }
//                }) {
//
//                override fun getParams(): Map<String, String> {
//                    val params: MutableMap<String, String> = HashMap()
//                    params[TAG_USERNAME] = prefManager.username.toString()
//                    params[TAG_PASSWORD] = prefManager.password.toString()
//                    params[TAG_REQUESTDATA] = "unit"
//                    params[TAG_MD5APP] = prefManager.md5Unit.toString()
//                    return params
//                }
//            }
//
//        strReq.retryPolicy = DefaultRetryPolicy(
//            90000,  // Socket timeout in milliseconds (30 seconds)
//            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        )
//
//        Volley.newRequestQueue(context).add(strReq)
//    }
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