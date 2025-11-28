package com.bay.chatapp.view.Auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bay.chatapp.R
import com.bay.chatapp.adapter.AuthPagerAdapter
import com.bay.chatapp.view.MainActivity
import com.bay.chatapp.viewmodel.AuthState
import com.bay.chatapp.viewmodel.AuthViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView
    private lateinit var usernameContainer: FrameLayout

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

        tvTitle = findViewById(R.id.tvTitle)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        progressBar = findViewById(R.id.progressAuth)
        usernameContainer = findViewById(R.id.usernameContainer)

        val adapter = AuthPagerAdapter(this)
        viewPager.adapter = adapter

        val tabTitles = listOf("Login", "Register")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

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

                is AuthState.NeedsUsername -> {
                    progressBar.visibility = View.GONE
                    openChooseUsernameFragment()
                }

                is AuthState.PasswordResetSent -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Reset password link sent. Check your email.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is AuthState.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        state.message ?: "Unknown error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            openMain()
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun openChooseUsernameFragment() {
        // hide login/register UI
        tvTitle.visibility = View.GONE
        tabLayout.visibility = View.GONE
        viewPager.visibility = View.GONE

        // show container and put fragment inside
        usernameContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.usernameContainer, ChooseUsernameFragment())
            .commit()
    }
}
