package com.cbi.monitoring_traksi.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        const val DATABASE_NAME = "ssms_monitoring_traksi"
        const val DATABASE_VERSION = 2
        const val DB_ARCHIVE = "archive"

        const val DB_TAB_JENIS_UNIT = "jenis_unit"
        const val DB_TAB_KODE_UNIT = "kode_unit"
        const val DB_TAB_ASET_UNIT = "aset_unit"
        const val DB_TAB_LIST_PERTANYAAN = "list_pertanyaan"
        const val DB_TAB_LAPORAN_P2H = "laporan_p2h"
        const val DB_TAB_ESTATE = "est"


        const val DB_ID = "id"

        //jenisUnit
        const val DB_NAMA_UNIT = "nama_unit"
        const val DB_JENIS_FORM_P2H = "jenis_form_p2h"
        const val DB_LIST_PERTANYAAN = "list_pertanyaan"

        //kode_unit
        const val DB_KODE = "kode"
        const val DB_EST = "est"
        const val DB_TYPE = "type"
        const val DB_NO_UNIT = "no_unit"
        const val DB_TAHUN = "tahun"

        //aset_kerja
        const val DB_NAMA_ASET = "nama_aset"

        //list pertanyaan
        const val DB_NAMA_PERTANYAAN = "nama_pertanyaan"
        const val DB_KONDISI_MESIN = "kondisi_mesin"


        //laporan_p2h
        const val DB_JENIS_UNIT = "jenis_unit"
        const val DB_ASET_UNIT = "aset_unit"
        const val DB_LOKASI_KERJA = "lokasi_kerja"
        const val DB_KODE_TYPE_NO_UNIT = "kode_type_no_unit"
        const val DB_TANGGAL_UPLOAD = "tanggal_upload"
        const val DB_LAT = "lat"
        const val DB_LON = "lon"
        const val DB_USER = "user"
        const val DB_FOTO_UNIT = "foto_unit"
        const val DB_STATUS_UNIT_BEROPERASI = "status_unit_beroperasi"
        const val DB_KERUSAKAN_UNIT = "kerusakan_unit"
        const val DB_UPLOADED_TIME = "uploaded_time"
        const val DB_APP_VERSION = "app_version"
    }

    private val createTableJenisUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_JENIS_UNIT (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_UNIT VARCHAR, " +
            "$DB_KODE VARCHAR, " +
            "$DB_JENIS_FORM_P2H VARCHAR, " +
            "$DB_LIST_PERTANYAAN VARCHAR)"

    private val createTableEstate = "CREATE TABLE IF NOT EXISTS $DB_TAB_ESTATE (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_EST VARCHAR)"

    private val createTableAsetUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_ASET_UNIT (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_ASET VARCHAR)"

    private val createTableKodeUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_KODE_UNIT (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_KODE VARCHAR, " +
            "$DB_EST VARCHAR, " +
            "$DB_TYPE VARCHAR, " +
            "$DB_NO_UNIT VARCHAR, " +
            "$DB_TAHUN INTEGER) "


    private val createTableItemPertanyaan = "CREATE TABLE IF NOT EXISTS $DB_TAB_LIST_PERTANYAAN (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_PERTANYAAN VARCHAR, " +
            "$DB_KONDISI_MESIN VARCHAR)"


    private val createTableLaporanP2H = "CREATE TABLE IF NOT EXISTS $DB_TAB_LAPORAN_P2H (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_JENIS_UNIT VARCHAR, " +
            "$DB_ASET_UNIT VARCHAR, " +
            "$DB_KODE_TYPE_NO_UNIT VARCHAR, " +
            "$DB_LOKASI_KERJA VARCHAR, " +
            "$DB_TANGGAL_UPLOAD VARCHAR, " +
            "$DB_LAT VARCHAR, " +
            "$DB_LON VARCHAR, " +
            "$DB_USER INTEGER, " +
            "$DB_FOTO_UNIT VARCHAR, " +
            "$DB_STATUS_UNIT_BEROPERASI VARCHAR, " +
            "$DB_KERUSAKAN_UNIT VARCHAR, " +
            "$DB_APP_VERSION VARCHAR, " +
            "$DB_UPLOADED_TIME VARCHAR, " +
            "$DB_ARCHIVE INTEGER)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTableJenisUnit)
        db.execSQL(createTableKodeUnit)
        db.execSQL(createTableAsetUnit)
        db.execSQL(createTableItemPertanyaan)
        db.execSQL(createTableLaporanP2H)
        db.execSQL(createTableEstate)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        if (oldVersion < 2) {
//            db.execSQL(createTableSixHours)
//        }
    }

}