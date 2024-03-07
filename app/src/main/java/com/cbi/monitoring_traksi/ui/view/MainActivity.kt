package com.cbi.monitoring_traksi.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import kotlinx.android.synthetic.main.activity_login.idEmail
import kotlinx.android.synthetic.main.activity_login.idPassword
import kotlinx.android.synthetic.main.activity_login.loadingLogin
import kotlinx.android.synthetic.main.activity_login.mbLogin
import kotlinx.android.synthetic.main.activity_main.loadingMain
import kotlinx.android.synthetic.main.activity_main.mbTambahMonitoring

class MainActivity : AppCompatActivity() {
    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewModel()

        loadingMain.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingMain)



        prefManager = PrefManager(this)

        Log.d("testing",prefManager!!.isFirstTimeLaunch.toString())
        if (prefManager!!.isFirstTimeLaunch) {
            handleSynchronizeData()
            Log.d("testing","masuk first time gan")
        } else {
            AppUtils.closeLoadingLayout(loadingMain)
            Log.d("testing","sudah gak first time gan")
        }
//
        clickButtonTambahMonitoring()
    }

    private fun clickButtonTambahMonitoring(){


        mbTambahMonitoring.setOnClickListener {

            val intent: Intent

            intent = Intent(this, FormTambahMonitoringActivity::class.java)

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

            Log.d("testing","gak ada internet gan")
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
        AppUtils.synchronizeDBJnsDanKodeUnit(
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