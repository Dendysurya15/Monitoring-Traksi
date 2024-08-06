package com.cbi.monitoring_traksi.data.repository

import android.app.Application
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cbi.monitoring_traksi.data.model.RegionalModel
import com.cbi.monitoring_traksi.utils.AppUtils
import org.json.JSONException
import org.json.JSONObject

class RegionalRepository(private val application: Application) {
    fun fetchData(onSuccess: (List<RegionalModel>) -> Unit, onError: (String) -> Unit) {
        val urlGet = AppUtils.mainServer + "getListDataRegional.php"

        val postRequest: StringRequest = object : StringRequest(
            Method.GET, urlGet,
            Response.Listener { response ->
                try {
                    val jObj = JSONObject(response)
                    val success = jObj.getInt("status")

                    if (success == 1) {
                        val dataListPupukArray = jObj.getJSONObject("listData")
                        val beforeSplitId = dataListPupukArray.getJSONArray("id")
                        val beforeSplitData = dataListPupukArray.getJSONArray("data")
                        val beforeSplitReg = dataListPupukArray.getJSONArray("reg")

                        val qcRegList = mutableListOf<RegionalModel>()
                        for (i in 0 until beforeSplitId.length()) {
                            val id = beforeSplitId.getInt(i)
                            val data = beforeSplitData.getString(i)
                            val reg = beforeSplitReg.getString(i)
                            qcRegList.add(RegionalModel(id, data, reg))
                        }

                        onSuccess(qcRegList)
                    } else {
                        onError(jObj.getString(AppUtils.TAG_MESSAGE))
                    }

                } catch (e: JSONException) {
                    onError("Data error, hubungi pengembang: $e")
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                onError("Terjadi kesalahan koneksi")
            }
        ) {
            // Add any necessary headers or parameters here
        }

        val queue = Volley.newRequestQueue(application)
        queue.cache.clear()
        queue.add(postRequest)
    }
}