package com.bay.chatapp.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bay.chatapp.R
import com.bay.chatapp.model.AppUser
import com.bay.chatapp.viewmodel.ContactViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import androidx.lifecycle.ViewModelProvider

class ProfileOptionsBottomSheet : BottomSheetDialogFragment() {

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
        return inflater.inflate(R.layout.bottomsheet_profile_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getString(ARG_UID).orEmpty()
        val username = arguments?.getString(ARG_USERNAME).orEmpty()
        val displayName = arguments?.getString(ARG_DISPLAY).orEmpty()
        val photoUrl = arguments?.getString(ARG_PHOTO).orEmpty()

        val img = view.findViewById<ShapeableImageView>(R.id.imgProfileAvatar)
        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvUsername = view.findViewById<TextView>(R.id.tvProfileUsername)
        val btnChat = view.findViewById<MaterialButton>(R.id.btnProfileChat)
        val btnUnfriend = view.findViewById<MaterialButton>(R.id.btnProfileUnfriend)

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
    }
}
