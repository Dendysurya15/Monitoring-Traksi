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
        const val DB_TAB_LAPORAN_KERUSAKAN = "laporan_kerusakan"

        const val DB_ID = "id"
        //jenisUnit

        const val DB_NAMA_UNIT = "nama_unit"

        //kode_unit
        const val DB_NAMA = "nama"
        const val DB_UNIT_KERJA = "unit_kerja"
        const val DB_TYPE_UNIT = "type_unit"
        const val DB_ID_JENIS_UNIT = "id_jenis_unit"

        //laporan_kerusakan
        const val DB_TANGGAL_PEMBUATAN = "tanggal_pembuatan"
        const val DB_UNIT = "unit"
        const val DB_LOKASI_UNIT = "lokasi_unit"
        const val DB_NAMA_OPERATOR = "nama_operator"
        const val DB_NAMA_POLISI = "nomor_polisi"
        const val DB_KERUSAKAN = "kerusakan"
        const val DB_FOTO = "foto"
        const val DB_KOMENTAR = "komentar"
        const val DB_APP_VER = "app_version"

    }

    private val createTableJenisUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_JENIS_UNIT (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA_UNIT VARCHAR)"

    private val createTableKodeUnit = "CREATE TABLE IF NOT EXISTS $DB_TAB_KODE_UNIT (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_NAMA VARCHAR, " +
            "$DB_UNIT_KERJA VARCHAR, " +
            "$DB_TYPE_UNIT VARCHAR, " +
            "$DB_ID_JENIS_UNIT INTEGER)"

    private val createTableLaporanKerusakan = "CREATE TABLE IF NOT EXISTS $DB_TAB_LAPORAN_KERUSAKAN (" +
            "$DB_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$DB_TANGGAL_PEMBUATAN VARCHAR, " +
            "$DB_UNIT INTEGER, " +
            "$DB_LOKASI_UNIT VARCHAR, " +
            "$DB_NAMA_OPERATOR VARCHAR, " +
            "$DB_NAMA_POLISI VARCHAR, " +
            "$DB_KERUSAKAN VARCHAR, " +
            "$DB_FOTO VARCHAR, " +
            "$DB_KOMENTAR VARCHAR, " +
            "$DB_APP_VER VARCHAR)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTableJenisUnit)
        db.execSQL(createTableKodeUnit)
        db.execSQL(createTableLaporanKerusakan)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        if (oldVersion < 2) {
//            db.execSQL(createTableSixHours)
//        }
    }

}