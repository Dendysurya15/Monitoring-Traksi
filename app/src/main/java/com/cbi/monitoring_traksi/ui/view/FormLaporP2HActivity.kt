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
import androidx.core.content.ContextCompat
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etJenisUnit
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etKodeUnit
import kotlinx.android.synthetic.main.activity_form_p2h_layout_informasi_unit.etTanggalPeriksa

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
import kotlinx.android.synthetic.main.layout_foto_unit.view.ivAddFotoUnit
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.view.deletePhotoKerusakan
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.view.etKomentar
import kotlinx.android.synthetic.main.layout_komentar_dan_foto.view.ivAddFotoPerPertanyaan

import kotlinx.android.synthetic.main.layout_pertanyaan.view.layout_komentar_foto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
    val dataKodeUnitList = mutableListOf<Map<String, Any>>()

    val pertanyaanPerPage = mutableMapOf<Int, MutableMap<Int, String>>()
    private var dataMapJenisUnitArray: Array<Map<String, Any>>? = null
    private var dataMapUnitKerjaArray: Array<Map<String, Any>>? = null
    private var dataMapKodeUnitArray: Array<Map<String, Any>>? = null

    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var locationViewModel: LocationViewModel
    private var lat: Double? = null
    private var lon: Double? = null
    private var isFormInformasiUnit :Boolean = false

    private val formLayoutInfoUnit: Array<View> by lazy {
        arrayOf(id_layout_activity_informasi_unit)
    }

    val arrInsertedDataTable =  mutableMapOf<Int, Map<String, Any>>()
    val globalPertanyaanAllJenisUnitMapping = mutableMapOf<Int, MutableList<MutableMap<String, Any>>>()
    val formLayoutsPertanyaan = mutableListOf<View>()
    private var pertanyaanListGlobal: MutableList<MutableMap<String, Any>> = mutableListOf()
    private var locationEnable:Boolean = false
    private var globalListPilJenisUnit: MutableMap<Int, String> = mutableMapOf()
    private var globalListPilUnitKerja: MutableMap<Int, String> = mutableMapOf()
    private var globalListPilKodeUnit: MutableMap<Int, String> = mutableMapOf()
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
            val jenis_unit = etJenisUnit.text.toString()
            val unit_kerja = etUnitKerja.text.toString()
            val kode_unit =  etKodeUnit.text.toString()


            if (locationEnable == true){
                if (jenis_unit != "" && unit_kerja != "" && kode_unit != ""  && !listNamaFoto["0"].isNullOrEmpty()){
                    isFormInformasiUnit = false
                    toggleFormVisibility(currentFormIndex)
                }else{
                    displayToasty(this,"Mohon untuk mengisi semua kolom dan upload foto unit terlebih dahulu")
                }
            }else{
                displayToasty(this, "Harap aktifkan GPS agar kami dapat mengakses koordinat Anda")
            }
        }

        val mbPrevForm1 = findViewById<Button>(resources.getIdentifier("mbPrevForm1", "id", packageName))
        mbPrevForm1.setOnClickListener {
            val intent: Intent
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }





    }

    private fun retakeCamera(id_foto: String, imageView: ImageView, pageForm: Int, deletePhoto: View?, kode_foto :String ){
        cameraViewModel.takeCameraPhotos(
            id_foto,
            imageView,
            pageForm,
            deletePhoto,
            kode_foto
        )
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

    private fun takeCameraNow(id_foto: String, pageForm: Int,  imageView: ImageView, deletePhoto : View?, kode_foto: String){
        val kodeFotoNoWhitespace = removeWhitespaces(kode_foto)
        if (listNamaFoto.containsKey(id_foto)){
            zoomOpen = true
            cameraViewModel.openZoomPhotos(listFileFoto[id_foto]!!){
                id_layout_activity_informasi_unit.visibility = View.GONE
              id_editable_foto_layout.visibility = View.VISIBLE
            }
        }else{
            if (isFormInformasiUnit == true){
                val jenis_unit = etJenisUnit.text.toString()
                val unit_kerja = etUnitKerja.text.toString()
                if (id_foto == "0" && jenis_unit != "" && unit_kerja != ""){
                    val kode_foto = "${etJenisUnit.text}_${etUnitKerja.text}"
                    val kodeFotoNoWhitespace = removeWhitespaces(kode_foto)
                    id_layout_activity_informasi_unit.visibility = View.GONE
                    id_take_foto_layout.visibility = View.VISIBLE

                    cameraViewModel.takeCameraPhotos(
                        id_foto,
                        imageView,
                        pageForm,
                        null,
                        kodeFotoNoWhitespace
                    )
                }
                else{
                    displayToasty(this, "Harap untuk mengisi Jenis Unit dan Unit Kerja terlebih dahulu")
                }
            }else{
                cameraViewModel.takeCameraPhotos(
                    id_foto,
                    imageView,
                    pageForm,
                    deletePhoto,
                    kodeFotoNoWhitespace
                )
            }

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
//                    takeCameraNow("0", id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit)
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


    fun removeWhitespaces(input: String): String {
        return input.replace(" ", "")
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
            val materialButtonNext = layout.findViewById<FloatingActionButton>(R.id.mbButtonNext)
            val materialButtonPrev = layout.findViewById<FloatingActionButton>(R.id.mbButtonPrev)
            val materialButtonPrevLastPage = layout.findViewById<FloatingActionButton>(R.id.mbButtonPrevLastPage)
            val materialButtonPrevTop = layout.findViewById<MaterialButton>(R.id.mbButtonPrevTop)
            val mbSaveFormP2H = layout.findViewById<FloatingActionButton>(R.id.mbSaveFormP2H)


            materialButtonNext.setOnClickListener {
                toggleFormVisibility(index + 1)
            }

            materialButtonPrev.setOnClickListener {
                toggleFormVisibility(index - 1)
            }

            materialButtonPrevLastPage.setOnClickListener {
                toggleFormVisibility(index - 1)
            }

            materialButtonPrevTop.setOnClickListener {
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

                    val jenis_unit = etJenisUnit.text.toString()
                    val unit_kerja = etUnitKerja.text.toString()
                    val kode_unit =  etKodeUnit.text.toString()



                        var isKomentarFillAll = true
                        var isFotoTakenAll = true
                        val kerusakanUnit = mutableMapOf<String, MutableMap<String, String>>()
                        formLayoutsPertanyaan.forEachIndexed { index, layout ->
                            val containerPertanyaan = layout.findViewById<LinearLayout>(R.id.listPertanyaanContainer)

                            for (i in 1 until containerPertanyaan.childCount) {
                                val layoutPertanyaan = containerPertanyaan.getChildAt(i) as ConstraintLayout
                                val idPertanyaan = layoutPertanyaan.id.toString()
                                val selectedValueDropdown = layoutPertanyaan.findViewById<AutoCompleteTextView>(R.id.etTemplateDropdown).text.toString()
                                val etValueKomentar = layoutPertanyaan.findViewById<TextInputEditText>(R.id.etKomentar).text.toString()
                                if (selectedValueDropdown == "Sudah dicek, tetapi perlu perbaikan"){
                                    val valueMap = mutableMapOf<String, String>()
                                    valueMap["komentar"] = etValueKomentar
                                    valueMap["foto"] = listNamaFoto[idPertanyaan] ?: ""
                                    kerusakanUnit[idPertanyaan] = valueMap

                                    // Check if komentar or foto is empty and update the flags
                                    if (etValueKomentar.isEmpty()) {
                                        isKomentarFillAll = false
                                    }
                                    if (valueMap["foto"].isNullOrEmpty()) {
                                        isFotoTakenAll = false
                                    }

                                }
                            }

                        }

                        if(isFotoTakenAll == false || isKomentarFillAll == false){
                            displayToasty(this, "Mohon untuk mengupload semua foto atau mengisi komentar perbaikan unit setiap pertanyaan!")
                        }else{
                            var kerusakanUnitJson  = ""
                            if (kerusakanUnit.isNotEmpty()) {
                                val jsonObject = JSONObject()
                                for ((idPertanyaan, valueMap) in kerusakanUnit) {
                                    val valueJsonObject = JSONObject(valueMap as Map<*, *>)
                                    jsonObject.put(idPertanyaan, valueJsonObject)
                                }
                                kerusakanUnitJson = jsonObject.toString()
                            }

                            val app_version = BuildConfig.VERSION_NAME

                            unitViewModel.pushDataToLaporanP2hSQL(
                                tanggal_upload = getCurrentDate(true),
                                jenis_unit = jenis_unit,
                                unit_kerja = unit_kerja,
                                kode_unit =  kode_unit,
                                lat = lat.toString(),
                                lon = lon.toString(),
                                user = prefManager!!.name!!,
                                status_unit_beroperasi = "Menunggu Persetujuan Beroperasi",
                                kerusakan_unit = kerusakanUnitJson,
                                foto_unit =  listNamaFoto["0"] ?: "",
                                app_version = app_version,
                                uploaded_time = "",
                                archive = 0,
                            )

                            unitViewModel.insertResultLaporP2H.observe(this) { isInserted ->
                                if (isInserted){
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
                                }else{
                                    displayToasty(this, "Terjadi kesalahan dalam menyimpan data")
                                }
                            }
                        }

                }

            }
        }

    }

    private fun getIdFromJenisUnit(idJenisUnit: String): Int? {
        return globalListPilJenisUnit .entries.find { it.value == idJenisUnit }?.key
    }

    private fun getIdFromUnitKerja(idUnitKerja: String): Int? {
        return globalListPilUnitKerja .entries.find { it.value == idUnitKerja }?.key
    }

    private fun getIdFromKodeUnit(idKodeUnit: String): Int? {
        return globalListPilKodeUnit .entries.find { it.value == idKodeUnit }?.key
    }
    private fun checkDataAvailability(){

        if (dataMapUnitKerjaArray != null && dataMapJenisUnitArray != null && dataMapKodeUnitArray != null) {

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


                globalListPilJenisUnit = globalListPilJenisUnit.toList().sortedBy { (_, value) -> value }.toMap().toMutableMap()

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

            dataUnitKerjaList?.let {data->
                globalListPilUnitKerja = data.mapNotNull {
                    val id = it["id"] as? Int
                    val unitKerja = it["nama_unit_kerja"] as? String
                    if (id != null && unitKerja != null) {
                        id to unitKerja
                    } else {
                        null
                    }
                }.toMap().toMutableMap()

            }

            dataMapKodeUnitArray?.let {data->
                globalListPilKodeUnit = data.mapNotNull {
                    val id = it["id"] as? Int
                    val unitKerja = it["nama_kode"] as? String
                    if (id != null && unitKerja != null) {
                        id to unitKerja
                    } else {
                        null
                    }
                }.toMap().toMutableMap()
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

                val textViewQuestion = includedLayout.findViewById<TextView>(R.id.titlePageForm)
                textViewQuestion.text = "Form page ke-${i + 1}"

                if (i == (batchCount - 1)) {
                    val mbSaveFormP2H = includedLayout.findViewById<FloatingActionButton>(R.id.mbSaveFormP2H)
                    mbSaveFormP2H.visibility = View.VISIBLE
                    val mbButtonPrevLastPage = includedLayout.findViewById<FloatingActionButton>(R.id.mbButtonPrevLastPage)
                    mbButtonPrevLastPage.visibility = View.VISIBLE
                    mbButtonPrevLastPage.backgroundTintList = ContextCompat.getColorStateList(this@FormLaporP2HActivity, R.color.graytextdark)
                } else {
                    val mbButtonForNext = includedLayout.findViewById<FloatingActionButton>(R.id.mbButtonNext)
                    mbButtonForNext.visibility = View.VISIBLE
                    mbButtonForNext.backgroundTintList = ContextCompat.getColorStateList(this@FormLaporP2HActivity, R.color.bluedark)
                    val mbButtonForPrev = includedLayout.findViewById<FloatingActionButton>(R.id.mbButtonPrev)
                    mbButtonForPrev.visibility = View.VISIBLE
                    mbButtonForPrev.backgroundTintList = ContextCompat.getColorStateList(this@FormLaporP2HActivity, R.color.graytextdark)
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
                            "Sudah dicek, tetapi perlu perbaikan"
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
                            if (selectedItem == "Sudah dicek, tetapi perlu perbaikan") {
                                layoutPertanyaan.layout_komentar_foto.visibility = View.VISIBLE
                                layoutPertanyaan.layout_komentar_foto.etKomentar.setText("")

                                val id_foto = key.toString()
                                layoutPertanyaan.layout_komentar_foto.ivAddFotoPerPertanyaan.setOnClickListener {
                                    includedLayout.visibility = View.GONE
                                    id_take_foto_layout.visibility = View.VISIBLE
                                    val kode_foto = "${etJenisUnit.text}_${etUnitKerja.text}"
                                    takeCameraNow(id_foto,
                                        i,
                                        layoutPertanyaan.layout_komentar_foto.ivAddFotoPerPertanyaan,
                                        layoutPertanyaan.layout_komentar_foto,
                                        kode_foto
                                    )
                                }

                                id_editable_foto_layout.retakePhoto.setOnClickListener {
                                    AppUtils.hideKeyboard(this@FormLaporP2HActivity)
                                    zoomOpen = false
                                    id_editable_foto_layout.visibility = View.GONE
                                    id_take_foto_layout.visibility = View.VISIBLE
                                    // in case ini adalah foto di layout informasi unit foto utama dengan id 0
                                    val kode_foto = "${etJenisUnit.text}_${etUnitKerja.text}"
                                    if (listNamaFoto.containsKey("0")){
                                        retakeCamera("0", id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit, -1,null, kode_foto)
                                    }else{
                                        retakeCamera(id_foto, layoutPertanyaan.layout_komentar_foto.ivAddFotoPerPertanyaan, i ,null,kode_foto)
                                    }

                                }
                                id_editable_foto_layout.closeZoom.setOnClickListener {
                                    zoomOpen = false
                                    id_take_foto_layout.visibility = View.GONE
                                    if(isFormInformasiUnit == false){
                                        toggleFormVisibility(currentFormIndex)
                                    }else{
                                        toggleFormVisibility(0)
                                    }
                                    cameraViewModel.closeZoomPhotos()
                                }

                            } else {
                                layoutPertanyaan.layout_komentar_foto.visibility = View.GONE
                            }
                        }

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
            closeLoadingLayout(loadingFetchingData)
        }
    }

    private fun setupDropdown(){

        val listPilJenisUnitValues = globalListPilJenisUnit.values.toList()

        Log.d("tesitng", listPilJenisUnitValues.toString())

        val adapterJenisUnitItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listPilJenisUnitValues)
        etJenisUnit.setAdapter(adapterJenisUnitItems)

        etJenisUnit.setOnItemClickListener { parent, _, position, _ ->

            val selectedNamaUnit = listPilJenisUnitValues[position]

            val selectedIdJenisUnit = globalListPilJenisUnit.filterValues { it == selectedNamaUnit }.keys.firstOrNull()



            loadingFetchingData.visibility = View.VISIBLE
            globalPertanyaanAllJenisUnitMapping[selectedIdJenisUnit]?.let { setupLayoutPertanyaan(it) }

            val filteredUnitKerjaList = dataMapUnitKerjaArray?.filter {
                it["id_jenis_unit"] == selectedIdJenisUnit
            }
            val unitKerjaArray = filteredUnitKerjaList?.map { it["nama_unit_kerja"] as? String }?.filterNotNull()
            val idUnitKerjaArray = filteredUnitKerjaList?.mapNotNull { it["id"] as? Int }?.toTypedArray()

            val unitKerjaArraySorted = unitKerjaArray?.sorted()

            Log.d("testing", unitKerjaArraySorted!!.toTypedArray().contentToString())
            val adapterUnitKerjaItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unitKerjaArraySorted ?: emptyList())
            etUnitKerja.setAdapter(adapterUnitKerjaItems)

            etUnitKerja.setText("")
            etKodeUnit.setText("")
//            etTypeUnit.setText("")


            etUnitKerja.setOnItemClickListener { _, _, position, _ ->
                val idPilUnitKerja = idUnitKerjaArray?.get(position)


                Log.d("testing", dataMapKodeUnitArray.contentToString())
                val filteredKodeUnitList = dataMapKodeUnitArray?.filter {
                    it["id_unit_kerja"] == idPilUnitKerja
                }

                val namaKodeUnitArray = filteredKodeUnitList?.mapNotNull {
                    val namaKode = it["nama_kode"] as? String
                    val typeUnit = it["type_unit"] as? String
                    if (namaKode != null && typeUnit != null) {
                        "$namaKode $typeUnit"
                    } else {
                        null
                    }
                }?.toTypedArray() ?: emptyArray()

                val namaKodeUnitArraySorted = namaKodeUnitArray?.sorted()
                Log.d("testing", namaKodeUnitArraySorted!!.toTypedArray().contentToString())
                val adapterKodeUnitItems = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, namaKodeUnitArraySorted ?: emptyList())
                etKodeUnit.setAdapter(adapterKodeUnitItems)

                etKodeUnit.setText("")
                etKodeUnit.setOnItemClickListener { _, _, position, _ ->
                        val pilKodeUnit = adapterKodeUnitItems.getItem(position).toString()

//                        etTypeUnit.setText('skldjfkds')
                }
            }
        }

    }


    private fun toggleFormVisibility(nextFormIndex: Int) {
        formLayoutInfoUnit.forEach { it.visibility = View.GONE }
        formLayoutsPertanyaan.forEach { it.visibility = View.GONE }

        if (isFormInformasiUnit == false && nextFormIndex >= 0 && nextFormIndex < formLayoutsPertanyaan.size) {
            formLayoutsPertanyaan[nextFormIndex].visibility = View.VISIBLE
            currentFormIndex = nextFormIndex
            isFormInformasiUnit = false

        }else if(nextFormIndex == -1){
            formLayoutInfoUnit[0].visibility = View.VISIBLE
            isFormInformasiUnit = true
        }else{
            formLayoutInfoUnit[0].visibility = View.VISIBLE
            isFormInformasiUnit = true
        }

    }

    private fun setupViewLayout() {
        toggleFormVisibility(currentFormIndex)
        val kode_foto = "${etJenisUnit.text}_${etUnitKerja.text}"
        parentFormP2H.id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit.setOnClickListener{
                if(prefManager!!.isCameraAllowed == false){
                    checkPermissionsCamera(this)
                }else{
                    takeCameraNow("0", -1,  id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit, null, kode_foto)
                }
        }

        id_editable_foto_layout.deletePhoto.visibility = View.INVISIBLE
        id_editable_foto_layout.deletePhoto.isClickable = true

        id_editable_foto_layout.retakePhoto.setOnClickListener {
            AppUtils.hideKeyboard(this@FormLaporP2HActivity)
            zoomOpen = false
            id_editable_foto_layout.visibility = View.GONE
            id_take_foto_layout.visibility = View.VISIBLE

            retakeCamera("0", id_layout_activity_informasi_unit.id_layout_foto_unit.ivAddFotoUnit, -1, null,kode_foto)
        }

        id_editable_foto_layout.closeZoom.setOnClickListener{
            id_take_foto_layout.visibility = View.GONE
            toggleFormVisibility(currentFormIndex)
            cameraViewModel.closeZoomPhotos()
        }

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

            if (isFormInformasiUnit == false && currentFormIndex >= 0 && currentFormIndex < formLayoutsPertanyaan.size) {
                id_take_foto_layout.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingFetchingData)

                cameraViewModel.closeCamera()
                formLayoutsPertanyaan[currentFormIndex].visibility = View.VISIBLE
                closeLoadingLayout(loadingFetchingData)
            }else{
                id_take_foto_layout.visibility = View.VISIBLE
                AppUtils.showLoadingLayout(this, window, loadingFetchingData)

                cameraViewModel.closeCamera()
                id_layout_activity_informasi_unit.visibility = View.VISIBLE
                closeLoadingLayout(loadingFetchingData)
            }

        }else{

            if(isFormInformasiUnit == true){
                val intent: Intent
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }else{
                toggleFormVisibility(currentFormIndex - 1)
            }

        }
    }

    private fun removeEntryByFileName(fname: String) {
        // Find the key for the given filename
        val key = listNamaFoto.entries.find { it.value == fname }?.key
        if (key != null) {
            // Remove the entries from both maps
            listFileFoto.remove(key)
            listNamaFoto.remove(key)
        }
    }

    private fun removeEntryByPhotoFile(photoFile: File) {
        // Find the key for the given photo file
        val key = listFileFoto.entries.find { it.value == photoFile }?.key
        if (key != null) {
            // Remove the entries from both maps
            listFileFoto.remove(key)
            listNamaFoto.remove(key)
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
            locationEnable = true
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

    override fun onPhotoTaken(photoFile: File, fname: String, resultCode: String, deletePhoto: View?, pageForm: Int) {
        toggleFormVisibility(pageForm)

        deletePhoto?.deletePhotoKerusakan?.visibility = View.VISIBLE
        deletePhoto?.deletePhotoKerusakan?.setOnClickListener{

            val isDeleted = cameraViewModel.deletePhotoSelected(fname)

            if(isDeleted){
                deletePhoto?.deletePhotoKerusakan?.visibility = View.GONE
            }
            val ivAddFotoPertanyaan = deletePhoto.findViewById<ImageView>(R.id.ivAddFotoPerPertanyaan)
            val originalImageResId = R.drawable.ic_add_image
            ivAddFotoPertanyaan.setImageResource(originalImageResId)

            removeEntryByFileName(fname)
            removeEntryByPhotoFile(photoFile)
        }

        listFileFoto[resultCode] = photoFile
        listNamaFoto[resultCode] = fname


    }

}