package com.bay.chatapp.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.view.adapter.UserAdapter
import com.bay.chatapp.data.entity.AppUser
import com.bay.chatapp.viewmodel.ContactViewModel
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import android.widget.ImageButton

class ContactsFragment : Fragment() {

    private lateinit var etSearch: TextInputEditText
    private lateinit var rvContacts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRequests: ImageButton

    private lateinit var contactViewModel: ContactViewModel
    private lateinit var adapter: UserAdapter

    // full list from ViewModel (for filtering)
    private var fullList: List<AppUser> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // inflate fragment layout
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)

        val root: View = view.findViewById(R.id.rootContacts)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etSearch = view.findViewById(R.id.etSearchContacts)
        rvContacts = view.findViewById(R.id.rvContacts)
        progressBar = view.findViewById(R.id.progressContacts)
        btnRequests = view.findViewById(R.id.btnRequests)

        btnRequests.setOnClickListener {
            startActivity(Intent(requireContext(), RequestsActivity::class.java))
        }

        adapter = UserAdapter(emptyList()) { user ->
            showProfileOptions(user)
        }
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter

        contactViewModel = ViewModelProvider(this)[ContactViewModel::class.java]

        contactViewModel.contactUsers.observe(viewLifecycleOwner) { users ->
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

        contactViewModel.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        contactViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // load contacts once fragment is created
        contactViewModel.loadContactUsers()

        return view
    }

    override fun onResume() {
        super.onResume()
        contactViewModel.loadContactUsers()
    }

    private fun showProfileOptions(user: AppUser) {
        val sheet = ContactOptions.newInstance(user)
        sheet.show(childFragmentManager, "profile_options")
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
}
