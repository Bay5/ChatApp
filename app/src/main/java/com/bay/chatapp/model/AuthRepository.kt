package com.bay.chatapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerWithEmail(
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // 1. Check if username exists
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    onResult(false, "Username already taken")
                    return@addOnSuccessListener
                }

                // 2. Create auth account
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val uid = result.user!!.uid

                        val user = AppUser(
                            uid = uid,
                            username = username,
                            displayName = username,   // or "" if you want
                            email = email,
                            photoUrl = result.user?.photoUrl?.toString() ?: ""
                        )

                        // 3. Write user profile
                        db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener { onResult(true, null) }
                            .addOnFailureListener { e -> onResult(false, e.message) }
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
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun loginWithGoogle(
        idToken: String,
        onResult: (success: Boolean, needUsername: Boolean, email: String?, error: String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser == null) {
                    onResult(false, false, null, "Firebase user is null")
                    return@addOnSuccessListener
                }

                val uid = firebaseUser.uid
                val email = firebaseUser.email
                val photoUrl = firebaseUser.photoUrl?.toString() ?: ""

                val userDoc = db.collection("users").document(uid)

                userDoc.get()
                    .addOnSuccessListener { snap ->
                        if (!snap.exists()) {
                            // First-time Google login → create document with empty username
                            val appUser = AppUser(
                                uid = uid,
                                username = "",
                                email = email ?: "",
                                photoUrl = photoUrl
                            )

                            userDoc.set(appUser)
                                .addOnSuccessListener {
                                    // username is empty → needs username screen
                                    onResult(true, true, email, null)
                                }
                                .addOnFailureListener { e ->
                                    onResult(false, false, null, e.message)
                                }

                        } else {
                            val username = snap.getString("username") ?: ""
                            if (username.isBlank()) {
                                // user exists but no username → choose username screen
                                onResult(true, true, email, null)
                            } else {
                                // complete profile → go to main
                                onResult(true, false, email, null)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, false, null, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, false, null, e.message)
            }
    }


    fun setUsernameForCurrentUser(username: String, onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "No logged in user")
            return
        }

        // Check if username already used
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val takenByOther = snap.documents.any { it.id != currentUser.uid }
                if (takenByOther) {
                    onResult(false, "Username already taken")
                    return@addOnSuccessListener
                }

                // Update this user's doc
                db.collection("users")
                    .document(currentUser.uid)
                    .update("username", username)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { e -> onResult(false, e.message) }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
}