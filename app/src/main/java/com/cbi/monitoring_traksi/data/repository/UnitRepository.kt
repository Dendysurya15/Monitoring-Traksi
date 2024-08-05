package com.cbi.monitoring_traksi.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import com.cbi.monitoring_traksi.data.database.DatabaseHelper
import com.cbi.monitoring_traksi.data.model.AsetUnitModel
import com.cbi.monitoring_traksi.data.model.EstateModel
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
            put(DatabaseHelper.DB_KODE, dataUnit.kode)
            put(DatabaseHelper.DB_JENIS_FORM_P2H, dataUnit.jenis_form_p2h)
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


    fun insertListEstate(dataEstate: EstateModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataEstate.id)
            put(DatabaseHelper.DB_EST, dataEstate.est)

        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_ESTATE, null, values)
        db.close()

        return rowsAffected > 0
    }


    fun insertDataKodeUnit(dataUnit: KodeUnitModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataUnit.id)
            put(DatabaseHelper.DB_KODE, dataUnit.kode)
            put(DatabaseHelper.DB_EST, dataUnit.est)
            put(DatabaseHelper.DB_TYPE, dataUnit.type)
            put(DatabaseHelper.DB_NO_UNIT, dataUnit.no_unit)
            put(DatabaseHelper.DB_TAHUN, dataUnit.tahun)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_KODE_UNIT, null, values)
        db.close()

        return rowsAffected > 0
    }

    fun insertDataAsetUnit(dataAsetUnit: AsetUnitModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_ID, dataAsetUnit.id)
            put(DatabaseHelper.DB_NAMA_ASET, dataAsetUnit.nama_aset)
        }
        val rowsAffected = db.insert(DatabaseHelper.DB_TAB_ASET_UNIT, null, values)
        db.close()

        return rowsAffected > 0
    }



    fun insertLaporP2HToSQL(data: LaporP2HModel): Boolean {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.DB_JENIS_UNIT, data.jenis_unit)
            put(DatabaseHelper.DB_ASET_UNIT, data.aset_unit)
            put(DatabaseHelper.DB_KODE_TYPE_NO_UNIT, data.kode_type_no_unit)
            put(DatabaseHelper.DB_LOKASI_KERJA, data.lokasi_kerja)
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

    fun deleteDataAsetUnit() {
        val db = databaseHelper.writableDatabase
        db.delete(DatabaseHelper.DB_TAB_ASET_UNIT, null, null)
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
                val kode = it.getString(it.getColumnIndex("kode"))
                val jenis_form_p2h = it.getString(it.getColumnIndex("jenis_form_p2h"))
                val list_pertanyaan = it.getString(it.getColumnIndex("list_pertanyaan"))
                val dataUnit = JenisUnitModel(
                    id,
                    nama_unit,
                    kode,
                    jenis_form_p2h,
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
    fun fetchAllAsetUnit(): List<AsetUnitModel> {
        val datasAsetUnitist = mutableListOf<AsetUnitModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_ASET_UNIT}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val nama_aset = it.getString(it.getColumnIndex("nama_aset"))
                val dataUnit = AsetUnitModel(
                    id,
                    nama_aset ,
                )
                datasAsetUnitist.add(dataUnit)
            }
        }

//        db.close()

        return datasAsetUnitist
    }

    @SuppressLint("Range")
    fun fetchAllEstateList(): List<EstateModel> {
        val datasEstateList = mutableListOf<EstateModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_ESTATE}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val est = it.getString(it.getColumnIndex("est"))
                val dataEstate = EstateModel(
                    id,
                    est ,
                )
                datasEstateList.add(dataEstate)
            }
        }

//        db.close()

        return datasEstateList.sortedBy { it.est }
    }

    @SuppressLint("Range")
    fun fetchAllKodeUnit(): List<KodeUnitModel> {
        val datasKodeUnitList = mutableListOf<KodeUnitModel>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.DB_TAB_KODE_UNIT}", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndex("id"))
                val kode = it.getString(it.getColumnIndex("kode"))
                val est = it.getString(it.getColumnIndex("est"))
                val type = it.getString(it.getColumnIndex("type"))
                val no_unit = it.getString(it.getColumnIndex("no_unit"))
                val tahun = it.getInt(it.getColumnIndex("tahun"))
                val dataUnit = KodeUnitModel(
                    id,
                    kode ,
                    est ,
                    type,
                    no_unit,
                    tahun,
                )
                datasKodeUnitList.add(dataUnit)
            }
        }

//        db.close()

        return datasKodeUnitList
    }

}