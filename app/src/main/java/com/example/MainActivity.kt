package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AppNavigation
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize database and repository using lifecycleScope
    val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
    val repository = AppRepository(database.appDao())
    val factory = AppViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[AppViewModel::class.java]

    setContent {
      MyApplicationTheme {
        AppNavigation(viewModel = viewModel)
      }
    }
  }
}

