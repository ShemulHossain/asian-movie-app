package com.example.asianmovieapp.repository

import com.example.asianmovieapp.Movie
import com.example.asianmovieapp.Review
import com.example.asianmovieapp.MovieApiService
import com.example.asianmovieapp.RetrofitInstance
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieRepository {
    private val apiService: MovieApiService = RetrofitInstance.api


    suspend fun getAllMovies(page: Int = 1, pageSize: Int = 100): List<Movie> {
        val response = apiService.getMovies(page = page, pageSize = pageSize)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    suspend fun getMoviesByGenre(genre: String): List<Movie> {
        val response = apiService.getMoviesByGenre(genre)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun getMoviesFilteredSorted(
        title: String? = null,
        genre: String? = null,
        sort: String? = null,
        order: String? = null,
        page: Int = 1,
        pageSize: Int = 100
    ): List<Movie> {
        val response = apiService.getMovies(title, genre, sort, order, page, pageSize)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }



    suspend fun getMoviesSortedByRating(): List<Movie> {
        val response = apiService.getMoviesSortedByRating()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun getMoviesSortedByDate(): List<Movie> {
        val response = apiService.getMoviesSortedByDate()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun getMoviesPaginated(
        page: Int,
        pageSize: Int = 10,
        title: String? = null,
        genre: String? = null,
        sort: String? = null,
        order: String? = null
    ): List<Movie> {
        val response = apiService.getMovies(
            title = title,
            genre = genre,
            sort = sort,
            order = order,
            page = page,
            pageSize = pageSize
        )
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Pagination failed: ${response.code()}")
        }
    }



    suspend fun searchMovies(title: String): List<Movie> {
        val response = apiService.searchMovies(title)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    suspend fun addMovie(movie: Movie): Movie {
        return apiService.addMovie(movie)
    }

    suspend fun deleteMovie(id: Int): Response<Unit> {
        return apiService.deleteMovie(id)
    }

    suspend fun updateMovie(movie: Movie) {
        apiService.updateMovie(movie.id, movie)
    }

    suspend fun addReview(review: Review): Response<Unit> {
        return apiService.addReview(review.movieId, review)
    }

    suspend fun updateReview(review: Review): Response<Unit> {
        return apiService.updateReview(review.movieId, review.id, review)
    }

    suspend fun deleteReview(movieId: Int, reviewId: Int): Response<Unit> {
        return apiService.deleteReview(movieId, reviewId)
    }

    suspend fun getTopRatedMovies(count: Int): List<Movie> {
        val response = apiService.getTopRatedMovies(count)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch top rated movies")
        }
    }

    suspend fun getLatestMovies(count: Int): List<Movie> {
        val response = apiService.getLatestMovies(count)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch latest movies")
        }
    }

    suspend fun getRandomMovie(): Movie {
        val response = apiService.getRandomMovie()
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("No random movie found")
        } else {
            throw Exception("Failed to fetch random movie: ${response.code()}")
        }
    }

    suspend fun fetchAverageRating(movieId: Int): Double? {
        return try {
            withContext(Dispatchers.IO) {
                apiService.getAverageRating(movieId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // or you can throw e depending on how you handle errors
        }
    }
}
