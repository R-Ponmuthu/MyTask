package com.example.task.ui.location.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.task.R
import com.example.task.base.BaseViewHolder
import com.example.task.model.Location


class LocationAdapter(
   locations: List<Location>
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    var locations: List<Location> = locations
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_location, parent, false))

    override fun getItemCount(): Int = locations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind()

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        override fun bind() {
            val location = locations[adapterPosition]
            itemView.apply {
                itemView.findViewById<TextView>(R.id.tvId).text = location.id.toString()
                itemView.findViewById<TextView>(R.id.tvLatitude).text = location.latitude.toString()
                itemView.findViewById<TextView>(R.id.tvLongitude).text = location.longitude.toString()
                itemView.findViewById<TextView>(R.id.tvTime).text = location.time.toString()
            }
        }
    }
}