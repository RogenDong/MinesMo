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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.dong.mines.mines.Mines
import me.dong.mines.ui.theme.minesTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.i("app-jni", "invoke init()...")
        try {
            Mines.newMap(67, 13, 28)//67
        } catch (e: Exception) {
            Log.e("app-jni", "invoke jni fail !")
            Log.e("app-jni", "${e.message}\n${e.stackTrace}")
        }
        setContent {
            minesTheme {
                Scaffold {
                    MinesCanvas()
//                        TouchGridCanvas(13, 28)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    val mod = Modifier
        .height(50.dp)
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp)
    val txtColor = Color.Black
    val tt by remember { mutableStateOf(Watch()) }
//    tt.start()
    minesTheme {
        Box(modifier = Modifier.background(brush = BRUSH_BG)) {
            GridCanvas(13, 28)
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 120.dp, end = 120.dp),
            onClick = { }
        ) {
            Text(
                textAlign = TextAlign.Center,
                color = txtColor,
                text = "you win",
            )
        }
        Row(
            modifier = mod,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val e = tt.elapsed()
            Text(
                text = String.format("⌛：%03d", e),
                textAlign = TextAlign.Center,
                color = txtColor,
            )
        }
        Row(
            modifier = mod,
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$CHAR_FLAG：0/99",
                textAlign = TextAlign.Center,
                color = txtColor,
            )
        }
    }
}