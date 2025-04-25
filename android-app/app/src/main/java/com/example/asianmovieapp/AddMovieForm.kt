package com.example.asianmovieapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.asianmovieapp.R
import androidx.compose.material.*
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AddMovieForm(
    onAdd: (String, String, String, Double, String, String) -> Unit,
    onClose: () -> Unit,
    initialTitle: String = "",
    initialGenre: String = "",
    initialReleaseDate: String = "",
    initialRating: String = "",
    initialImageUrl: String = "",
    initialDescription: String = ""
) {
    var title by remember { mutableStateOf(initialTitle) }
    var genre by remember { mutableStateOf(initialGenre) }
    var releaseDate by remember { mutableStateOf(initialReleaseDate) }
    var rating by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf(initialImageUrl) }
    var description by remember { mutableStateOf(initialDescription) }

    Column(Modifier.padding(60.dp)) {
        TextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.title)) }, modifier = Modifier
            .fillMaxWidth()
            .testTag("TitleInput"))
        Spacer(Modifier.height(8.dp))
        TextField(value = genre, onValueChange = { genre = it }, label = { Text(stringResource(R.string.genre)) }, modifier = Modifier
            .fillMaxWidth()
            .testTag("GenreInput")
        )
        Spacer(Modifier.height(8.dp))
        TextField(value = releaseDate, onValueChange = { releaseDate = it }, label = { Text(stringResource(R.string.release_format)) }, modifier = Modifier
            .fillMaxWidth()
            .testTag("ReleaseDateInput"))
        Spacer(Modifier.height(8.dp))
        TextField(value = rating, onValueChange = { rating = it }, label = { Text(stringResource(R.string.rating)) }, modifier = Modifier
            .fillMaxWidth()
            .testTag("RatingInput")
        )
        Spacer(Modifier.height(8.dp))
        TextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text(stringResource(R.string.image_url)) }, modifier = Modifier
            .fillMaxWidth()
            .testTag("ImageUrlInput"))
        Spacer(Modifier.height(8.dp))
        TextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier
            .fillMaxWidth()
            .testTag("DescriptionInput"))
        Spacer(Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    onAdd(
                        title,
                        genre,
                        releaseDate,
                        rating.toDoubleOrNull() ?: 0.0,
                        imageUrl,
                        description
                    )
                    onClose()
                },
                modifier = Modifier.testTag("SubmitButton")
            ) {
                Text(stringResource(R.string.add_movie))
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(onClick = onClose, modifier = Modifier.testTag("CancelButton")) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
