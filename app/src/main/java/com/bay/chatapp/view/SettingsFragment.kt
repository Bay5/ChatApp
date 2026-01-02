package com.bay.chatapp.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.imageview.ShapeableImageView
import androidx.fragment.app.Fragment
import com.bay.chatapp.R
import com.bay.chatapp.view.Auth.AuthActivity
import com.bay.chatapp.data.repository.AuthRepository
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvDisplayName = view.findViewById<TextView>(R.id.tvDisplayName)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val etDisplayName = view.findViewById<TextInputEditText>(R.id.etDisplayNameSettings)
        val etUsername = view.findViewById<TextInputEditText>(R.id.etUsernameSettings)
        val btnSaveProfile = view.findViewById<Button>(R.id.btnSaveProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val ivAvatar = view.findViewById<ShapeableImageView>(R.id.ivAvatar)
        val switchDarkMode = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchDarkMode)

        // Initialize Switch state
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDarkMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()
            val mode = if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            } else {
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            }
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
        }

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val repo = AuthRepository()

        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { snap ->
                    val displayName = snap.getString("displayName").orEmpty()
                    val username = snap.getString("username").orEmpty()
                    val photo = snap.getString("photoUrl").orEmpty()
                    etDisplayName.setText(displayName)
                    etUsername.setText(username)
                    if (photo.isNotBlank()) {
                        Glide.with(requireContext())
                            .load(photo)
                            .centerCrop()
                            .into(ivAvatar)
                    } else {
                        ivAvatar.setImageResource(R.drawable.baseline_person_24)
                    }
                }
                .addOnFailureListener {
                    ivAvatar.setImageResource(R.drawable.baseline_person_24)
                }
        } else {
            ivAvatar.setImageResource(R.drawable.baseline_person_24)
        }

        btnSaveProfile.setOnClickListener {
            val name = etDisplayName.text?.toString()?.trim().orEmpty()
            val uname = etUsername.text?.toString()?.trim().orEmpty()
            if (name.isBlank() && uname.isBlank()) {
                Toast.makeText(requireContext(), "Nothing to save", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (name.isNotBlank()) {
                repo.setDisplayNameForUser(name) { ok, err ->
                    if (ok) {
                        Toast.makeText(requireContext(), "Display Name updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), err ?: "Failed to update name", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (uname.isBlank()) return@setOnClickListener
            val currentUid = auth.currentUser?.uid
            if (currentUid == null) {
                Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.collection("users").document(currentUid).get()
                .addOnSuccessListener { snap ->
                    val lastChanged = snap.getLong("usernameChangedAt") ?: 0L
                    val now = System.currentTimeMillis()
                    val daysSince = if (lastChanged > 0) {
                        TimeUnit.MILLISECONDS.toDays(now - lastChanged)
                    } else Long.MAX_VALUE
                    val currentUsername = snap.getString("username").orEmpty()
                    val allowed = currentUsername.isBlank() || daysSince >= 30
                    if (!allowed) {
                        val remainingDays = 30 - daysSince
                        Toast.makeText(
                            requireContext(),
                            "You can change username in $remainingDays days",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addOnSuccessListener
                    }

                    repo.setUsernameForUser(uname) { ok, err ->
                        if (ok) {
                            db.collection("users").document(currentUid)
                                .update("usernameChangedAt", now)
                            Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), err ?: "Failed to update username", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), e.message ?: "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }

        btnLogout.setOnClickListener {
            // 1) Firebase logout
            FirebaseAuth.getInstance().signOut()

            // 2) Google logout (safe even if user used email/password only)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(requireContext(), gso)
            googleClient.signOut().addOnCompleteListener {
                // 3) Go back to auth screen
                val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
    }
}
