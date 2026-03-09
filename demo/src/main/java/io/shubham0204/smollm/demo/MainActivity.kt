package io.shubham0204.smollm.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
            val uiState by mainViewModel.collectAsState()

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
fun MainContent(
    uiState: MainViewModel.UiState,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onButtonClick) {
                Text(text = uiState.buttonCopy)
            }
            Spacer(modifier = Modifier.width(16.dp))

            Text(text = uiState.statusText)
        }

        if (uiState.aiResponse.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AI Response:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = uiState.aiResponse,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MaterialTheme {
        MainContent(
            uiState = MainViewModel.UiState(
                statusText = "Preview",
                modelStatus = ModelStatus.DOWNLOADING(0.45f),
                aiResponse = "Hello! I am a preview response.",
                buttonCopy = "Download model",
            ),
            onButtonClick = {}
        )
    }
}
