package com.example.asianmovieapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.asianmovieapp.Movie
import com.example.asianmovieapp.Review
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.asianmovieapp.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
@OptIn(ExperimentalMaterial3Api::class)


@Composable
fun MovieDetailsScreen(
    movie: Movie,
    modifier: Modifier = Modifier,
    onEditClick: (Movie) -> Unit,
    onDeleteClick: (Movie) -> Unit,
    onAddReview: (String, Int, String) -> Unit,
    onEditReview: (Review) -> Unit,
    onDeleteReview: (Review) -> Unit,
    onRefreshMovie: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    var showReviewForm by remember { mutableStateOf(false) }
    var newReviewer by remember { mutableStateOf("") }
    var newRating by remember { mutableStateOf("") }
    var newReviewText by remember { mutableStateOf("") }
    var editingReview by remember { mutableStateOf<Review?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movie.title, modifier = Modifier.testTag("DetailsTitle")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },modifier = Modifier.testTag("TopAppBar")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize().testTag("DetailsScreenColumn")
        ) {
            item {
                GlideImage(
                    url = movie.imageUrl ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .testTag("MovieImage")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp).testTag("MovieDetailsInfo")) {
                    Text(movie.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("TitleText"))
                    Text(text = "${stringResource(R.string.genre)}: ${movie.genre}", fontWeight = FontWeight.SemiBold, modifier = Modifier.testTag("GenreText"))
                    Text(text = "${stringResource(R.string.release_date)}: ${movie.releaseDate.take(10)}", modifier = Modifier.testTag("ReleaseDateText"))
                    Text(text = "${stringResource(R.string.rating)}: ⭐ ${movie.rating}", modifier = Modifier.testTag("RatingText"))
                    Text(text = "${stringResource(R.string.description)}: ${movie.description}", modifier = Modifier.testTag("DescriptionText"))


                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { onEditClick(movie) }, modifier = Modifier.testTag("EditMovieButton")) {
                            Text(stringResource(R.string.edit_movie))
                        }
                        Button(onClick = { onDeleteClick(movie) }, modifier = Modifier.testTag("DeleteMovieButton")) {
                            Text(stringResource(R.string.delete_movie))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.review), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp).testTag("ReviewHeader")
                )
            }

            items(movie.reviews) { review ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp).testTag("ReviewCard")
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text("${review.reviewerName} (${review.rating}/10)", fontWeight = FontWeight.Bold, modifier = Modifier.testTag("ReviewerText"))
                        Text(review.reviewText, modifier = Modifier.testTag("ReviewText"))
                        Row {
                            IconButton(onClick = {
                                editingReview = review
                                newReviewer = review.reviewerName
                                newRating = review.rating.toString()
                                newReviewText = review.reviewText
                                showReviewForm = true
                            },modifier = Modifier.testTag("EditReviewButton")) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_movie))
                            }
                            IconButton(onClick = {
                                onDeleteReview(review)
                                onRefreshMovie()
                                snackbarMessage = "Review deleted."
                            },modifier = Modifier.testTag("DeleteReviewButton")) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_movie))
                            }
                        }
                    }
                }
            }

            item {
                if (!showReviewForm) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showReviewForm = true },
                            modifier = Modifier.align(Alignment.Center).testTag("OpenReviewFormButton")
                        ) {
                            Text(stringResource(R.string.leave_review))
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = newReviewer,
                            onValueChange = { newReviewer = it },
                            label = { Text(stringResource(R.string.reviewer_name)) },
                            modifier = Modifier.fillMaxWidth().testTag("ReviewerInput")
                        )
                        OutlinedTextField(
                            value = newRating,
                            onValueChange = { newRating = it },
                            label = { Text(stringResource(R.string.rating_input)) },
                            modifier = Modifier.fillMaxWidth().testTag("RatingInput")
                        )
                        OutlinedTextField(
                            value = newReviewText,
                            onValueChange = { newReviewText = it },
                            label = { Text(stringResource(R.string.review_input)) },
                            modifier = Modifier.fillMaxWidth().testTag("ReviewTextInput")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val ratingInt = newRating.toIntOrNull()
                                if (
                                    newReviewer.isNotBlank() &&
                                    ratingInt != null &&
                                    ratingInt in 1..10 &&
                                    newReviewText.isNotBlank()
                                ) {
                                    if (editingReview != null) {
                                        onEditReview(
                                            editingReview!!.copy(
                                                reviewerName = newReviewer,
                                                reviewText = newReviewText,
                                                rating = ratingInt
                                            )
                                        )
                                        snackbarMessage = "Review updated!"
                                    } else {
                                        onAddReview(newReviewer, ratingInt, newReviewText)
                                        snackbarMessage = "Review submitted!"
                                    }
                                    editingReview = null
                                    newReviewer = ""
                                    newRating = ""
                                    newReviewText = ""
                                    showReviewForm = false
                                } else {
                                    snackbarMessage = "Please fill all fields correctly."
                                }
                            },
                            modifier = Modifier.align(Alignment.End).testTag("SubmitReviewButton")
                        ) {
                            Text(if (editingReview != null) "Update" else "Submit")
                        }
                    }
                }
            }
        }
    }

    // Snackbar Launcher
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }
}
