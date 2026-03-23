package com.sayeedjoy.linkarena.util

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun String.isValidUrl(): Boolean {
    return this.startsWith("http://") || this.startsWith("https://")
}

fun String.extractDomain(): String? {
    return try {
        val url = if (this.startsWith("http")) this else "https://$this"
        java.net.URL(url).host
    } catch (e: Exception) {
        null
    }
}
