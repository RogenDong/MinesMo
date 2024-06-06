package me.dong.mines

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import me.dong.mines.mines.Mines
import me.dong.mines.ui.theme.minesTheme

val BRUSH_BG = Brush.linearGradient(
    listOf(
        Color(0xFF8E72E0),
        Color(0xFF42A5F5),
        Color(0xFF8E72E0),
    )
)

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.i("app-jni", "invoke init()...")
        try {
            Mines.newMap(67, 13, 28)
        } catch (e: Exception) {
            Log.e("app-jni", "invoke jni fail !")
            Log.e("app-jni", "${e.message}\n${e.stackTrace}")
        }
        setContent {
            minesTheme {
                Scaffold {
                    Box(modifier = Modifier.background(brush = BRUSH_BG)) {
                        MinesCanvas()
//                        TouchGridCanvas(13, 28)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    minesTheme {
        Box(
            modifier = Modifier.background(Color.Black),
        ) {
            GridCanvas(13, 28)
        }
    }
}