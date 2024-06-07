package com.cbi.monitoring_traksi.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.cbi.monitoring_traksi.data.database.DatabaseHelper
import com.cbi.monitoring_traksi.data.model.DataLaporanModel
import com.cbi.monitoring_traksi.data.model.LaporP2HModel

class HistoryRepositoryP2H(context: Context) {
    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)

    @SuppressLint("Range")
    fun fetchByDateLaporanP2H(id: String): List<LaporP2HModel> {
        val dataLaporanP2H = mutableListOf<LaporP2HModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_LAPORAN_P2H}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val jenis_unit = it.getString(it.getColumnIndex("jenis_unit"))
                val unit_kerja = it.getString(it.getColumnIndex("unit_kerja"))
                val kode_unit = it.getString(it.getColumnIndex("kode_unit"))
                val type_unit = it.getString(it.getColumnIndex("type_unit"))
                val tanggal_upload = it.getString(it.getColumnIndex("tanggal_upload"))
                val lat = it.getString(it.getColumnIndex("lat"))
                val lon = it.getString(it.getColumnIndex("lon"))
                val user = it.getString(it.getColumnIndex("user"))
                val foto_unit = it.getString(it.getColumnIndex("foto_unit"))
                val status_unit_beroperasi = it.getString(it.getColumnIndex("status_unit_beroperasi"))
                val kerusakan_unit = it.getString(it.getColumnIndex("kerusakan_unit"))
                val app_version = it.getString(it.getColumnIndex("app_version"))
                val uploaded_time = it.getString(it.getColumnIndex("uploaded_time"))
                val archive = it.getInt(it.getColumnIndex("archive"))

                val dataQuery = LaporP2HModel(
                    id,
                    jenis_unit,
                    unit_kerja,
                    kode_unit,
                    type_unit ,
                    tanggal_upload,
                    lat,
                    lon,
                    user,
                    foto_unit,
                    status_unit_beroperasi,
                    kerusakan_unit,
                    app_version,
                    uploaded_time ,
                    archive
                )
                dataLaporanP2H.add(dataQuery)
            }
        }


        return dataLaporanP2H
    }
}