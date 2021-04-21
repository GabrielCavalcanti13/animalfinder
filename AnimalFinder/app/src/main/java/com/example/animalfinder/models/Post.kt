package com.example.animalfinder.models

import com.google.firebase.firestore.PropertyName

data class Post(
        var description: String = "",
        var specie: String = "",
        var location: String = "",
        @get:PropertyName("image_url")  @set:PropertyName("image_url") var imageUrl: String = "",
        @get:PropertyName("post_time_ms")  @set:PropertyName("post_time_ms") var postTimeMs: Long = 0,
        var user: User? = null
)