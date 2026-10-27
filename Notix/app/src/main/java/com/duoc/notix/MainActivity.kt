package com.duoc.notix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.duoc.notix.ui.NotixApp
import com.duoc.notix.ui.theme.NotixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as NotixApplication
        setContent {
            NotixTheme {
                NotixApp(app.authRepository, app.procedureRepository, app.documentStore)
            }
        }
    }
}
