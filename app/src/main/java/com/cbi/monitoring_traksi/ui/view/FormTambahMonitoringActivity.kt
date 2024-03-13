package com.cbi.monitoring_traksi.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.JenisUnitModel
import com.cbi.monitoring_traksi.data.model.KodeUnitModel
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etJenisUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etKodeUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etTanggalPeriksa
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etTypeUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etUnitKerja
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
import kotlin.math.log

class FormTambahMonitoringActivity : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel

//    private var namaUnitArray: Array<String>? = null

    private var adapterJenisUnitItems: ArrayAdapter<String>? = null
    private val formLayouts: Array<View> by lazy {
        arrayOf(FormLayout1, FormLayout2, FormLayout3, FormLayout4, FormLayout5, FormLayout6)
    }
    @SuppressLint("DiscouragedApi", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_tambah_monitoring)

        etTanggalPeriksa.isEnabled = false
        etTanggalPeriksa.inputType = InputType.TYPE_NULL
        val currentDate = getCurrentDate()
        etTanggalPeriksa.setText(currentDate)

        initViewModel()
        prefManager = PrefManager(this)
        unitViewModel.loadDataJenisUnit()
        val dataMapJenisUnitArray = intent.getSerializableExtra("dataMapJenisUnitArray") as? Array<Map<String, Any>>
        val dataMapUnitKerjaArray = intent.getSerializableExtra("dataMapUnitKerjaArray") as? Array<Map<String, Any>>
        val dataMapKodeUnitArray = intent.getSerializableExtra("dataMapKodeUnitArray") as? Array<Map<String, Any>>

        //halaman pengisian informasi unit
        dataMapJenisUnitArray?.let { data ->
            val namaUnitList = data.map { it["nama_unit"] as? String }.filterNotNull()
            val idUnitList = data.map { it["id"] as? Int }.filterNotNull()
            val idNamaUnitArray = idUnitList.toTypedArray()
            val namaUnitArray = namaUnitList.toTypedArray()

            adapterJenisUnitItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                namaUnitArray
            )
            etJenisUnit.setAdapter(adapterJenisUnitItems)

            etJenisUnit.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    etJenisUnit.showDropDown()
                }
                false
            }

            etJenisUnit.setOnItemClickListener { parent, _, position, _ ->
                AppUtils.hideKeyboard(this)
                val pilJenisUnit = parent.getItemAtPosition(position).toString()
                val idPilJenisUnit = idNamaUnitArray[position]

                // Filter dataMapUnitKerjaArray based on the selected ID
                val filteredUnitKerjaList = dataMapUnitKerjaArray?.filter {
                    it["id_jenis_unit"] == idPilJenisUnit
                }
                val namaUnitKerjaArray = filteredUnitKerjaList?.mapNotNull { it["nama_unit_kerja"] as? String }?.toTypedArray()
                val idUnitKerjaArray = filteredUnitKerjaList?.mapNotNull { it["id"] as? Int }?.toTypedArray()

                etUnitKerja.setText("")
                etKodeUnit.setText("")
                etTypeUnit.setText("")

                // Populate the ArrayAdapter for etUnitKerja with the new data
                namaUnitKerjaArray?.let { unitKerjaArray ->
                    val adapterUnitKerjaItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unitKerjaArray)
                    etUnitKerja.setAdapter(adapterUnitKerjaItems)


                    etUnitKerja.setOnItemClickListener { _, _, position, _ ->
                        val pilUnitKerja = adapterUnitKerjaItems.getItem(position).toString()
                        val idPilUnitKerja = idUnitKerjaArray?.get(position)


                        val filteredKodeUnitList = dataMapKodeUnitArray?.filter {
                            it["id_unit_kerja"] == idPilUnitKerja
                        }
                        val namaKodeUnitArray = filteredKodeUnitList?.mapNotNull { it["nama_kode"] as? String }?.toTypedArray()
                        val typeUnitArray = filteredKodeUnitList?.mapNotNull { it["type_unit"] as? String }?.toTypedArray()
                        etKodeUnit.setText("")
                        etTypeUnit.setText("")

                        namaKodeUnitArray?.let { unitKerjaArray ->
                            val adapterKodeUnitItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unitKerjaArray)
                            etKodeUnit.setAdapter(adapterKodeUnitItems)

                            etKodeUnit.setOnItemClickListener { _, _, position, _ ->
                                val pilKodeUnit = adapterKodeUnitItems.getItem(position).toString()
                                etTypeUnit.setText(typeUnitArray?.get(position))

                            }
                        }
                    }
                }

            }
        }

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

    private fun initViewModel() {
        unitViewModel = ViewModelProvider(
            this,
            UnitViewModel.Factory(application, UnitRepository(this))
        )[UnitViewModel::class.java]

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