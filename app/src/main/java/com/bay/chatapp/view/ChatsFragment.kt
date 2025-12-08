package com.bay.chatapp.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bay.chatapp.R
import com.bay.chatapp.adapter.ChatListAdapter
import com.bay.chatapp.model.ChatItem
import com.bay.chatapp.viewmodel.ChatListViewModel

class ChatsFragment : Fragment() {

    private lateinit var rvChats: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: ChatListViewModel
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvChats = view.findViewById(R.id.rvChats)
        progressBar = view.findViewById(R.id.progressBarChats) // optional if you add it, or remove

        adapter = ChatListAdapter(emptyList()) { chatItem ->
            openChat(chatItem)
        }

        rvChats.layoutManager = LinearLayoutManager(requireContext())
        rvChats.adapter = adapter

        viewModel = ViewModelProvider(this)[ChatListViewModel::class.java]

        viewModel.chats.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.loadChats()
    }

    private fun openChat(item: ChatItem) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("otherUid", item.otherUid)
            putExtra("otherUsername", item.otherUsername)
            putExtra("otherPhotoUrl", item.otherPhotoUrl)
        }
        startActivity(intent)
    }
}
