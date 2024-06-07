package com.cbi.monitoring_traksi.ui.viewModel

import android.app.Application
import android.content.Context
import android.view.View
import android.view.Window
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.data.repository.HistoryRepositoryP2H
import com.cbi.monitoring_traksi.utils.PrefManager
import kotlinx.coroutines.launch

class HistoryP2HViewModel(
    application: Application,
    private val context: Context,
    private val historyRepo: HistoryRepositoryP2H,
    private val loadingView: View? = null,
    private val window: Window? = null,
    private val prefManager: PrefManager? = null,
) : AndroidViewModel(application) {

    private val _queryGetLaporanP2H = MutableLiveData<List<LaporP2HModel>>()
    val resultQueryDateLaporanP2H: LiveData<List<LaporP2HModel>> get() = _queryGetLaporanP2H
    fun loadLaporanP2HByDate(dateRequest : String) {
        viewModelScope.launch {
            try {
                _queryGetLaporanP2H.value = historyRepo.fetchByDateLaporanP2H(dateRequest)
            }catch (e:Exception){
                e.printStackTrace()
                _queryGetLaporanP2H.value = emptyList()
            }
        }
    }


    class Factory(
        private val application: Application,
        private val context: Context,
        private val historyRepo: HistoryRepositoryP2H,
        private val loadingView: View? = null,
        private val window: Window? = null,
        private val prefManager: PrefManager? = null,

        ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryP2HViewModel::class.java)) {
                return HistoryP2HViewModel(
                    application,
                    context,
                    historyRepo,
                    loadingView,
                    window,
                    prefManager,

                    ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}