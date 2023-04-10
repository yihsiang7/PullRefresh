package com.yihsiang.pullrefresh

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.yihsiang.pullrefresh.ui.theme.PullRefreshTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PullRefreshTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = {
                        SmallTopAppBar(
                            title = { Text("Basic") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Filled.ArrowBack, null)
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    var refreshing by remember { mutableStateOf(false) }
                    LaunchedEffect(refreshing) {
                        if (refreshing) {
                            delay(1200)
                            Log.i("TAG", "onCreate: $refreshing")
                            refreshing = false
                        }
                    }

                    PullRefresh(
                        state = rememberPullRefreshState(isRefreshing = refreshing),
                        onRefresh = { refreshing = true },
                    ) {
                        Content(padding)
                    }
                }
            }
        }
    }
}

@Composable
fun Content(contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(30) { index ->
            androidx.compose.material3.Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                androidx.compose.material3.Text(
                    text = index.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 30.dp),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}