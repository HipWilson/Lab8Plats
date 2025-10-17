package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.api.PexelsApi
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.repository.PhotoRepository
import com.example.myapplication.ui.screens.details.DetailsScreen
import com.example.myapplication.ui.screens.details.DetailsViewModel
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.home.HomeViewModel
import com.example.myapplication.ui.screens.profile.ProfileScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar dependencias
        val database = AppDatabase.getInstance(this)
        val retrofit = Retrofit.Builder()
            .baseUrl(PexelsApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(PexelsApi::class.java)
        val repository = PhotoRepository(api, database)

        val homeViewModel = HomeViewModel(repository)
        val detailsViewModel = DetailsViewModel(repository)

        setContent {
            var isDarkMode by remember { mutableStateOf(isSystemInDarkTheme()) }
            val navController = rememberNavController()

            MyApplicationTheme(darkTheme = isDarkMode) {
                NavHost(navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNavigateToDetails = { photoId ->
                                navController.navigate("details/$photoId")
                            },
                            onNavigateToProfile = {
                                navController.navigate("profile")
                            },
                            viewModel = homeViewModel
                        )
                    }

                    composable("details/{photoId}") { backStackEntry ->
                        val photoId = backStackEntry.arguments?.getString("photoId") ?: return@composable
                        DetailsScreen(
                            photoId = photoId,
                            onBack = { navController.popBackStack() },
                            viewModel = detailsViewModel
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode }
                        )
                    }
                }
            }
        }
    }
}