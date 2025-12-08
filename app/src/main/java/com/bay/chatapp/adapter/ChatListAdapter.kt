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

        fun bind(item: ChatItem) {
            val name = item.otherDisplayName.ifBlank {
                item.otherUsername.ifBlank { "Unknown" }
            }
            tvName.text = name
            tvLastMessage.text = item.lastMessage

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
