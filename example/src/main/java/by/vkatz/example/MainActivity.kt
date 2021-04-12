package by.vkatz.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.vkatz.example.services.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UI() }
    }

    @Composable
    @Preview(widthDp = 480, heightDp = 600, showSystemUi = false)
    fun UI() {
        val model by viewModels<Services1Model>()
        Column(Modifier.fillMaxSize().background(Color.White).padding(8.dp)) {
            var data by remember { mutableStateOf(model.getRefs()) }
            Button(onClick = { data = model.getRefs() }) { Text(text = "refresh") }
            Text(data)
        }
    }
}