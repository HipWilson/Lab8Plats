package com.example.myapplication.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Obtiene el número de columnas de una grilla basado en el ancho de pantalla
 */
@Composable
fun getGridColumns(): Int {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    return when {
        screenWidth >= 900.dp -> 4
        screenWidth >= 600.dp -> 3
        else -> 2
    }
}

/**
 * Normaliza una query para usar como clave de caché
 */
fun normalizeQuery(query: String): String {
    return query.trim().lowercase()
}

/**
 * Formatea timestamp a fecha legible
 */
fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("es"))
    return format.format(date)
}

/**
 * Convierte bytes a KB, MB, GB
 */
fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}