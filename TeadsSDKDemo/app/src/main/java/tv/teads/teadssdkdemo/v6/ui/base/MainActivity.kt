package tv.teads.teadssdkdemo.v6.ui.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import tv.teads.teadssdkdemo.v6.ui.base.navigation.NavigationHandler
import tv.teads.teadssdkdemo.v6.ui.base.navigation.Route
import tv.teads.teadssdkdemo.v6.ui.base.theme.TeadsSDKDemoTheme
import tv.teads.teadssdkdemo.v5.InReadWebViewColumnScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TeadsSDKDemoTheme {
                var currentRoute by remember { mutableStateOf<Route>(Route.Demo) }

                // Handle device back button
                val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(currentRoute) {
                    val callback = object : OnBackPressedCallback(currentRoute != Route.Demo) {
                        override fun handleOnBackPressed() {
                            currentRoute = Route.Demo
                        }
                    }
                    backDispatcher?.addCallback(lifecycleOwner, callback)
                    onDispose { callback.remove() }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentRoute) {
                                        Route.InReadWebViewColumn -> "InRead WebView Column"
                                        else -> "Teads SDK Demo"
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    textAlign = if (currentRoute == Route.Demo) TextAlign.Center else TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            navigationIcon = {
                                if (currentRoute == Route.InReadWebViewColumn) {
                                    IconButton(onClick = { currentRoute = Route.Demo }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back to Demo"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    when (currentRoute) {
                        Route.Demo -> {
                            val viewModel: DemoViewModel = viewModel()

                            // Set up navigation callback
                            viewModel.setOnNavigateCallback { navRoute ->
                                when (navRoute) {
                                    Route.InReadWebViewColumn -> {
                                        currentRoute = navRoute
                                    }
                                    else -> {
                                        NavigationHandler.navigateToRoute(
                                            fromActivity = this@MainActivity,
                                            route = navRoute
                                        )
                                    }
                                }
                            }

                            DemoScreen(
                                modifier = Modifier.padding(paddingValues),
                                viewModel = viewModel
                            )
                        }
                        Route.InReadWebViewColumn -> {
                            InReadWebViewColumnScreen(
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
