package com.cbi.monitoring_traksi.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.monitoring_traksi.data.model.LoginModel
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import org.json.JSONException
import org.json.JSONObject

class LoginRepository(private val context: Context, private val window: Window, private val loadingLayout: View) {
    fun loginUser(email: String, password: String, callback: (LoginModel) -> Unit) {
        val prefManager = PrefManager(context)
        val pmu = prefManager.email
        val pmp = prefManager.password

        if (AppUtils.checkConnectionDevice(context)) {
            onlineAuth(email, password, callback)
        }
//        if (username == pmu && password == pmp) {
//            callback(LoginModel(success = true, statusCode = 1, message = "Login berhasil."))
//        } else if (AppUtils.checkConnectionDevice(context)) {
//            onlineAuth(username, password, callback)
//        } else {
//            callback(LoginModel(success = false, statusCode = 0, message = "asdkljfklajsdfkljk"))
//        }
    }

    private fun onlineAuth(email: String, password: String, callback: (LoginModel) -> Unit) {
        val prefManager = PrefManager(context)
        val url = AppUtils.serverMp + "config/apk-login.php"



        if (email == prefManager.email && password == prefManager.password) {
            callback(LoginModel(success = true, statusCode = 1, message = "Login berhasil."))

            Log.d("masuk sini", "masuk berhasil gan")
        } else {
            Log.d("masuk sini","Masuk else ")
            AppUtils.showLoadingLayout(context, window, loadingLayout)
            @Suppress("UNUSED_ANONYMOUS_PARAMETER") val strReq: StringRequest =
                @SuppressLint("SuspiciousIndentation")
                object : StringRequest(
                    Method.POST,
                    url,
                    Response.Listener { response ->
                        try {
                            val jObj = JSONObject(response)
                            val success = jObj.getInt(AppUtils.TAG_SUCCESS)
                            if (success == 1) {
                                prefManager.session = true
                                prefManager.userid = jObj.getInt(AppUtils.TAG_USERID).toString()
                                prefManager.name = jObj.getString(AppUtils.TAG_NAMA)
                                prefManager.departemen = jObj.getString(AppUtils.TAG_DEPARTEMEN)
                                prefManager.lokasi_kerja =
                                    jObj.getString(AppUtils.TAG_LOKASIKERJA)
                                prefManager.jabatan = jObj.getString(AppUtils.TAG_JABATAN)
                                prefManager.nohp = jObj.getString(AppUtils.TAG_NOHP)
                                prefManager.email = jObj.getString(AppUtils.TAG_EMAIL)
                                prefManager.lokasi = jObj.getString(AppUtils.TAG_LOKASI)
                                prefManager.akses = jObj.getString(AppUtils.TAG_AKSES)
                                prefManager.password = jObj.getString(AppUtils.TAG_PASSWORD)
                                prefManager.login = (prefManager.login + 1)
                                prefManager.afdeling = jObj.getString(AppUtils.TAG_AFDELING)
                                if (prefManager.reg!!.isEmpty() || prefManager.dataReg!!.isEmpty()) {
                                    Log.d("masuk sini","reg gak empty gan ")
                                    callback(LoginModel(success = true, statusCode = 2, message = "Login berhasil."))



                                } else {
                                    Log.d("masuk sini","reg empty gan ")
                                    callback(LoginModel(success = true, statusCode = 1, message = "Login berhasil."))
                                }
                            } else {
                                callback(LoginModel(success = false, statusCode = 0, message = jObj.getString(AppUtils.TAG_MESSAGE)))
                            }
                            loadingLayout.visibility = View.GONE
                        } catch (e: JSONException) {
                            callback(LoginModel(success = false, statusCode = 0, message = "Data error, hubungi pengembang: $e"))
                            loadingLayout.visibility = View.GONE
                        }
                    },
                    Response.ErrorListener { error ->
                        callback(LoginModel(success = false, statusCode = 0, message = "Terjadi kesalahan koneksi"))
                        loadingLayout.visibility = View.GONE
                    }) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["email"] = email
                        params["password"] = password
                        return params
                    }
                }
            Volley.newRequestQueue(context).add(strReq)
        }
    }
}