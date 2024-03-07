package com.cbi.monitoring_traksi.data.repository

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.monitoring_traksi.data.database.DatabaseHelper
import com.cbi.monitoring_traksi.data.model.JenisUnitModel
import com.cbi.monitoring_traksi.data.model.KodeUnitModel
import com.cbi.monitoring_traksi.utils.AppUtils
import org.json.JSONException
import org.json.JSONObject

class UnitRepository(context: Context)  {

    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)
    fun insertDataJenisUnit(dataUnit: JenisUnitModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_NAMA_UNIT, dataUnit.nama_unit)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_JENIS_UNIT, null, values)
//        db.close()

        return rowsAffected > 0
    }


    fun insertDataKodeUnit(dataUnit: KodeUnitModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_NAMA, dataUnit.nama)
            put(DatabaseHelper.DB_UNIT_KERJA, dataUnit.unit_kerja)
            put(DatabaseHelper.DB_TYPE_UNIT, dataUnit.type_unit)
            put(DatabaseHelper.DB_ID_JENIS_UNIT, dataUnit.id_jenis_unit)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_KODE_UNIT, null, values)
//        db.close()

        return rowsAffected > 0
    }


    fun deleteDataJenisUnit() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_JENIS_UNIT, null, null)
        db.close()
    }

    fun deleteDataKodeUnit() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_KODE_UNIT, null, null)
        db.close()
    }
//
//    fun deleteDataUnit() {
//        val db = databaseHelper.writableDatabase
//        db.delete(DatabaseHelper.DB_TAB_UNIT, null, null)
//        db.close()
//    }

//    fun fetchData(onSuccess: (List<JenisUnitModel>) -> Unit, onError: (String) -> Unit) {
//        val urlGet = AppUtils.mainServer + "getListDataRegional.php"
//
//        val postRequest: StringRequest = object : StringRequest(
//            Method.GET, urlGet,
//            Response.Listener { response ->
//                try {
//                    val jObj = JSONObject(response)
//
//
//
//
//                    Log.d("testing",jObj.toString())
////                    val success = jObj.getInt("status")
//
////                    if (success == 1) {
////                        val dataListPupukArray = jObj.getJSONObject("listData")
////                        val beforeSplitId = dataListPupukArray.getJSONArray("id")
////                        val beforeSplitData = dataListPupukArray.getJSONArray("data")
////                        val beforeSplitReg = dataListPupukArray.getJSONArray("reg")
////
////                        val qcRegList = mutableListOf<UnitModel>()
////                        for (i in 0 until beforeSplitId.length()) {
////                            val id = beforeSplitId.getInt(i)
////                            val data = beforeSplitData.getString(i)
////                            val reg = beforeSplitReg.getString(i)
////                            qcRegList.add(UnitModel(id, data, reg))
////                        }
////
////                        onSuccess(qcRegList)
////                    } else {
////                        onError(jObj.getString(AppUtils.TAG_MESSAGE))
////                    }
//
//
//
//
//
//                } catch (e: JSONException) {
//                    onError("Data error, hubungi pengembang: $e")
//                    e.printStackTrace()
//                }
//            },
//            Response.ErrorListener {
//                onError("Terjadi kesalahan koneksi")
//            }
//        ) {
//            // Add any necessary headers or parameters here
//        }
//
//        val queue = Volley.newRequestQueue(application)
//        queue.cache.clear()
//        queue.add(postRequest)
//    }
}