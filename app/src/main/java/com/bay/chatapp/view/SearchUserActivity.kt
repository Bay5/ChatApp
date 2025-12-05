package com.bay.chatapp.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.adapter.UserAdapter
import com.bay.chatapp.model.AppUser
import com.bay.chatapp.model.Contact
import com.bay.chatapp.viewmodel.ContactViewModel
import com.bay.chatapp.viewmodel.UserSearchViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class SearchUserActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvUsers: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var userSearchViewModel: UserSearchViewModel
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var adapter: UserAdapter

    private var selectedUser: AppUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_user)

        val root: View = findViewById(R.id.rootSearchUser)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etSearch = findViewById(R.id.etSearch)
        rvUsers = findViewById(R.id.rvUsers)
        progressBar = findViewById(R.id.progressSearch)

        adapter = UserAdapter(emptyList()) { user ->
            onUserClicked(user)
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        userSearchViewModel = ViewModelProvider(this)[UserSearchViewModel::class.java]
        contactViewModel = ViewModelProvider(this)[ContactViewModel::class.java]

        // observe search results
        userSearchViewModel.users.observe(this) { list ->
            adapter.submitList(list)
        }

        userSearchViewModel.loading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        userSearchViewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        contactViewModel.contactWithUser.observe(this) { contact ->
            val user = selectedUser ?: return@observe
            handleContactStatus(user, contact)
        }

        contactViewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userSearchViewModel.search(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onUserClicked(user: AppUser) {
        selectedUser = user
        contactViewModel.checkContact(user.uid)
    }

    private fun handleContactStatus(user: AppUser, contact: Contact?) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()

        when {
            contact == null || contact.contactStatus == "" || contact.contactStatus == "REJECTED" -> {
                AlertDialog.Builder(this)
                    .setTitle(user.displayName.ifBlank { user.username })
                    .setMessage("Send contact request to @${user.username}?")
                    .setPositiveButton("Send") { _, _ ->
                        contactViewModel.sendRequest(user.uid)
                        Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            contact.contactStatus == "ACCEPTED" -> {
                openChat(user)
            }

            contact.contactStatus == "REQUESTED" -> {
                val youAreSender = contact.requestedBy == currentUid

                if (youAreSender) {
                    Toast.makeText(this, "Request already sent", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(user.displayName.ifBlank { user.username })
                        .setMessage("@${user.username} wants to add you as a contact.")
                        .setPositiveButton("Accept") { _, _ ->
                            contactViewModel.accept(contact.id)
                            Toast.makeText(this, "Contact accepted", Toast.LENGTH_SHORT).show()
                            openChat(user)
                        }
                        .setNegativeButton("Reject") { _, _ ->
                            contactViewModel.reject(contact.id)
                            Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show()
                        }
                        .show()
                }
            }
        }
    }

    private fun openChat(user: AppUser) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("otherUid", user.uid)
            putExtra("otherUsername", user.username)
            putExtra("otherPhotoUrl", user.photoUrl)
        }
        startActivity(intent)
        finish()  // optional
    }
}
