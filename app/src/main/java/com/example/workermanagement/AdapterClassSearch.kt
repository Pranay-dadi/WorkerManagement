package com.example.workermanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import org.w3c.dom.Text

class AdapterClassSearch(private val workList: List<WorkData>) : RecyclerView.Adapter<AdapterClassSearch.MyViewHolder>() {
    class MyViewHolder(itemView : View):RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.tvNames)
        val phone: TextView = itemView.findViewById(R.id.tvPhones)
        val pin: TextView = itemView.findViewById(R.id.tvPins)
        val wage: TextView = itemView.findViewById(R.id.tvWages)
        val type: TextView = itemView.findViewById(R.id.tvTypes)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterClassSearch.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.works, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterClassSearch.MyViewHolder, position: Int) {
        val work = workList[position]

        holder.name.text = work.name
        holder.phone.text = work.phone
        holder.pin.text = work.pin
        holder.wage.text = work.wage
        holder.type.text = work.type

    }

    override fun getItemCount(): Int {
        return workList.size
    }
}