package com.bo.asistenciaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bo.asistenciaapp.presentation.admin.navigation.AppNavHost
import com.bo.asistenciaapp.presentation.login.LoginScreen
import com.bo.asistenciaapp.ui.theme.AsistenciaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsistenciaAppTheme {
                setContent {
                    AppNavHost()
                }
            }
        }
    }
}