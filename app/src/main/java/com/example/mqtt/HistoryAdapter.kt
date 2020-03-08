package com.example.mqtt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(dataSet: ArrayList<String>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private val history = ArrayList<String>()

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val textView : TextView = v.findViewById(R.id.row_text)
    }

    fun add(data : String) {
        history.add(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.history_row, parent, false))
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = history.get(position)
    }
}