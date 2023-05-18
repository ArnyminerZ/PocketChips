package com.arnyminerz.pocketchips.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.arnyminerz.pocketchips.connections.HostConnectionsManager
import com.arnyminerz.pocketchips.databinding.EndpointAwaitingItemBinding
import com.google.android.gms.nearby.connection.ConnectionInfo

class AwaitingEndpointsAdapter(
    private val viewModel: HostConnectionsManager
) : ListAdapter<Pair<String, ConnectionInfo>, AwaitingEndpointsAdapter.VH>(
    object : DiffUtil.ItemCallback<Pair<String, ConnectionInfo>>() {
        override fun areContentsTheSame(
            oldItem: Pair<String, ConnectionInfo>,
            newItem: Pair<String, ConnectionInfo>
        ): Boolean = oldItem.first == newItem.first && oldItem.second.equals(newItem)

        override fun areItemsTheSame(
            oldItem: Pair<String, ConnectionInfo>,
            newItem: Pair<String, ConnectionInfo>
        ): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(
            viewModel,
            EndpointAwaitingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (endpointId, connectionInfo) = getItem(position)
        holder.bind(endpointId, connectionInfo)
    }


    class VH(
        private val viewModel: HostConnectionsManager,
        private val binding: EndpointAwaitingItemBinding
    ) : ViewHolder(binding.root) {
        fun bind(endpointId: String, connectionInfo: ConnectionInfo) {
            binding.model = viewModel
            binding.deviceName = connectionInfo.endpointName
            binding.endpointId = endpointId
        }
    }
}