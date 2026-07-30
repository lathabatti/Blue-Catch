package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.FishRepository
import com.example.ui.FishViewModel
import com.example.ui.FishViewModelFactory
import com.example.ui.screens.FishAppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Room Database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = FishRepository(database.fishDao())

        // 2. Instantiate ViewModel with Factory
        val viewModel = ViewModelProvider(
            this,
            FishViewModelFactory(repository)
        )[FishViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                FishAppNavigation(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
