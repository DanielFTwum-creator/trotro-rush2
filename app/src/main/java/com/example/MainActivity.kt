package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.audio.HapticFeedbackManager
import com.example.data.AppRepository
import com.example.ui.TrotroRushApp

class MainActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = AppRepository(applicationContext)
        HapticFeedbackManager.init(applicationContext)

        setContent {
            TrotroRushApp(repository = repository)
        }
    }
}
