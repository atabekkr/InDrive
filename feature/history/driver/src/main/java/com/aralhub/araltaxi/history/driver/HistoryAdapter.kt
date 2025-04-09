package com.aralhub.araltaxi.history.driver

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aralhub.ui.databinding.ItemDriverHistoryBinding
import com.aralhub.ui.model.RideHistoryUI
import com.aralhub.ui.utils.setOnSafeClickListener
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class HistoryAdapter : ListAdapter<RideHistoryUI, HistoryAdapter.HistoryItemViewHolder>(
    HistoryItemDiffCallback
) {

    private var onItemClickListener: ((RideHistoryUI) -> Unit) = {}
    fun setOnItemClickListener(listener: (RideHistoryUI) -> Unit) {
        onItemClickListener = listener
    }

    inner class HistoryItemViewHolder(private val binding: ItemDriverHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RideHistoryUI) {

            binding.tvPrice.text = binding.root.context.getString(
                com.aralhub.ui.R.string.standard_uzs_price,
                (item.amount + item.waitAmount).toString()
            )

            val rideInfo = StringBuilder()
            val dateTime = OffsetDateTime.parse(item.createdAt)
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val timeOnly = formatter.format(dateTime)
            rideInfo.append(
                "$timeOnly, ",
                item.locations.getOrNull(0)?.name,
                " → ",
                item.locations.getOrNull(1)?.name
            )
            binding.tvInfo.text = rideInfo

            binding.root.setOnSafeClickListener {
                onItemClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val binding =
            ItemDriverHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object HistoryItemDiffCallback : DiffUtil.ItemCallback<RideHistoryUI>() {
        override fun areItemsTheSame(oldItem: RideHistoryUI, newItem: RideHistoryUI): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: RideHistoryUI, newItem: RideHistoryUI): Boolean {
            return oldItem == newItem
        }
    }

}