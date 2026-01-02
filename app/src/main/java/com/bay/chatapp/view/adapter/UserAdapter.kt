package com.bay.chatapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.data.entity.AppUser
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide

class UserAdapter(
    private var items: List<AppUser>,
    private val onClick: (AppUser) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun submitList(newItems: List<AppUser>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ShapeableImageView = itemView.findViewById(R.id.imgAvatar)
        private val tvDisplayName: TextView = itemView.findViewById(R.id.tvDisplayName)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)

        fun bind(user: AppUser) {
            val nameToShow = if (user.displayName.isNotBlank()) {
                user.displayName
            } else if (user.username.isNotBlank()) {
                user.username
            } else {
                user.email
            }
            tvDisplayName.text = nameToShow

            tvUsername.text = if (user.username.isNotBlank()) {
                "@${user.username}"
            } else {
                ""
            }

            if (user.photoUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(user.photoUrl)
                    .centerCrop()
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.baseline_person_24)
            }

            itemView.setOnClickListener {
                onClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
