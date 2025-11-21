package com.bay.chatapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun registerWithEmail(
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // 1. (optional) check if username is taken
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    onResult(false, "Username already taken")
                    return@addOnSuccessListener
                }

                // 2. create auth user
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val uid = result.user!!.uid
                        val appUser = AppUser(
                            uid = uid,
                            username = username,
                            displayName = username,
                            email = email,
                            photoUrl = result.user?.photoUrl?.toString() ?: ""
                        )
                        db.collection("users")
                            .document(uid)
                            .set(appUser)
                            .addOnSuccessListener {
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.message)
                            }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun loginWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
}
