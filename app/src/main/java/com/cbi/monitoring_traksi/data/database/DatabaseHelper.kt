package com.cbi.monitoring_traksi.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        const val DATABASE_NAME = "ssms_monitoring_traksi"
        const val DATABASE_VERSION = 2

        const val DB_TAB_JENIS_UNIT = "jenis_unit"
        const val DB_TAB_KODE_UNIT = "kode_unit"
        const val DB_TAB_UNIT_KERJA = "unit_kerja"
        const val DB_TAB_LIST_PERTANYAAN = "list_pertanyaan"
        const val DB_TAB_LAPORAN_P2H = "laporan_p2h"
        const val DB_TAB_DATA = "data"

        const val DB_ID = "id"

        //jenisUnit
        const val DB_NAMA_UNIT = "nama_unit"
        const val DB_JENIS = "jenis"
        const val DB_LIST_PERTANYAAN = "list_pertanyaan"

        //kode_unit
        const val DB_NAMA_KODE = "nama_kode"
        const val DB_TYPE_UNIT = "type_unit"
        const val DB_ID_UNIT_KERJA = "id_unit_kerja"

        //unit_kerja
        const val DB_NAMA_UNIT_KERJA = "nama_unit_kerja"
        const val DB_ID_JENIS_UNIT = "id_jenis_unit"

        //list pertanyaan
        const val DB_NAMA_PERTANYAAN = "nama_pertanyaan"
        const val DB_KONDISI_MESIN = "kondisi_mesin"

        //data_laporan
        const val DB_CREATED_AT = "created_at"
        const val DB_ID_LAPORAN = "id_laporan"
        const val DB_ID_PERTANYAAN = "id_pertanyaaan"
        const val DB_KONDISI = "kondisi"
        const val DB_KOMENTAR = "komentar"
        const val DB_FOTO = "foto"


        //laporan_p2h
        const val DB_TANGGAL_UPLOAD = "tanggal_upload"
        const val DB_LAT = "lat"
        const val DB_LON = "lon"
        const val DB_ID_USER = "id_user"
        const val DB_FOTO_UNIT = "foto_unit"
        const val DB_STATUS_PEMERIKSAAN = "status"
        const val DB_APP_VERSION = "app_version"
    }

    private val createTableJenisUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_JENIS_UNIT (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_UNIT VARCHAR, " +
            "$DB_JENIS VARCHAR, " +
            "$DB_LIST_PERTANYAAN VARCHAR)"

    private val createTableKodeUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_KODE_UNIT (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_KODE VARCHAR, " +
            "$DB_TYPE_UNIT VARCHAR, " +
            "$DB_ID_UNIT_KERJA INTEGER)"

    private val createTableUnitKerja = "CREATE TABLE IF NOT EXISTS $DB_TAB_UNIT_KERJA (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_UNIT_KERJA VARCHAR, " +
            "$DB_ID_JENIS_UNIT INTEGER)"

    private val createTableItemPertanyaan = "CREATE TABLE IF NOT EXISTS $DB_TAB_LIST_PERTANYAAN (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_PERTANYAAN VARCHAR, " +
            "$DB_KONDISI_MESIN VARCHAR)"

    private val createTableData = "CREATE TABLE IF NOT EXISTS $DB_TAB_DATA (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_CREATED_AT VARCHAR, " +
            "$DB_ID_LAPORAN INTEGER, " +
            "$DB_ID_PERTANYAAN VARCHAR, " +
            "$DB_KONDISI VARCHAR, " +
            "$DB_KOMENTAR VARCHAR, " +
            "$DB_FOTO VARCHAR)"


    private val createTableLaporanP2H = "CREATE TABLE IF NOT EXISTS $DB_TAB_LAPORAN_P2H (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_ID_JENIS_UNIT INTEGER, " +
            "$DB_TANGGAL_UPLOAD VARCHAR, " +
            "$DB_LAT VARCHAR, " +
            "$DB_LON VARCHAR, " +
            "$DB_ID_USER INTEGER, " +
            "$DB_FOTO_UNIT VARCHAR, " +
            "$DB_STATUS_PEMERIKSAAN VARCHAR, " +
            "$DB_APP_VERSION VARCHAR)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTableJenisUnit)
        db.execSQL(createTableKodeUnit)
        db.execSQL(createTableUnitKerja)
        db.execSQL(createTableItemPertanyaan)
        db.execSQL(createTableData)
        db.execSQL(createTableLaporanP2H)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        if (oldVersion < 2) {
//            db.execSQL(createTableSixHours)
//        }
    }

}