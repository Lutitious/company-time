package dev.lutitious.companytime

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lutitious.companytime.ui.theme.CompanyTimeTheme
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.Locale
import kotlin.system.measureTimeMillis

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompanyTimeTheme {
                StopwatchScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen() {
    var time by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var wageRate by remember { mutableDoubleStateOf(sharedPreferences.getString("wage", "7.25")?.toDouble() ?: 7.25) }
    var currencySymbol by remember { mutableStateOf(sharedPreferences.getString("currency", "$") ?: "$") }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            var lastTime = System.currentTimeMillis()
            while (isRunning) {
                val elapsedTime = measureTimeMillis {
                    delay(16L) // Approximately 60 FPS
                }
                time += elapsedTime
                lastTime += elapsedTime
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "currency" -> currencySymbol = sharedPreferences.getString("currency", "$") ?: "$"
                "wage" -> wageRate = sharedPreferences.getString("wage", "7.25")?.toDouble() ?: 7.25
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val formattedTime = remember(time) {
        val duration = Duration.ofMillis(time)
        String.format(Locale.getDefault(), "%02d:%02d:%02d.%03d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart())
    }

    val moneyMade = remember(time) {
        val hours = time / 3600000.0
        hours * wageRate
    }

    val robotoMono = FontFamily(Font(R.font.roboto_mono))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Company Time!") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.2f))
            Text(
                text = String.format(Locale.getDefault(), "Money Made: %s%.2f", currencySymbol, moneyMade),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontFamily = robotoMono
                )
            )
            Spacer(modifier = Modifier.weight(0.2f))
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = robotoMono
                )
            )
            Spacer(modifier = Modifier.weight(0.2f))
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(80.dp)
            ) {
                Text(if (isRunning) "Stop" else "Start", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp))
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Button(
                onClick = { time = 0L },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(80.dp)
            ) {
                Text("Reset", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp))
            }
            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StopwatchScreenPreview() {
    CompanyTimeTheme {
        StopwatchScreen()
    }
}