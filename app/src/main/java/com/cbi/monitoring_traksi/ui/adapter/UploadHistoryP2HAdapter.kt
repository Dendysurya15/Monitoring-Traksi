package com.cbi.monitoring_traksi.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
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
                    onDeleteClickListener.onDeleteClick(currentItem)
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

        holder.itemTitlePeriksaUnit.text = "${position + 1}. ${item.jenis_unit} ${item.unit_kerja} ${item.type_unit}"
        holder.itemLokasiPeriksaUnit.text = ": ${item.unit_kerja}"
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


//        when (position % 5) {
//            0 -> {
//                holder.viewList.backgroundTintList =
//                    ContextCompat.getColorStateList(context, R.color.colorPrimary)
//                holder.iconList.setColorFilter(
//                    context.resources.getColor(
//                        R.color.colorPrimary,
//                        null
//                    ), PorterDuff.Mode.SRC_IN
//                )
//            }
//            1 -> {
//                holder.viewList.backgroundTintList =
//                    ContextCompat.getColorStateList(context, R.color.list1)
//                holder.iconList.setColorFilter(
//                    context.resources.getColor(
//                        R.color.list1,
//                        null
//                    ), PorterDuff.Mode.SRC_IN
//                )
//            }
//            2 -> {
//                holder.viewList.backgroundTintList =
//                    ContextCompat.getColorStateList(context, R.color.list2)
//                holder.iconList.setColorFilter(
//                    context.resources.getColor(
//                        R.color.list2,
//                        null
//                    ), PorterDuff.Mode.SRC_IN
//                )
//            }
//            3 -> {
//                holder.viewList.backgroundTintList =
//                    ContextCompat.getColorStateList(context, R.color.list3)
//                holder.iconList.setColorFilter(
//                    context.resources.getColor(
//                        R.color.list3,
//                        null
//                    ), PorterDuff.Mode.SRC_IN
//                )
//            }
//            4 -> {
//                holder.viewList.backgroundTintList =
//                    ContextCompat.getColorStateList(context, R.color.list4)
//                holder.iconList.setColorFilter(
//                    context.resources.getColor(
//                        R.color.list4,
//                        null
//                    ), PorterDuff.Mode.SRC_IN
//                )
//            }
//        }


//        holder.itemDate.text = AppUtils.formatDate(item.no_daily.substring(0, 11))


//        val sortedList = currentList.sortedWith(
//            if (isDescendingOrder) compareByDescending<HistoryP2HViewModel> {
//                it.id
//            } else compareBy<HistoryP2HViewModel> {
//                it.id
//            }
//        )
//        val isLastItemInSorted = sortedList.indexOf(item) == sortedList.size - 1
//
//        val marginBottom = if (isLastItemInSorted) {
//            50
//        } else {
//            0
//        }
//
//        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
//        layoutParams.bottomMargin = marginBottom
//        holder.itemView.layoutParams = layoutParams
//
//        val animation = AnimationUtils.loadAnimation(holder.itemView.context, android.R.anim.slide_in_left)
//        holder.itemView.startAnimation(animation)
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

//    @SuppressLint("NotifyDataSetChanged")
//    fun toggleSortingOrder() {
//        isDescendingOrder = !isDescendingOrder
//        submitList(
//            currentList.sortedWith(
//                if (isDescendingOrder) compareByDescending<HistoryP2HViewModel> {
//                    it.id
//                } else compareBy<HistoryP2HViewModel> {
//                    it.id
//                }
//            )
//        )
//        notifyDataSetChanged()
//    }

    interface OnDeleteClickListener {
        fun onDeleteClick(item: LaporP2HModel)
    }
}