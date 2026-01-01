package com.bay.chatapp.view

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.view.adapter.RequestAdapter
import com.bay.chatapp.viewmodel.ContactViewModel

class RequestsActivity : AppCompatActivity() {

    private lateinit var viewModel: ContactViewModel
    private lateinit var adapter: RequestAdapter
    private lateinit var rvRequests: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_requests)

        val root: View = findViewById(R.id.rootRequests)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarRequests)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvRequests = findViewById(R.id.rvRequests)
        progressBar = findViewById(R.id.progressRequests)
        tvEmpty = findViewById(R.id.tvEmptyRequests)

        viewModel = ViewModelProvider(this)[ContactViewModel::class.java]

        adapter = RequestAdapter(
            emptyList(),
            onAccept = { user ->
                viewModel.acceptRequestFrom(user.uid)
            },
            onReject = { user ->
                viewModel.rejectRequestFrom(user.uid)
            }
        )

        rvRequests.layoutManager = LinearLayoutManager(this)
        rvRequests.adapter = adapter

        viewModel.incomingRequests.observe(this) { users ->
            adapter.submitList(users)
            tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadIncomingRequests()
    }
}
