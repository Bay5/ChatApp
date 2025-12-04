package com.bay.chatapp.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // generate stable doc id for pair of users
    private fun contactId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    // send / overwrite contact request
    fun sendContactRequest(toUid: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, "Not logged in")

        val sorted = listOf(currentUid, toUid).sorted()
        val id = sorted.joinToString("_")  // same as contactId(currentUid, toUid)

        val contact = Contact(
            id = id,
            userA = sorted[0],
            userB = sorted[1],
            requestedBy = currentUid,
            contactStatus = "REQUESTED",
            updatedAt = System.currentTimeMillis()
        )

        db.collection("contacts")
            .document(id)
            .set(contact)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun acceptContact(id: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("contacts")
            .document(id)
            .update(
                "contactStatus", "ACCEPTED",
                "updatedAt", System.currentTimeMillis()
            )
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun rejectContact(id: String, onResult: (Boolean, String?) -> Unit) {
        db.collection("contacts")
            .document(id)
            .update(
                "contactStatus", "REJECTED",
                "updatedAt", System.currentTimeMillis()
            )
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    // check current contactStatus with other user
    fun getContactWith(otherUid: String, onResult: (Contact?, String?) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(null, "Not logged in")
        val id = contactId(currentUid, otherUid)

        db.collection("contacts")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onResult(doc.toObject(Contact::class.java), null)
                } else {
                    onResult(null, null)
                }
            }
            .addOnFailureListener { e -> onResult(null, e.message) }
    }

    // get all ACCEPTED contacts for current user → list of other uids
    fun getAcceptedContacts(onResult: (List<String>, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(emptyList(), "Not logged in")

        db.collection("contacts")
            .whereEqualTo("contactStatus", "ACCEPTED")
            .get()
            .addOnSuccessListener { snap ->
                val others = snap.documents
                    .mapNotNull { it.toObject(Contact::class.java) }
                    .filter { it.userA == uid || it.userB == uid }
                    .map { if (it.userA == uid) it.userB else it.userA }

                onResult(others, null)
            }
            .addOnFailureListener { e -> onResult(emptyList(), e.message) }
    }
}
