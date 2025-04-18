package com.aralhub.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aralhub.ui.databinding.ItemOfferBinding
import com.aralhub.ui.model.OfferItem
import com.aralhub.ui.model.OfferItemDiffCallback
import com.aralhub.ui.utils.GlideEx

class OfferItemAdapter :
    ListAdapter<OfferItem, OfferItemAdapter.ViewHolder>(OfferItemDiffCallback) {

    private var onItemAcceptClickListener: ((OfferItem) -> Unit) = {}
    fun setOnItemAcceptClickListener(listener: (OfferItem) -> Unit) {
        onItemAcceptClickListener = listener
    }

    private var onItemDeclineClickListener: (OfferItem, Int) -> Unit = { offerItem, position -> }
    fun setOnItemDeclineClickListener(listener: (OfferItem, Int) -> Unit = { offerItem, position -> }) {
        onItemDeclineClickListener = listener
    }

    inner class ViewHolder(private val itemOfferBinding: ItemOfferBinding) :
        RecyclerView.ViewHolder(itemOfferBinding.root) {
        fun bind(offerItem: OfferItem) {
            itemOfferBinding.btnAccept.setOnClickListener {
                onItemAcceptClickListener.invoke(offerItem)
            }

            itemOfferBinding.btnDecline.setOnClickListener {
                onItemDeclineClickListener.invoke(offerItem, adapterPosition)
            }

            Log.i("OfferItemAdapter", "Expiration time: ${offerItem.expiresAt}")
            itemOfferBinding.btnAccept.setExpirationTime(offerItem.expiresAt)

            itemOfferBinding.apply {
                tvDriverName.text = offerItem.driver.name
                tvPrice.text = offerItem.offeredPrice + " som"
                tvRating.text = "${offerItem.driver.rating} ★"
                tvTime.text = offerItem.timeToArrive
                tvCar.text = offerItem.driver.carName

                GlideEx.displayAvatar(
                    offerItem.driver.avatar,
                    itemOfferBinding.ivAvatar,
                )
            }
        }
    }

    fun removeItem(position: Int) {
        val currentList = currentList.toMutableList()
        currentList.removeAt(position)
        submitList(currentList)
    }

    fun removeItem(offerItem: OfferItem) {
        val currentList = currentList.toMutableList()
        val position = currentList.indexOfFirst { it.id == offerItem.id }
        if (position != -1) {
            currentList.removeAt(position)
            submitList(currentList)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))
}