package com.cbi.monitoring_traksi.data.model

import androidx.room.Ignore

data class LaporP2HModel(
    @Ignore val id: Int,
    val jenis_unit: String,
    val unit_kerja: String,
    val kode_unit: String,
    val type_unit: String,
    val tanggal_upload: String,
    val lat: String,
    val lon: String,
    val user: String,
    val foto_unit: String,
    val status_unit_beroperasi: String,
    val kerusakan_unit: String,
    val app_version: String,
    val uploaded_time: String,
    val archive:Int
)