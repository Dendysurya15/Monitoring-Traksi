package com.cbi.monitoring_traksi.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.cbi.monitoring_traksi.data.database.DatabaseHelper
import com.cbi.monitoring_traksi.data.model.DataLaporanModel
import com.cbi.monitoring_traksi.data.model.JenisUnitModel
import com.cbi.monitoring_traksi.data.model.KodeUnitModel
import com.cbi.monitoring_traksi.data.model.ItemPertanyaanModel
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.data.model.UnitKerjaModel

class UnitRepository(context: Context)  {

    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)
    fun insertDataJenisUnit(dataUnit: JenisUnitModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_NAMA_UNIT, dataUnit.nama_unit)
            put(DatabaseHelper.DB_JENIS, dataUnit.jenis)
            put(DatabaseHelper.DB_LIST_PERTANYAAN, dataUnit.list_pertanyaan)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_JENIS_UNIT, null, values)
        db.close()

        return rowsAffected > 0
    }

    fun insertDataPertanyaan(dataUnit: ItemPertanyaanModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_NAMA_PERTANYAAN, dataUnit.nama_pertanyaan)
            put(DatabaseHelper.DB_KONDISI_MESIN, dataUnit.kondisi_mesin)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_LIST_PERTANYAAN, null, values)
        db.close()

        return rowsAffected > 0
    }


    fun insertDataKodeUnit(dataUnit: KodeUnitModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_NAMA_KODE, dataUnit.nama_kode)
            put(DatabaseHelper.DB_TYPE_UNIT, dataUnit.type_unit)
            put(DatabaseHelper.DB_ID_UNIT_KERJA, dataUnit.id_unit_kerja)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_KODE_UNIT, null, values)
        db.close()

        return rowsAffected > 0
    }

    fun insertDataUnitKerja(dataUnit: UnitKerjaModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_NAMA_UNIT_KERJA, dataUnit.nama_unit_kerja)
            put(DatabaseHelper.DB_ID_JENIS_UNIT, dataUnit.id_jenis_unit)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_UNIT_KERJA, null, values)
//        db.close()

        return rowsAffected > 0
    }

    fun insertToTableData(data: DataLaporanModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_CREATED_AT, data.created_at)
            put(DatabaseHelper.DB_ID_LAPORAN, data.id_laporan)
            put(DatabaseHelper.DB_ID_PERTANYAAN, data.id_pertanyaan)
            put(DatabaseHelper.DB_KONDISI, data.kondisi)
            put(DatabaseHelper.DB_KOMENTAR ,data.komentar)
            put(DatabaseHelper.DB_FOTO ,data.foto)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_DATA, null, values)

        return rowsAffected > 0
    }

    fun insertLaporP2HToSQL(data: LaporP2HModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_JENIS_UNIT, data.jenis_unit)
            put(DatabaseHelper.DB_UNIT_KERJA, data.unit_kerja)
            put(DatabaseHelper.DB_KODE_UNIT, data.kode_unit)
            put(DatabaseHelper.DB_TANGGAL_UPLOAD, data.tanggal_upload)
            put(DatabaseHelper.DB_LAT, data.lat)
            put(DatabaseHelper.DB_LON, data.lon)
            put(DatabaseHelper.DB_USER, data.user)
            put(DatabaseHelper.DB_FOTO_UNIT, data.foto_unit)
            put(DatabaseHelper.DB_STATUS_UNIT_BEROPERASI, data.status_unit_beroperasi)
            put(DatabaseHelper.DB_KERUSAKAN_UNIT, data.kerusakan_unit)
            put(DatabaseHelper.DB_APP_VERSION, data.app_version)
            put(DatabaseHelper.DB_UPLOADED_TIME, data.uploaded_time)
            put(DatabaseHelper.DB_ARCHIVE, data.archive)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_LAPORAN_P2H, null, values)
//        db.close()

        return rowsAffected > 0
    }

    fun fetchLastIdlaporanP2H(): Int {
        var id_last_insert_laporanp2h = 0
        val db = databaseHelper.readableDatabase
        val query = "SELECT MAX(id) FROM ${DatabaseHelper.DB_TAB_LAPORAN_P2H}" // Adjust 'id' to your primary key column name
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            id_last_insert_laporanp2h = cursor.getInt(0)
        }

        return id_last_insert_laporanp2h
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

    fun deleteDataUnitKerja() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_UNIT_KERJA, null, null)
        db.close()
    }

    fun deleteDataPertanyaan() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_LIST_PERTANYAAN, null, null)
        db.close()
    }


    @SuppressLint("Range")
    fun fetchAllJenisUnit(): List<JenisUnitModel> {
        val datasJenisUnitList = mutableListOf<JenisUnitModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_JENIS_UNIT}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val nama_unit = it.getString(it.getColumnIndex("nama_unit"))
                val jenis = it.getString(it.getColumnIndex("jenis"))
                val list_pertanyaan = it.getString(it.getColumnIndex("list_pertanyaan"))
                val dataUnit = JenisUnitModel(
                    id,
                    nama_unit,
                    jenis,
                    list_pertanyaan
                )
                datasJenisUnitList.add(dataUnit)
            }
        }

//        db.close()

        return datasJenisUnitList
    }

    @SuppressLint("Range")
    fun fetchAllListPertanyaanBasedOnJenisUnit(arrayHere: Array<String>): List<ItemPertanyaanModel> {
        val listPertanyaanArr = mutableListOf<ItemPertanyaanModel>()


        val db = databaseHelper.readableDatabase
        val selectionArgs = Array(arrayHere.size) { "?" }.joinToString(",")

        val selection = "id IN ($selectionArgs)"
        val cursor = db.query(
            DatabaseHelper.DB_TAB_LIST_PERTANYAAN,
            null,
            selection,
            arrayHere,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val namaPertanyaan = it.getString(it.getColumnIndex("nama_pertanyaan"))
                val kondisiMesin = it.getString(it.getColumnIndex("kondisi_mesin"))
                val dataPertanyaan = ItemPertanyaanModel(
                    id,
                    namaPertanyaan,
                    kondisiMesin,
                )
                listPertanyaanArr.add(dataPertanyaan)
            }
        }

//        db.close()

        return listPertanyaanArr

    }



    @SuppressLint("Range")
    fun fetchAllUnitKerja(): List<UnitKerjaModel> {
        val datasUnitKerjaList = mutableListOf<UnitKerjaModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_UNIT_KERJA}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val nama_unit_kerja = it.getString(it.getColumnIndex("nama_unit_kerja"))
                val id_jenis_unit = it.getInt(it.getColumnIndex("id_jenis_unit"))
                val dataUnit = UnitKerjaModel(
                    id,
                    nama_unit_kerja ,
                    id_jenis_unit ,
                )
                datasUnitKerjaList.add(dataUnit)
            }
        }

//        db.close()

        return datasUnitKerjaList
    }

    @SuppressLint("Range")
    fun fetchAllKodeUnit(): List<KodeUnitModel> {
        val datasKodeUnitList = mutableListOf<KodeUnitModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_KODE_UNIT}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val nama_kode = it.getString(it.getColumnIndex("nama_kode"))
                val type_unit = it.getString(it.getColumnIndex("type_unit"))
                val id_unit_kerja = it.getInt(it.getColumnIndex("id_unit_kerja"))
                val dataUnit = KodeUnitModel(
                    id,
                    nama_kode ,
                    type_unit ,
                    id_unit_kerja,
                )
                datasKodeUnitList.add(dataUnit)
            }
        }

//        db.close()

        return datasKodeUnitList
    }


//    @SuppressLint("Range")
//    fun fetchKodeUnitById(id : Int): List<KodeUnitModel>{
//
//        val listKodeUnitByChoice = mutableListOf<KodeUnitModel>()
//        val id_jenis_unit = id
//
//        Log.d("testing", "id inputan yaitu " + id.toString())
//        val db = databaseHelper.readableDatabase
//        val query = "SELECT * FROM ${DatabaseHelper.DB_TAB_KODE_UNIT} WHERE ${DatabaseHelper.DB_ID_JENIS_UNIT} = $id_jenis_unit"
//        val cursor = db.rawQuery(query, null)
//
//        cursor.use {
//            while (it.moveToNext()) {
//                val id = it.getInt(it.getColumnIndex("id"))
//                val nama = it.getString(it.getColumnIndex("nama_unit"))
//                val unit_kerja = it.getString(it.getColumnIndex("unit_kerja"))
//                val type_unit = it.getString(it.getColumnIndex("type_unit"))
//                val id_jenis_unit = it.getInt(it.getColumnIndex("id_jenis_unit"))
//
//                val dataUnit = KodeUnitModel(
//                    id,
//                    nama,
//                    unit_kerja,
//                    type_unit,
//                    id_jenis_unit,
//                )
//                listKodeUnitByChoice.add(dataUnit)
//            }
//        }
//
//
//        db.close()
//
//
//        return listKodeUnitByChoice
//
//    }
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