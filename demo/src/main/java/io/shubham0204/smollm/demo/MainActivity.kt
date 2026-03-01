package io.shubham0204.smollm.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.shubham0204.smolchat.core.ModelStatus
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = koinViewModel()
            val uiState by mainViewModel.uiState.collectAsState()

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(
                        uiState = uiState,
                        onButtonClick = { mainViewModel.onButtonClicked() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    uiState: MainViewModel.UiState,
    onButtonClick: () -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(onClick = onButtonClick) {
            Text(text = "Download Model")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "${uiState.statusText} (${uiState.modelStatus})")
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MaterialTheme {
        MainContent(
            uiState = MainViewModel.UiState(
                statusText = "Preview",
                modelStatus = ModelStatus.ON_DISK
            ),
            onButtonClick = {}
        )
    }
}
