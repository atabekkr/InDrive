package com.aralhub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.aralhub.ui.utils.setOnSafeClickListener
import com.aralhub.ui.R
import com.aralhub.ui.databinding.ItemOrderBinding
import com.aralhub.ui.model.OrderItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class OrderItemAdapter :
    Adapter<OrderItemAdapter.ViewHolder>() {

    private var listOfOrders = listOf<OrderItem>()
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    private var onItemClickListener: ((OrderItem) -> Unit)? = null
    fun setOnItemClickListener(listener: (OrderItem) -> Unit) {
        onItemClickListener = listener
    }

    inner class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderItem: OrderItem) {
            val context = binding.root.context
            binding.tvClientName.text = orderItem.name
            binding.tvPickUpLocation.text = orderItem.pickUpAddress
            binding.tvDistance.text = orderItem.pickUpDistance
            binding.tvDistanceRoad.text = orderItem.roadDistance
            binding.tvPrice.text = context.getString(R.string.standard_uzs_price, orderItem.roadPrice)
            binding.ivPaymentMethod.setImageResource(orderItem.paymentType.resId)

            binding.root.setOnSafeClickListener {
                onItemClickListener?.invoke(orderItem)
            }

            Glide.with(binding.ivAvatar.context)
                .load(orderItem.avatar)
                .centerCrop()
                .placeholder(com.aralhub.ui.R.drawable.ic_user)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivAvatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = listOfOrders.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(listOfOrders[position])

    fun submitList(list: List<OrderItem>) {
        this.listOfOrders = list
    }

}