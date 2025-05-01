package com.example.asianmovieapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.input.VisualTransformation

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
    var rating by remember { mutableStateOf(initialRating) }
    var imageUrl by remember { mutableStateOf(initialImageUrl) }
    var description by remember { mutableStateOf(initialDescription) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Add Movie Details", style = MaterialTheme.typography.titleLarge)
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = releaseDate,
                    onValueChange = { releaseDate = it },
                    label = { Text("Release Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it },
                    label = { Text("Rating (0.0 - 10.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = VisualTransformation.None
                )
            }

            item {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }

                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
