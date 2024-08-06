package com.cbi.monitoring_traksi.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.ui.view.MainActivity
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.loading_view.view.blurLoadView
import kotlinx.android.synthetic.main.loading_view.view.lottieLoadAnimate
import kotlinx.android.synthetic.main.loading_view.view.overlayLoadView
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

object AppUtils {

    const val mainServer = "https://srs-ssms.com/"
    const val serverMp = "https://mobilepro.srs-ssms.com/"
    const val serverPs = "https://palmsentry.srs-ssms.com/"
    const val serverListDBServer = "https://srs-ssms.com/aplikasi_traksi/fetchAllDataFleetManagement.php"
    const val TAG_SUCCESS = "success"
    const val TAG_MESSAGE = "message"

    const val LOG_UPLOAD = "uploadLog"

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
    const val REQUEST_CHECK_SETTINGS = 0x1

    const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
    const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
        UPDATE_INTERVAL_IN_MILLISECONDS / 2
    const val LOG_LOC = "locationLog"
    const val LOG_PS = "palmSentryLog"
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

    fun getSetDropdownHeight(window: Window, view: AutoCompleteTextView) {
        val rootView = window.decorView.rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val screenHeight = rootView.height
                val editTextHeight = view.height
                val editTextY = IntArray(2)
                view.getLocationOnScreen(editTextY)
                view.dropDownHeight = (screenHeight - editTextY[1] - editTextHeight) - 150

                rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }



    fun showLoadingLayout(context: Context, window: Window, loadingView: View) {
        loadingView.lottieLoadAnimate.visibility = View.VISIBLE
        loadingView.overlayLoadView.visibility = View.VISIBLE
        loadingView.overlayLoadView.setOnTouchListener { _, _ -> true }
        blurViewLayout(context, window, loadingView.blurLoadView)
    }

    fun uploadDataRows(
        context: Context,
        urlInsert: String,
        params: Map<String, String>,
        callback: UploadCallback
    ) {
        var messageInsert: String
        var successResponseInsert = 0

        val postRequest: StringRequest = object : StringRequest(
            Method.POST, urlInsert,
            Response.Listener { response ->
                try {
                    val jObj = JSONObject(response)
                    messageInsert = try {
                        jObj.getString("message")
                    } catch (e: Exception) {
                        e.toString()
                    }
                    successResponseInsert = try {
                        jObj.getInt("success")
                    } catch (e: Exception) {
                        0
                    }
                    Log.d(
                        LOG_UPLOAD,
                        "upload data -- m: $messageInsert, s: $successResponseInsert"
                    )

                    callback.onUploadComplete(messageInsert, successResponseInsert)
                } catch (e: JSONException) {
                    messageInsert = "Failed to parse server response: ${e.message}"
                    Log.e(LOG_UPLOAD, "Failed to parse server response2: ${e.message}")

                    callback.onUploadComplete(messageInsert, successResponseInsert)
                }
            },
            Response.ErrorListener {
                messageInsert = "Terjadi kesalahan koneksi: $it"
                Log.e(LOG_UPLOAD, "Terjadi kesalahan koneksi: $it")

                callback.onUploadComplete(messageInsert, successResponseInsert)
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.cache.clear()
        queue.add(postRequest)
    }

    fun uploadFilePhotos(urlPhotos: String, sourceFile: File): Boolean {
        val fileName: String = sourceFile.name
        val requestBody: RequestBody =
            MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    sourceFile.asRequestBody()
                )
                .build()
        val request: Request = Request
            .Builder()
            .url(urlPhotos)
            .post(requestBody)
            .build()
        return try {
            val response: okhttp3.Response = OkHttpClient()
                .newCall(request)
                .execute()
            response.isSuccessful
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
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

//    fun checkCameraPermissions(context: Context?) {
//        if (ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.CAMERA)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permission is not granted
//            ActivityCompat.requestPermissions(
//                (context as Activity?)!!, arrayOf<String>(android.Manifest.permission.CAMERA),
//                100
//            )
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun checkGeneralPermissions(context: Context, activity: Activity) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.WRITE_SECURE_SETTINGS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.CAMERA
                    )
                }
            }).check()

    }

    fun getCurrentDate(fullTime: Boolean = false): String {
        val calendar = Calendar.getInstance()
        val dateFormat = if (fullTime) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        }
        return dateFormat.format(calendar.time)
    }
    fun checkPermissionsCamera(context: Context?) {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(context!!, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                context as Activity, permissionsToRequest, 100
            )
        }
    }

    fun splitStringWatermark(input: String, chunkSize: Int): String {
        return if (input.length > chunkSize) {
            val regex = "(.{$chunkSize})"
            input.replace(Regex(regex), "$1-\n")
        } else {
            input
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    fun synchronizeDBSqlite(
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
        }

        val strReq: StringRequest =
            @SuppressLint("SetTextI18n")
            object : StringRequest(
                Method.POST,
                serverListDBServer,
                Response.Listener { response ->
                    try {

                        val jObj = JSONObject(response)

                        Log.d("testing",jObj.toString())


                        val jenisUnitsArray = jObj.getJSONArray("jenis_unit")
                        val kodeUnitsArray = jObj.getJSONArray("kode_unit")
                        val asetUnitsArray = jObj.getJSONArray("aset_unit")
                        val itemPertanyaansArray = jObj.getJSONArray("list_pertanyaan")


                        unitViewModel.deleteDataJenisUnit()
                        unitViewModel.deleteDataKodeUnit()
                        unitViewModel.deleteDataAsetUnit()
                        unitViewModel.deleteDataPertanyaan()
                        for (i in 0 until jenisUnitsArray.length()) {
                            val jenisUnitObject = jenisUnitsArray.getJSONObject(i)
                            val idJenisUnit = jenisUnitObject.getInt("id")
                            val namaJenisUnit = jenisUnitObject.getString("nama_unit")
                            val kode = jenisUnitObject.getString("kode")
                            val jenisFormP2H = jenisUnitObject.getString("jenis_form_p2h")
                            val listPertanyaan= jenisUnitObject.getString("list_pertanyaan")

                            unitViewModel.insertDataJenisUnit(
                                id = idJenisUnit,
                                nama_unit = namaJenisUnit,
                                kode = kode,
                                jenis_form_p2h = jenisFormP2H,
                                list_pertanyaan =  listPertanyaan
                            )
                        }

                        for (i in 0 until kodeUnitsArray.length()) {
                            val kodeUnitObject = kodeUnitsArray.getJSONObject(i)
                            val idKodeUnit = kodeUnitObject.getInt("id")
                            val kode = kodeUnitObject.getString("kode")
                            val est = kodeUnitObject.getString("est")
                            val type = kodeUnitObject.getString("type")
                            val no_unit = kodeUnitObject.getString("no_unit")
                            val tahun = kodeUnitObject.getInt("tahun")

                            unitViewModel.insertDataKodeUnit(
                                id = idKodeUnit,
                                kode = kode,
                                est = est,
                                type = type,
                                no_unit = no_unit,
                                tahun = tahun,
                            )

                        }

                        for (i in 0 until asetUnitsArray.length()) {
                            val asetUnitObject = asetUnitsArray.getJSONObject(i)
                            val idAsetUnit = asetUnitObject.getInt("id")
                            val nama_aset = asetUnitObject.getString("nama_aset")

                            unitViewModel.insertDataAsetUnit(
                                id = idAsetUnit,
                                nama_aset = nama_aset,
                            )


                        }

                        for (i in 0 until itemPertanyaansArray.length()) {
                            val pertanyaanObject = itemPertanyaansArray.getJSONObject(i)
                            val idPertanyaan = pertanyaanObject.getInt("id")
                            val nama_pertanyaan = pertanyaanObject.getString("nama_pertanyaan")
                            val kondisi_mesin = pertanyaanObject.getString("kondisi_mesin")

                            unitViewModel.insertDataPertanyaan(
                                id = idPertanyaan,
                                nama_pertanyaan = nama_pertanyaan,
                                kondisi_mesin = kondisi_mesin,
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

                            if (isSuccess) {
                                successArrayInsert.add(true)
                                Log.d("testing", "Sukses insert data kode Unit !")
                            } else {
                                Log.d("testing", "Terjadi kesalahan, mengunduh data kode unit")
                                successArrayInsert.add(false)
                            }
                        }

                        unitViewModel.insertResultAsetUnit.observe(
                            context as LifecycleOwner
                        ) { isSuccess ->

                            if (isSuccess) {
                                Log.d("testing", "Sukses insert data Aset Unit !")
                                closeLoadingLayout(loaderView)
                                successArrayInsert.add(true)
                            } else {
                                successArrayInsert.add(false)
                                Log.d("testing", "Terjadi kesalahan, mengunduh data Aset Unit")

                            }
                        }

                        unitViewModel.insertResultPertanyaan.observe(
                            context as LifecycleOwner
                        ) { isSuccess ->

                            if (isSuccess) {
                                Log.d("testing", "Sukses insert data pertanyaan !")
                                closeLoadingLayout(loaderView)
                                successArrayInsert.add(true)
//
                            } else {
                                successArrayInsert.add(false)
                                Log.d("testing", "Terjadi kesalahan, mengunduh data pertanyaan")

                            }
                        }
                        fetchEstateData(context, unitViewModel) { estateSuccess ->
                            val allSuccess = successArrayInsert.all { it } && estateSuccess

                            if (allSuccess) {
                                prefManager.isFirstTimeLaunch = false
                                Log.d("testing", "All operations succeeded!")
                                AlertDialogUtility.alertDialog(
                                    context,
                                    "Sukses",
                                    "Berhasil Mengunduh Data",
                                    "success.json"
                                )
                            } else {
                                Log.d("testing", "At least one operation failed.")
                                closeLoadingLayout(loaderView)
                                AlertDialogUtility.alertDialog(
                                    context,
                                    "Gagal",
                                    "Gagal Mengunduh Data",
                                    "error.json"
                                )
                            }

                        }
                        val allSuccess = successArrayInsert.all { it }



//                        if (allSuccess) {
//                            prefManager.isFirstTimeLaunch = false
//                            AlertDialogUtility.alertDialog(
//                                context,
//                                "Sukses",
//                                "Berhasil Mengunduh Data",
//                                "success.json"
//                            )
//                        } else {
//                            closeLoadingLayout(loaderView)
//
//                            Log.d("testing", "At least one operation insert is failed.")
//                        }
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
                            synchronizeDBSqlite(
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
                        synchronizeDBSqlite(
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


    private fun fetchEstateData(
        context: Context,
        unitViewModel: UnitViewModel,
        onComplete: (Boolean) -> Unit
    ) {
        val request = StringRequest(
            Method.GET,
            "https://srs-ssms.com/aplikasi_traksi/fetchAllEstateList.php",
            { response ->
                try {
                    val jObj = JSONObject(response)
                    val jenisUnitsArray = jObj.getJSONArray("list_est")


                    for (i in 0 until jenisUnitsArray.length()) {
                        val estateObject = jenisUnitsArray.getJSONObject(i)
                        val idEstate = estateObject.getInt("id")
                        val nameEstate = estateObject.getString("est")
                        val idReg = estateObject.getInt("id_reg")
                        unitViewModel.insertListEstate(
                            id = idEstate,
                            est = nameEstate,
                            id_reg = idReg,
                        )
                    }

                    Log.d("testing", "Sukses Insert data List Estate")
                    onComplete(true)
                } catch (e: JSONException) {
                    Log.d("testing", "${context.getString(R.string.error_volley1)}: $e")
                    e.printStackTrace()

                    AlertDialogUtility.withSingleAction(
                        context,
                        context.getString(R.string.try_again),
                        context.getString(R.string.failed),
                        context.getString(R.string.desc_failed_download),
                        "error.json"
                    ) {
                        fetchEstateData(context, unitViewModel, onComplete)
                    }
                    onComplete(false)
                }
            },
            { error ->
                Log.d("testing", "${context.getString(R.string.error_volley2)}: $error")

                AlertDialogUtility.withSingleAction(
                    context,
                    context.getString(R.string.try_again),
                    context.getString(R.string.failed),
                    context.getString(R.string.desc_failed_download),
                    "error.json"
                ) {
                    fetchEstateData(context, unitViewModel, onComplete)
                }
                onComplete(false)
            }
        )

        request.retryPolicy = DefaultRetryPolicy(
            90000,  // Socket timeout in milliseconds (90 seconds)
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        Volley.newRequestQueue(context).add(request)
    }

    fun checkSoftKeyboard(context: Context, view: View, function: () -> Unit) {
        var isKeyboardVisible = false
        val keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val newHeight = view.height
            if (newHeight != 0) {
                if (isKeyboardVisible) {
                    isKeyboardVisible = false
                    function()
                } else {
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    val isAcceptingText = imm.isAcceptingText
                    val activity = context as Activity
                    val focusedView = activity.currentFocus
                    val isKeyboardOpenAndActive = focusedView != null && isAcceptingText
                    if (isKeyboardOpenAndActive) {
                        isKeyboardVisible = true
                    }
                }
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(keyboardLayoutListener)
    }
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: return
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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

    fun handleStringtoJsonObjectPertanyaan(jsonString: String): ArrayList<String> {
        val jsonObject = JSONObject(jsonString)
        val values = ArrayList<String>()

        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.getString(key)
            values.add(value)
        }

        return values
    }

    interface UploadCallback {
        fun onUploadComplete(message: String, success: Int)
    }
}