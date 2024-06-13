package me.dong.mines

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@SuppressLint("DefaultLocale")
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    class TT {
        private var start: Long = 0
        fun start() {
            start = System.currentTimeMillis()
        }

        fun elapsed(): Long {
            if (start == 0L) return 0
            val now = System.currentTimeMillis()
            if (now > start) return 0
            val elapsed = now - start
            if (elapsed <= 999000) return elapsed
            start = now
            return 0
        }
    }

    val tt by remember { mutableStateOf(TT()) }
//    tt.start()
    minesTheme {
        Box(modifier = Modifier.background(brush = BRUSH_BG)) {
            GridCanvas(13, 28)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val e = tt.elapsed()
            Text(
                text = String.format("⌛：%03d\t\t00/30", e),
                textAlign = TextAlign.Center,
                color = Color.White,
            )
        }
    }
}