package com.example.asianmovieapp

import retrofit2.http.GET
import retrofit2.http.*
import retrofit2.Response
import retrofit2.http.Path

interface MovieApiService {

    @GET("/api/Movies")
    suspend fun getMovies(
        @Query("title") title: String? = null,
        @Query("genre") genre: String? = null,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): Response<List<Movie>>



    @POST("/api/Movies")
    suspend fun addMovie(@Body movie: Movie): Movie

    @DELETE("/api/Movies/{id}")
    suspend fun deleteMovie(@Path("id") id: Int): Response<Unit>

    @PUT("api/Movies/{id}")
    suspend fun updateMovie(@Path("id") id: Int, @Body movie: Movie): Response<Unit>

    @POST("/api/Movies/{id}/reviews")
    suspend fun addReview(
        @Path("id") movieId: Int,
        @Body review: Review
    ): Response<Unit>

    @PUT("/api/Movies/{movieId}/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("movieId") movieId: Int,
        @Path("reviewId") reviewId: Int,
        @Body review: Review
    ): Response<Unit>

    @DELETE("/api/Movies/{movieId}/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("movieId") movieId: Int,
        @Path("reviewId") reviewId: Int
    ): Response<Unit>

    @GET("/api/Movies/search")
    suspend fun searchMovies(@Query("title") title: String): Response<List<Movie>>

    @GET("/api/Movies/genre/{genre}")
    suspend fun getMoviesByGenre(@Path("genre") genre: String): Response<List<Movie>>

    @GET("/api/Movies/sort/rating")
    suspend fun getMoviesSortedByRating(): Response<List<Movie>>

    @GET("/api/Movies/sort/date")
    suspend fun getMoviesSortedByDate(): Response<List<Movie>>

    @GET("/api/Movies/top")
    suspend fun getTopRatedMovies(@Query("count") count: Int): Response<List<Movie>>

    @GET("/api/Movies/latest")
    suspend fun getLatestMovies(@Query("count") count: Int): Response<List<Movie>>

    @GET("api/Movies/random")
    suspend fun getRandomMovie(): Response<Movie>

    @GET("api/Movies/{id}/average-rating")
    suspend fun getAverageRating(@Path("id") movieId: Int): Double

}
