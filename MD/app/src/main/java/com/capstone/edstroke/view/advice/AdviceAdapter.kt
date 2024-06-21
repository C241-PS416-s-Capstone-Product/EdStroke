package com.capstone.edstroke.view.advice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.capstone.edstroke.data.response.AdviceResponse
import com.capstone.edstroke.databinding.ItemAdviceBinding

class AdviceAdapter(private val adviceList: List<AdviceResponse>) : RecyclerView.Adapter<AdviceAdapter.AdviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdviceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAdviceBinding.inflate(inflater, parent, false)
        return AdviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdviceViewHolder, position: Int) {
        holder.bind(adviceList[position])
    }

    override fun getItemCount(): Int = adviceList.size

    inner class AdviceViewHolder(private val binding: ItemAdviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(adviceItem: AdviceResponse) {
            binding.tvDescription.text = adviceItem.advice
        }
    }
}
