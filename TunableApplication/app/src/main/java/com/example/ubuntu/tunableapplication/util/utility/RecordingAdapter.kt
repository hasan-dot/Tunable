package com.example.ubuntu.tunableapplication.util.utility

import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.ubuntu.tunableapplication.R
import com.example.ubuntu.tunableapplication.util.models.RecordItem
import com.example.ubuntu.tunableapplication.util.models.RecordListener


class RecordingAdapter(var context: Context, var data: ArrayList<RecordItem>?, var listener: RecordListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.recording_item, parent, false)
        return Item(view)
    }

    override fun getItemCount(): Int {
        return if (data == null) 0 else data!!.size
    }

    fun removeAt(position: Int) {
        data!!.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Item) {
            holder.title.text = data!![position].title
            holder.duration.text = data!![position].duration
            holder.caption.text = data!![position].caption
        }
    }

    inner class Item(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var duration: TextView = itemView.findViewById(R.id.time)
        var caption: TextView = itemView.findViewById(R.id.caption)
        private var play: FloatingActionButton = itemView.findViewById(R.id.btn_play)

        init {
            var toggle = true
            play.setOnClickListener { v ->
                if (toggle) {
                    toggle = false
                    listener.onClickPlay(v, layoutPosition)
                }
                else {
                    toggle = true
                    listener.onClickStop(v, layoutPosition)
                }
            }
        }

    }
}

