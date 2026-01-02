package com.bay.chatapp.view.Auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bay.chatapp.R
import com.bay.chatapp.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

class ChooseUsernameFragment : Fragment() {

    private lateinit var etDisplayName: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_choose_username, container, false)

        etDisplayName = view.findViewById(R.id.etDisplayName)
        etUsername = view.findViewById(R.id.etUsername)
        btnSave = view.findViewById(R.id.btnSaveUsername)

        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim().orEmpty()
            val displayName = etDisplayName.text.toString().trim().orEmpty()

            if (username.isEmpty()) {
                Toast.makeText(requireContext(), "Username required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (displayName.isEmpty()) {
                Toast.makeText(requireContext(), "Display Name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.completeProfile(username, displayName)
        }
        return view
    }
}