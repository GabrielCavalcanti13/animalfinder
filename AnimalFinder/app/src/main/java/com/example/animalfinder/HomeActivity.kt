package com.example.animalfinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.animalfinder.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "HomeActivity"
class HomeActivity : AppCompatActivity() {

    private lateinit var firestoredb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firestoredb = FirebaseFirestore.getInstance()
        val postsReference = firestoredb
                .collection("posts")
                .limit(15)
                .orderBy("post_time_ms", Query.Direction.DESCENDING)
        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying photos", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            for (post in postList) {
                Log.i(TAG, "Post ${post}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigation_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}