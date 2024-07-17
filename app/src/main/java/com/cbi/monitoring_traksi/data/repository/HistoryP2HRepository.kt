package com.cbi.monitoring_traksi.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.cbi.monitoring_traksi.data.database.DatabaseHelper
import com.cbi.monitoring_traksi.data.model.LaporP2HModel

class HistoryP2HRepository(context: Context) {
    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)


    fun deleteItem(id: String): Boolean {
        val db = databaseHelper.writableDatabase
        val rowsAffected = db.delete(DatabaseHelper.DB_TAB_LAPORAN_P2H, "id=?", arrayOf(id))
//        db.close()

        return rowsAffected > 0
    }
    fun updateArchiveMtc(id: String): Boolean {
        val db = databaseHelper.writableDatabase

        val values = ContentValues()
        values.put(DatabaseHelper.DB_ARCHIVE, 1)

        val rowsAffected = db.update(DatabaseHelper.DB_TAB_LAPORAN_P2H, values, "id=?", arrayOf(id))
//        db.close()

        return rowsAffected > 0
    }

    fun updateUploadTimeP2HLocal(id: String, uploadTime :String): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues()
        values.put(DatabaseHelper.DB_UPLOADED_TIME, uploadTime)
        val rowsAffected = db.update(DatabaseHelper.DB_TAB_LAPORAN_P2H, values, "id=?", arrayOf(id))
//        db.close()

        return rowsAffected > 0
    }

        fun fetchNamaPertanyaanBasedOnId(idList: Collection<String>): MutableList<String> {
            val db = databaseHelper.readableDatabase
            val idString = idList.joinToString(",") { "'$it'" } // Convert IDs to a comma-separated string
            val query = "SELECT nama_pertanyaan FROM ${DatabaseHelper.DB_TAB_LIST_PERTANYAAN} " +
                    "WHERE id IN ($idString)"

            val cursor = db.rawQuery(query, null)
            val namaPertanyaanList = mutableListOf<String>()

            if (cursor.moveToFirst()) {
                do {
                    val namaPertanyaan = cursor.getString(cursor.getColumnIndexOrThrow("nama_pertanyaan"))
                    namaPertanyaanList.add(namaPertanyaan)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return namaPertanyaanList
        }

    @SuppressLint("Range")
    fun fetchByDateLaporanP2H(dateRequest: String, kerusakanFiltered : Boolean = false, tanpaKerusakan : Boolean = false): List<LaporP2HModel> {
        val dataLaporanP2H = mutableListOf<LaporP2HModel>()

        val db = databaseHelper.readableDatabase
        // Extract date part from the dateRequest parameter
        val dateOnly = dateRequest.substring(0, 10)

        val args = mutableListOf<String>()
        args.add(dateOnly)


        var query = "SELECT * FROM ${DatabaseHelper.DB_TAB_LAPORAN_P2H} " +
                "WHERE DATE(tanggal_upload) = ?"

        if (kerusakanFiltered) {

            query += " AND (kerusakan_unit != ?)"
            args.add("")
        }

        if (tanpaKerusakan) {
            query += " AND (kerusakan_unit = ?)"
            args.add("")
        }

        val argsArray = args.toTypedArray()

        // Execute the query with the date parameter
        val cursor = db.rawQuery(query, argsArray)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val jenis_unit = it.getString(it.getColumnIndex("jenis_unit"))
                val unit_kerja = it.getString(it.getColumnIndex("unit_kerja"))
                val kode_unit = it.getString(it.getColumnIndex("kode_unit"))

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
    fun deleteHistoryP2H() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_LAPORAN_P2H, null, null)
        db.close()
    }
}