package com.cbi.monitoring_traksi.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cbi.monitoring_traksi.data.model.JenisUnitModel
import com.cbi.monitoring_traksi.data.model.KodeUnitModel
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

    private val _dataJenisUnit = MutableLiveData<List<JenisUnitModel>>()

    private val _dataKodeUnit = MutableLiveData<List<KodeUnitModel>>()

    private val _dataUnitKerja = MutableLiveData<List<UnitKerjaModel>>()

    val dataJenisUnitList: LiveData<List<JenisUnitModel>> get() = _dataJenisUnit

    val dataKodeUnitList: LiveData<List<KodeUnitModel>> get() = _dataKodeUnit

    val dataUnitkerjaList: LiveData<List<UnitKerjaModel>> get() = _dataUnitKerja

    fun insertDataJenisUnit(
        id: Int,
        nama_unit: String,

    ) {
        viewModelScope.launch {
            try {
                val dataJenisUnit = JenisUnitModel(
                    id,
                    nama_unit,
                )
                val isInserted = traksiUnitRepository.insertDataJenisUnit(dataJenisUnit)
                Log.d("testing","isInserted gan")
                _insertResultJnsUnit.value = isInserted
            } catch (e: Exception) {
                e.printStackTrace()
                _insertResultJnsUnit.value = false
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
                Log.d("testing","isInserted gan")
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
                Log.d("testing","isInserted gan")
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