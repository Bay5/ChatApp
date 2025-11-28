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
            .endAt(trimmed + "\uf8ff")   // prefix search
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
}
