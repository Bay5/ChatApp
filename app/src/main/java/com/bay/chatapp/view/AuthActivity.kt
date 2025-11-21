package com.bay.chatapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bay.chatapp.R
import com.bay.chatapp.adapter.AuthPagerAdapter
import com.bay.chatapp.viewmodel.AuthState
import com.bay.chatapp.viewmodel.AuthViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        val root: View = findViewById(R.id.rootAuth)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        progressBar = findViewById(R.id.progressAuth)

        val adapter = AuthPagerAdapter(this)
        viewPager.adapter = adapter

        val tabTitles = listOf("Login", "Register")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // 🔹 ViewModel
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Idle -> {
                    progressBar.visibility = View.GONE
                }
                is AuthState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is AuthState.Success -> {
                    progressBar.visibility = View.GONE
                    openMain()
                }
                is AuthState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 🔹 Auto-skip auth if already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            openMain()
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
