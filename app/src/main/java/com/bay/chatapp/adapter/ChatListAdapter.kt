package com.bay.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.model.ChatItem
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private var items: List<ChatItem>,
    private val onClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    fun submitList(newItems: List<ChatItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ShapeableImageView = itemView.findViewById(R.id.imgChatAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvChatName)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvChatLastMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvUnread: TextView = itemView.findViewById(R.id.tvUnreadBadge)

        fun bind(item: ChatItem) {
            val name = item.otherDisplayName.ifBlank {
                item.otherUsername.ifBlank { "Unknown" }
            }
            tvName.text = name
            tvLastMessage.text = item.lastMessage
            tvTimestamp.text = if (item.lastTimestamp > 0L) formatTime(item.lastTimestamp) else ""

            if (item.unreadCount > 0) {
                tvUnread.visibility = View.VISIBLE
                tvUnread.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
            } else {
                tvUnread.visibility = View.GONE
            }

            if (item.otherPhotoUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(item.otherPhotoUrl)
                    .centerCrop()
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.baseline_person_24)
            }

            itemView.setOnClickListener {
                onClick(item)
            }
        }

        private fun formatTime(millis: Long): String {
            val df = SimpleDateFormat("HH.mm", Locale.getDefault())
            return df.format(Date(millis))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
