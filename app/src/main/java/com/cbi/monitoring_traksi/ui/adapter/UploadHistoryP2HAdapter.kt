package com.cbi.monitoring_traksi.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cbi.monitoring_traksi.R
import com.cbi.monitoring_traksi.data.model.LaporP2HModel
import com.cbi.monitoring_traksi.ui.viewModel.HistoryP2HViewModel
import com.cbi.monitoring_traksi.utils.AppUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UploadHistoryP2HAdapter(
    private val context: Context,
    private val historyP2HViewModel: HistoryP2HViewModel,
    private val onDeleteClickListener: OnDeleteClickListener
) : ListAdapter<LaporP2HModel, UploadHistoryP2HAdapter.ViewHolder>(ItemDiffCallback()) {

    private var isDescendingOrder = true

    class ViewHolder(itemView: View, onDeleteClickListener: OnDeleteClickListener) :
        RecyclerView.ViewHolder(itemView) {



        val itemTitlePeriksaUnit: TextView = itemView.findViewById(R.id.titlePeriksaUnit)
        val itemLokasiPeriksaUnit: TextView = itemView.findViewById(R.id.lokasiPeriksaUnit)
        val itemJenisKerusakan: TextView = itemView.findViewById(R.id.listJenisKerusakan)
        val itemfotoKerusakan: TextView = itemView.findViewById(R.id.listFotoKerusakan)
        val itemStatusArchive: TextView = itemView.findViewById(R.id.statusArchive)
        val itemLastUpdate: TextView = itemView.findViewById(R.id.lastUpdate)

        val deleteButton: FloatingActionButton = itemView.findViewById(R.id.fbDelData)


        private lateinit var currentItem: LaporP2HModel

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDeleteClick(position, currentItem)
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
        return ViewHolder(view, onDeleteClickListener)
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position)
        holder.bind(item)
        holder.itemTitlePeriksaUnit.text = "${position + 1}. ${item.jenis_unit} ${item.unit_kerja} ${item.type_unit}"
        holder.itemLokasiPeriksaUnit.text = ": ${item.unit_kerja}"
        val tes = AppUtils.getCurrentDate()
        var textStatusArchive = "Tersimpan - "
        var textLastUpdate = "${item.tanggal_upload}"
        if (item.archive == 1){
            textStatusArchive = "Terupload - "
            textLastUpdate = "${item.uploaded_time}"
            holder.itemStatusArchive.setTextColor(ContextCompat.getColor(context, R.color.greenbutton))
            holder.itemLastUpdate.setTextColor(ContextCompat.getColor(context, R.color.greenbutton))
        }
        holder.itemStatusArchive.text = textStatusArchive
        holder.itemLastUpdate.text = textLastUpdate

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
}