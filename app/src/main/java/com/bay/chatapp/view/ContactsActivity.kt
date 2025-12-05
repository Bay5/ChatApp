package com.bay.chatapp.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.adapter.UserAdapter
import com.bay.chatapp.model.AppUser
import com.bay.chatapp.viewmodel.ContactViewModel
import com.google.android.material.textfield.TextInputEditText

class ContactsActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvContacts: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var contactViewModel: ContactViewModel
    private lateinit var adapter: UserAdapter

    // full list from ViewModel (for filtering)
    private var fullList: List<AppUser> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contacts)

        val root: View = findViewById(R.id.rootContacts)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etSearch = findViewById(R.id.etSearchContacts)
        rvContacts = findViewById(R.id.rvContacts)
        progressBar = findViewById(R.id.progressContacts)

        adapter = UserAdapter(emptyList()) { user ->
            openChat(user)
        }
        rvContacts.layoutManager = LinearLayoutManager(this)
        rvContacts.adapter = adapter

        contactViewModel = ViewModelProvider(this)[ContactViewModel::class.java]

        contactViewModel.contactUsers.observe(this) { users ->
            fullList = users.sortedBy { user ->
                val name = when {
                    user.displayName.isNotBlank() -> user.displayName
                    user.username.isNotBlank() -> user.username
                    else -> user.email
                }
                name.lowercase()
            }
            applyFilter(etSearch.text?.toString().orEmpty())
        }

        contactViewModel.loading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        contactViewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        contactViewModel.loadContactUsers()
    }

    private fun applyFilter(query: String) {
        if (query.isBlank()) {
            adapter.submitList(fullList)
            return
        }

        val q = query.trim().lowercase()
        val filtered = fullList.filter { user ->
            val displayName = user.displayName.ifBlank { user.username.ifBlank { user.email } }
            displayName.lowercase().contains(q)
        }
        adapter.submitList(filtered)
    }

    private fun openChat(user: AppUser) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("otherUid", user.uid)
            putExtra("otherUsername", user.username)
            putExtra("otherPhotoUrl", user.photoUrl)
        }
        startActivity(intent)
    }
}
