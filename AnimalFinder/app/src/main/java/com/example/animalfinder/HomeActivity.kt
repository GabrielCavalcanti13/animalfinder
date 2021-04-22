package com.example.animalfinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.animalfinder.models.Post
import com.example.animalfinder.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_home.*

private const val TAG = "HomeActivity"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
open class HomeActivity : AppCompatActivity() {

    private var signedUser: User? = null
    private lateinit var firestoredb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        posts = mutableListOf()
        adapter = PostsAdapter(this, posts)
        rvPosts.adapter = adapter
        rvPosts.layoutManager  = LinearLayoutManager(this)

        firestoredb = FirebaseFirestore.getInstance()
        firestoredb.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String)
                .get().addOnSuccessListener { userSnapshot ->
                    signedUser = userSnapshot.toObject(User::class.java)
                }

        var postsReference = firestoredb
                .collection("posts")
                .limit(15)
                .orderBy("post_time_ms", Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null) {
            supportActionBar?.title = username
            postsReference = postsReference.whereEqualTo("user.username", username)
        }

        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying photos", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
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
            intent.putExtra(EXTRA_USERNAME, signedUser?.username)
            startActivity(intent)
        }
        if (item.itemId == R.id.navigation_camera) {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}