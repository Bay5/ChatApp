package com.bay.chatapp.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.bay.chatapp.R
import com.bay.chatapp.data.entity.AppUser
import com.bay.chatapp.viewmodel.ContactViewModel
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.DialogFragment
import androidx.core.graphics.drawable.toDrawable

class ProfileOptionsBottomSheet : DialogFragment() {

    companion object {
        private const val ARG_UID = "arg_uid"
        private const val ARG_USERNAME = "arg_username"
        private const val ARG_DISPLAY = "arg_display"
        private const val ARG_PHOTO = "arg_photo"

        fun newInstance(user: AppUser): ProfileOptionsBottomSheet {
            val f = ProfileOptionsBottomSheet()
            val b = Bundle()
            b.putString(ARG_UID, user.uid)
            b.putString(ARG_USERNAME, user.username)
            b.putString(ARG_DISPLAY, user.displayName)
            b.putString(ARG_PHOTO, user.photoUrl)
            f.arguments = b
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_options, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getString(ARG_UID).orEmpty()
        val username = arguments?.getString(ARG_USERNAME).orEmpty()
        val displayName = arguments?.getString(ARG_DISPLAY).orEmpty()
        val photoUrl = arguments?.getString(ARG_PHOTO).orEmpty()

        val scrim = view.findViewById<View>(R.id.rootDialogScrim)
        val img = view.findViewById<ShapeableImageView>(R.id.imgProfileAvatar)
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvUsername = view.findViewById<TextView>(R.id.tvProfileUsername)
        val btnChat = view.findViewById<MaterialButton>(R.id.btnProfileChat)
        val btnUnfriend = view.findViewById<MaterialButton>(R.id.btnProfileUnfriend)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDialog)

        val nameToShow = if (displayName.isNotBlank()) displayName
        else if (username.isNotBlank()) username else ""
        tvName.text = nameToShow
        tvUsername.text = if (username.isNotBlank()) "@$username" else ""

        if (photoUrl.isNotBlank()) {
            Glide.with(view.context)
                .load(photoUrl)
                .centerCrop()
                .into(img)
        } else {
            img.setImageResource(R.drawable.baseline_person_24)
        }

        btnChat.setOnClickListener {
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("otherUid", uid)
                putExtra("otherUsername", username)
                putExtra("otherPhotoUrl", photoUrl)
            }
            startActivity(intent)
            dismiss()
        }

        btnUnfriend.setOnClickListener {
            val vm = ViewModelProvider(requireParentFragment())[ContactViewModel::class.java]
            vm.unfriend(uid)
            dismiss()
        }

        btnClose.setOnClickListener { dismiss() }
        scrim.setOnClickListener { dismiss() }
    }
}
