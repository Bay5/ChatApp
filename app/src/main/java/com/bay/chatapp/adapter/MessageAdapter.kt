package com.bay.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private var items: List<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    companion object {
        private const val VIEW_TYPE_ME = 1
        private const val VIEW_TYPE_OTHER = 2
    }

    fun submitList(newItems: List<ChatMessage>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val msg = items[position]
        return if (msg.senderId == currentUid) VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ME) {
            val v = inflater.inflate(R.layout.item_message_me, parent, false)
            MeViewHolder(v)
        } else {
            val v = inflater.inflate(R.layout.item_message_other, parent, false)
            OtherViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        if (holder is MeViewHolder) holder.bind(msg)
        if (holder is OtherViewHolder) holder.bind(msg)
    }

    override fun getItemCount(): Int = items.size

    inner class MeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvMessageMe)
        fun bind(msg: ChatMessage) {
            tvText.text = msg.text
        }
    }

    inner class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvMessageOther)
        fun bind(msg: ChatMessage) {
            tvText.text = msg.text
        }
    }
}
