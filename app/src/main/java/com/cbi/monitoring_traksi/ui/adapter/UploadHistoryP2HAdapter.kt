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
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.ui.view.MainActivity
import com.cbi.monitoring_traksi.ui.viewModel.HistoryP2HViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.detail_foto_unit
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.listKerusakanContainer
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.mbStatusBeroperasi
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.scListKerusakan
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvCaptionDenganKerusakan
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvCaptionTanpaKerusakan
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvLokasiUnit
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvNamaUnit
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvTglCreated
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvTitleDenganKerusakan
import kotlinx.android.synthetic.main.layout_detail_p2h_adapter.view.tvTitleTanpaKerusakan
import kotlinx.android.synthetic.main.list_item_kerusakan_per_unit.view.fotoItemKerusakanUnit

import org.json.JSONObject
import java.io.File

class UploadHistoryP2HAdapter(
    private val context: Context,
    private val historyP2HViewModel: HistoryP2HViewModel,
    private val onDeleteClickListener: OnDeleteClickListener,
    private val onClickDataListener: OnClickDataListener,
) : ListAdapter<LaporP2HModel, UploadHistoryP2HAdapter.ViewHolder>(ItemDiffCallback()) {

    private var isDescendingOrder = false
    class ViewHolder(itemView: View, onDeleteClickListener: OnDeleteClickListener, onClickDataListener:OnClickDataListener, context: Context,historyModel : HistoryP2HViewModel) :
        RecyclerView.ViewHolder(itemView) {


        val mainActivity = context as MainActivity
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

                    val rootView = mainActivity.findViewById<View>(android.R.id.content)

                    val parentLayout = rootView.findViewById<ConstraintLayout>(R.id.clParentAlertDialog)
                    val layoutBuilder =
                        LayoutInflater.from(context).inflate(R.layout.layout_detail_p2h_adapter, parentLayout)

                    val scrollView = layoutBuilder.findViewById<ScrollView>(R.id.scListKerusakan)


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
                    }
                    layoutBuilder.tvTglCreated.text = "$textStatusArchive$textLastUpdate"
                    layoutBuilder.tvNamaUnit.text = "${currentItem.jenis_unit} ${currentItem.unit_kerja} ${currentItem.kode_unit}"
                    layoutBuilder.tvLokasiUnit.text = "Unit ${currentItem.unit_kerja}"

                    val rootApp = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
                    val dirApp = File(rootApp, "LaporP2H")
                    val fileFotoUnit = File(dirApp, currentItem.foto_unit)

                    Glide.with(context).load(Uri.fromFile(fileFotoUnit))
                        .diskCacheStrategy(
                            DiskCacheStrategy.NONE
                        ).skipMemoryCache(true).centerCrop()
                        .into(layoutBuilder.detail_foto_unit)

                    layoutBuilder.detail_foto_unit.setOnClickListener{
                        preview_image(fileFotoUnit, context)
                    }

                    val idPertanyaan = mutableListOf<String>()


                    if (!currentItem.kerusakan_unit.isNullOrEmpty()) {
                        val layoutParams = scrollView.layoutParams
                        layoutParams.height = dpToPx(200, context)
                        scrollView.layoutParams = layoutParams

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
                                layoutBuilder.tvTitleDenganKerusakan.visibility = View.VISIBLE
                                layoutBuilder.tvCaptionDenganKerusakan.visibility = View.VISIBLE
                                layoutBuilder.listKerusakanContainer.visibility = View.VISIBLE

                                val namaKerusakanUnit = listItemView.findViewById<TextView>(R.id.namaKerusakanUnit)
                                val komentarKerusakanUnit = listItemView.findViewById<TextView>(R.id.komentarKerusakanUnit)

                                namaKerusakanUnit.text = "$inc. ${namaPertanyaan.getOrNull(index)?: "Unknown Data"}"
                                komentarKerusakanUnit.text = data["komentar"]
                                val fileKerusakanUnit = File(dirApp, data["foto"])

                                Glide.with(context).load(Uri.fromFile(fileKerusakanUnit))
                                    .diskCacheStrategy(
                                        DiskCacheStrategy.NONE
                                    ).skipMemoryCache(true).centerCrop()
                                    .into(listItemView.fotoItemKerusakanUnit)

                                listItemView.fotoItemKerusakanUnit.setOnClickListener{
                                    preview_image(fileKerusakanUnit, context)
                                }

                                it.addView(listItemView)

                                inc++
                            }
                        }
                    }else{

                        val layoutParams = scrollView.layoutParams
                        scrollView.layoutParams = layoutParams
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        layoutBuilder.tvTitleTanpaKerusakan.visibility = View.VISIBLE
                        layoutBuilder.tvCaptionTanpaKerusakan.visibility = View.VISIBLE

                    }

                    if (alertDialog.window != null) {
                        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                    }
                    alertDialog.show()

                }
            }
        }


        fun dpToPx(dp: Int, context: Context): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

       fun preview_image(file:File , context: Context){

           val zoomLayout = LayoutInflater.from(context).inflate(R.layout.layout_preview_foto_detail_laporan, null)
           val zoomImageView: ImageView = zoomLayout.findViewById(R.id.zoomImageView)


           Glide.with(context).load(Uri.fromFile(file))
               .diskCacheStrategy(DiskCacheStrategy.NONE)
               .skipMemoryCache(true)
               .into(zoomImageView)

           val defaultRotation = 90f
           zoomImageView.rotation = defaultRotation

           val zoomBuilder: AlertDialog.Builder = AlertDialog.Builder(context).setView(zoomLayout)
           val zoomDialog: AlertDialog = zoomBuilder.create()

           val closeZoomButton: ImageView = zoomLayout.findViewById(R.id.closeZoomButton)
           closeZoomButton.setOnClickListener {
               zoomDialog.dismiss()
           }

           val rotateButton: ImageView = zoomLayout.findViewById(R.id.rotateButton)
           var currentRotation = defaultRotation
           rotateButton.setOnClickListener {
               currentRotation += 90f
               if (currentRotation >= 360f) {
                   currentRotation = 0f
               }
               zoomImageView.rotation = currentRotation
           }

           zoomDialog.show()

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
        holder.itemTitlePeriksaUnit.text = "${item.jenis_unit} ${item.unit_kerja} ${item.kode_unit}"
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
        else {
            holder.itemStatusArchive.setTextColor(ContextCompat.getColor(context, R.color.yellowbutton))
            holder.itemLastUpdate.setTextColor(ContextCompat.getColor(context, R.color.yellowbutton))
            }
        holder.itemJenisKerusakan.text = textItemKerusakan
        holder.itemStatusArchive.text = textStatusArchive
        holder.itemLastUpdate.text = textLastUpdate
        holder.itemfotoKerusakan.text = textjumlahKerusakan
        holder.deleteButton.visibility = if (item.archive == 0) View.VISIBLE else View.GONE


//        Log.d("testing", itemCount.toString())
        // Set bottom margin for the last item
        if (position == itemCount - 1) {
            holder.itemView.setBottomMargin(130) // 50dp margin
        } else {
            holder.itemView.setBottomMargin(0) // Reset margin for other items
        }

    }


    fun View.setBottomMargin(marginBottom: Int) {
        val params = this.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = marginBottom
        this.layoutParams = params
    }
    fun toggleSortingOrder() {
        isDescendingOrder = !isDescendingOrder

        val sortedList = currentList.sortedWith(
            if (isDescendingOrder) compareByDescending<LaporP2HModel> {
                it.id
            } else compareBy<LaporP2HModel> {
                it.id
            }
        )

        submitList(sortedList) {
            // After the new list is submitted, update the bottom margin for the last item
            if (itemCount > 0) {
                notifyItemChanged(itemCount - 1)
            }
        }
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