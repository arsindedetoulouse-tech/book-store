package com.arsinde.oldboobstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.arsinde.oldboobstore.core.designsystem.theme.OldBoobStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OldBoobStoreTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigationHost()
                }
            }
        }
    }
}