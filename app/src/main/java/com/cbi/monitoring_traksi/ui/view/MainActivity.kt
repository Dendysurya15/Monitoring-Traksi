package com.cbi.monitoring_traksi.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.utils.AppUtils
import kotlinx.android.synthetic.main.activity_login.idEmail
import kotlinx.android.synthetic.main.activity_login.idPassword
import kotlinx.android.synthetic.main.activity_login.loadingLogin
import kotlinx.android.synthetic.main.activity_login.mbLogin
import kotlinx.android.synthetic.main.activity_main.mbTambahMonitoring

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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