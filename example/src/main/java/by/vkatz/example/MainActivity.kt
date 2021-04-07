package by.vkatz.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.vkatz.example.services.*

class MainModel(services: Services = Services) : ViewModel() {
    private val api = services.api
    private val db = services.db
    private val ext = services.external
    private val data = services.data

    fun getApiData() = api.getData()
    fun getDbData() = db.getData()
    fun getExtId() = ext.getId()
    fun getApiDataViaData() = data.getApiData()
    fun getDbDataViaData() = data.getDbData()
    fun getExternalServiceIdStatic() = data.getExternalServiceIdStatic()
    fun getExternalServiceIdDynamic() = data.getExternalServiceIdDynamic()

    fun getData() = listOf(
        "--- Data ---",
        "\ngetApiData: ${getApiData().joinToString(", ")}",
        "\ngetDbData: ${getDbData().joinToString(", ")}",
        "\ngetExtId: [will change every time as item provided via newInstance] ${getExtId()} ",
        "\ngetApiDataViaData: ${getApiDataViaData().joinToString(", ")}",
        "\ngetDbDataViaData: ${getDbDataViaData().joinToString(", ")}",
        "\ngetExternalServiceIdStatic: [will be same, as instance of ext passed as item and not as provide] ${getExternalServiceIdStatic()}",
        "\ngetExternalServiceIdDynamic: [will change, as instance of ext passed as providable] ${getExternalServiceIdDynamic()}",
        "\n--- Instances ---",
        "\napi: $api",
        "\ndb: $db",
        "\next:$ext",
        "\ndata.api: ${data.api}",
        "\ndata.db: ${data.db}",
        "\ndata.externalStatic:${data.externalStatic}",
    )
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UI() }
    }

    @Composable
    @Preview(widthDp = 480, heightDp = 600, showSystemUi = false)
    fun UI() {
        val model by viewModels<MainModel>()
        Column(Modifier.fillMaxSize().background(Color.White).padding(8.dp)) {
            var data by remember { mutableStateOf(model.getData()) }
            Button(onClick = { data = model.getData() }) { Text(text = "refresh") }
            LazyColumn() {
                items(data) {
                    Text(it)
                }
            }
        }
    }
}