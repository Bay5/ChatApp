package com.bay.chatapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
                onResult(users, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }
}
