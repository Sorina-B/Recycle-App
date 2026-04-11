package com.example.recycleapp

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recycleapp.databinding.ItemScanHistoryBinding
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val historyList: List<ScanHistory>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    class HistoryViewHolder(val binding: ItemScanHistoryBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding=ItemScanHistoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HistoryViewHolder(binding)
    }
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem=historyList[position]
        val sdf= SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date=sdf.format(Date(currentItem.timestamp))
        holder.binding.tvItemName.text=currentItem.productName
        holder.binding.tvItemDate.text=date
        holder.binding.tvItemInstructions.text=currentItem.instructions
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}

