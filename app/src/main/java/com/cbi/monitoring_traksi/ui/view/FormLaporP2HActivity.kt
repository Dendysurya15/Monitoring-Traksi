package com.cbi.monitoring_traksi.ui.view

//import kotlinx.android.synthetic.main.layout_pertanyaan.view.etHasilPeriksa


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.cbi.monitoring_traksi.BuildConfig
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.repository.CameraRepository
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.viewModel.CameraViewModel
import com.cbi.monitoring_traksi.ui.viewModel.LocationViewModel
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
//import com.cbi.monitoring_traksi.utils.AppUtils.checkCameraPermissions
import com.cbi.monitoring_traksi.utils.AppUtils.checkPermissionsCamera
import com.cbi.monitoring_traksi.utils.AppUtils.closeLoadingLayout
import com.cbi.monitoring_traksi.utils.AppUtils.handleStringtoJsonObjectPertanyaan
import com.cbi.monitoring_traksi.utils.PrefManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etJenisUnit
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etKodeUnit
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etTanggalPeriksa
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etTypeUnit
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etUnitKerja
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.ivSignLocation
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.view.id_layout_foto_unit
import kotlinx.android.synthetic.main.activity_layout_form_p2h.id_editable_foto_layout
import kotlinx.android.synthetic.main.activity_layout_form_p2h.id_layout_activity_informasi_unit
import kotlinx.android.synthetic.main.activity_layout_form_p2h.id_take_foto_layout
import kotlinx.android.synthetic.main.activity_layout_form_p2h.loadingFetchingData
import kotlinx.android.synthetic.main.activity_layout_form_p2h.parentFormP2H
import kotlinx.android.synthetic.main.activity_layout_form_p2h.view.id_layout_activity_informasi_unit

import kotlinx.android.synthetic.main.edit_foto_layout.view.closeZoom
import kotlinx.android.synthetic.main.edit_foto_layout.view.deletePhoto
import kotlinx.android.synthetic.main.edit_foto_layout.view.retakePhoto
import kotlinx.android.synthetic.main.layout_dropdown_hasil_periksa.view.etTemplateDropdown
import kotlinx.android.synthetic.main.layout_foto_unit.view.ivAddFotoUnit
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.view.etKomentar
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.view.ivAddFotoPerPertanyaan
import kotlinx.android.synthetic.main.layout_pertanyaan.view.etHasilPeriksa
import kotlinx.android.synthetic.main.layout_pertanyaan.view.layout_komentar_foto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


open class FormLaporP2HActivity : AppCompatActivity(), CameraRepository.PhotoCallback  {

    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel
    private var currentFormIndex: Int = 0
    val dataJenisUnitList = mutableListOf<Map<String, Any>>()
    val dataUnitKerjaList = mutableListOf<Map<String, Any>>()
    val pertanyaanList = mutableListOf<Map<String, Any>>()
    val dataKodeUnitList = mutableListOf<Map<String, Any>>()
    val dataPertanyaanList = mutableListOf<Map<String, Any>>()
    val pertanyaanPerPage = mutableMapOf<Int, MutableMap<Int, String>>()
    val pertanyaanPerPage2 = mutableMapOf<Int, MutableMap<Int, String>>()
    val pertanyaanIds: MutableList<String> = mutableListOf()
    private var dataMapJenisUnitArray: Array<Map<String, Any>>? = null
    private var dataMapUnitKerjaArray: Array<Map<String, Any>>? = null
    private var dataMapListPertanyaanArray: Array<Map<String, Any>>? = null
    private var dataMapKodeUnitArray: Array<Map<String, Any>>? = null
    private var dataMapPertanyaanArray: Array<Map<String, Any>>? = null
    private var adapterJenisUnitItems: ArrayAdapter<String>? = null
    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var locationViewModel: LocationViewModel
    private var lat: Double? = null
    private var lon: Double? = null
    val viewsArray = ArrayList<View>()
    var pertanyaanPerJenisUnit: MutableMap<String, Array<String>> = mutableMapOf()
    lateinit var ListPertanyaanStr: Array<String>

    private val formLayoutInfoUnit: Array<View> by lazy {
        arrayOf(id_layout_activity_informasi_unit)
    }

    val arrInsertedDataTable =  mutableMapOf<Int, Map<String, Any>>()
    val globalJenisUnitMap: MutableMap<Int, String> = mutableMapOf()
    val globalPertanyaanAllJenisUnitMapping = mutableMapOf<Int, MutableList<MutableMap<String, Any>>>()
    val formLayoutsPertanyaan = mutableListOf<View>()
    private var pertanyaanListGlobal: MutableList<MutableMap<String, Any>> = mutableListOf()

    private var globalListPilJenisUnit: MutableMap<Int, String> = mutableMapOf()
    private var globalListPilUnitKerja: MutableMap<Int, String> = mutableMapOf()
    val listNamaFoto = mutableMapOf<String, String>()
    val listFileFoto = mutableMapOf<String, File>()

    private var accuracyRange = 0
    private var fixAccuracy = 0
    private var minAccuracyGPS = 10
    private var minRangeAct = 50

    //for photos
    private var zoomOpen = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                locationViewModel.startLocationUpdates()
            } else {
                showSnackbar("Location permission denied.")
            }
        }

    @SuppressLint("DiscouragedApi", "ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_form_p2h)
        prefManager = PrefManager(this)
        loadingFetchingData.visibility = View.VISIBLE
        setupViewLayout()
        initViewModel()
        AppUtils.showLoadingLayout(this, window, loadingFetchingData)

        //fetch data untuk arrayAdapter
        unitViewModel.loadDataJenisUnit()
        unitViewModel.dataJenisUnitList.observe(this){data->
            if (data != null) {
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
        }

        unitViewModel.loadDataUnitKerja()
        unitViewModel.dataUnitkerjaList.observe(this) { data ->
            if (data != null) {
                data.forEach { record ->
                    val recordMap = mutableMapOf<String, Any>()

                    recordMap["id"] = record.id
                    recordMap["nama_unit_kerja"] = record.nama_unit_kerja
                    recordMap["id_jenis_unit"] = record.id_jenis_unit

                    dataUnitKerjaList.add(recordMap)
                }

                dataMapUnitKerjaArray = dataUnitKerjaList.toTypedArray()

            checkDataAvailability()

            }

        }

        unitViewModel.loadDataKodeUnit()
        unitViewModel.dataKodeUnitList.observe(this) { data ->

            if (data != null) {
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
        }

        unitViewModel.pertanyaanBasedOnJenisUnitList.observe(this) { itemMap ->

            itemMap.forEach { (id, records) ->
                val pertanyaanList = mutableListOf<MutableMap<String, Any>>()

                records.forEach { record ->
                    val recordMap = mutableMapOf<String, Any>()
                    recordMap["id"] = record.id
                    recordMap["nama_pertanyaan"] = record.nama_pertanyaan
                    recordMap["kondisi_mesin"] = record.kondisi_mesin

                    pertanyaanList.add(recordMap)


                }
                 globalPertanyaanAllJenisUnitMapping[id] = pertanyaanList
            }

        }


        etTanggalPeriksa.isEnabled = false
        etTanggalPeriksa.inputType = InputType.TYPE_NULL
        val currentDate = getCurrentDate()
        etTanggalPeriksa.setText(currentDate)


        val nextButton = findViewById<Button>(resources.getIdentifier("mbNextForm0", "id", packageName))
        nextButton.setOnClickListener {
            if (formLayoutsPertanyaan.isEmpty()){
                    displayToasty(this,"Harap memilih jenis unit terlebih dahulu")
            }else{
                toggleFormVisibility(currentFormIndex)
            }
        }

        val mbPrevForm1 = findViewById<Button>(resources.getIdentifier("mbPrevForm1", "id", packageName))
        mbPrevForm1.setOnClickListener {
            val intent: Intent
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }


        id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit.setOnClickListener{
            if(prefManager!!.isCameraAllowed == false){
                checkPermissionsCamera(this)
            }else{
                id_layout_activity_informasi_unit.visibility = View.GONE
                id_take_foto_layout.visibility = View.VISIBLE
                takeCameraNow("0", id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit)
            }
        }

        id_editable_foto_layout.deletePhoto.visibility = View.INVISIBLE
        id_editable_foto_layout.deletePhoto.isClickable = true
        id_editable_foto_layout.closeZoom.setOnClickListener {
            zoomOpen = false
            id_take_foto_layout.visibility = View.GONE
            id_layout_activity_informasi_unit.visibility = View.VISIBLE
            cameraViewModel.closeZoomPhotos()
        }

        id_editable_foto_layout.retakePhoto.setOnClickListener {


            Log.d("testing", "terklik")
            AppUtils.hideKeyboard(this)
            zoomOpen = false
            id_editable_foto_layout.visibility = View.GONE
            id_take_foto_layout.visibility = View.VISIBLE
            retakeCamera("0", id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit)
        }

    }

    private fun retakeCamera(id_foto: String, imageView: ImageView){
//        if (listNamaFoto.containsKey(id_foto)){
//            zoomOpen = true
//            cameraViewModel.openZoomPhotos(listFileFoto[id_foto]!!){
//                id_layout_activity_informasi_unit.visibility = View.GONE
////                id_editable_foto_layout.visibility = View.VISIBLE
//            }
//        }else{


        cameraViewModel.takeCameraPhotos(
            id_foto,
            imageView
        )
//        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            showSnackbar("Location permission is required for this app.")
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun takeCameraNow(id_foto: String, imageView: ImageView){
        if (listNamaFoto.containsKey(id_foto)){
            zoomOpen = true
            cameraViewModel.openZoomPhotos(listFileFoto[id_foto]!!){
                id_layout_activity_informasi_unit.visibility = View.GONE
              id_editable_foto_layout.visibility = View.VISIBLE
            }
        }else{
            cameraViewModel.takeCameraPhotos(
                id_foto,
                imageView
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                // If not granted camera permission
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    prefManager!!.isCameraAllowed = true
                    id_layout_activity_informasi_unit.visibility = View.GONE
                    id_take_foto_layout.visibility = View.VISIBLE
                    takeCameraNow("0", id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit)
                }else{
                    prefManager!!.isCameraAllowed = false
                    id_layout_activity_informasi_unit.visibility = View.VISIBLE
                    id_take_foto_layout.visibility = View.GONE

                    showPermissionDeniedDialogForCamera()
                }
                return
            }
            // Add more cases if you have multiple permission requests in your app
        }
    }

    fun showPermissionDeniedDialogForCamera() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Camera permission is required for this feature. Please grant the permission in the app settings or uninstall the app.")
            setPositiveButton("Settings") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }
    private fun displayToasty(context: Context, message: String){
        Toasty.success(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleClicksForm(){
        arrInsertedDataTable.clear()
        formLayoutsPertanyaan.forEachIndexed { index, layout ->
            val materialButtonNext = layout.findViewById<MaterialButton>(R.id.mbButtonNext)
            val materialButtonPrev = layout.findViewById<MaterialButton>(R.id.mbButtonPrev)
            val mbSaveFormP2H = layout.findViewById<MaterialButton>(R.id.mbSaveFormP2H)


            materialButtonNext.setOnClickListener {
                toggleFormVisibility(index + 1)
            }

            materialButtonPrev.setOnClickListener {
                toggleFormVisibility(index - 1)
            }


            mbSaveFormP2H.setOnClickListener {
                AlertDialogUtility.withTwoActions(
                    this,
                    "Ya",
                    "Peringatan",
                    "Apakah anda yakin menyimpan data?",
                    "warning.json"
                ) {
                    val jenisunit = getIdFromJenisUnit(etJenisUnit.text.toString())!!.toInt()
                    val app_version = BuildConfig.VERSION_NAME


                    unitViewModel.pushDataToLaporanP2hSQL(
                        tanggal_upload = getCurrentDate(true),
                        id_jenis_unit = jenisunit,
                        lat = lat.toString(),
                        lon = lon.toString(),
                        id_user = prefManager!!.userid!!.toInt(),
                        status = "pending",
                        foto_unit =  "",
                        app_version = app_version
                    )

                    var lastIdLaporP2H = 0
                    unitViewModel.fetchLastIdLaporanP2HSQL()
                    unitViewModel.last_id_laporp2h.observe(this){
                        lastIdLaporP2H = it
                    }

                    val hasilPeriksa = mutableMapOf<Int, MutableMap<String, MutableMap<String, String>>>()

                    formLayoutsPertanyaan.forEachIndexed { index, layout ->
                        val containerPertanyaan = layout.findViewById<LinearLayout>(R.id.listPertanyaanContainer)
                        val selectedValuesMap = mutableMapOf<String, MutableMap<String, String>>()

                        for (i in 1 until containerPertanyaan.childCount) {
                            val layoutPertanyaan = containerPertanyaan.getChildAt(i) as ConstraintLayout
                            val idPertanyaan = layoutPertanyaan.id.toString()
                            val etTemplateDropdown = layoutPertanyaan.findViewById<AutoCompleteTextView>(R.id.etTemplateDropdown)
                            val gasTerus = layoutPertanyaan.layout_komentar_foto.etKomentar.text.toString()
                            val selectedValue = etTemplateDropdown.text.toString()

                            val valueMap = mutableMapOf<String, String>()
                            valueMap["jawabanHasilPeriksa"] = selectedValue
                            valueMap["komentarHasilPeriksa"] = gasTerus
                            valueMap["namaFoto"] = listNamaFoto[idPertanyaan] ?: ""
                            selectedValuesMap[idPertanyaan] = valueMap
                        }

                        hasilPeriksa[index] = selectedValuesMap
                    }


                    hasilPeriksa.forEach { (index, pertanyaanMap) ->
                        pertanyaanMap.forEach { (idPertanyaan, valueMap) ->
                            unitViewModel.pushToTableData(
                                created_at = getCurrentDate(true),
                                id_laporan = lastIdLaporP2H,
                                id_pertanyaan = idPertanyaan.toInt(),
                                kondisi = valueMap["jawabanHasilPeriksa"].toString(),
                                komentar = valueMap["komentarHasilPeriksa"].toString(),
                                foto = valueMap["namaFoto"].toString(),
                            )

                            unitViewModel.insertResultLaporP2H.observe(this) { isInserted ->
                                    val resultMap = mapOf(
                                        "is_inserted" to isInserted,
                                        "id_laporan" to lastIdLaporP2H,
                                        "id_pertanyaan" to  idPertanyaan.toInt(),
                                        "kondisi" to valueMap["jawabanHasilPeriksa"].toString(),
                                        "komentar" to valueMap["komentarHasilPeriksa"].toString(),
                                        "foto" to valueMap["namaFoto"].toString(),
                                    )
                                    arrInsertedDataTable[idPertanyaan.toInt()] = resultMap
                            }

                        }
                    }


                    val hasFalse = arrInsertedDataTable.any { (_, value) ->
                        value["is_inserted"] == false
                    }

                    if (hasFalse) {
                        retryInsertDataLaporanPertanyaan(arrInsertedDataTable, 3)
                    }else{

                        AlertDialogUtility.alertDialogAction(
                            this,
                            "Sukses",
                            "Data berhasil disimpan!",
                            "success.json"
                        ) {
                            AppUtils.showLoadingLayout(this, window, loadingFetchingData)
                            val intent = Intent(this, MainActivity
                            ::class.java)
                            startActivity(intent)
                        }
                    }



                }





            }
        }

    }

    private fun retryInsertDataLaporanPertanyaan(
        arrInsertedDataTable: MutableMap<Int, Map<String, Any>>,
        retryCount: Int
    ) {
        if (retryCount == 0) return

        val filteredMap = arrInsertedDataTable.filterValues { it["is_inserted"] == false }

        if (filteredMap.isEmpty()) {
            return
        }

        filteredMap.forEach { (id, data) ->
            val id_laporan = data["id_laporan"]
            val idPertanyaan = data["id_pertanyaan"]
            val kondisi = data["kondisi"]
            val komentar = data["komentar"]
            val foto = data["foto"]

            unitViewModel.pushToTableData(
                created_at = getCurrentDate(true),
                id_laporan = id_laporan as Int,
                id_pertanyaan = idPertanyaan as Int,
                kondisi = kondisi as String,
                komentar = komentar as String,
                foto = foto as String,
            )

            unitViewModel.insertResultLaporP2H.observe(this) { isInserted ->
                val updatedData = data.toMutableMap()
                updatedData["is_inserted"] = isInserted
                arrInsertedDataTable[id] = updatedData

                // Check if all entries are now inserted
                if (arrInsertedDataTable.all { it.value["is_inserted"] == true }) {
                } else if (id == filteredMap.keys.last()) {
                    retryInsertDataLaporanPertanyaan(arrInsertedDataTable, retryCount - 1)
                }
            }
        }
    }
    private fun getIdFromJenisUnit(jenisunit: String): Int? {
        return globalJenisUnitMap.entries.find { it.value == jenisunit }?.key
    }
    private fun checkDataAvailability(){

        if (dataMapUnitKerjaArray != null && dataMapJenisUnitArray != null && dataMapKodeUnitArray != null ) {

            var isDoneRetrieveData = false

            dataMapJenisUnitArray?.let { data ->
                val idUnitList = data.map { it["id"] as? Int }.filterNotNull()
                val listPertanyaanJenisUnit = data.map { it["list_pertanyaan"] as? String }.filterNotNull()

                globalListPilJenisUnit = data.mapNotNull {
                    val id = it["id"] as? Int
                    val namaUnit = it["nama_unit"] as? String
                    if (id != null && namaUnit != null) {
                        id to namaUnit
                    } else {
                        null
                    }
                }.toMap().toMutableMap()

                val listPertanyaanJenisUnitArray = listPertanyaanJenisUnit.toTypedArray()

                GlobalScope.launch(Dispatchers.IO) {

                    listPertanyaanJenisUnitArray.forEachIndexed { index, datas ->
                        val id = idUnitList[index]
                        val values = handleStringtoJsonObjectPertanyaan(datas)
                        unitViewModel.loadDataListPertanyaanBasedOnJenisUnit(values.toTypedArray(), id)
                    }

                    withContext(Dispatchers.Main) {
                        isDoneRetrieveData = true
                    }
                }
            }






            GlobalScope.launch(Dispatchers.Main) {
                while (!isDoneRetrieveData) {
                    delay(100)
                }
                AppUtils.closeLoadingLayout(loadingFetchingData)
                setupDropdown()
            }
        }

    }

    private fun setupLayoutPertanyaan(pertanyaanList: MutableList<MutableMap<String, Any>>) {
        // Clear previous data
        pertanyaanPerPage.clear()
        formLayoutsPertanyaan.clear()

        GlobalScope.launch(Dispatchers.Main) {
            // Do background processing
            val layoutData = withContext(Dispatchers.IO) {
                // Group questions by condition
                val pertanyaanMap = pertanyaanList.groupBy { it["kondisi_mesin"] }
                val matiList = pertanyaanMap["mati"] ?: emptyList()
                val hidupList = pertanyaanMap["hidup"] ?: emptyList()

                // Merge and sort the questions
                val mergedList = mutableListOf<Pair<Int, String>>()
                mergedList.addAll(matiList.map { it["id"] as Int to it["nama_pertanyaan"] as String })
                mergedList.addAll(hidupList.map { it["id"] as Int to it["nama_pertanyaan"] as String })

                val minBatch = 10
                var batchCount = 0
                var remainingQuestions = mergedList.size

                while (remainingQuestions > 0) {
                    remainingQuestions -= minBatch
                    batchCount++
                }

                // Create paginated data
                val paginatedData = mutableMapOf<Int, MutableMap<Int, String>>()
                var resetCount = 1
                var inc = 0

                for (item in mergedList) {
                    if (resetCount > minBatch) {
                        resetCount = 1
                        inc++
                    }

                    if (!paginatedData.containsKey(inc)) {
                        paginatedData[inc] = mutableMapOf()
                    }
                    paginatedData[inc]?.put(item.first, item.second)
                    resetCount++
                }

                Triple(paginatedData, batchCount, mergedList.size)
            }

            val (paginatedData, batchCount, _) = layoutData

            // Update the UI with the processed data
            val layout = findViewById<ConstraintLayout>(R.id.parentFormP2H)
            for (i in 0 until batchCount) {
                val includedLayout = layoutInflater.inflate(R.layout.activity_form_p2h_layout_pertanyaan, null)
                includedLayout.id = View.generateViewId()
                includedLayout.visibility = View.GONE
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
                includedLayout.layoutParams = layoutParams

                val textViewQuestion = includedLayout.findViewById<TextView>(R.id.textTitleTest)
                textViewQuestion.text = "Form page ke-${i + 1}"

                if (i == (batchCount - 1)) {
                    val mbSaveFormP2H = includedLayout.findViewById<TextView>(R.id.mbSaveFormP2H)
                    mbSaveFormP2H.visibility = View.VISIBLE
                } else {
                    val mbButtonForNext = includedLayout.findViewById<TextView>(R.id.mbButtonNext)
                    mbButtonForNext.visibility = View.VISIBLE
                    mbButtonForNext.text = "Next"
                }

                val pertanyaanThisPage = paginatedData[i]

                if (pertanyaanThisPage is Map<*, *>) {
                    val containerPertanyaan = includedLayout.findViewById<LinearLayout>(R.id.listPertanyaanContainer)
                    pertanyaanThisPage.forEach { (key, value) ->
                        val layoutPertanyaan = layoutInflater.inflate(R.layout.layout_pertanyaan, null) as ConstraintLayout
                        layoutPertanyaan.id = key as Int

                        val textTitlePertanyaan = layoutPertanyaan.findViewById<TextView>(R.id.textPertanyaan)
                        textTitlePertanyaan.visibility = View.VISIBLE
                        textTitlePertanyaan.text = value as String

                        val test = layoutPertanyaan.findViewById<ConstraintLayout>(R.id.etHasilPeriksa)
                        test.visibility = View.VISIBLE
                        val etTemplateDropdown = layoutPertanyaan.findViewById<AutoCompleteTextView>(R.id.etTemplateDropdown)

                        val dropdownOptions = arrayOf(
                            "Sudah dicek",
                            "Belum dicek",
                            "Sudah dicek, tetapi ada perbaikan"
                        )
                        val adapter = ArrayAdapter(
                            this@FormLaporP2HActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            dropdownOptions
                        )
                        etTemplateDropdown.setAdapter(adapter)
                        etTemplateDropdown.setText("Sudah dicek", false)

                        val defaultPosition = adapter.getPosition("Sudah dicek")
                        etTemplateDropdown.setSelection(defaultPosition)

                        etTemplateDropdown.setOnItemClickListener { _, _, position, _ ->
                            val selectedItem = dropdownOptions[position]
                            if (selectedItem == "Sudah dicek, tetapi ada perbaikan") {
                                layoutPertanyaan.layout_komentar_foto.visibility = View.VISIBLE
                                layoutPertanyaan.layout_komentar_foto.etKomentar.setText("")

                                val id_foto = key.toString()
                                layoutPertanyaan.layout_komentar_foto.ivAddFotoPerPertanyaan.setOnClickListener {
                                    includedLayout.visibility = View.GONE
                                    id_take_foto_layout.visibility = View.VISIBLE
                                    takeCameraNow(id_foto, layoutPertanyaan.layout_komentar_foto.ivAddFotoPerPertanyaan)
                                }

                                id_editable_foto_layout.retakePhoto.setOnClickListener {
                                    AppUtils.hideKeyboard(this@FormLaporP2HActivity)
                                    zoomOpen = false
                                    id_editable_foto_layout.visibility = View.GONE
                                    id_take_foto_layout.visibility = View.VISIBLE
                                    retakeCamera(id_foto, layoutPertanyaan.layout_komentar_foto.ivAddFotoPerPertanyaan)
                                }

                            } else {
                                layoutPertanyaan.layout_komentar_foto.visibility = View.GONE
                                layoutPertanyaan.layout_komentar_foto.etKomentar.setText("")
                            }
                        }

                        // Create layout parameters for layoutPertanyaan
                        val params = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.MATCH_PARENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )

                        if (key == pertanyaanThisPage.keys.last()) {
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
            AppUtils.closeLoadingLayout(loadingFetchingData)
        }
    }

    private fun setupDropdown(){

        val listPilJenisUnitValues = globalListPilJenisUnit.values.toList()

        val adapterJenisUnitItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listPilJenisUnitValues)
        etJenisUnit.setAdapter(adapterJenisUnitItems)

        etJenisUnit.setOnItemClickListener { parent, _, position, _ ->

            val selectedNamaUnit = listPilJenisUnitValues[position]

            val selectedIdJenisUnit = globalListPilJenisUnit.filterValues { it == selectedNamaUnit }.keys.firstOrNull()



//            AppUtils.showLoadingLayout(this, window, loadingFetchingData)
            loadingFetchingData.visibility = View.VISIBLE
            globalPertanyaanAllJenisUnitMapping[selectedIdJenisUnit]?.let { setupLayoutPertanyaan(it) }

            val filteredUnitKerjaList = dataMapUnitKerjaArray?.filter {
                it["id_jenis_unit"] == selectedIdJenisUnit
            }
            val unitKerjaArray = filteredUnitKerjaList?.map { it["nama_unit_kerja"] as? String }?.filterNotNull()
            val idUnitKerjaArray = filteredUnitKerjaList?.mapNotNull { it["id"] as? Int }?.toTypedArray()
            val adapterUnitKerjaItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unitKerjaArray ?: emptyList())
            etUnitKerja.setAdapter(adapterUnitKerjaItems)

            etUnitKerja.setText("")
            etKodeUnit.setText("")
            etTypeUnit.setText("")


            etUnitKerja.setOnItemClickListener { _, _, position, _ ->
                val idPilUnitKerja = idUnitKerjaArray?.get(position)

                val filteredKodeUnitList = dataMapKodeUnitArray?.filter {
                    it["id_unit_kerja"] == idPilUnitKerja
                }

                val namaKodeUnitArray = filteredKodeUnitList?.mapNotNull { it["nama_kode"] as? String }?.toTypedArray() ?: emptyArray()
                val typeUnitArray = filteredKodeUnitList?.mapNotNull { it["type_unit"] as? String }?.toTypedArray()

                val adapterKodeUnitItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, namaKodeUnitArray)
                etKodeUnit.setAdapter(adapterKodeUnitItems)

                etKodeUnit.setText("")
                etTypeUnit.setText("")
                etKodeUnit.setOnItemClickListener { _, _, position, _ ->
                        val pilKodeUnit = adapterKodeUnitItems.getItem(position).toString()
                        etTypeUnit.setText(typeUnitArray?.get(position))
                }
            }
        }

    }


    private fun toggleFormVisibility(nextFormIndex: Int) {


        formLayoutInfoUnit.forEach { it.visibility = View.GONE }
        formLayoutsPertanyaan.forEach { it.visibility = View.GONE }

        if (nextFormIndex >= 0 && nextFormIndex < formLayoutsPertanyaan.size) {
            formLayoutsPertanyaan[nextFormIndex].visibility = View.VISIBLE
            currentFormIndex = nextFormIndex


        }else if(nextFormIndex == -1){
            formLayoutInfoUnit[0].visibility = View.VISIBLE
        }else{
            displayToasty(this, "Terjadi error untuk load page ini")
        }


    }

    private fun setupViewLayout() {
        parentFormP2H.id_layout_activity_informasi_unit.visibility = View.VISIBLE
    }

    private fun initViewModel() {
        unitViewModel = ViewModelProvider(
            this,
            UnitViewModel.Factory(application, UnitRepository(this))
        )[UnitViewModel::class.java]

        val cameraRepository = CameraRepository(this, window, id_take_foto_layout, id_editable_foto_layout)
        cameraRepository.setPhotoCallback(this)
        cameraViewModel = ViewModelProvider(
            this,
            CameraViewModel.Factory(cameraRepository)
        )[CameraViewModel::class.java]

        locationViewModel = ViewModelProvider(
            this,
            LocationViewModel.Factory(application, ivSignLocation, this)
        )[LocationViewModel::class.java]
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

    private fun getCurrentDate(fullTime: Boolean = false): String {
        val calendar = Calendar.getInstance()
        val dateFormat = if (fullTime) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        }
        return dateFormat.format(calendar.time)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (cameraViewModel.statusCamera() || zoomOpen) {

            if (currentFormIndex >= 0 && currentFormIndex < formLayoutsPertanyaan.size) {
                id_take_foto_layout.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingFetchingData)

                cameraViewModel.closeCamera()
                formLayoutsPertanyaan[currentFormIndex].visibility = View.VISIBLE
                AppUtils.closeLoadingLayout(loadingFetchingData)
            }else{
                id_take_foto_layout.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingFetchingData)

                cameraViewModel.closeCamera()
                id_layout_activity_informasi_unit.visibility = View.VISIBLE
                AppUtils.closeLoadingLayout(loadingFetchingData)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.locationPermissions.observe(this) { isLocationEnabled ->
            if (!isLocationEnabled) {
                requestLocationPermission()
            } else {
                locationViewModel.startLocationUpdates()
            }
        }

        locationViewModel.locationData.observe(this) { location ->
            lat = location.latitude
            lon = location.longitude
        }

    }

    override fun onPause() {
        super.onPause()
        locationViewModel.stopLocationUpdates()

    }

    override fun onDestroy() {
        super.onDestroy()
        locationViewModel.stopLocationUpdates()


    }

    override fun onPhotoTaken(photoFile: File, fname: String, resultCode: String) {
        id_layout_activity_informasi_unit.visibility = View.VISIBLE

        listFileFoto[resultCode] = photoFile
        listNamaFoto[resultCode] = fname


    }

}