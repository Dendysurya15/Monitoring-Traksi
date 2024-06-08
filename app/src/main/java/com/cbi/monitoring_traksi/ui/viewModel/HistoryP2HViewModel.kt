package com.cbi.monitoring_traksi.ui.viewModel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.monitoring_traksi.data.database.DatabaseHelper
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.data.repository.HistoryP2HRepository
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class HistoryP2HViewModel(
    application: Application,
    private val context: Context,
    private val historyRepo: HistoryP2HRepository,
    private val loadingView: View? = null,
    private val window: Window? = null,
    private val prefManager: PrefManager? = null,
) : AndroidViewModel(application) {

    private val _queryGetLaporanP2H = MutableLiveData<List<LaporP2HModel>>()
    private val urlCheckPhotos = AppUtils.mainServer + "aplikasi_traksi/" + "checkPhotosLaporanP2H.php"
    private val urlCheckData = AppUtils.mainServer + "aplikasi_traksi/" + "checkDataLaporanP2H.php"
    private val urlInsertData = AppUtils.mainServer + "aplikasi_traksi/" + "postNewLaporanP2H.php"
    private val urlUploadPhotos = AppUtils.mainServer + "aplikasi_traksi/" + "uploadPhotosLaporanP2H.php"
    private val uploadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var messageCheckFoto = ""
    private var successResponse = 0
    private var messageInsert = ""
    private var successResponseInsert = 0
    private var messageCheckData = ""
    private var successResponseCheckData = 0

    private var uploading = false
    private var stoppedByConditions = false
    private var shouldStop = false
    private val handler = Handler()
    private val delay = 5000
    private val timeOut = 300000
    private val idUpload = ArrayList<Int>()
    private val _uploadResult = MutableLiveData<List<LaporP2HModel>>()
    val uploadResult: LiveData<List<LaporP2HModel>> get() = _uploadResult

    private val _deleteItemResult = MutableLiveData<Boolean>()
    val deleteItemResult: LiveData<Boolean> get() = _deleteItemResult


    val resultQueryDateLaporanP2H: LiveData<List<LaporP2HModel>> get() = _queryGetLaporanP2H
    fun loadLaporanP2HByDate(dateRequest : String) {
        viewModelScope.launch {
            try {
                _queryGetLaporanP2H.value = historyRepo.fetchByDateLaporanP2H(dateRequest)
            }catch (e:Exception){
                e.printStackTrace()
                _queryGetLaporanP2H.value = emptyList()
            }
        }
    }

    fun deleteItemList(id: String) {
        viewModelScope.launch {
            try {
                val isDeleted = historyRepo.deleteItem(id)
                _deleteItemResult.value = isDeleted
            } catch (e: Exception) {
                e.printStackTrace()
                _deleteItemResult.value = false
            }
        }
    }

    private val runnableCode = object : Runnable {
        override fun run() {
            if (shouldStop) {
                return
            }

            if (uploading) {
                checkPhotosServer()
            }

            if (successResponse == 2) {

                stoppedByConditions = true
                shouldStop = true
                uploading = false
                handler.removeCallbacks(this)

                if (successResponseCheckData == 1) {
                    Toasty.info(
                        context,
                        messageCheckData,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                if (messageCheckFoto.isNotEmpty()) {
                    handler.postDelayed({
                        if (successResponse == 0 || successResponse == 1) {
                            Toasty.warning(
                                context,
                                messageCheckFoto,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toasty.success(
                                context,
                                messageCheckFoto,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }, if (successResponseCheckData == 1) 1000 else 0)

                    if (messageInsert.isNotEmpty()) {
                        handler.postDelayed({
                            if (successResponseInsert == 1) {
                                Toasty.success(
                                    context,
                                    messageInsert,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                Toasty.warning(
                                    context,
                                    messageInsert,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }, if (successResponseCheckData == 1) 2000 else 1000)


                        AppUtils.closeLoadingLayout(loadingView!!)

                    } else {
                        AppUtils.closeLoadingLayout(loadingView!!)
                    }

                }
            }

            handler.postDelayed(this, delay.toLong())
        }
    }

    private fun checkPhotosServer() {
        if (shouldStop) {
            return
        }

        val currentDate = AppUtils.getCurrentDate(true)
        val laporanP2HList = historyRepo.fetchByDateLaporanP2H(currentDate)
        val arrayCheckFoto = ArrayList<String>()
        for (i in idUpload.indices) {
            laporanP2HList.map { data ->
                if (idUpload[i] == data.id.toInt()) {
                    val fotoPerLaporan = data.foto_unit
                    if (fotoPerLaporan.isNotEmpty()) {
                        if (!arrayCheckFoto.contains(fotoPerLaporan)) {
                            arrayCheckFoto.add(fotoPerLaporan)
                        }
                    }
                }
            }
        }


        val postRequest: StringRequest = object : StringRequest(
            Method.POST, urlCheckPhotos,
            Response.Listener { response ->
                try {
                    Log.d(AppUtils.LOG_UPLOAD, "response full: $response")
                    val jObj = JSONObject(response)
                    messageCheckFoto = try {
                        jObj.getString("message")
                    } catch (e: Exception) {
                        "error $urlCheckPhotos, error code:$e"
                    }
                    successResponse = jObj.getInt("success")
                    if (successResponse == 1) {
                        val photoArr = jObj.getString("listfoto").split(",")
                        for (p in photoArr.indices) {
                            if (photoArr[p].isNotEmpty() || photoArr[p] != "") {
                                val myDir =
                                    File(
                                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            .toString(), "LaporP2H"
                                    )
                                val imageData =
                                    File(myDir, photoArr[p].replace("\"", "").replace(" ", ""))
                                uploadScope.launch {
                                    try {
                                        AppUtils.uploadFilePhotos(urlUploadPhotos, imageData)
                                    } catch (e: Exception) {
                                        Log.e(AppUtils.LOG_UPLOAD, "uploadFile: $e")
                                    }
                                }
                            }
                        }
                    } else if (successResponse == 2) {
                        for (i in idUpload.indices) {
                            laporanP2HList.map { data ->
                                if (idUpload[i] == data.id) {
                                    val params = mapOf(
                                        DatabaseHelper.DB_JENIS_UNIT to data.jenis_unit,
                                        DatabaseHelper.DB_UNIT_KERJA to data.unit_kerja,
                                        DatabaseHelper.DB_KODE_UNIT to data.kode_unit,
                                        DatabaseHelper.DB_TYPE_UNIT to data.type_unit,
                                        DatabaseHelper.DB_TANGGAL_UPLOAD to data.tanggal_upload,
                                        DatabaseHelper.DB_LAT to data.lat,
                                        DatabaseHelper.DB_LON to data.lon,
                                        DatabaseHelper.DB_USER to data.user,
                                        DatabaseHelper.DB_FOTO_UNIT to data.foto_unit,
                                        DatabaseHelper.DB_STATUS_UNIT_BEROPERASI to data.status_unit_beroperasi,
                                        DatabaseHelper.DB_KERUSAKAN_UNIT to data.kerusakan_unit,
                                        DatabaseHelper.DB_APP_VERSION to data.app_version
                                    )

                                    AppUtils.uploadDataRows(context, urlInsertData, params, object :
                                        AppUtils.UploadCallback {
                                        override fun onUploadComplete(
                                            message: String,
                                            success: Int
                                        ) {
                                            messageInsert = message
                                            successResponseInsert = success
                                        }
                                    })

                                    if (successResponseInsert == 1) {
                                        val currentDate = AppUtils.getCurrentDate(true)
                                        if (data.archive == 0) {
                                            if (historyRepo.updateArchiveMtc(data.id.toString())) {
                                                historyRepo.updateUploadTimeP2HLocal(data.id.toString(),currentDate )
                                                Log.d(AppUtils.LOG_UPLOAD, "Success archive id ${data.id}!")
                                            } else {
                                                Log.e(AppUtils.LOG_UPLOAD, "Failed archive!")
                                            }
                                        }


                                        //live observer to update the recycle latest data
                                        val resultQueryLaporanP2H = historyRepo.fetchByDateLaporanP2H(currentDate)
                                        _uploadResult.value = resultQueryLaporanP2H
                                    }
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    messageCheckFoto = "Failed to parse server response: ${e.message}"
                    Log.e(AppUtils.LOG_UPLOAD, "Failed to parse server response1: ${e.message}")
                }
            },
            Response.ErrorListener {
                messageCheckFoto = "Terjadi kesalahan:$it"
                Log.e(AppUtils.LOG_UPLOAD, "Terjadi kesalahan:$it")
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = java.util.HashMap()

                val arrayCheckFotoStr = arrayCheckFoto.joinToString(";") { it }
                params["foto"] = arrayCheckFotoStr
                Log.d(AppUtils.LOG_UPLOAD, "file yg dicheck: $arrayCheckFotoStr")

                return params
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.cache.clear()
        queue.add(postRequest)
    }

    private val stopRunnable = Runnable {
        if (!stoppedByConditions) {
            shouldStop = true
            uploading = false
            handler.removeCallbacks(runnableCode)

            if (successResponseCheckData == 1) {
                Toasty.info(
                    context,
                    messageCheckData,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            if (messageCheckFoto.isNotEmpty()) {
                handler.postDelayed({
                    if (successResponse == 0 || successResponse == 1) {
                        Toasty.warning(context, messageCheckFoto, Toast.LENGTH_SHORT).show()
                    } else {
                        Toasty.success(context, messageCheckFoto, Toast.LENGTH_SHORT).show()
                    }
                }, if (successResponseCheckData == 1) 1000 else 0)

                if (messageInsert.isNotEmpty()) {
                    handler.postDelayed({
                        if (successResponseInsert == 1) {
                            Toasty.success(context, messageInsert, Toast.LENGTH_SHORT).show()
                        } else {
                            Toasty.warning(context, messageInsert, Toast.LENGTH_SHORT).show()
                        }
                    }, if (successResponseCheckData == 1) 2000 else 1000)
//
//                    AppUtils.checkDataWaterLevel(
//                        null,
//                        window!!,
//                        context,
//                        prefManager!!,
//                        waterLvlViewModel!!,
//                        loadingView!!,
//                        "yes"
//                    )


                    Log.d("uploadLog", "stopRunnable")
                    AppUtils.closeLoadingLayout(loadingView!!)


                } else {
                    AppUtils.closeLoadingLayout(loadingView!!)
                }
            }
        }
    }

    fun uploadToServer(dataRequest:String) {
        idUpload.clear()
        val resultQueryLaporanP2H = historyRepo.fetchByDateLaporanP2H(dataRequest)


        val arrFieldMappingLaporanP2H = resultQueryLaporanP2H.map { laporan ->
            mapOf(
                "id" to laporan.id,
                "tanggal_upload" to laporan.tanggal_upload,
                "jenis_unit" to laporan.jenis_unit,
                "unit_kerja" to laporan.unit_kerja,
                "kode_unit" to laporan.kode_unit,
                "type_unit" to laporan.type_unit,
                "user" to laporan.user,
            )
        }

        var completedRequests = 0

        for (i in arrFieldMappingLaporanP2H.indices) {

            val laporan = arrFieldMappingLaporanP2H[i]
            val id = laporan["id"].toString()
            val user = laporan["user"].toString()
            val jenisUnit = laporan["jenis_unit"].toString()
            val unitKerja = laporan["unit_kerja"].toString()
            val kodeUnit = laporan["kode_unit"].toString()
            val typeUnit = laporan["type_unit"].toString()
            val tanggalUpload = laporan["tanggal_upload"].toString()

            val postRequest: StringRequest = object : StringRequest(
                Method.POST, urlCheckData,
                Response.Listener { response ->
                    try {
                        val jObj = JSONObject(response)

                        messageCheckData = try {
                            jObj.getString("message")
                        } catch (e: Exception) {
                            e.toString()
                        }
                        successResponseCheckData = try {
                            jObj.getInt("success")
                        } catch (e: Exception) {
                            0
                        }

                        if (successResponseCheckData == 2) {
                            idUpload.add(id.toInt())
                        }

                        Log.d(AppUtils.LOG_UPLOAD, "messageCheckData: $messageCheckData")
                    } catch (e: JSONException) {
                        Log.e(AppUtils.LOG_UPLOAD, "Failed to parse server response: ${e.message}")
                    } finally {
                        completedRequests++

                        if (completedRequests == arrFieldMappingLaporanP2H.size) {
                            uploading = true
                            handler.postDelayed(runnableCode, delay.toLong())
                            handler.postDelayed(stopRunnable, timeOut.toLong())
                        }
                    }
                },
                Response.ErrorListener {
                    Log.e(AppUtils.LOG_UPLOAD, "Terjadi kesalahan koneksi: $it")

                    completedRequests++

                    if (completedRequests == arrFieldMappingLaporanP2H.size) {
                        val currentDate = AppUtils.getCurrentDate(true)
                        uploadToServer(currentDate)
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params[DatabaseHelper.DB_USER] = user
                    params[DatabaseHelper.DB_JENIS_UNIT] = jenisUnit
                    params[DatabaseHelper.DB_UNIT_KERJA] = unitKerja
                    params[DatabaseHelper.DB_KODE_UNIT] = kodeUnit
                    params[DatabaseHelper.DB_TYPE_UNIT] = typeUnit
                    params[DatabaseHelper.DB_TANGGAL_UPLOAD] = tanggalUpload
                    return params
                }
            }
            val queue = Volley.newRequestQueue(context)
            queue.cache.clear()
            queue.add(postRequest)
        }

        Log.d("uploadLog", "sudah selesai mengirim semua data gan")
    }

    class Factory(
        private val application: Application,
        private val context: Context,
        private val historyRepo: HistoryP2HRepository,
        private val loadingView: View? = null,
        private val window: Window? = null,
        private val prefManager: PrefManager? = null,

        ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryP2HViewModel::class.java)) {
                return HistoryP2HViewModel(
                    application,
                    context,
                    historyRepo,
                    loadingView,
                    window,
                    prefManager,

                    ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}