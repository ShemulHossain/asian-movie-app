package com.example.asianmovieapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import android.widget.ImageView
import androidx.compose.ui.platform.LocalContext

@Composable
fun GlideImage(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        factory = { ImageView(context) },
        modifier = modifier
    ) { imageView ->
        Glide.with(context)
            .load(url)
            .into(imageView)
    }
}
