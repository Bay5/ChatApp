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
import com.bay.chatapp.viewmodel.UserSearchViewModel
import com.google.android.material.textfield.TextInputEditText

class SearchUserActivity : AppCompatActivity() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvUsers: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: UserSearchViewModel
    private lateinit var adapter: UserAdapter

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
            openChat(user)
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        viewModel = ViewModelProvider(this)[UserSearchViewModel::class.java]

        viewModel.users.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.loading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun openChat(user: AppUser) {
        // Start ChatActivity, pass uid + username
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("otherUid", user.uid)
            putExtra("otherUsername", user.username)
            putExtra("otherPhotoUrl", user.photoUrl)
        }
        startActivity(intent)
        finish()  // optional
    }
}