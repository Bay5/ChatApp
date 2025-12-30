package com.bay.chatapp.data.Repository

import com.bay.chatapp.data.entity.Contact
import com.bay.chatapp.data.local.ChatAppDatabase
import com.bay.chatapp.data.local.ContactEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ContactRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val local = ChatAppDatabase.get()
    private val contactDao = local.contactDao()
    private val repoScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private fun contactId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    fun sendContactRequest(toUid: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, "Not logged in")

        val sorted = listOf(currentUid, toUid).sorted()
        val id = sorted.joinToString("_")

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
        repoScope.launch {
            contactDao.upsert(
                ContactEntity(
                    id = contact.id,
                    userA = contact.userA,
                    userB = contact.userB,
                    requestedBy = contact.requestedBy,
                    contactStatus = contact.contactStatus,
                    updatedAt = contact.updatedAt
                )
            )
        }
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
        val parts = id.split("_")
        val userA = parts.getOrNull(0) ?: ""
        val userB = parts.getOrNull(1) ?: ""
        repoScope.launch {
            contactDao.upsert(
                ContactEntity(
                    id = id,
                    userA = userA,
                    userB = userB,
                    requestedBy = auth.currentUser?.uid ?: "",
                    contactStatus = "ACCEPTED",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
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
        val parts = id.split("_")
        val userA = parts.getOrNull(0) ?: ""
        val userB = parts.getOrNull(1) ?: ""
        repoScope.launch {
            contactDao.upsert(
                ContactEntity(
                    id = id,
                    userA = userA,
                    userB = userB,
                    requestedBy = auth.currentUser?.uid ?: "",
                    contactStatus = "REJECTED",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun unfriend(otherUid: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onResult(false, "Not logged in")
        val id = contactId(currentUid, otherUid)
        db.collection("contacts")
            .document(id)
            .update(
                "contactStatus", "REJECTED",
                "updatedAt", System.currentTimeMillis()
            )
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
        repoScope.launch {
            contactDao.upsert(
                ContactEntity(
                    id = id,
                    userA = currentUid,
                    userB = otherUid,
                    requestedBy = currentUid,
                    contactStatus = "REJECTED",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

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
        // No local read path added here yet
    }

    fun getAcceptedContacts(onResult: (List<String>, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(emptyList(), "Not logged in")

        val coll = db.collection("contacts")
        coll.whereEqualTo("userA", uid)
            .whereEqualTo("contactStatus", "ACCEPTED")
            .get()
            .addOnSuccessListener { snapA ->
                coll.whereEqualTo("userB", uid)
                    .whereEqualTo("contactStatus", "ACCEPTED")
                    .get()
                    .addOnSuccessListener { snapB ->
                        val docs = snapA.documents + snapB.documents
                        val contacts = docs.mapNotNull { it.toObject(Contact::class.java) }
                        val others = contacts.map {
                            if (it.userA == uid) it.userB else it.userA
                        }
                        onResult(others.distinct(), null)
                    }
                    .addOnFailureListener { e -> onResult(emptyList(), e.message) }
            }
            .addOnFailureListener { e -> onResult(emptyList(), e.message) }
    }
}
