package com.cbi.monitoring_traksi.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.JenisUnitModel
import com.cbi.monitoring_traksi.data.model.KodeUnitModel
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.PrefManager

import kotlinx.android.synthetic.main.activity_form_5.mbSaveMonitoringUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etJenisUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etKodeUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etNamaOperator
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etTanggalPeriksa
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etTypeUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etUnitKerja
import kotlinx.android.synthetic.main.activity_form_informasi_unit.mbPrevForm1
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout1
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout2
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout3
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout4
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout5
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout6
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.loadingFetchingData
import kotlinx.android.synthetic.main.activity_main.loadingMain
import kotlinx.android.synthetic.main.layout_dropdown_hasil_periksa.view.etTemplateDropdown
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.etKomentar
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.view.etKomentar
import kotlinx.android.synthetic.main.layout_pertanyaan.etHasilPeriksa
import kotlinx.android.synthetic.main.layout_pertanyaan.layout_komentar_foto
import kotlinx.android.synthetic.main.layout_pertanyaan.textPertanyaan
import kotlinx.android.synthetic.main.layout_pertanyaan.view.etHasilPeriksa
import kotlinx.android.synthetic.main.layout_pertanyaan.view.layout_komentar_foto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

class FormTambahMonitoringActivity : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel

    private var currentFormIndex: Int = 0
    val dataJenisUnitList = mutableListOf<Map<String, Any>>()
    val dataUnitKerjaList = mutableListOf<Map<String, Any>>()
    val dataKodeUnitList = mutableListOf<Map<String, Any>>()
    private var dataMapJenisUnitArray: Array<Map<String, Any>>? = null
    private var dataMapUnitKerjaArray: Array<Map<String, Any>>? = null
    private var dataMapKodeUnitArray: Array<Map<String, Any>>? = null
    private var adapterJenisUnitItems: ArrayAdapter<String>? = null
    private val formLayouts: Array<View> by lazy {
        arrayOf(FormLayout1, FormLayout2, FormLayout3, FormLayout4, FormLayout5, FormLayout6)
    }
    @SuppressLint("DiscouragedApi", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_tambah_monitoring)
        initViewModel()
        loadingFetchingData.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingFetchingData)


        //fetch data untuk arrayAdapter
        unitViewModel.loadDataJenisUnit()
        unitViewModel.dataJenisUnitList.observe(this){data->
            data.forEach { record ->
                val recordMap = mutableMapOf<String, Any>()
                recordMap["id"] = record.id
                recordMap["nama_unit"] = record.nama_unit

                dataJenisUnitList.add(recordMap)
            }

            dataMapJenisUnitArray = dataJenisUnitList.toTypedArray()

            checkDataAvailability()
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

            checkDataAvailability()
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

            checkDataAvailability()
        }


        etTanggalPeriksa.isEnabled = false
        etTanggalPeriksa.inputType = InputType.TYPE_NULL
        val currentDate = getCurrentDate()
        etTanggalPeriksa.setText(currentDate)

        prefManager = PrefManager(this)

        //handle next button each page
        for (i in 0 until formLayouts.size - 1) {

            val nextButton = findViewById<Button>(resources.getIdentifier("mbNextForm${i + 1}", "id", packageName))
            nextButton.setOnClickListener {
                toggleFormVisibility(i + 2) // Show the next form
            }
        }

//        //handle prev button each page
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

            val jenisUnitValue = etJenisUnit.text.toString()
            val unitKerjaValue = etUnitKerja.text.toString()
            val kodeUnitValue = etKodeUnit.text.toString()
            val typeUnitValue = etTypeUnit.text.toString()
            val namaOperatorvalue = etNamaOperator.text.toString()


            Log.d("testing", jenisUnitValue)
            Log.d("testing", unitKerjaValue)
            Log.d("testing", kodeUnitValue)
            Log.d("testing", typeUnitValue)
            Log.d("testing", namaOperatorvalue)
        }
    }


    private fun checkDataAvailability(){

        if (dataMapUnitKerjaArray != null && dataMapJenisUnitArray != null && dataMapKodeUnitArray != null) {

            AppUtils.closeLoadingLayout(loadingFetchingData)
            setupDropdown()
        }
    }

    private fun setupDropdown(){
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

                        // Populate the ArrayAdapter for etKodeUnit with the new data
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
    }


    private fun toggleFormVisibility(nextFormIndex: Int) {

        formLayouts.forEach { it.visibility = View.GONE }


        if (nextFormIndex in 1..formLayouts.size) {

            val currentFormIndex = nextFormIndex


            var j = 1
            var keyExists = true
            while (keyExists ) {
                val key = "form_${currentFormIndex-1}_$j"

                val resourceId = resources.getIdentifier(key, "string", packageName)
                if (resourceId != 0) {

                    val value = getString(resourceId)

                    val pertanyaanLayoutId = resources.getIdentifier("pertanyaan${currentFormIndex-1}_$j", "id", packageName)

                    val pertanyaanLayout = findViewById<View>(pertanyaanLayoutId)
                    pertanyaanLayout.visibility = View.VISIBLE
                    val textViewPertanyaan = pertanyaanLayout.findViewById<TextView>(R.id.textPertanyaan)
                    textViewPertanyaan.text = value

                    val dropdownOptions = arrayOf("Sudah dicek", "Belum dicek", "Sudah dicek, tetapi ada perbaikan")
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dropdownOptions)
                    pertanyaanLayout.etHasilPeriksa.etTemplateDropdown.setAdapter(adapter)


                    var komentarText = ""

                    pertanyaanLayout.etHasilPeriksa.etTemplateDropdown.setOnItemClickListener { _, _, position, _ ->
                        val selectedItem = dropdownOptions[position]

                        if (selectedItem == "Sudah dicek, tetapi ada perbaikan") {
                            pertanyaanLayout.layout_komentar_foto.visibility = View.VISIBLE
                            pertanyaanLayout.layout_komentar_foto.etKomentar.setText(komentarText)
                        } else {
                            pertanyaanLayout.layout_komentar_foto.visibility = View.GONE
                            komentarText = layout_komentar_foto.etKomentar.text.toString()
                            pertanyaanLayout.layout_komentar_foto.etKomentar.setText("")

                        }
                    }

                    j++
                } else {

                    keyExists = false // Stop the inner loop
                }
            }
//

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