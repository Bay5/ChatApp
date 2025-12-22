package com.bay.chatapp.data.Repository

import com.bay.chatapp.data.entity.AppUser
import com.bay.chatapp.data.local.ChatAppDatabase
import com.bay.chatapp.data.local.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val local = ChatAppDatabase.get()
    private val userDao = local.userDao()
    private val repoScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    fun searchUsersByUsername(
        query: String,
        onResult: (List<AppUser>, String?) -> Unit
    ) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            onResult(emptyList(), null)
            return
        }

        val currentUid = auth.currentUser?.uid

        db.collection("users")
            .orderBy("username")
            .startAt(trimmed)
            .endAt(trimmed + "\uf8ff")
            .limit(20)
            .get()
            .addOnSuccessListener { snap ->
                val users = snap.documents
                    .mapNotNull { it.toObject(AppUser::class.java) }
                    .filter { it.uid != currentUid }
                repoScope.launch {
                    userDao.upsertAll(users.map {
                        UserEntity(
                            uid = it.uid,
                            username = it.username,
                            displayName = it.displayName,
                            email = it.email,
                            photoUrl = it.photoUrl
                        )
                    })
                }
                onResult(users, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }

    fun getUsersByIds(
        uids: List<String>,
        onResult: (List<AppUser>, String?) -> Unit
    ) {
        if (uids.isEmpty()) {
            onResult(emptyList(), null)
            return
        }

        val limited = uids.take(10)

        db.collection("users")
            .whereIn("uid", limited)
            .get()
            .addOnSuccessListener { snap ->
                val users = snap.documents
                    .mapNotNull { it.toObject(AppUser::class.java) }
                repoScope.launch {
                    userDao.upsertAll(users.map {
                        UserEntity(
                            uid = it.uid,
                            username = it.username,
                            displayName = it.displayName,
                            email = it.email,
                            photoUrl = it.photoUrl
                        )
                    })
                }
                onResult(users, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }
}
