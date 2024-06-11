package com.cbi.monitoring_traksi.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.data.repository.HistoryP2HRepository
import com.cbi.monitoring_traksi.data.repository.UnitRepository
import com.cbi.monitoring_traksi.ui.adapter.UploadHistoryP2HAdapter
import com.cbi.monitoring_traksi.ui.viewModel.HistoryP2HViewModel
import com.cbi.monitoring_traksi.ui.viewModel.UnitViewModel
import com.cbi.monitoring_traksi.utils.AlertDialogUtility
import com.cbi.monitoring_traksi.utils.AppUtils
import com.cbi.monitoring_traksi.utils.AppUtils.getCurrentDate
import com.cbi.monitoring_traksi.utils.PrefManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.animationView
import kotlinx.android.synthetic.main.activity_main.dateToday
import kotlinx.android.synthetic.main.activity_main.fbUploadData
import kotlinx.android.synthetic.main.activity_main.iblogout
import kotlinx.android.synthetic.main.activity_main.loadingFetchingData
import kotlinx.android.synthetic.main.activity_main.loadingMain

import kotlinx.android.synthetic.main.activity_main.mbTambahMonitoring
import kotlinx.android.synthetic.main.activity_main.name_user_login
import kotlinx.android.synthetic.main.activity_main.rvListData
import kotlinx.android.synthetic.main.alert_dialog_view.view.lottieDialog

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), UploadHistoryP2HAdapter.OnDeleteClickListener , UploadHistoryP2HAdapter.OnClickDataListener {
    private var prefManager: PrefManager? = null
    private lateinit var unitViewModel: UnitViewModel

    private lateinit var historyP2HViewModel: HistoryP2HViewModel
    private var uploadHistoryP2HAdapter: UploadHistoryP2HAdapter? = null

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

        prefManager = PrefManager(this)
        if (prefManager!!.isFirstTimeLaunch) {
            handleSynchronizeData()
        }
        name_user_login.text = prefManager!!.name
        dateToday.text =  AppUtils.getCurrentDate()

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

        historyP2HViewModel.resultQueryDateLaporanP2H.observe(this) { list->
            if (list.size == 0) {
                findViewById<ImageView>(R.id.ivNoData).visibility = View.VISIBLE
//                animationView.visibility = View.VISIBLE
//                animationView.playAnimation()
                findViewById<TextView>(R.id.tvNoData).visibility = View.VISIBLE
            } else{
                uploadHistoryP2HAdapter!!.submitList(list)
            }
            sizeListAdapeter = list.size

            allListUploaded = list.all { it.archive == 1 }
        }

        fbUploadData.setOnClickListener{
            if (AppUtils.checkConnectionDevice(this)) {

                Log.d("testing", allListUploaded.toString())
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

        clickAny()
        setupRecyclerList()
    }

    private fun setupRecyclerList(){
        val currentDate = getCurrentDate(true)
        historyP2HViewModel.loadLaporanP2HByDate(currentDate)
        AppUtils.closeLoadingLayout(loadingMain)
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
                prefManager!!.email = ""
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
}