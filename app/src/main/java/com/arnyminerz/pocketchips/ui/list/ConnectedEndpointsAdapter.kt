package com.arnyminerz.pocketchips.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.arnyminerz.pocketchips.databinding.EndpointConnectedItemBinding
import com.google.android.gms.nearby.connection.ConnectionInfo

class ConnectedEndpointsAdapter : ListAdapter<String, ConnectedEndpointsAdapter.VH>(
    object : DiffUtil.ItemCallback<String>() {
        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean = oldItem == newItem

        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(
            EndpointConnectedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val endpointId = getItem(position)
        holder.bind(endpointId)
    }


    class VH(
        private val binding: EndpointConnectedItemBinding
    ) : ViewHolder(binding.root) {
        fun bind(endpointId: String) {
            binding.deviceName = endpointId
        }
    }
}