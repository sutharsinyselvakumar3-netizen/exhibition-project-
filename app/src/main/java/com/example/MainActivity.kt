package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.StreamState
import com.example.ui.components.TopAgriBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RelaysFireScreen
import com.example.ui.screens.RobotControlScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SoilWaterScreen
import com.example.ui.theme.AgriBlue
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.RoyalWhite
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory

enum class AgriNavDestination(val label: String) {
    HOME("HOME"),
    ROBOT("ROBOT"),
    SOIL_WATER("SOIL + WATER"),
    RELAYS_FIRE("RELAYS + FIRE")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AgriRobotApp()
            }
        }
    }
}

@Composable
fun AgriRobotApp() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))

    val status by viewModel.robotStatus.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val cameraState by viewModel.cameraState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val esp32TestStatus by viewModel.esp32TestStatus.collectAsStateWithLifecycle()
    val cameraTestStatus by viewModel.cameraTestStatus.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AgriNavDestination.HOME) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission for Android 13+ (POST_NOTIFICATIONS)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    if (isSettingsOpen) {
        BackHandler {
            isSettingsOpen = false
        }
        SettingsScreen(
            currentSettings = settings,
            esp32TestStatus = esp32TestStatus,
            cameraTestStatus = cameraTestStatus,
            onSaveSettings = { updated ->
                viewModel.saveSettings(updated)
            },
            onResetDefaults = {
                viewModel.resetSettingsToDefaults()
            },
            onTestEsp32 = {
                viewModel.testEsp32Connection()
            },
            onTestCamera = {
                viewModel.testCameraConnection()
            },
            onClose = {
                isSettingsOpen = false
            }
        )
    } else {
        BackHandler(enabled = currentScreen != AgriNavDestination.HOME) {
            currentScreen = AgriNavDestination.HOME
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = RoyalWhite,
            topBar = {
                val screenTitle = when (currentScreen) {
                    AgriNavDestination.HOME -> "AI AGRI ROBOT"
                    AgriNavDestination.ROBOT -> "ROBOT CONTROL"
                    AgriNavDestination.SOIL_WATER -> "SOIL + WATER"
                    AgriNavDestination.RELAYS_FIRE -> "RELAYS + FIRE"
                }
                TopAgriBar(
                    title = screenTitle,
                    isEsp32Online = status.connected,
                    isCameraOnline = cameraState is StreamState.Streaming,
                    onSettingsClick = { isSettingsOpen = true }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CardWhite,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // 1. HOME
                    NavigationBarItem(
                        selected = currentScreen == AgriNavDestination.HOME,
                        onClick = { currentScreen = AgriNavDestination.HOME },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = {
                            Text(
                                text = "HOME",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == AgriNavDestination.HOME) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = agriNavColors(),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // 2. ROBOT
                    NavigationBarItem(
                        selected = currentScreen == AgriNavDestination.ROBOT,
                        onClick = { currentScreen = AgriNavDestination.ROBOT },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PrecisionManufacturing,
                                contentDescription = "Robot"
                            )
                        },
                        label = {
                            Text(
                                text = "ROBOT",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == AgriNavDestination.ROBOT) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = agriNavColors(),
                        modifier = Modifier.testTag("nav_tab_robot")
                    )

                    // 3. SOIL + WATER
                    NavigationBarItem(
                        selected = currentScreen == AgriNavDestination.SOIL_WATER,
                        onClick = { currentScreen = AgriNavDestination.SOIL_WATER },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Soil and Water"
                            )
                        },
                        label = {
                            Text(
                                text = "SOIL + WATER",
                                fontSize = 10.sp,
                                fontWeight = if (currentScreen == AgriNavDestination.SOIL_WATER) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = agriNavColors(),
                        modifier = Modifier.testTag("nav_tab_soil_water")
                    )

                    // 4. RELAYS + FIRE
                    NavigationBarItem(
                        selected = currentScreen == AgriNavDestination.RELAYS_FIRE,
                        onClick = { currentScreen = AgriNavDestination.RELAYS_FIRE },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Relays and Fire"
                            )
                        },
                        label = {
                            Text(
                                text = "RELAYS + FIRE",
                                fontSize = 10.sp,
                                fontWeight = if (currentScreen == AgriNavDestination.RELAYS_FIRE) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = agriNavColors(),
                        modifier = Modifier.testTag("nav_tab_relays_fire")
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AgriNavDestination.HOME -> {
                        HomeScreen(
                            status = status,
                            cameraState = cameraState,
                            soilDryThreshold = settings.soilDryThreshold,
                            soilWetThreshold = settings.soilWetThreshold,
                            waterLowThreshold = settings.waterLowThreshold,
                            waterCriticalThreshold = settings.waterCriticalThreshold
                        )
                    }

                    AgriNavDestination.ROBOT -> {
                        RobotControlScreen(
                            status = status,
                            cameraState = cameraState,
                            cameraIp = settings.cameraIp,
                            onRetryCamera = { viewModel.reconnectCamera() },
                            onOpenSettings = { isSettingsOpen = true },
                            onMotorCommand = { cmd -> viewModel.sendMotorCommand(cmd) },
                            onSpeedChange = { spd -> viewModel.sendSpeed(spd) },
                            onCameraServoChange = { ang -> viewModel.sendCameraServo(ang) },
                            onEmergencyStop = { viewModel.emergencyStop() }
                        )
                    }

                    AgriNavDestination.SOIL_WATER -> {
                        SoilWaterScreen(
                            status = status,
                            cameraState = cameraState,
                            cameraIp = settings.cameraIp,
                            soilDryThreshold = settings.soilDryThreshold,
                            soilWetThreshold = settings.soilWetThreshold,
                            waterLowThreshold = settings.waterLowThreshold,
                            waterCriticalThreshold = settings.waterCriticalThreshold,
                            onRetryCamera = { viewModel.reconnectCamera() },
                            onOpenSettings = { isSettingsOpen = true },
                            onToggleRelaySoil = { viewModel.toggleRelaySoil() },
                            onSoilServoChange = { ang -> viewModel.sendSoilServo(ang) }
                        )
                    }

                    AgriNavDestination.RELAYS_FIRE -> {
                        RelaysFireScreen(
                            status = status,
                            cameraState = cameraState,
                            cameraIp = settings.cameraIp,
                            onRetryCamera = { viewModel.reconnectCamera() },
                            onOpenSettings = { isSettingsOpen = true },
                            onToggleRelaySoil = { viewModel.toggleRelaySoil() },
                            onToggleRelayPump = { viewModel.toggleRelayPump() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun agriNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AgriBlue,
    selectedTextColor = DarkNavy,
    indicatorColor = Color(0xFFDBEAFE),
    unselectedIconColor = NavyMuted,
    unselectedTextColor = NavyMuted
)
