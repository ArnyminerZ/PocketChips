package com.arnyminerz.pocketchips.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.arnyminerz.pocketchips.connections.ClientSetupManager
import com.arnyminerz.pocketchips.databinding.EndpointAvailableItemBinding
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo

class AvailableEndpointsAdapter(
    private val viewModel: ClientSetupManager
) : ListAdapter<Pair<String, DiscoveredEndpointInfo>, AvailableEndpointsAdapter.VH>(
    object : DiffUtil.ItemCallback<Pair<String, DiscoveredEndpointInfo>>() {
        override fun areContentsTheSame(
            oldItem: Pair<String, DiscoveredEndpointInfo>,
            newItem: Pair<String, DiscoveredEndpointInfo>
        ): Boolean = oldItem.first == newItem.first && oldItem.second.equals(newItem)

        override fun areItemsTheSame(
            oldItem: Pair<String, DiscoveredEndpointInfo>,
            newItem: Pair<String, DiscoveredEndpointInfo>
        ): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(
            viewModel,
            EndpointAvailableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (endpointId, endpointInfo) = getItem(position)
        holder.bind(endpointId, endpointInfo)
    }


    class VH(
        private val viewModel: ClientSetupManager,
        private val binding: EndpointAvailableItemBinding
    ) : ViewHolder(binding.root) {
        fun bind(endpointId: String, endpointInfo: DiscoveredEndpointInfo) {
            binding.model = viewModel
            binding.deviceName = endpointInfo.endpointName
            binding.endpointId = endpointId
        }
    }
}