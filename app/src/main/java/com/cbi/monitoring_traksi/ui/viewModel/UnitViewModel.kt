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
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import kotlinx.coroutines.launch

class UnitViewModel(application: Application, private val traksiUnitRepository: UnitRepository) : AndroidViewModel(application) {

    private val _insertResultJnsUnit = MutableLiveData<Boolean>()
    val insertResultJnsUnit: LiveData<Boolean> get() = _insertResultJnsUnit

    private val _insertResultKodeUnit = MutableLiveData<Boolean>()
    val insertResultKodeUnit: LiveData<Boolean> get() = _insertResultKodeUnit

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
         nama: String,
         unit_kerja: String,
         type_unit: String,
         id_jenis_unit: Int
        ) {
        viewModelScope.launch {
            try {
                val dataKodeUnit = KodeUnitModel(
                    id,
                    nama,
                    unit_kerja,
                    type_unit,
                    id_jenis_unit
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