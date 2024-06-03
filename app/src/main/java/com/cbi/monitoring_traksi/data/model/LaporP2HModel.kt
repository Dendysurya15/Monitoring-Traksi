package com.cbi.monitoring_traksi.data.model

import androidx.room.Ignore

data class LaporP2HModel(@Ignore val id: Int, val id_jenis_unit: Int,val id_unit_kerja: Int, val id_kode_unit: Int, val tanggal_upload: String, val lat : String, val lon : String, val id_user: Int, val foto_unit : String, val status_pemeriksaan : String, val app_version : String )