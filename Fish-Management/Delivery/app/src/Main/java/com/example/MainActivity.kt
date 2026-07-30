package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DeliveryDashboardScreen
import com.example.ui.screens.LogisticsDashboardScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        val currentRole by viewModel.currentRole.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Crossfade(
            targetState = currentRole,
            modifier = Modifier.padding(innerPadding),
            label = "ScreenTransition"
          ) { role ->
            when (role) {
              is AppRole.Setup -> {
                RoleSelectionScreen(viewModel = viewModel)
              }
              is AppRole.Delivery -> {
                DeliveryDashboardScreen(viewModel = viewModel, staffId = role.staffId)
              }
              is AppRole.Admin -> {
                LogisticsDashboardScreen(viewModel = viewModel)
              }
              else -> {
                RoleSelectionScreen(viewModel = viewModel)
              }
            }
          }
        }
      }
    }
  }
}

