package com.example.task.ui.picture.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.task.R
import com.example.task.base.BaseViewHolder
import com.example.task.model.Location
import com.example.task.model.Picture


class PictureAdapter(
    pictures: List<Picture>
) : RecyclerView.Adapter<PictureAdapter.ViewHolder>() {

    var onItemClick: ((Picture) -> Unit)? = null

    var pictures: List<Picture> = pictures
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_picture, parent, false)
        )

    override fun getItemCount(): Int = pictures.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind()

    inner class ViewHolder(itemView: View) : BaseViewHolder(itemView) {

        override fun bind() {
            val picture = pictures[adapterPosition]
            itemView.apply {

                Log.e("Path", picture.path)

                Glide
                    .with(context)
                    .load(picture.path)
                    .centerCrop()
                    .into(itemView.findViewById<AppCompatImageView>(R.id.picView))

                itemView.setOnClickListener {
                    onItemClick!!.invoke(pictures[adapterPosition])
                }
            }
        }
    }
}