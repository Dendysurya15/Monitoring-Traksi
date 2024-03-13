package com.cbi.monitoring_traksi.data.model

import androidx.room.Ignore

data class DataJenisUnit(
    @Ignore val id: Int,
    val nama_unit: String,
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

data class DataLaporanKerusakan(
    @Ignore val id: Int,
    val tanggal_pembuatan: String,
    val unit: Int,
    val lokasi_unit: String,
    val nama_operator: String,
    val nomor_polisi: String,
    val kerusakan: String,
    val foto: String,
    val komentar: String,
    val app_version: String,
)

