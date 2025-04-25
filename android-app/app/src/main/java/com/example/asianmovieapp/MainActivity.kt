package com.example.asianmovieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import com.example.asianmovieapp.ui.MovieDetailsScreen
import com.example.asianmovieapp.ui.theme.AsianMovieAppTheme
import com.example.asianmovieapp.viewmodel.MovieViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.Saver
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.ui.res.stringResource
import com.example.asianmovieapp.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val MovieSaver = Saver<Movie?, Bundle>(
            save = { movie ->
                if (movie == null) null else Bundle().apply {
                    putInt("id", movie.id)
                    putString("title", movie.title)
                    putString("genre", movie.genre)
                    putString("releaseDate", movie.releaseDate)
                    putDouble("rating", movie.rating)
                    putString("imageUrl", movie.imageUrl)
                    putString("description", movie.description)
                }
            },
            restore = { bundle ->
                if (bundle == null) null else Movie(
                    id = bundle.getInt("id"),
                    title = bundle.getString("title") ?: "",
                    genre = bundle.getString("genre") ?: "",
                    releaseDate = bundle.getString("releaseDate") ?: "",
                    rating = bundle.getDouble("rating"),
                    imageUrl = bundle.getString("imageUrl") ?: "",
                    description = bundle.getString("description") ?: "",
                    reviews = emptyList() // We'll refresh these from the backend anyway
                )
            }
        )


        setContent {
            val viewModel: MovieViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            var showForm by remember { mutableStateOf(false) }
            var editingMovie by remember { mutableStateOf<Movie?>(null) }
            var selectedMovie by rememberSaveable(stateSaver = MovieSaver) { mutableStateOf(null) }
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()


            AsianMovieAppTheme {
                NavHost(navController = navController, startDestination = "movie_list") {
                    composable("movie_list") {
                        var searchQuery by rememberSaveable { mutableStateOf("") }
                        var selectedGenre by rememberSaveable { mutableStateOf("All") }
                        var selectedSort by rememberSaveable { mutableStateOf("None") }
                        val genreOptions = listOf("All", "Action", "Comedy", "Anime", "Drama", "Horror")
                        val sortOptions = listOf("None", "Rating", "Release Date")
                        var filterMode by rememberSaveable { mutableStateOf("Normal") }


                        LaunchedEffect(selectedGenre, selectedSort, searchQuery, filterMode) {
                            viewModel.resetPagination()
                            when (filterMode) {
                                "TopRated" -> viewModel.loadTopRatedMovies()
                                "Latest" -> viewModel.loadLatestMovies()
                                else -> {
                                    viewModel.loadMoviesFilteredSorted(
                                        title = if (searchQuery.isNotBlank()) searchQuery else null,
                                        genre = selectedGenre,
                                        sort = selectedSort
                                    )
                                }
                            }
                        }


                        Scaffold(
                            floatingActionButton = {
                                FloatingActionButton(onClick = {
                                    editingMovie = null
                                    showForm = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_movie),modifier = Modifier.testTag("AddMovieFAB"))
                                }
                            },
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            modifier = Modifier.fillMaxSize()
                        ) { padding ->
                            if (showForm) {
                                AddMovieForm(
                                    initialTitle = editingMovie?.title.orEmpty(),
                                    initialGenre = editingMovie?.genre.orEmpty(),
                                    initialReleaseDate = editingMovie?.releaseDate.orEmpty(),
                                    initialRating = editingMovie?.rating?.toString() ?: "0.0",
                                    initialImageUrl = editingMovie?.imageUrl.orEmpty(),
                                    initialDescription = editingMovie?.description.orEmpty(),
                                    onAdd = { title, genre, date, rating, url, desc ->
                                        if (editingMovie != null) {
                                            viewModel.updateMovie(editingMovie!!.copy(
                                                title = title,
                                                genre = genre,
                                                releaseDate = date,
                                                rating = rating,
                                                imageUrl = url,
                                                description = desc
                                            ))
                                        } else {
                                            viewModel.addMovie(
                                                Movie(
                                                    id = 0,
                                                    title = title,
                                                    genre = genre,
                                                    releaseDate = date,
                                                    rating = rating,
                                                    imageUrl = url,
                                                    description = desc,
                                                    reviews = emptyList()
                                                )
                                            )
                                        }
                                        showForm = false
                                        editingMovie = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Movie saved!")
                                        }
                                    },
                                    onClose = {
                                        showForm = false
                                        editingMovie = null
                                    }
                                )

                            } else {
                                when {
                                    uiState.isSuccess -> {
                                        val movies = uiState.getOrNull().orEmpty()

                                        Column(modifier = Modifier.padding(padding)) {
                                            // Search
                                            OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                label = { Text(stringResource(R.string.search_by_title)) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                                    .testTag("SearchInput"),
                                            )

                                            // Filters
                                            FlowRow(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                mainAxisSpacing = 12.dp,
                                                crossAxisSpacing = 12.dp
                                            ) {
                                                DropdownMenuBox(
                                                    label = stringResource(R.string.filter_by_genre),
                                                    options = genreOptions,
                                                    selectedOption = selectedGenre,
                                                    onOptionSelected = { selectedGenre = it }
                                                )

                                                DropdownMenuBox(
                                                    label = stringResource(R.string.sort_by),
                                                    options = sortOptions,
                                                    selectedOption = selectedSort,
                                                    onOptionSelected = { selectedSort = it }
                                                )

                                                Button(onClick = { filterMode = "TopRated" },modifier = Modifier.testTag("TopRatedButton")) {
                                                    Text(stringResource(R.string.top_rated))
                                                }

                                                Button(onClick = { filterMode = "Latest" },modifier = Modifier.testTag("LatestButton")) {
                                                    Text(stringResource(R.string.latest))
                                                }

                                                Button(onClick = {
                                                    selectedGenre = "All"
                                                    selectedSort = "None"
                                                    searchQuery = ""
                                                    filterMode = "Normal"
                                                }, modifier = Modifier.testTag("ClearButton")) {
                                                    Text(stringResource(R.string.clear))
                                                }

                                                Button(onClick = {
                                                    viewModel.loadRandomMovie { randomMovie ->
                                                        selectedMovie = randomMovie
                                                        navController.navigate("movie_details/${randomMovie.id}")
                                                    }
                                                }, modifier = Modifier.testTag("RandomMovieButton")) {
                                                    Text(stringResource(R.string.random_movie))
                                                }
                                            }


                                            MovieList(
                                                movies = movies,
                                                onDelete = { movie ->
                                                    viewModel.deleteMovie(movie.id)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Movie deleted.")
                                                    }
                                                },
                                                onEdit = {
                                                    editingMovie = it
                                                    showForm = true
                                                },
                                                onSelect = {
                                                    selectedMovie = it
                                                    navController.navigate("movie_details/${it.id}")
                                                }
                                            )
                                            if (viewModel.hasMorePages) {
                                                Button(onClick = {
                                                    viewModel.loadNextPage(
                                                        title = if (searchQuery.isNotBlank()) searchQuery else null,
                                                        genre = selectedGenre,
                                                        sort = selectedSort,
                                                        order = "desc",
                                                        pageSize = 1
                                                    )
                                                }) {
                                                    Text("Load More")
                                                }
                                            }

                                        }
                                    }

                                    uiState.isFailure -> {
                                        Text(stringResource(R.string.failed_to_load), modifier = Modifier.padding(padding))
                                    }

                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(padding),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    composable("movie_details/{movieId}") { backStackEntry ->
                        val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
                        if (movieId != null) {
                            val movie by produceState<Movie?>(initialValue = null) {
                                viewModel.refreshMovieById(movieId) {
                                    value = it
                                }
                            }

                            movie?.let {
                                // Reuse existing MovieDetailsScreen (from your MovieDetailsScreen.kt)
                                MovieDetailsScreen(
                                    movie = it,
                                    modifier = Modifier.padding(PaddingValues(16.dp)),
                                    onEditClick = { updated ->
                                        editingMovie = updated
                                        showForm = true
                                        navController.popBackStack()
                                    },
                                    onDeleteClick = { toDelete ->
                                        viewModel.deleteMovie(toDelete.id)
                                        navController.popBackStack()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Movie deleted.")
                                        }
                                    },
                                    onAddReview = { name, rating, text ->
                                        viewModel.addReview(
                                            Review(
                                                id = 0,
                                                movieId = it.id,
                                                reviewerName = name,
                                                reviewText = text,
                                                rating = rating
                                            )
                                        )
                                        viewModel.refreshMovieById(it.id) {}
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Review submitted!")
                                        }
                                    },
                                    onEditReview = { review ->
                                        viewModel.updateReview(review)
                                        viewModel.refreshMovieById(it.id) {}
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Review updated.")
                                        }
                                    },
                                    onDeleteReview = { review ->
                                        viewModel.deleteReview(it.id, review.id)
                                        viewModel.refreshMovieById(it.id) {}
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Review deleted.")
                                        }
                                    },
                                    onRefreshMovie = {
                                        viewModel.refreshMovieById(it.id) {}
                                    },
                                    onBack = { navController.popBackStack()}
                                        )
                            }
                        }
                    }

                }
            }
        }
    }
}



@Composable
fun GlideImage(url: String, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(url),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun MovieList(
    movies: List<Movie>,
    onDelete: (Movie) -> Unit,
    onEdit: (Movie) -> Unit,
    onSelect: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies) { movie ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(movie) }
                    .testTag("MovieCard_${movie.id}"),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(movie.imageUrl),
                        contentDescription = "Movie Poster",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(movie.title, style = MaterialTheme.typography.headlineSmall)
                    Text("Genre: ${movie.genre}")
                    Text("Release: ${movie.releaseDate.take(10)}")
                    Text("Rating: ⭐ ${movie.rating}")

                    Spacer(modifier = Modifier.height(8.dp))

                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedOption)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AsianMovieAppTheme {
        Text("Hello Android!")
    }
}
