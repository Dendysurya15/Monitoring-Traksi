package com.cbi.monitoring_traksi.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import kotlinx.android.synthetic.main.activity_main.iblogout
import kotlinx.android.synthetic.main.activity_main.loadingMain
import kotlinx.android.synthetic.main.activity_main.mbTambahMonitoring

class MainActivity : AppCompatActivity() {
    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel
    var completedObserversCount = 0
    val dataJenisUnitList = mutableListOf<Map<String, Any>>()
    val dataUnitKerjaList = mutableListOf<Map<String, Any>>()
    val dataKodeUnitList = mutableListOf<Map<String, Any>>()
    private var dataMapJenisUnitArray: Array<Map<String, Any>>? = null
    private var dataMapUnitKerjaArray: Array<Map<String, Any>>? = null
    private var dataMapKodeUnitArray: Array<Map<String, Any>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewModel()

        loadingMain.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingMain)

        prefManager = PrefManager(this)
        if (prefManager!!.isFirstTimeLaunch) {
            handleSynchronizeData()

        } else {
            AppUtils.closeLoadingLayout(loadingMain)

        }
        clickAny()
    }

    private fun clickAny(){



        mbTambahMonitoring.setOnClickListener {

            val intent = Intent(this, FormLaporanP2HLayoutPertanyaanActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        iblogout.setOnClickListener {
            AlertDialogUtility.withTwoActions(
                this,
                getString(R.string.yes),
                getString(R.string.caution),
                getString(R.string.desc_confirm3),
                "warning.json"
            ) {
                loadingMain.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingMain)

                prefManager!!.isFirstTimeLaunch = true
                prefManager!!.lastUpdate = ""
                prefManager!!.session = false
                prefManager!!.email = ""
                prefManager!!.password = ""
                prefManager!!.remember = false


                unitViewModel.deleteDataJenisUnit()
                unitViewModel.deleteDataKodeUnit()
                unitViewModel.deleteDataUnitKerja()


                val intent = Intent(this, SplashScreenActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }

    }
//    private fun checkObserversCompletedAndMoveActivity() {
//
//        completedObserversCount++
//
//        if (completedObserversCount == 3) {
//
//            val intent = Intent(this, FormLaporanP2HLayoutPertanyaanActivity::class.java)
//            intent.putExtra("dataMapJenisUnitArray", dataMapJenisUnitArray)
//            intent.putExtra("dataMapUnitKerjaArray", dataMapUnitKerjaArray)
//            intent.putExtra("dataMapKodeUnitArray", dataMapKodeUnitArray)
//
//            startActivity(intent)
//            finishAffinity()
//        }
//    }
    private fun initViewModel() {
        unitViewModel = ViewModelProvider(
            this,
            UnitViewModel.Factory(application, UnitRepository(this))
        )[UnitViewModel::class.java]

    }

    private fun handleSynchronizeData(arg: String? = "") {
        if (AppUtils.checkConnectionDevice(this)) {

            if (arg!!.isNotEmpty()) {
                AlertDialogUtility.withTwoActions(
                    this,
                    getString(R.string.yes),
                    getString(R.string.caution),
                    getString(R.string.desc_confirm2),
                    "warning.json"
                ) {
                    loadingMain.visibility = View.VISIBLE
                    AppUtils.showLoadingLayout(this, window, loadingMain)
                    synchronizeData(arg)
                }
            } else {
             synchronizeData()
            }

        } else {

            AlertDialogUtility.withSingleAction(
                this,
                getString(R.string.try_again),
                getString(R.string.failed),
                getString(R.string.error_volley3),
                "error.json"
            ) {
                handleSynchronizeData(arg)
            }
        }
    }

    private fun synchronizeData(arg: String? = "") {
        AppUtils.synchronizeDBSqlite(
            this,
            prefManager!!,
            unitViewModel,
            loadingMain,
            arg
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}