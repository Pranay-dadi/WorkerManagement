package com.example.workermanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import org.w3c.dom.Text

class AdapterClassManage(private val workList: MutableList<WorkData>, private val workReference: DatabaseReference) : RecyclerView.Adapter<AdapterClassManage.MyViewHolder>() {
    class MyViewHolder(itemView : View):RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.tvName)
        val phone: TextView = itemView.findViewById(R.id.tvPhone)
        val pin: TextView = itemView.findViewById(R.id.tvPin)
        val wage: TextView = itemView.findViewById(R.id.tvWage)
        val tyoe: TextView = itemView.findViewById(R.id.tvType)
        val delete: Button = itemView.findViewById(R.id.deleteBtn)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterClassManage.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.work, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterClassManage.MyViewHolder, position: Int) {
        val work = workList[position]

        holder.name.text = work.name
        holder.phone.text = work.phone
        holder.pin.text = work.pin
        holder.wage.text = work.wage
        holder.tyoe.text = work.type

        holder.delete.setOnClickListener{
            workReference.child(work.id).removeValue()
            workList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,workList.size)
        }
    }

    override fun getItemCount(): Int {
        return workList.size
    }
}