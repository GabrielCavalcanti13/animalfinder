package com.example.animalfinder

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.animalfinder.models.Post
import com.example.animalfinder.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*
import java.util.*

private const val PICK_PHOTO_CODE = 123
private const val REQUEST_CODE = 321
class CreateActivity : AppCompatActivity() {
    private var photo: Uri? = null
    private var signedUser: User? = null
    private lateinit var firestoredb: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        storageReference = FirebaseStorage.getInstance().reference
        firestoredb = FirebaseFirestore.getInstance()
        firestoredb.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String)
                .get().addOnSuccessListener { userSnapshot ->
                    signedUser = userSnapshot.toObject(User::class.java)
                }

        getLocation()
        btnSelectImage.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        btnTakeImage.setOnClickListener {
            btnTakeImage.isEnabled = false
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE)
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
                etLocation.text.toString(),
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
            etLocation.text.clear()
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
        if (requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK) {
            photo = data?.data
            imageView.setImageURI(photo)
        }
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val takenImage = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(takenImage)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getLocation() {
        val task = fusedLocationProviderClient.lastLocation
        val geocoder : Geocoder
        geocoder = Geocoder(this, Locale.getDefault())


        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        task.addOnSuccessListener {
            if(it != null){
                val addressList = geocoder.getFromLocation(it.latitude, it.longitude, 10) as ArrayList<Address>
                val addressName = addressList[0].getAddressLine(0)
                println(addressName)
                val country = addressList[0].countryName
                var city = addressList[0].locality
                if (city == null && addressList.size > 0) {
                    for (adr in addressList) {
                        if (adr.locality != null && adr.locality.isNotEmpty()) {
                            city = adr.locality
                        }
                    }
                }
                var photoLocation = ""
                if (city == null) {
                    photoLocation = country
                } else {
                    photoLocation = "$city - $country"
                }
                etLocation.setText(photoLocation)
            }
        }
    }
}