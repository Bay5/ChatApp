package com.bay.chatapp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.data.entity.AppUser
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class RequestAdapter(
    private var users: List<AppUser>,
    private val onAccept: (AppUser) -> Unit,
    private val onReject: (AppUser) -> Unit
) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    fun submitList(newUsers: List<AppUser>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ShapeableImageView = itemView.findViewById(R.id.imgAvatar)
        private val tvDisplayName: TextView = itemView.findViewById(R.id.tvDisplayName)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        private val btnReject: ImageButton = itemView.findViewById(R.id.btnReject)

        fun bind(user: AppUser) {
            tvDisplayName.text = if (user.displayName.isNotBlank()) user.displayName else user.username
            tvUsername.text = "@${user.username}"
            
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_person_24)
                .into(imgAvatar)

            btnAccept.setOnClickListener { onAccept(user) }
            btnReject.setOnClickListener { onReject(user) }
        }
    }
}
