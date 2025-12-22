package com.bay.chatapp.data.Repository

import com.bay.chatapp.data.entity.AppUser
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
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    onResult(false, "Username already taken")
                    return@addOnSuccessListener
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val uid = result.user!!.uid

                        val user = AppUser(
                            uid = uid,
                            username = username,
                            displayName = username,
                            email = email,
                            photoUrl = result.user?.photoUrl?.toString() ?: ""
                        )

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

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
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
                            val appUser = AppUser(
                                uid = uid,
                                username = "",
                                email = email ?: "",
                                photoUrl = photoUrl
                            )

                            userDoc.set(appUser)
                                .addOnSuccessListener {
                                    onResult(true, true, email, null)
                                }
                                .addOnFailureListener { e ->
                                    onResult(false, false, null, e.message)
                                }

                        } else {
                            val username = snap.getString("username") ?: ""
                            if (username.isBlank()) {
                                onResult(true, true, email, null)
                            } else {
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

    fun setUsernameForUser(username: String, onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "No logged in user")
            return
        }

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

    fun setDisplayNameForUser(displayName: String, onResult: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "No logged in user")
            return
        }

        db.collection("users")
            .whereEqualTo("displayName", displayName)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                db.collection("users")
                    .document(currentUser.uid)
                    .update("displayName", displayName)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { e -> onResult(false, e.message) }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
}