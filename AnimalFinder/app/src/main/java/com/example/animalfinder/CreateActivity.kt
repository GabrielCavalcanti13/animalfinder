package com.example.animalfinder

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.animalfinder.models.Post
import com.example.animalfinder.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*

private const val PICK_PHOTO_CODE = 123
class CreateActivity : AppCompatActivity() {
    private var photo: Uri? = null
    private var signedUser: User? = null
    private lateinit var firestoredb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        storageReference = FirebaseStorage.getInstance().reference
        firestoredb = FirebaseFirestore.getInstance()
        firestoredb.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String)
                .get().addOnSuccessListener { userSnapshot ->
                    signedUser = userSnapshot.toObject(User::class.java)
                }

        btnSelectImage.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }
        btnSubmit.setOnClickListener {
            submitButtonClick()
        }
    }

    private fun submitButtonClick() {
        if ((photo == null) || (etDescription.text.isBlank()) || (etSpecie.text.isBlank())) {
            Toast.makeText(this, "Missing information", Toast.LENGTH_SHORT).show()
            return
        }
        if (signedUser == null) {
            Toast.makeText(this, "No signed user", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        val photoUpload = photo as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        photoReference.putFile(photoUpload).continueWithTask { photoUploadTask ->
            photoReference.downloadUrl
        }.continueWithTask { downloadUrlTask ->
            val post = Post(
                etDescription.text.toString(),
                etSpecie.text.toString(),
                "Recife - Brazil",
                downloadUrlTask.result.toString(),
                System.currentTimeMillis(),
                signedUser)
            firestoredb.collection("posts").add(post)
        }.addOnCompleteListener { postCreationTask ->
            btnSubmit.isEnabled = true
            if (!postCreationTask.isSuccessful) {
                Toast.makeText(this, "Failed to post photo", Toast.LENGTH_SHORT).show()
            }
            etDescription.text.clear()
            etSpecie.text.clear()
            imageView.setImageResource(0)
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedUser?.username)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                photo = data?.data
                imageView.setImageURI(photo)
            }
        }
    }
}