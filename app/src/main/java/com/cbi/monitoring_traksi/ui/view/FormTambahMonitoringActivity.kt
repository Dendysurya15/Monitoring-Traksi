package com.cbi.monitoring_traksi.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.cbi.monitoring_traksi.R
import kotlinx.android.synthetic.main.activity_form_1_sebelum_engine_hidup.mbNextForm2
import kotlinx.android.synthetic.main.activity_form_2_sebelum_engine_hidup.mbNextForm3
import kotlinx.android.synthetic.main.activity_form_3_sebelum_engine_hidup.mbNextForm4
import kotlinx.android.synthetic.main.activity_form_4_sebelum_engine_hidup.mbNextForm5
import kotlinx.android.synthetic.main.activity_form_informasi_unit.mbNextForm1
import kotlinx.android.synthetic.main.activity_form_informasi_unit.mbPrevForm1
import kotlinx.android.synthetic.main.activity_form_saat_engine_hidup.mbSaveFormSaatEngineHidup
import kotlinx.android.synthetic.main.activity_form_saat_engine_hidup.mbSaveMonitoringUnit
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout1
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout2
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout3
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout4
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout5
import kotlinx.android.synthetic.main.activity_form_tambah_monitoring.FormLayout6

class FormTambahMonitoringActivity : AppCompatActivity() {

    private val formLayouts: Array<View> by lazy {
        arrayOf(FormLayout1, FormLayout2, FormLayout3, FormLayout4, FormLayout5, FormLayout6)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_tambah_monitoring)

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

}