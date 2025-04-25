package com.example.asianmovieapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asianmovieapp.Movie
import com.example.asianmovieapp.Review
import com.example.asianmovieapp.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import retrofit2.Response
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class MovieViewModel : ViewModel() {

    private val repository = MovieRepository()
    var hasMorePages by mutableStateOf(true)
        private set



    private val _uiState = MutableStateFlow<Result<List<Movie>>>(Result.success(emptyList()))
    val uiState: StateFlow<Result<List<Movie>>> = _uiState

    private var currentPage = 1
    private var currentGenre = "All"
    private var currentSort = "None"

    init {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch {
            _uiState.value = runCatching {
                currentPage = 1
                repository.getAllMovies()
            }
        }
    }

    fun resetPagination() {
        currentPage = 0
        hasMorePages = true
    }


    fun loadNextPage(
        title: String? = null,
        genre: String? = null,
        sort: String? = null,
        order: String? = "asc",
        pageSize: Int = 10
    ) {
        viewModelScope.launch {
            val nextPage = currentPage + 1
            try {
                val movies = repository.getMoviesPaginated(
                    page = nextPage,
                    pageSize = pageSize,
                    title = title,
                    genre = genre,
                    sort = sort,
                    order = order
                )
                if (movies.isNotEmpty()) {
                    val current = _uiState.value.getOrNull() ?: emptyList()
                    _uiState.value = Result.success(current + movies)
                    currentPage = nextPage
                    hasMorePages = true
                } else {
                    hasMorePages = false
                }
            } catch (e: Exception) {
                _uiState.value = Result.failure(e)
                hasMorePages = false
            }
        }
    }





    fun addMovie(movie: Movie) {
        viewModelScope.launch {
            try {
                repository.addMovie(movie)
                fetchMovies()
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error adding movie", e)
            }
        }
    }

    fun deleteMovie(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteMovie(id)
                fetchMovies()
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error deleting movie", e)
            }
        }
    }

    fun updateMovie(updatedMovie: Movie) {
        viewModelScope.launch {
            try {
                repository.updateMovie(updatedMovie)
                fetchMovies()
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error updating movie", e)
            }
        }
    }

    fun getMovieById(movieId: Int, callback: (Movie?) -> Unit) {
        val movie = _uiState.value.getOrNull()?.find { it.id == movieId }
        callback(movie)
    }

    fun refreshMovieById(movieId: Int, callback: (Movie?) -> Unit) {
        viewModelScope.launch {
            try {
                val movies = repository.getAllMovies()
                val updatedMovie = movies.find { it.id == movieId }
                val currentMovies = _uiState.value.getOrNull()?.toMutableList() ?: mutableListOf()
                val index = currentMovies.indexOfFirst { it.id == movieId }
                if (index != -1 && updatedMovie != null) {
                    currentMovies[index] = updatedMovie
                    _uiState.value = Result.success(currentMovies)
                } else {
                    fetchMovies()
                }
                callback(updatedMovie)
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error in refreshMovieById", e)
                callback(null)
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            try {
                val response = repository.addReview(review)
                if (response.isSuccessful) {
                    refreshMovieById(review.movieId) {}
                } else {
                    Log.e("MovieViewModel", "Failed to add review: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Exception in addReview", e)
            }
        }
    }

    fun updateReview(review: Review) {
        viewModelScope.launch {
            try {
                val response = repository.updateReview(review)
                if (response.isSuccessful) {
                    refreshMovieById(review.movieId) {}
                } else {
                    Log.e("MovieViewModel", "Failed to update review: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Exception in updateReview", e)
            }
        }
    }

    fun deleteReview(movieId: Int, reviewId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteReview(movieId, reviewId)
                if (response.isSuccessful) {
                    refreshMovieById(movieId) {}
                } else {
                    Log.e("MovieViewModel", "Failed to delete review: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Exception in deleteReview", e)
            }
        }
    }

    fun fetchMoviesByGenre(genre: String) {
        currentGenre = genre
        currentPage = 1
        viewModelScope.launch {
            _uiState.value = runCatching {
                if (genre.equals("All", ignoreCase = true)) {
                    repository.getAllMovies()
                } else {
                    repository.getMoviesByGenre(genre)
                }
            }
        }
    }

    fun fetchMoviesSortedBy(sortBy: String) {
        currentSort = sortBy
        currentPage = 1
        viewModelScope.launch {
            _uiState.value = runCatching {
                when (sortBy) {
                    "Rating" -> repository.getMoviesSortedByRating()
                    "Release Date" -> repository.getMoviesSortedByDate()
                    else -> repository.getAllMovies()
                }
            }
        }
    }

    fun searchMoviesByTitle(title: String)
    {
        viewModelScope.launch {
            _uiState.value = runCatching {
                repository.searchMovies(title)
            }
        }
    }

    fun loadTopRatedMovies(count: Int = 8) {
        viewModelScope.launch {
            try {
                val topRated = repository.getTopRatedMovies(count)
                _uiState.value = Result.success(topRated)
            } catch (e: Exception) {
                _uiState.value = Result.failure(e)
            }
        }
    }

    fun loadLatestMovies(count: Int = 8) {
        viewModelScope.launch {
            try {
                val latest = repository.getLatestMovies(count)
                _uiState.value = Result.success(latest)
            } catch (e: Exception) {
                _uiState.value = Result.failure(e)
            }
        }
    }


    fun loadMoviesFilteredSorted(
        title: String? = null,
        genre: String? = null,
        sort: String? = null,
        order: String? = null
    ) {
        viewModelScope.launch {
            try {
                val result = repository.getMoviesFilteredSorted(
                    title = title,
                    genre = if (genre != "All") genre else null,
                    sort = when (sort) {
                        "Rating" -> "rating"
                        "Release Date" -> "releasedate"
                        else -> null
                    },
                    order = if (sort != "None") "desc" else null
                )
                _uiState.value = Result.success(result)
            } catch (e: Exception) {
                _uiState.value = Result.failure(e)
            }
        }
    }

    fun loadRandomMovie(onLoaded: (Movie) -> Unit) {
        viewModelScope.launch {
            try {
                val movie = repository.getRandomMovie()
                onLoaded(movie)
            } catch (e: Exception) {
                _uiState.value = Result.failure(e)
            }
        }
    }


}
