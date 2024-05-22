package com.cbi.monitoring_traksi.data.model

import androidx.room.Ignore

data class DataJenisUnit(
    @Ignore val id: Int,
    val nama_unit: String,
    val jenis: String,
    val list_pertanyaan: String,
)

data class DataKodeUnit(
    @Ignore val id: Int,
    val nama_kode: String,
    val type_unit: String,
    val id_unit_kerja: Int,
)

data class DataUnitKerja(
    @Ignore val id: Int,
    val nama_unit_kerja: String,
    val id_jenis_unit: Int,
)

data class DataItemPeratnyaan(
    @Ignore val id : Int,
    val nama_pertanyaan : String,
    val kondisi_mesin : String,
)

data class DataLaporan(
    @Ignore val id : Int,
    val id_laporan : Int,
    val id_pertanyaan : Int,
    val kondisi : String,
    val komentar : String,
    val foto : String,
)

data class DataLaporP2H(
    @Ignore val id : Int,
    val id_kode_unit: Int,
    val tanggal_upload : String,
    val lat : String,
    val lon : String,
    val id_user : Int,
    val foto_unit : String,
    val status : String,
    val app_version : String,
)
