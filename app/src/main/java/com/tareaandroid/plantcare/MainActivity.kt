package com.tareaandroid.plantcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tareaandroid.plantcare.navigation.PlantCareApp
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantCareTheme {
                PlantCareApp()
            }
        }
    }
}
