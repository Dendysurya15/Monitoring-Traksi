package com.cbi.monitoring_traksi.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.repository.HistoryP2HRepository
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.adapter.UploadHistoryP2HAdapter
import com.cbi.monitoring_traksi.ui.viewModel.HistoryP2HViewModel
import com.cbi.monitoring_traksi.ui.viewModel.RegionalViewModel
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etKodeUnit
import kotlinx.android.synthetic.main.activity_main.loadingFetchingData
import kotlinx.android.synthetic.main.activity_main.loadingMain
import kotlinx.android.synthetic.main.activity_pilih_regional.loadingReg
import kotlinx.android.synthetic.main.activity_pilih_regional.mbSaveRegional

class PilihRegionalActivity : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    private lateinit var viewModel: RegionalViewModel

    private var idReg = 1
    private var reg = ""
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_regional)

        prefManager = PrefManager(this)
        loadingReg.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingReg)


        viewModel = ViewModelProvider(this)[RegionalViewModel::class.java]
        viewModel.qcRegData.observe(this) { qcRegData ->
            AppUtils.closeLoadingLayout(loadingReg)

            val idRegArr = qcRegData.map { it.id }.toTypedArray()
            val dataRegArr = qcRegData.map { it.data }.toTypedArray()
            val regArr = qcRegData.map { it.reg }.toTypedArray()

            val adapterItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regArr)
            val ddChooseReg = findViewById<AutoCompleteTextView>(R.id.etPilihRegional)
            AppUtils.getSetDropdownHeight(window, ddChooseReg)
            ddChooseReg.setAdapter(adapterItems)


            ddChooseReg.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    ddChooseReg.showDropDown()
                }
                false
            }

            ddChooseReg.setOnItemClickListener { parent, _, position, _ ->
                reg = parent.getItemAtPosition(position).toString()
                idReg = idRegArr[position]


            }
        }

        viewModel.error.observe(this) { error ->
            AppUtils.closeLoadingLayout(loadingReg)

            AlertDialogUtility.withSingleAction(
                this, "Ulang", "Peringatan", error, "warning.json"
            ) {
                loadingReg.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingMain)

                val intent = Intent(this, PilihRegionalActivity::class.java)
                startActivity(intent)
            }
        }

        viewModel.fetchData()

        mbSaveRegional.setOnClickListener {
            loadingReg.visibility = View.VISIBLE
            AppUtils.showLoadingLayout(this, window, loadingReg)
                if (reg.isEmpty()) {
                    Toasty.warning(
                        this,
                        "Silakan pilih regional terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    prefManager!!.idReg = idReg

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                }

        }

    }


    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        AlertDialogUtility.withTwoActions(
            this,
            "Ya",
            "Peringatan",
            "Apakah anda yakin menutup aplikasi?",
            "warning.json"
        ) {
            finishAffinity()
        }
    }

}