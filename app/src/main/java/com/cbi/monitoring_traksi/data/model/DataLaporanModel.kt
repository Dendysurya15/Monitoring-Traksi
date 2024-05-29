package com.cbi.monitoring_traksi.data.model

import androidx.room.Ignore

data class DataLaporanModel(@Ignore val id: Int, val created_at: String, val id_laporan: Int, val id_pertanyaan: Int, val kondisi: String, val komentar:String, val foto:String)