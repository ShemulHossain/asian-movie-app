package com.example.asianmovieapp

data class Movie(
    val id: Int,
    val title: String,
    val genre: String,
    val releaseDate: String,
    val rating: Double,
    val imageUrl: String? = null,
    val description: String,
    val reviews: List<Review> = emptyList()
)
