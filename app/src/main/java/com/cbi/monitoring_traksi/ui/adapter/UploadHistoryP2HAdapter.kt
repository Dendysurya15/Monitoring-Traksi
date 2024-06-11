package com.cbi.monitoring_traksi.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.ui.view.MainActivity
import com.cbi.monitoring_traksi.ui.viewModel.HistoryP2HViewModel
import com.cbi.monitoring_traksi.utils.AppUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.detail_foto_unit
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.mbStatusBeroperasi
//import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.mbStatusBeroperasi
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.titleListPertanyaan


import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvLokasiUnit
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvNamaUnit
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvTglCreated
import kotlinx.android.synthetic.main.list_item_kerusakan_per_unit.view.fotoItemKerusakanUnit
import kotlinx.android.synthetic.main.list_item_kerusakan_per_unit.view.komentarKerusakanUnit
import kotlinx.android.synthetic.main.list_item_kerusakan_per_unit.view.namaKerusakanUnit

import org.json.JSONObject
import java.io.File

class UploadHistoryP2HAdapter(
    private val context: Context,
    private val historyP2HViewModel: HistoryP2HViewModel,
    private val onDeleteClickListener: OnDeleteClickListener,
    private val onClickDataListener: OnClickDataListener,
) : ListAdapter<LaporP2HModel, UploadHistoryP2HAdapter.ViewHolder>(ItemDiffCallback()) {

    private var isDescendingOrder = true

    class ViewHolder(itemView: View, onDeleteClickListener: OnDeleteClickListener, onClickDataListener:OnClickDataListener, context: Context,historyModel : HistoryP2HViewModel) :
        RecyclerView.ViewHolder(itemView) {


        val test = context as MainActivity
        val itemTitlePeriksaUnit: TextView = itemView.findViewById(R.id.titlePeriksaUnit)
        val itemLokasiPeriksaUnit: TextView = itemView.findViewById(R.id.lokasiPeriksaUnit)
        val itemJenisKerusakan: TextView = itemView.findViewById(R.id.listJenisKerusakan)
        val itemfotoKerusakan: TextView = itemView.findViewById(R.id.listFotoKerusakan)
        val itemStatusArchive: TextView = itemView.findViewById(R.id.statusArchive)
        val itemLastUpdate: TextView = itemView.findViewById(R.id.lastUpdate)

        val deleteButton: FloatingActionButton = itemView.findViewById(R.id.fbDelData)

        val idContainerList : CardView = itemView.findViewById(R.id.idListDataP2H)


        private lateinit var currentItem: LaporP2HModel

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDeleteClick(position, currentItem)
                }
            }

            idContainerList.setOnClickListener{
                val position = adapterPosition


                if(position != RecyclerView.NO_POSITION){
                    onClickDataListener.onClickList(position, currentItem)

                    val rootView = test.findViewById<View>(android.R.id.content)

                    val parentLayout = rootView.findViewById<ConstraintLayout>(R.id.clParentAlertDialog)
                    val layoutBuilder =
                        LayoutInflater.from(context).inflate(R.layout.layout_detail_p2h_adapter, parentLayout)

                    val cardView = layoutBuilder.findViewById<CardView>(R.id.containerData)

                    val parentWidth = rootView.width
                    val parentHeight = rootView.height

                    val cardViewLayoutParams = cardView.layoutParams
                    cardViewLayoutParams.width = (parentWidth * 0.9).toInt()
                    cardViewLayoutParams.height = (parentHeight * 0.7).toInt()
                    cardView.layoutParams = cardViewLayoutParams

                    val builder: AlertDialog.Builder =
                        AlertDialog.Builder(context).setView(layoutBuilder)
                    val alertDialog: AlertDialog = builder.create()

                    val cb = layoutBuilder.findViewById<ImageView>(R.id.closeButton)

                    cb.setOnClickListener{
                        alertDialog.dismiss()
                    }

                    var textStatusArchive = "Tersimpan - "
                    var textLastUpdate = "${currentItem.tanggal_upload}"
                    if (currentItem.archive == 1){
                        textStatusArchive = "Terupload - "
                        textLastUpdate = "${currentItem.uploaded_time}"
                        layoutBuilder.tvTglCreated.setTextColor(ContextCompat.getColor(context, R.color.greenbutton))
                        layoutBuilder.tvTglCreated.setTextColor(ContextCompat.getColor(context, R.color.greenbutton))
                    }
                    layoutBuilder.tvTglCreated.text = "$textStatusArchive$textLastUpdate"
                    layoutBuilder.tvNamaUnit.text = "${currentItem.jenis_unit} ${currentItem.unit_kerja} ${currentItem.type_unit}"
                    layoutBuilder.tvLokasiUnit.text = "(Unit ${currentItem.unit_kerja})"
                    layoutBuilder.mbStatusBeroperasi.text = currentItem.status_unit_beroperasi

                    val rootApp = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
                    val dirApp = File(rootApp, "LaporP2H")
                    val file = File(dirApp, currentItem.foto_unit)

                    Glide.with(context).load(Uri.fromFile(file))
                        .diskCacheStrategy(
                            DiskCacheStrategy.NONE
                        ).skipMemoryCache(true).centerCrop()
                        .into(layoutBuilder.detail_foto_unit)

                    val idPertanyaan = mutableListOf<String>()
                    if (!currentItem.kerusakan_unit.isNullOrEmpty()) {

                        val jsonObject = JSONObject(currentItem.kerusakan_unit)
                        val keys = jsonObject.keys()
                        val listKerusakan = mutableListOf<Map<String, String>>()

                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = jsonObject.getJSONObject(key)

                            val keyValueMap = mutableMapOf<String, String>()
                            keyValueMap["pertanyaan"] = key
                            keyValueMap["komentar"] = value.getString("komentar")
                            keyValueMap["foto"] = value.getString("foto")
                            listKerusakan.add(keyValueMap)

                            idPertanyaan.add(key)
                        }

                        val namaPertanyaan = mutableListOf<String>()
                        historyModel.loadNamaPertanyaanBasedFromId(idPertanyaan)
                        historyModel.queryGetNamaPertanyaan.observe(context as LifecycleOwner){data->
                            data.map{it->
                                namaPertanyaan.add(it)
                            }
                        }

                        val parentLinearLayout = layoutBuilder.findViewById<LinearLayout>(R.id.listKerusakanContainer)


                        parentLinearLayout?.let {
                            it.removeAllViews()
                            var inc = 1
                            listKerusakan.forEachIndexed { index, data ->
                                val layoutInflater = LayoutInflater.from(context)
                                val listItemView = layoutInflater.inflate(R.layout.list_item_kerusakan_per_unit, it, false)
                                val namaKerusakanUnit = listItemView.findViewById<TextView>(R.id.namaKerusakanUnit)
                                val komentarKerusakanUnit = listItemView.findViewById<TextView>(R.id.komentarKerusakanUnit)

                                namaKerusakanUnit.text = "$inc. ${namaPertanyaan.getOrNull(index)?: "Unknown Data"}"
                                komentarKerusakanUnit.text = data["komentar"]
                                val file = File(dirApp, data["foto"])

                                Glide.with(context).load(Uri.fromFile(file))
                                    .diskCacheStrategy(
                                        DiskCacheStrategy.NONE
                                    ).skipMemoryCache(true).centerCrop()
                                    .into(listItemView.fotoItemKerusakanUnit)

                                it.addView(listItemView)

                                inc++
                            }
                        }
                    }

                    if (alertDialog.window != null) {
                        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                    }
                    alertDialog.show()

                }
            }
        }

        fun bind(item: LaporP2HModel) {
            currentItem = item
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_history_p2h, parent, false)
        return ViewHolder(view, onDeleteClickListener, onClickDataListener, context, historyP2HViewModel)
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position)
        holder.bind(item)
        holder.itemTitlePeriksaUnit.text = "${position + 1}. ${item.jenis_unit} ${item.unit_kerja} ${item.type_unit}"
        holder.itemLokasiPeriksaUnit.text = ": ${item.unit_kerja}"
        var textjumlahKerusakan = ": Tidak ada"
        var textItemKerusakan = ": Tidak ada"
        val idPertanyaan = mutableListOf<String>()

        // Check if kerusakan_unit data exists and is not empty
        if (!item.kerusakan_unit.isNullOrEmpty()) {
            val jsonObject = JSONObject(item.kerusakan_unit)
            textjumlahKerusakan = ": ${jsonObject.length()} foto"
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                idPertanyaan.add(keys.next())
            }

            historyP2HViewModel.loadNamaPertanyaanBasedFromId(idPertanyaan)
            historyP2HViewModel.queryGetNamaPertanyaan.observe(context as LifecycleOwner){data->
                var joinedText = data.joinToString(", ")
                if (joinedText.length > 70) {
                    joinedText = joinedText.substring(0, 70) + "..."
                }
                textItemKerusakan = ": $joinedText"
            }
        }

        var textStatusArchive = "Tersimpan - "
        var textLastUpdate = "${item.tanggal_upload}"
        if (item.archive == 1){
            textStatusArchive = "Terupload - "
            textLastUpdate = "${item.uploaded_time}"
            holder.itemStatusArchive.setTextColor(ContextCompat.getColor(context, R.color.greenbutton))
            holder.itemLastUpdate.setTextColor(ContextCompat.getColor(context, R.color.greenbutton))
        }
        holder.itemJenisKerusakan.text = textItemKerusakan
        holder.itemStatusArchive.text = textStatusArchive
        holder.itemLastUpdate.text = textLastUpdate
        holder.itemfotoKerusakan.text = textjumlahKerusakan
        holder.deleteButton.visibility = if (item.archive == 0) View.VISIBLE else View.GONE

        val isLastItem = position == currentList.lastIndex

        val marginBottom = if (isLastItem) {
            val marginBottomDp = 25
            val density = context.resources.displayMetrics.density
            (marginBottomDp * density).toInt()
        } else {
            0
        }

        // Set the layout params with bottom margin
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = marginBottom
        holder.itemView.layoutParams = layoutParams

    }

    class ItemDiffCallback : DiffUtil.ItemCallback<LaporP2HModel>() {
        override fun areItemsTheSame(
            oldItem: LaporP2HModel,
            newItem: LaporP2HModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: LaporP2HModel,
            newItem: LaporP2HModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position:Int, item: LaporP2HModel)
    }

    interface OnClickDataListener {
        fun onClickList(position: Int, item: LaporP2HModel)
    }
}