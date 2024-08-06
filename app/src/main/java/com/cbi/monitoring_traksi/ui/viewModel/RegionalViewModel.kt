package com.cbi.monitoring_traksi.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cbi.monitoring_traksi.data.model.RegionalModel
import com.cbi.monitoring_traksi.data.repository.RegionalRepository

class RegionalViewModel(application: Application) : AndroidViewModel(application) {
    private val qcRegRepository: RegionalRepository = RegionalRepository(application)

    private val _qcRegData = MutableLiveData<List<RegionalModel>>()
    val qcRegData: LiveData<List<RegionalModel>>
        get() = _qcRegData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    fun fetchData() {
        qcRegRepository.fetchData(
            onSuccess = { qcRegData ->
                _qcRegData.postValue(qcRegData)
            },
            onError = { error ->
                _error.postValue(error)
            }
        )
    }
}
