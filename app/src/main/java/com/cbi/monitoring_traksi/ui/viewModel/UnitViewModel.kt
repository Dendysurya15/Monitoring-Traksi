package com.cbi.monitoring_traksi.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cbi.monitoring_traksi.data.model.AsetUnitModel
import com.cbi.monitoring_traksi.data.model.EstateModel
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

    private val _insertResultAsetUnit = MutableLiveData<Boolean>()
    val insertResultAsetUnit: LiveData<Boolean> get() = _insertResultAsetUnit

    private val _insertResultLaporP2H = MutableLiveData<Boolean>()
    val insertResultLaporP2H: LiveData<Boolean> get() = _insertResultLaporP2H

    private val _insertQueryKeTableDataResult = MutableLiveData<Boolean>()
    val insertQueryKeTableData: LiveData<Boolean> get() = _insertQueryKeTableDataResult

    private val _insertResultPertanyaan = MutableLiveData<Boolean>()

    private val _insertResultListEstate = MutableLiveData<Boolean>()

    private val _last_id_laporp2h = MutableLiveData<Int>()

    val last_id_laporp2h: LiveData<Int> get() = _last_id_laporp2h
    val insertResultPertanyaan: LiveData<Boolean> get() = _insertResultPertanyaan

    val insertResultListEstate: LiveData<Boolean> get() = _insertResultListEstate

    private val _dataJenisUnit = MutableLiveData<List<JenisUnitModel>>()

    private val _dataAsetUnit = MutableLiveData<List<AsetUnitModel>>()

    private val _dataKodeUnit = MutableLiveData<List<KodeUnitModel>>()

    private val _dataEstate = MutableLiveData<List<EstateModel>>()

    private val _dataUnitKerja = MutableLiveData<List<UnitKerjaModel>>()

    private val _dataPertanyaan = MutableLiveData<Map<String, List<ItemPertanyaanModel>>>()
    

    val dataJenisUnitList: LiveData<List<JenisUnitModel>> get() = _dataJenisUnit

    val dataAsetUnitList: LiveData<List<AsetUnitModel>> get() = _dataAsetUnit

    val dataKodeUnitList: LiveData<List<KodeUnitModel>> get() = _dataKodeUnit

    val dataEstateList: LiveData<List<EstateModel>> get() = _dataEstate

    val dataUnitkerjaList: LiveData<List<UnitKerjaModel>> get() = _dataUnitKerja

    val pertanyaanBasedOnJenisUnitList: LiveData<Map<String, List<ItemPertanyaanModel>>> get() = _dataPertanyaan


    fun insertDataJenisUnit(
        id: Int,
        nama_unit: String,
        kode: String,
        jenis_form_p2h: String,
        list_pertanyaan: String,
    ) {
        viewModelScope.launch {
            try {
                val dataJenisUnit = JenisUnitModel(
                    id,
                    nama_unit,
                    kode,
                    jenis_form_p2h,
                    list_pertanyaan,
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


    fun insertListEstate(
        id: Int,
        est: String,
        id_reg: Int,
        ) {
        viewModelScope.launch {
            try {
                val dataEst = EstateModel(
                    id,
                    est,
                    id_reg
                )
                val isInserted = traksiUnitRepository.insertListEstate(dataEst)

                _insertResultListEstate.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultListEstate.value = false
            }
        }
    }
    fun insertDataKodeUnit(
         id: Int,
         kode: String,
         est : String,
         type: String,
         no_unit: String,
         tahun : Int,
        ) {
        viewModelScope.launch {
            try {
                val dataKodeUnit = KodeUnitModel(
                    id,
                    kode,
                    est,
                    type,
                    no_unit,
                    tahun,
                )
                val isInserted = traksiUnitRepository.insertDataKodeUnit(dataKodeUnit)

                _insertResultKodeUnit.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultKodeUnit.value = false
            }
        }
    }

    fun insertDataAsetUnit(
        id: Int,
        nama_aset: String,
    ) {
        viewModelScope.launch {
            try {
                val dataAsetUnit = AsetUnitModel(
                    id,
                    nama_aset
                )
                val isInserted = traksiUnitRepository.insertDataAsetUnit(dataAsetUnit)

                _insertResultAsetUnit.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultAsetUnit.value = false
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

    fun deleteDataAsetUnit() {
        viewModelScope.launch {
            traksiUnitRepository.deleteDataAsetUnit()
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

    fun loadDataAsetUnit() {
        viewModelScope.launch {
            val dataAsetUnit = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllAsetUnit()
            }
            _dataAsetUnit.value = dataAsetUnit
        }
    }

    fun loadDataListPertanyaanBasedOnJenisUnit(arrayHere: Array<String>, id: String) {
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
            aset_unit: String,
            kode_type_no_unit: String,
            lokasi_kerja: String,
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
                        aset_unit ,
                        kode_type_no_unit ,
                        lokasi_kerja ,
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



    fun loadDataKodeUnit() {
        viewModelScope.launch {
            val dataUnit = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllKodeUnit()
            }
            _dataKodeUnit.value = dataUnit
        }
    }

    fun loadDataListEstate() {
        viewModelScope.launch {
            val dataEstate = withContext(Dispatchers.IO) {
                traksiUnitRepository.fetchAllEstateList()
            }
            _dataEstate.value = dataEstate
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