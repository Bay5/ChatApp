package com.bay.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.model.ChatMessage

class MessageAdapter(
    private var items: List<ChatMessage>,
    private val currentUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_ME = 1
        private const val VIEW_OTHER = 2
    }

    fun submitList(newItems: List<ChatMessage>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val msg = items[position]
        return if (msg.fromUid == currentUid) VIEW_ME else VIEW_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_ME) {
            val v = inflater.inflate(R.layout.item_message_me, parent, false)
            MeViewHolder(v)
        } else {
            val v = inflater.inflate(R.layout.item_message_other, parent, false)
            OtherViewHolder(v)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        if (holder is MeViewHolder) holder.bind(msg)
        else if (holder is OtherViewHolder) holder.bind(msg)
    }

    inner class MeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMsg: TextView = itemView.findViewById(R.id.tvMessageMe)
        fun bind(msg: ChatMessage) {
            tvMsg.text = msg.text
        }
    }

    inner class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMsg: TextView = itemView.findViewById(R.id.tvMessageOther)
        fun bind(msg: ChatMessage) {
            tvMsg.text = msg.text
        }
    }
}
