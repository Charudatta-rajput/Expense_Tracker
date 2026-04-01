package com.charudatta.zorvyn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.charudatta.zorvyn.presentation.navigation.NavGraph
import com.charudatta.zorvyn.ui.theme.ZorvynTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ZorvynTheme {
                NavGraph()
            }
        }
    }
}