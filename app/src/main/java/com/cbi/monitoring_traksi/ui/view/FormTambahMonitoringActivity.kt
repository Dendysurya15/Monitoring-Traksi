package com.cbi.monitoring_traksi.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.JenisUnitModel
import com.cbi.monitoring_traksi.data.model.KodeUnitModel
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.PrefManager
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etJenisUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etTanggalPeriksa
import kotlinx.android.synthetic.main.activity_form_informasi_unit.mbPrevForm1
import kotlinx.android.synthetic.main.activity_form_saat_engine_hidup.mbSaveMonitoringUnit
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout1
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout2
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout3
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout4
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout5
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout6
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormTambahMonitoringActivity : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel

    private val formLayouts: Array<View> by lazy {
        arrayOf(FormLayout1, FormLayout2, FormLayout3, FormLayout4, FormLayout5, FormLayout6)
    }
    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_tambah_monitoring)

        etTanggalPeriksa.isEnabled = false
        etTanggalPeriksa.inputType = InputType.TYPE_NULL
        val currentDate = getCurrentDate()
        etTanggalPeriksa.setText(currentDate)

        
        val listArray = arrayOf("nais","naisbro")
        val adapterItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listArray)
        etJenisUnit.setAdapter(adapterItems)

        prefManager = PrefManager(this)
        unitViewModel = ViewModelProvider(this)[UnitViewModel::class.java]

//        unitViewModel.jnsUnitData.observe(this) { jnsUnitData ->
////            AppUtils.closeLoadingLayout(loadingReg)
//
////            val idRegArr = jnsUnitData.map { it.id }.toTypedArray()
////            val dataRegArr = jnsUnitData.map { it.data }.toTypedArray()
//            val jenisUnitArr = jnsUnitData.map { it.nama_unit }.toTypedArray()
//
//
//            Log.d("testing",jenisUnitArr.contentToString())

//            val adapterItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regArr)
//            val ddChooseReg = findViewById<AutoCompleteTextView>(R.id.ddChooseReg)
//            AppUtils.getSetDropdownHeight(window, ddChooseReg)
//            ddChooseReg.setAdapter(adapterItems)

//            ddChooseReg.setOnTouchListener { _, event ->
//                if (event.action == MotionEvent.ACTION_UP) {
//                    ddChooseReg.showDropDown()
//                }
//                false
//            }

//            ddChooseReg.setOnItemClickListener { parent, _, position, _ ->
//                reg = parent.getItemAtPosition(position).toString()
//                dataReg = dataRegArr[position]
//                idReg = idRegArr[position]
//            }
//        }




        //handle next button each page
        for (i in 0 until formLayouts.size - 1) {

            val nextButton = findViewById<Button>(resources.getIdentifier("mbNextForm${i + 1}", "id", packageName))
            nextButton.setOnClickListener {
                toggleFormVisibility(i + 2) // Show the next form
            }
        }

        //handle prev button each page
        for (i in 1 until formLayouts.size) {

            val prevButton = findViewById<Button>(resources.getIdentifier("mbPrevForm${i + 1}", "id", packageName))
            prevButton.setOnClickListener {
                toggleFormVisibility(i) // Show the previous form
            }
        }

        mbPrevForm1.setOnClickListener {
            val intent: Intent
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        mbSaveMonitoringUnit.setOnClickListener {
            showAlertDialog("Error", "Belum ada fitur ini gan.")
        }
    }

    private fun toggleFormVisibility(nextFormIndex: Int) {

        formLayouts.forEach { it.visibility = View.GONE }
        if (nextFormIndex in 1..formLayouts.size) {
            formLayouts[nextFormIndex - 1].visibility = View.VISIBLE
        }

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

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

}