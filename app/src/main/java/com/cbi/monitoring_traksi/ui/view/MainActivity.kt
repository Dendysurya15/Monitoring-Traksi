package com.cbi.monitoring_traksi.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etJenisUnit
import kotlinx.android.synthetic.main.activity_login.idEmail
import kotlinx.android.synthetic.main.activity_login.idPassword
import kotlinx.android.synthetic.main.activity_login.loadingLogin
import kotlinx.android.synthetic.main.activity_login.mbLogin
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

        Log.d("testing", completedObserversCount.toString())

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

            //fetch data arrayAdapter untuk halaman selanjutnya/form
            unitViewModel.loadDataJenisUnit()
            unitViewModel.dataJenisUnitList.observe(this){data->
                data.forEach { record ->
                    val recordMap = mutableMapOf<String, Any>()
                    recordMap["id"] = record.id
                    recordMap["nama_unit"] = record.nama_unit

                    dataJenisUnitList.add(recordMap)
                }

                dataMapJenisUnitArray = dataJenisUnitList.toTypedArray()


                checkObserversCompletedAndMoveActivity()
            }

            unitViewModel.loadDataUnitKerja()
            unitViewModel.dataUnitkerjaList.observe(this) { data ->
                data.forEach { record ->
                    val recordMap = mutableMapOf<String, Any>()

                    // Populate the map with keys and values
                    recordMap["id"] = record.id
                    recordMap["nama_unit_kerja"] = record.nama_unit_kerja
                    recordMap["id_jenis_unit"] = record.id_jenis_unit

                    dataUnitKerjaList.add(recordMap)
                }

                dataMapUnitKerjaArray = dataUnitKerjaList.toTypedArray()



                checkObserversCompletedAndMoveActivity()
            }

            unitViewModel.loadDataKodeUnit()
            unitViewModel.dataKodeUnitList.observe(this) { data ->

                data.forEach { record ->
                    val recordMap = mutableMapOf<String, Any>()

                    // Populate the map with keys and values
                    recordMap["id"] = record.id
                    recordMap["nama_kode"] = record.nama_kode
                    recordMap["type_unit"] = record.type_unit
                    recordMap["id_unit_kerja"] = record.id_unit_kerja

                    dataKodeUnitList.add(recordMap)
                }

                dataMapKodeUnitArray = dataKodeUnitList.toTypedArray()
                checkObserversCompletedAndMoveActivity()
            }

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
    private fun checkObserversCompletedAndMoveActivity() {

        completedObserversCount++

        if (completedObserversCount == 3) {

            val intent = Intent(this, FormTambahMonitoringActivity::class.java)
            intent.putExtra("dataMapJenisUnitArray", dataMapJenisUnitArray)
            intent.putExtra("dataMapUnitKerjaArray", dataMapUnitKerjaArray)
            intent.putExtra("dataMapKodeUnitArray", dataMapKodeUnitArray)

            startActivity(intent)
            finishAffinity()
        }
    }
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