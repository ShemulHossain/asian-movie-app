package com.example.asianmovieapp

data class Review(
    val id: Int,
    val movieId: Int,
    val reviewerName: String,
    val reviewText: String,
    val rating: Int
)
