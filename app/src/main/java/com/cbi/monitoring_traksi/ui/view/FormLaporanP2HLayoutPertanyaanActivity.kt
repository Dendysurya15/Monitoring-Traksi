package com.cbi.monitoring_traksi.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.AppUtils.handleListPertanyaanDropdownArray
import com.cbi.monitoring_traksi.utils.PrefManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import es.dmoral.toasty.Toasty

import kotlinx.android.synthetic.main.activity_form_informasi_unit.etJenisUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etKodeUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etNamaOperator
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etTanggalPeriksa
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etTypeUnit
import kotlinx.android.synthetic.main.activity_form_informasi_unit.etUnitKerja
import kotlinx.android.synthetic.main.activity_form_informasi_unit.mbPrevForm1

import kotlinx.android.synthetic.main.activity_form_layout_form_p2h.FormLayout1
import kotlinx.android.synthetic.main.activity_form_layout_form_p2h.FormLayout2
import kotlinx.android.synthetic.main.activity_form_layout_form_p2h.FormLayout3
import kotlinx.android.synthetic.main.activity_form_layout_form_p2h.FormLayout4
import kotlinx.android.synthetic.main.activity_form_layout_form_p2h.FormLayout5
import kotlinx.android.synthetic.main.activity_form_layout_form_p2h.FormLayout6
import kotlinx.android.synthetic.main.activity_form_layout_form_p2h.loadingFetchingData
import kotlinx.android.synthetic.main.activity_main.loadingMain
import kotlinx.android.synthetic.main.layout_dropdown_hasil_periksa.view.etTemplateDropdown
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.view.etKomentar
import kotlinx.android.synthetic.main.layout_pertanyaan.view.etHasilPeriksa
import kotlinx.android.synthetic.main.layout_pertanyaan.view.layout_komentar_foto
//import kotlinx.android.synthetic.main.layout_pertanyaan.view.etHasilPeriksa
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormLaporanP2HLayoutPertanyaanActivity : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel

    private var currentFormIndex: Int = 0
    val dataJenisUnitList = mutableListOf<Map<String, Any>>()
    val dataUnitKerjaList = mutableListOf<Map<String, Any>>()
    val pertanyaanList = mutableListOf<Map<String, Any>>()
    val dataKodeUnitList = mutableListOf<Map<String, Any>>()
    val dataPertanyaanList = mutableListOf<Map<String, Any>>()
    val pertanyaanPerPage = mutableMapOf<Int, MutableList<String>>()
    val pertanyaanIds: MutableList<String> = mutableListOf()
    private var dataMapJenisUnitArray: Array<Map<String, Any>>? = null
    private var dataMapUnitKerjaArray: Array<Map<String, Any>>? = null
    private var dataMapListPertanyaanArray: Array<Map<String, Any>>? = null
    private var dataMapKodeUnitArray: Array<Map<String, Any>>? = null
    private var dataMapPertanyaanArray: Array<Map<String, Any>>? = null
    private var adapterJenisUnitItems: ArrayAdapter<String>? = null
    val viewsArray = ArrayList<View>()
    var pertanyaanPerJenisUnit: MutableMap<String, Array<String>> = mutableMapOf()
    lateinit var ListPertanyaanStr: Array<String>

    private val formLayouts: Array<View> by lazy {
        arrayOf(FormLayout1, FormLayout2, FormLayout3, FormLayout4, FormLayout5, FormLayout6)
    }

    private val formLayoutInfoUnit: Array<View> by lazy {
        arrayOf(FormLayout1)
    }
    val formLayoutsPertanyaan = mutableListOf<View>()
    val LayoutsPertanyaanPerPage = mutableListOf<View>()

    @SuppressLint("DiscouragedApi", "ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_layout_form_p2h)
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
                recordMap["jenis"] = record.jenis
                recordMap["list_pertanyaan"] = record.list_pertanyaan

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

        unitViewModel.pertanyaanBasedOnJenisUnitList.observe(this) { data ->
            pertanyaanList.clear()
            pertanyaanPerPage.clear()

            data.forEach { record ->
                val recordMap = mutableMapOf<String, Any>()
                recordMap["id"] = record.id
                recordMap["nama_pertanyaan"] = record.nama_pertanyaan
                recordMap["kondisi_mesin"] = record.kondisi_mesin

                pertanyaanList.add(recordMap)
            }

            dataMapListPertanyaanArray = pertanyaanList.toTypedArray()

            val pertanyaanMap = pertanyaanList.groupBy { it["kondisi_mesin"] }

            val matiList = pertanyaanMap["mati"] ?: emptyList()
            val hidupList = pertanyaanMap["hidup"] ?: emptyList()

            val sortedArray = (matiList + hidupList).toTypedArray()

            val sortedPertanyaanNames = sortedArray.map { it["nama_pertanyaan"] }
            val sortedPertanyaanNamesArray = sortedPertanyaanNames.toTypedArray()


            val minBatch = 10
            var batchCount = 0
            var remainingQuestions = sortedPertanyaanNamesArray.size

            while (remainingQuestions > 0) {
                remainingQuestions -= minBatch
                batchCount++
            }

            var resetCount = 1
            var inc = 0


            for (item in sortedPertanyaanNamesArray) {
                // Store to new array with key inc
                if (resetCount > minBatch) {
                    resetCount = 1
                    inc++
                }

                if (!pertanyaanPerPage.containsKey(inc)) {
                    pertanyaanPerPage[inc] = mutableListOf()
                }
                pertanyaanPerPage[inc]?.add(item.toString())
                resetCount++
            }


            val layout = findViewById<ConstraintLayout>(R.id.parentFormP2H) // Assuming you have a parent layout to add the duplicates to
            for (i in 0 until batchCount) {

                val includedLayout =
                    layoutInflater.inflate(R.layout.activity_form_base_layout_pertanyaan, null)
                includedLayout.id = View.generateViewId() // Generate unique id for each duplicate
                includedLayout.visibility = View.GONE
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
                includedLayout.layoutParams = layoutParams

                val textViewQuestion = includedLayout.findViewById<TextView>(R.id.textTitleTest)
                textViewQuestion.text = "Form page ke-${i + 1}"

                //jika form sudah berada di page terakhir visible button save
                if (i == (batchCount - 1)){
                    val mbSaveFormP2H = includedLayout.findViewById<TextView>(R.id.mbSaveFormP2H)
                    mbSaveFormP2H.visibility = View.VISIBLE
                }else{
                    val mbButtonForNext = includedLayout.findViewById<TextView>(R.id.mbButtonNext)
                    mbButtonForNext.visibility = View.VISIBLE
                    mbButtonForNext.text = "Next"
                }

                val pertanyaanThisPage = pertanyaanPerPage[i]

                if (pertanyaanThisPage is List<*>) {
                    val containerPertanyaan = includedLayout.findViewById<LinearLayout>(R.id.listPertanyaanContainer)
                    for (j in 0 until pertanyaanThisPage.size) {

                        val layoutPertanyaan = layoutInflater.inflate(R.layout.layout_pertanyaan, null) as ConstraintLayout
                        layoutPertanyaan.id = j

                        val textTitlePertanyaan = layoutPertanyaan.findViewById<TextView>(R.id.textPertanyaan)
                        textTitlePertanyaan.visibility = View.VISIBLE
                        textTitlePertanyaan.text = pertanyaanThisPage[j]

                        val test = layoutPertanyaan.findViewById<ConstraintLayout>(R.id.etHasilPeriksa)
                        test.visibility = View.VISIBLE
                        val etTemplateDropdown = layoutPertanyaan.findViewById<AutoCompleteTextView>(R.id.etTemplateDropdown)

                        val dropdownOptions = arrayOf("Sudah dicek", "Belum dicek", "Sudah dicek, tetapi ada perbaikan")
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dropdownOptions)
                        etTemplateDropdown.setAdapter(adapter)

                        etTemplateDropdown.setText("Sudah dicek", false)

                        val defaultPosition = adapter.getPosition("Sudah dicek")
                        etTemplateDropdown.setSelection(defaultPosition)

                        layoutPertanyaan.etHasilPeriksa.etTemplateDropdown.setOnItemClickListener { _, _, position, _ ->
                        val selectedItem = dropdownOptions[position]
                            var komentarText = ""
                        if (selectedItem == "Sudah dicek, tetapi ada perbaikan") {
                            layoutPertanyaan.layout_komentar_foto.visibility = View.VISIBLE
                            layoutPertanyaan.layout_komentar_foto.etKomentar.setText(komentarText)
                        } else {
                            layoutPertanyaan.layout_komentar_foto.visibility = View.GONE
                            komentarText = layoutPertanyaan.etKomentar.text.toString()
                            layoutPertanyaan.layout_komentar_foto.etKomentar.setText("")

                        }
                    }


                        // Create layout parameters for layoutPertanyaan
                        val params = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.MATCH_PARENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )

                        if (j == pertanyaanThisPage.size - 1) { // Check if it's the last item
                            params.bottomMargin = (200 * resources.displayMetrics.density).toInt() // Convert 16dp to pixels
                        }

                        layoutPertanyaan.layoutParams = params

                        containerPertanyaan.addView(layoutPertanyaan)
                    }
                }

                layout.addView(includedLayout)
                formLayoutsPertanyaan.add(includedLayout)


            }

            handleClicksForm()
        }


        etTanggalPeriksa.isEnabled = false
        etTanggalPeriksa.inputType = InputType.TYPE_NULL
        val currentDate = getCurrentDate()
        etTanggalPeriksa.setText(currentDate)

        prefManager = PrefManager(this)


        val nextButton = findViewById<Button>(resources.getIdentifier("mbNextForm0", "id", packageName))
        nextButton.setOnClickListener {
            if (formLayoutsPertanyaan.isEmpty()){
                    displayToasty(this,"Harap memilih jenis unit terlebih dahulu")
            }else{
                toggleFormVisibility(0)
            }
        }


        mbPrevForm1.setOnClickListener {
            val intent: Intent
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }


    }

    private fun displayToasty(context: Context, message: String){
        Toasty.success(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleClicksForm(){


        formLayoutsPertanyaan.forEachIndexed { index, layout ->
            val materialButtonNext = layout.findViewById<MaterialButton>(R.id.mbButtonNext)
            val materialButtonPrev = layout.findViewById<MaterialButton>(R.id.mbButtonPrev)
            val mbSaveFormP2H = layout.findViewById<MaterialButton>(R.id.mbSaveFormP2H)

            materialButtonNext.setOnClickListener {
//                logDropdownValues(layout as ConstraintLayout)
                toggleFormVisibility(index + 1)
            }

            materialButtonPrev.setOnClickListener {
//                logDropdownValues(layout as ConstraintLayout)
                toggleFormVisibility(index - 1)
            }

            mbSaveFormP2H.setOnClickListener {

//                logDropdownValues(layout as ConstraintLayout)
                displayToasty(this, "Sudah di submit gan")



                val jawabanUserPerPage = mutableMapOf<Int, MutableList<String>>()

                formLayoutsPertanyaan.forEachIndexed { index, layout ->

                    val containerPertanyaan = layout.findViewById<LinearLayout>(R.id.listPertanyaanContainer)
                    val selectedValues = mutableListOf<String>()
                    for (i in 1 until containerPertanyaan.childCount) {
                        val layoutPertanyaan = containerPertanyaan.getChildAt(i) as ConstraintLayout
                        val etTemplateDropdown = layoutPertanyaan.findViewById<AutoCompleteTextView>(R.id.etTemplateDropdown)
                        val selectedValue = etTemplateDropdown.text.toString()

                        selectedValues.add(selectedValue)
                    }

                    jawabanUserPerPage[index] = selectedValues
                }


                jawabanUserPerPage.forEach { (index, values) ->
                    Log.d("jawabanUserPerPage", "Page $index: $values")
                }

            }

        }

    }


//    private fun logDropdownValues(layout: ConstraintLayout) {
//        val containerPertanyaan = layout.findViewById<LinearLayout>(R.id.listPertanyaanContainer)
//
//        for (i in 1 until containerPertanyaan.childCount) {
//            val layoutPertanyaan = containerPertanyaan.getChildAt(i) as ConstraintLayout
//            val etTemplateDropdown = layoutPertanyaan.findViewById<AutoCompleteTextView>(R.id.etTemplateDropdown)
//            val selectedValue = etTemplateDropdown.text.toString()
//
//
//            Log.d("DropdownValues", "Page ${layout.id}: Pertanyaan $i -> $selectedValue")
//        }
//    }
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
            val listPertanyaanJenisUnit = data.map { it["list_pertanyaan"] as? String }.filterNotNull()

            val idNamaUnitArray = idUnitList.toTypedArray()
            val namaUnitArray = namaUnitList.toTypedArray()
            val listPertanyaanJenisUnitArray = listPertanyaanJenisUnit.toTypedArray()
            adapterJenisUnitItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,
                namaUnitArray
            )
            etJenisUnit.setAdapter(adapterJenisUnitItems)

//            etJenisUnit.setOnTouchListener { _, event ->
//                if (event.action == MotionEvent.ACTION_UP) {
//                    etJenisUnit.showDropDown()
//                }
//                false
//            }


            etJenisUnit.setOnItemClickListener { parent, _, position, _ ->
                AppUtils.hideKeyboard(this)
                val pilJenisUnit = parent.getItemAtPosition(position).toString()
                val idPilJenisUnit = idNamaUnitArray[position]

                var ListPertanyaanIdJenisUnit: String? = null
                ListPertanyaanIdJenisUnit = listPertanyaanJenisUnitArray[position]

                val values = handleListPertanyaanDropdownArray(ListPertanyaanIdJenisUnit)

                ListPertanyaanStr = values.toTypedArray()

                unitViewModel.loadDataListPertanyaanBasedOnJenisUnit(ListPertanyaanStr)

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


        formLayoutInfoUnit.forEach { it.visibility = View.GONE }
        formLayoutsPertanyaan.forEach { it.visibility = View.GONE }
//        LayoutsPertanyaanPerPage.forEach { it.visibility = View.GONE }


        if (nextFormIndex >= 0 && nextFormIndex < formLayoutsPertanyaan.size) {

//            currentLayout = "Layout " +  formLayoutsPertanyaan[nextFormIndex].toString()
//
//
            formLayoutsPertanyaan[nextFormIndex].visibility = View.VISIBLE
//



//            val currentFormIndex = nextFormIndex
//
//            val linearLayout = findViewById<LinearLayout>(R.id.linScrollViewPage1) // Get reference to LinearLayout
//
//            var j = 1
//            var keyExists = true
//            while (keyExists ) {
//                val key = "form_${currentFormIndex-1}_$j"
//
//                val resourceId = resources.getIdentifier(key, "string", packageName)
//                if (resourceId != 0) {
//
//                    val value = getString(resourceId)
//
//
//                    val includeLayout = layoutInflater.inflate(R.layout.layout_pertanyaan, linearLayout, false) // Inflate the layout
//
//                    val textPertanyaan = includeLayout.findViewById<TextView>(R.id.textPertanyaan)
//
//                    // Set text for the TextView
//                    textPertanyaan.text = "Your text goes here"
//

//
//                    linearLayout.addView(includeLayout)
//
//
//
//
////                    val pertanyaanLayoutId = resources.getIdentifier("pertanyaan${currentFormIndex-1}_$j", "id", packageName)
////
////                    pertanyaanIds.add("pertanyaan${currentFormIndex-1}_$j")
////                    val pertanyaanLayout = findViewById<View>(pertanyaanLayoutId)
////                    pertanyaanLayout.visibility = View.VISIBLE
////                    val textViewPertanyaan = pertanyaanLayout.findViewById<TextView>(R.id.textPertanyaan)
////                    textViewPertanyaan.text = value
////
////                    val dropdownOptions = arrayOf("Sudah dicek", "Belum dicek", "Sudah dicek, tetapi ada perbaikan")
////                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, dropdownOptions)
////                    pertanyaanLayout.etHasilPeriksa.etTemplateDropdown.setAdapter(adapter)
////
////
////                    var komentarText = ""
////
////                    pertanyaanLayout.etHasilPeriksa.etTemplateDropdown.setOnItemClickListener { _, _, position, _ ->
////                        val selectedItem = dropdownOptions[position]
////
////                        if (selectedItem == "Sudah dicek, tetapi ada perbaikan") {
////                            pertanyaanLayout.layout_komentar_foto.visibility = View.VISIBLE
////                            pertanyaanLayout.layout_komentar_foto.etKomentar.setText(komentarText)
////                        } else {
////                            pertanyaanLayout.layout_komentar_foto.visibility = View.GONE
////                            komentarText = layout_komentar_foto.etKomentar.text.toString()
////                            pertanyaanLayout.layout_komentar_foto.etKomentar.setText("")
////
////                        }
////                    }
//
//                    j++
//                } else {
//
//                    keyExists = false // Stop the inner loop
//                }
//            }
//


        }else if(nextFormIndex == -1){
            formLayoutInfoUnit[0].visibility = View.VISIBLE
        }else{
            displayToasty(this, "gak ada lagi bang")
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