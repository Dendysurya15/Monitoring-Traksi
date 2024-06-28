package com.cbi.monitoring_traksi.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cbi.monitoring_traksi.data.model.DataLaporanModel
import com.cbi.monitoring_traksi.data.model.JenisUnitModel
import com.cbi.monitoring_traksi.data.model.KodeUnitModel
import com.cbi.monitoring_traksi.data.model.ItemPertanyaanModel
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.data.model.UnitKerjaModel
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnitViewModel(application: Application, private val traksiUnitRepository: UnitRepository) : AndroidViewModel(application) {

    private val _insertResultJnsUnit = MutableLiveData<Boolean>()
    val insertResultJnsUnit: LiveData<Boolean> get() = _insertResultJnsUnit

    private val _insertResultKodeUnit = MutableLiveData<Boolean>()
    val insertResultKodeUnit: LiveData<Boolean> get() = _insertResultKodeUnit

    private val _insertResultUnitKerja = MutableLiveData<Boolean>()
    val insertResultUnitKerja: LiveData<Boolean> get() = _insertResultUnitKerja

    private val _insertResultLaporP2H = MutableLiveData<Boolean>()
    val insertResultLaporP2H: LiveData<Boolean> get() = _insertResultLaporP2H

    private val _insertQueryKeTableDataResult = MutableLiveData<Boolean>()
    val insertQueryKeTableData: LiveData<Boolean> get() = _insertQueryKeTableDataResult

    private val _insertResultPertanyaan = MutableLiveData<Boolean>()

    private val _last_id_laporp2h = MutableLiveData<Int>()

    val last_id_laporp2h: LiveData<Int> get() = _last_id_laporp2h
    val insertResultPertanyaan: LiveData<Boolean> get() = _insertResultPertanyaan

    private val _dataJenisUnit = MutableLiveData<List<JenisUnitModel>>()

    private val _dataKodeUnit = MutableLiveData<List<KodeUnitModel>>()

    private val _dataUnitKerja = MutableLiveData<List<UnitKerjaModel>>()

    private val _dataPertanyaan = MutableLiveData<Map<Int, List<ItemPertanyaanModel>>>()


    private val _dataLaporan = MutableLiveData<List<DataLaporanModel>>()

    val dataJenisUnitList: LiveData<List<JenisUnitModel>> get() = _dataJenisUnit

    val dataKodeUnitList: LiveData<List<KodeUnitModel>> get() = _dataKodeUnit

    val dataUnitkerjaList: LiveData<List<UnitKerjaModel>> get() = _dataUnitKerja

    val pertanyaanBasedOnJenisUnitList: LiveData<Map<Int, List<ItemPertanyaanModel>>> get() = _dataPertanyaan


    fun insertDataJenisUnit(
        id: Int,
        nama_unit: String,
        jenis: String,
        list_pertanyaan: String,
    ) {
        viewModelScope.launch {
            try {
                val dataJenisUnit = JenisUnitModel(
                    id,
                    nama_unit,
                    jenis,
                    list_pertanyaan ,
                )
                val isInserted = traksiUnitRepository.insertDataJenisUnit(dataJenisUnit)

                _insertResultJnsUnit.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultJnsUnit.value = false
            }
        }
    }


    fun insertDataPertanyaan(
        id: Int,
        nama_pertanyaan: String,
        kondisi_mesin: String,

        ) {
        viewModelScope.launch {
            try {
                val dataPertanyaan = ItemPertanyaanModel(
                    id,
                    nama_pertanyaan,
                    kondisi_mesin ,
                )
                val isInserted = traksiUnitRepository.insertDataPertanyaan(dataPertanyaan)

                _insertResultPertanyaan.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultPertanyaan.value = false
            }
        }
    }

    fun insertDataKodeUnit(
         id: Int,
         nama_kode: String,
         type_unit: String,
         id_unit_kerja: Int
        ) {
        viewModelScope.launch {
            try {
                val dataKodeUnit = KodeUnitModel(
                    id,
                    nama_kode,
                    type_unit,
                    id_unit_kerja
                )
                val isInserted = traksiUnitRepository.insertDataKodeUnit(dataKodeUnit)

                _insertResultKodeUnit.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultKodeUnit.value = false
            }
        }
    }

    fun insertDataUnitKerja(
        id: Int,
        nama_unit_kerja: String,
        id_jenis_unit: Int
    ) {
        viewModelScope.launch {
            try {
                val dataUnitKerja = UnitKerjaModel(
                    id,
                    nama_unit_kerja,
                    id_jenis_unit
                )
                val isInserted = traksiUnitRepository.insertDataUnitKerja(dataUnitKerja)

                _insertResultUnitKerja.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultUnitKerja.value = false
            }
        }
    }



    fun deleteDataJenisUnit() {
        viewModelScope.launch {
            traksiUnitRepository.deleteDataJenisUnit()
        }
    }


    fun deleteDataPertanyaan(){
        viewModelScope.launch {
            traksiUnitRepository.deleteDataPertanyaan()
        }
    }

    fun deleteDataKodeUnit() {
        viewModelScope.launch {
            traksiUnitRepository.deleteDataKodeUnit()
        }
    }

    fun deleteDataUnitKerja() {
        viewModelScope.launch {
            traksiUnitRepository.deleteDataUnitKerja()
        }
    }

    fun loadDataJenisUnit() {
        viewModelScope.launch {
            val dataUnit = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllJenisUnit()
            }
            _dataJenisUnit.value = dataUnit
        }
    }

    fun loadDataListPertanyaanBasedOnJenisUnit(arrayHere: Array<String>, id: Int) {
        viewModelScope.launch {
            val dataUnit = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllListPertanyaanBasedOnJenisUnit(arrayHere)
            }

            if (dataUnit.isNotEmpty()) {
                val dataWithCustomId = mapOf(id to dataUnit)
                _dataPertanyaan.value = dataWithCustomId
            }
        }
    }

    fun fetchLastIdLaporanP2HSQL() {
        viewModelScope.launch {
            try {
                _last_id_laporp2h.value = traksiUnitRepository.fetchLastIdlaporanP2H()
            } catch (e: Exception) {
                e.printStackTrace()
                _last_id_laporp2h.value = 0
            }
        }
    }
        fun pushDataToLaporanP2hSQL(
            id: Int? = 0,
            jenis_unit: String,
            unit_kerja: String,
            kode_unit: String,
            tanggal_upload: String,
            lat: String,
            lon: String,
            user : String,
            foto_unit : String,
            status_unit_beroperasi : String,
            kerusakan_unit : String,
            app_version : String,
            uploaded_time :String,
            archive : Int,
        ) {
            viewModelScope.launch {
                try {
                    val dataSubmitLaporan = LaporP2HModel(
                        id!!,
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
                        uploaded_time,
                        archive ,
                    )
                    val isInserted = traksiUnitRepository.insertLaporP2HToSQL(dataSubmitLaporan)

                    _insertResultLaporP2H.value = isInserted
                } catch (e: Exception) {
                    e.printStackTrace()
                    _insertResultLaporP2H.value = false
                }
            }
        }

    fun     pushToTableData(
        id: Int? = 0,
        created_at : String,
        id_laporan : Int,
        id_pertanyaan : Int,
        kondisi : String,
        komentar : String,
        foto : String,
    ) {
        viewModelScope.launch {
            try {
                val dataSubmit = DataLaporanModel(
                    id!!,
                    created_at,
                    id_laporan,
                    id_pertanyaan,
                    kondisi,
                    komentar,
                    foto
                )

                val isInserted = traksiUnitRepository.insertToTableData(dataSubmit)

                _insertQueryKeTableDataResult.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertQueryKeTableDataResult.value = false
            }
        }
    }

    fun loadDataUnitKerja() {
        viewModelScope.launch {
            val dataUnit = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllUnitKerja()
            }
            _dataUnitKerja.value = dataUnit
        }
    }




    fun loadDataKodeUnit() {
        viewModelScope.launch {
            val dataUnit = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllKodeUnit()
            }
            _dataKodeUnit.value = dataUnit
        }
    }
    fun loadDataTypeUnit() {
        viewModelScope.launch {
            val dataUnit = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllKodeUnit()
            }
            _dataKodeUnit.value = dataUnit
        }
    }

//        fun loadDataPertanyan() {
//            viewModelScope.launch {
//                val dataUnit = withContext(Dispatchers.IO) {
//                    traksiUnitRepository.fetchAllPertanyaan()
//                }
//                _dataPertanyaan.value = dataUnit
//            }
//        }
//


        @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val traksiUnitRepository: UnitRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UnitViewModel::class.java)) {
                return UnitViewModel(application, traksiUnitRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}