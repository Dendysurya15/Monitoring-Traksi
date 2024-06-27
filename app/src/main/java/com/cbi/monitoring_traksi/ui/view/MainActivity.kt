package com.cbi.monitoring_traksi.ui.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.data.repository.CameraRepository
import com.cbi.monitoring_traksi.data.repository.HistoryP2HRepository
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.adapter.UploadHistoryP2HAdapter
import com.cbi.monitoring_traksi.ui.viewModel.CameraViewModel
import com.cbi.monitoring_traksi.ui.viewModel.HistoryP2HViewModel
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.AppUtils.closeLoadingLayout
import com.cbi.monitoring_traksi.utils.AppUtils.getCurrentDate
import com.cbi.monitoring_traksi.utils.AppUtils.showLoadingLayout
import com.cbi.monitoring_traksi.utils.PrefManager
import com.google.android.material.datepicker.MaterialDatePicker
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_layout_form_p2h.id_editable_foto_layout
import kotlinx.android.synthetic.main.activity_layout_form_p2h.id_take_foto_layout
import kotlinx.android.synthetic.main.activity_main.animationView
import kotlinx.android.synthetic.main.activity_main.dateToday
import kotlinx.android.synthetic.main.activity_main.fbUploadData
import kotlinx.android.synthetic.main.activity_main.iblogout
import kotlinx.android.synthetic.main.activity_main.id_preview_foto
import kotlinx.android.synthetic.main.activity_main.loadingFetchingData
import kotlinx.android.synthetic.main.activity_main.loadingMain

import kotlinx.android.synthetic.main.activity_main.mbTambahMonitoring
import kotlinx.android.synthetic.main.activity_main.name_user_login
import kotlinx.android.synthetic.main.activity_main.rvListData
import kotlinx.android.synthetic.main.alert_dialog_view.view.lottieDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), UploadHistoryP2HAdapter.OnDeleteClickListener , UploadHistoryP2HAdapter.OnClickDataListener {
    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel
    private lateinit var historyP2HViewModel: HistoryP2HViewModel
    private var uploadHistoryP2HAdapter: UploadHistoryP2HAdapter? = null

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dateButton : Button
    private lateinit var globalFormattedDate: String
    private var globalSpinnerFilterChoice : Int = 0
    private lateinit var sortOptionsFilterHistoryP2H: Array<String>

    private lateinit var filter: Spinner
    var sizeListAdapeter = 0
    var allListUploaded: Boolean = false
    private var totalList = 0
    private var firstScroll = false
    private var firstPage = true
    private var sortedBool = false

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewModel()
        loadingMain.visibility = View.VISIBLE
        AppUtils.showLoadingLayout(this, window, loadingMain)
        initializeSortOptionsFilterHistoryP2H(this)
        setupSpinnerSortBy()

        dateButton = findViewById(R.id.dateToday)
        dateButton.text = getTodaysDate()

        val currentDate = LocalDate.now()
        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todaysDate = currentDate.format(formatterDate)
        filter = findViewById<Spinner>(R.id.spinner_unit)
        globalFormattedDate = todaysDate

        prefManager = PrefManager(this)
        if (prefManager!!.isFirstTimeLaunch) {
            handleSynchronizeData()
        }else{
            AppUtils.closeLoadingLayout(loadingMain)
        }
        name_user_login.text = prefManager!!.name

        rvListData.layoutManager = LinearLayoutManager(this)
        rvListData.adapter = uploadHistoryP2HAdapter

        rvListData.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                try {
                    if (firstScroll && firstPage) {
                        if (dy <= 0) {
                            runOnUiThread {
                                fbUploadData.visibility = View.VISIBLE
                            }
                        } else {
                            runOnUiThread {
                                fbUploadData.visibility = View.GONE
                            }
                        }
                    }
                } finally {
                    firstScroll = true
                }
            }
        })

        loadListAdapter()

        fbUploadData.setOnClickListener{
            if (AppUtils.checkConnectionDevice(this)) {


                if (sizeListAdapeter != 0) {
                    if (allListUploaded == true){
                        AlertDialogUtility.alertDialog(
                            this,
                            "Peringatan",
                            "Semua data dalam list sudah terupload",
                            "warning.json"
                        )
                    }else{
                        AlertDialogUtility.withTwoActions(
                            this,
                            "Ya",
                            "Peringatan",
                            "Apakah anda yakin mengunggah data?",
                            "warning.json"
                        ) {
                            loadingFetchingData.visibility = View.VISIBLE
                            AppUtils.showLoadingLayout(this, window, loadingFetchingData)

                            Toasty.info(this, "Sedang mengunggah data..", Toast.LENGTH_SHORT).show()
                            val currentDate = getCurrentDate(true)
                            historyP2HViewModel.uploadToServer(currentDate)
                        }

                    }

                } else {
                    AlertDialogUtility.alertDialog(
                        this,
                        "Peringatan",
                        "Tidak ada data dalam list",
                        "warning.json"
                    )
                }
            } else {
                AlertDialogUtility.alertDialog(
                    this,
                    "Peringatan",
                    "Pastikan jaringan anda stabil dan perangkat sudah terkoneksi internet",
                    "network_error.json"
                )
            }
        }

        historyP2HViewModel.uploadResult.observe(this) { updatedList ->
            uploadHistoryP2HAdapter!!.submitList(updatedList)
            uploadHistoryP2HAdapter!!.notifyDataSetChanged()
        }


        handleClickSpinner()
        clickAny()
        setupRecyclerList()
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
    fun setLayoutVisibility(isZooming: Boolean) {
        val informationUnitLayout: View = findViewById(R.id.parentMainHalamanUtama)
        val editablePhotoLayout: View = findViewById(R.id.id_preview_foto)

        if (isZooming) {
            informationUnitLayout.visibility = View.GONE
            editablePhotoLayout.visibility = View.VISIBLE
        } else {
            informationUnitLayout.visibility = View.VISIBLE
            editablePhotoLayout.visibility = View.GONE
        }
    }

    private fun initializeSortOptionsFilterHistoryP2H(context: Context) {
        sortOptionsFilterHistoryP2H = arrayOf(
            context.getString(R.string.filter_history1),
            context.getString(R.string.filter_history2),
            context.getString(R.string.filter_history3)
        )
    }


    private fun handleClickSpinner(){
        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                AppUtils.showLoadingLayout(this@MainActivity, window, loadingMain)
                globalSpinnerFilterChoice = position
                handleSpinnerResultData(position)
                loadListAdapter()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do something when nothing is selected
            }
        }
    }

    private fun handleSpinnerResultData(position: Int){
        when (position) {
            0 -> historyP2HViewModel.loadLaporanP2HByDate(globalFormattedDate, false)
            1 -> historyP2HViewModel.loadLaporanP2HByDate(globalFormattedDate, true)
            2 -> historyP2HViewModel.loadLaporanP2HByDate(globalFormattedDate, false, true)
        }
    }
    private fun setupRecyclerList(){
        val currentDate = getCurrentDate(true)
        historyP2HViewModel.loadLaporanP2HByDate(currentDate)
    }
    @SuppressLint("WrongViewCast")
    private fun setupSpinnerSortBy(){
        val spinner: Spinner = findViewById(R.id.spinner_unit)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptionsFilterHistoryP2H)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun clickAny(){

        mbTambahMonitoring.setOnClickListener {
            try {
                val intent = Intent(this, FormLaporP2HActivity::class.java)
                startActivity(intent)
                finishAffinity()

            } catch (e: Exception) {
                Log.e("testing", "Error occurred: ${e.message}")
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
                prefManager!!.username = ""
                prefManager!!.password = ""
                prefManager!!.remember = false


                unitViewModel.deleteDataJenisUnit()
                unitViewModel.deleteDataKodeUnit()
                unitViewModel.deleteDataUnitKerja()
                unitViewModel.deleteDataPertanyaan()


                val intent = Intent(this, SplashScreenActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }

    }

    private fun initViewModel() {
        unitViewModel = ViewModelProvider(
            this,
            UnitViewModel.Factory(application, UnitRepository(this))
        )[UnitViewModel::class.java]
        historyP2HViewModel = ViewModelProvider(
            this,
            HistoryP2HViewModel.Factory(
                application,
                this,
                HistoryP2HRepository(this),
                loadingFetchingData,
                window,
                PrefManager(this),
            )
        )[HistoryP2HViewModel::class.java]

        uploadHistoryP2HAdapter = UploadHistoryP2HAdapter(this, historyP2HViewModel, this, this)
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

    private  fun loadListAdapter(){
        historyP2HViewModel.resultQueryDateLaporanP2H.observe(this) { list->

            findViewById<TextView>(R.id.countItemLaporan).setText("Total (${list.size}) Laporan")
            if (list.size == 0) {
                findViewById<ImageView>(R.id.ivNoData).visibility = View.VISIBLE
//                animationView.visibility = View.VISIBLE
//                animationView.playAnimation()
                findViewById<TextView>(R.id.tvNoData).visibility = View.VISIBLE
            } else{

                findViewById<ImageView>(R.id.ivNoData).visibility = View.GONE
                findViewById<TextView>(R.id.tvNoData).visibility = View.GONE
            }
            uploadHistoryP2HAdapter!!.submitList(list)
            closeLoadingLayout(loadingMain)
            sizeListAdapeter = list.size

            allListUploaded = list.all { it.archive == 1 }
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

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            100 -> {
//                // If not granted camera permission
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    prefManager!!.isCameraAllowed = true
//                }else{
//                    prefManager!!.isCameraAllowed = false
//                }
//                return
//            }
//            // Add more cases if you have multiple permission requests in your app
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                val permissionsMap = permissions.mapIndexed { index, permission ->
                    permission to (grantResults[index] == PackageManager.PERMISSION_GRANTED)
                }.toMap()

                val cameraGranted = permissionsMap[android.Manifest.permission.CAMERA] ?: false
                val readGranted = permissionsMap[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
                val writeGranted = permissionsMap[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

                prefManager?.isCameraAllowed = cameraGranted

                if (!cameraGranted || !readGranted || !writeGranted) {
                    Log.e("testing", "Necessary permissions were not granted")
                } else {
                    // All permissions granted, proceed with your logic
                }
            }
        }
    }

    override fun onClickList(position: Int, item: LaporP2HModel){

    }

    override fun onDeleteClick(position:Int, item: LaporP2HModel) {
            AlertDialogUtility.withTwoActions(
                this,
                "Ya",
                "Peringatan",
                "Apakah anda yakin menghapus data?",
                "warning.json"
            ) {
                historyP2HViewModel.deleteItemList(item.id.toString())

                historyP2HViewModel.deleteItemResult.observe(this){isSuccess->
                    if (isSuccess ) {
                        Toasty.success(this, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        val oldList = uploadHistoryP2HAdapter?.currentList?.toMutableList()



                        oldList?.removeAt(position)
                        uploadHistoryP2HAdapter?.submitList(oldList)
                        uploadHistoryP2HAdapter?.notifyItemRemoved(position)

                        for (i in position until oldList?.size!!) {
                            uploadHistoryP2HAdapter?.notifyItemChanged(i)
                        }

                        if(oldList.size == 0 ){
                            Handler(Looper.getMainLooper()).postDelayed({
                                findViewById<ImageView>(R.id.ivNoData).visibility = View.VISIBLE
                                findViewById<TextView>(R.id.tvNoData).visibility = View.VISIBLE
                            }, 1500) // Delay of 2000 milliseconds (2 seconds)
                        }
                    } else {
                        Toasty.warning(
                            this,
                            "Terjadi kesalahan, hubungi pengembang!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


            }
    }
    private fun getTodaysDate(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return makeDateString(day, month, year)
    }


    fun openDatePicker(view: View) {
        initMaterialDatePicker()
    }

    private fun initMaterialDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Pilih Tanggal")
//        builder.setTheme(R.style.DatePickerTheme)
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds())

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val displayDate = makeDateString(day, month, year)
            dateButton.text = displayDate
            loadingMain.visibility = View.VISIBLE

            val formattedDate = formatDateForBackend(day, month, year)

            globalFormattedDate = formattedDate

            AppUtils.showLoadingLayout(this, window, loadingMain)

            handleSpinnerResultData(globalSpinnerFilterChoice)
        }
        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun formatDateForBackend(day: Int, month: Int, year: Int): String {
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    private fun makeDateString(day: Int, month: Int, year: Int): String {
        return "${getMonthFormat(month)} $day $year"
    }

    private fun getMonthFormat(month: Int): String {
        return when (month) {
            1 -> "JAN"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "APR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AUG"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            12 -> "DEC"
            else -> "JAN" // Default should never happen
        }
    }
}