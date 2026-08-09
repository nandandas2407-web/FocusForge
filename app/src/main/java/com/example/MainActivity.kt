// ============================================================
// FILE: app/src/main/java/com/example/MainActivity.kt
// PURPOSE: Main activity hosting Jetpack Compose navigation, adaptive tablet layout,
//          liquid glass navigation, and full viewmodel bindings for FocusForge.
// CREATED: 2026-08-09
// ============================================================

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    private val focusViewModel by viewModels<FocusViewModel>()
    private val taskViewModel by viewModels<TaskViewModel>()
    private val calendarViewModel by viewModels<CalendarViewModel>()
    private val statsViewModel by viewModels<StatsViewModel>()
    private val themeViewModel by viewModels<ThemeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FocusForgeTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

                val blockedApps by focusViewModel.blockedApps.collectAsStateWithLifecycle()
                val focusSessions by focusViewModel.focusSessions.collectAsStateWithLifecycle()
                val timerSeconds by focusViewModel.timerSeconds.collectAsStateWithLifecycle()
                val isTimerRunning by focusViewModel.isTimerRunning.collectAsStateWithLifecycle()
                val timerMode by focusViewModel.timerMode.collectAsStateWithLifecycle()
                val youtubeWhitelist by focusViewModel.youtubeWhitelist.collectAsStateWithLifecycle()
                val websiteBlocks by focusViewModel.websiteBlocks.collectAsStateWithLifecycle()
                val streakGoal by focusViewModel.streakGoal.collectAsStateWithLifecycle()

                val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
                val calendarEvents by calendarViewModel.events.collectAsStateWithLifecycle()
                val usageStats by statsViewModel.usageStats.collectAsStateWithLifecycle()
                val themeSettings by themeViewModel.settings.collectAsStateWithLifecycle()

                val navItems = listOf(
                    NavigationItem("dashboard", "Home", Icons.Outlined.Home, Icons.Filled.Home),
                    NavigationItem("timer", "Focus", Icons.Outlined.Timer, Icons.Filled.Timer),
                    NavigationItem("blocker", "Blocker", Icons.Outlined.Block, Icons.Filled.Block),
                    NavigationItem("tasks", "Tasks", Icons.Outlined.Checklist, Icons.Filled.Checklist),
                    NavigationItem("settings", "Theme", Icons.Outlined.Palette, Icons.Filled.Palette)
                )

                var hasCompletedOnboarding by remember { mutableStateOf(false) }

                if (!hasCompletedOnboarding) {
                    OnboardingScreen(
                        onFinishOnboarding = { hasCompletedOnboarding = true }
                    )
                } else {
                    val isTablet = Responsive.isTablet()

                    if (isTablet) {
                        // Tablet: Side Navigation Rail + Content
                        Row(modifier = Modifier.fillMaxSize().background(GlassTokens.DarkBase)) {
                            GlassNavRail(
                                currentRoute = currentRoute,
                                onNavigate = { route -> navController.navigate(route) },
                                items = navItems
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                MainNavHost(
                                    navController = navController,
                                    focusViewModel = focusViewModel,
                                    taskViewModel = taskViewModel,
                                    calendarViewModel = calendarViewModel,
                                    themeViewModel = themeViewModel,
                                    blockedApps = blockedApps,
                                    focusSessions = focusSessions,
                                    timerSeconds = timerSeconds,
                                    isTimerRunning = isTimerRunning,
                                    timerMode = timerMode,
                                    youtubeWhitelist = youtubeWhitelist,
                                    websiteBlocks = websiteBlocks,
                                    streakGoal = streakGoal,
                                    tasks = tasks,
                                    calendarEvents = calendarEvents,
                                    usageStats = usageStats,
                                    themeSettings = themeSettings
                                )
                            }
                        }
                    } else {
                        // Phone: Bottom Navigation Bar + Content
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(GlassTokens.DarkBase),
                            containerColor = Color.Transparent,
                            bottomBar = {
                                GlassBottomNav(
                                    currentRoute = currentRoute,
                                    onNavigate = { route -> navController.navigate(route) },
                                    items = navItems
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                MainNavHost(
                                    navController = navController,
                                    focusViewModel = focusViewModel,
                                    taskViewModel = taskViewModel,
                                    calendarViewModel = calendarViewModel,
                                    themeViewModel = themeViewModel,
                                    blockedApps = blockedApps,
                                    focusSessions = focusSessions,
                                    timerSeconds = timerSeconds,
                                    isTimerRunning = isTimerRunning,
                                    timerMode = timerMode,
                                    youtubeWhitelist = youtubeWhitelist,
                                    websiteBlocks = websiteBlocks,
                                    streakGoal = streakGoal,
                                    tasks = tasks,
                                    calendarEvents = calendarEvents,
                                    usageStats = usageStats,
                                    themeSettings = themeSettings
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainNavHost(
    navController: androidx.navigation.NavHostController,
    focusViewModel: FocusViewModel,
    taskViewModel: TaskViewModel,
    calendarViewModel: CalendarViewModel,
    themeViewModel: ThemeViewModel,
    blockedApps: List<com.example.data.entity.BlockedAppEntity>,
    focusSessions: List<com.example.data.entity.FocusSessionEntity>,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    timerMode: String,
    youtubeWhitelist: List<com.example.data.entity.YoutubeWhitelistEntity>,
    websiteBlocks: List<com.example.data.entity.WebsiteBlockEntity>,
    streakGoal: com.example.data.entity.StreakGoalEntity?,
    tasks: List<com.example.data.entity.TaskEntity>,
    calendarEvents: List<com.example.data.entity.CalendarEventEntity>,
    usageStats: List<com.example.data.repository.AppUsageInfo>,
    themeSettings: com.example.data.entity.ThemeSettingsEntity?
) {
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToTimer = { navController.navigate("timer") },
                onNavigateToBlocker = { navController.navigate("blocker") },
                onNavigateToShorts = { navController.navigate("shorts_reels") },
                onNavigateToTasks = { navController.navigate("tasks") },
                onNavigateToYoutube = { navController.navigate("youtube_study") },
                tasks = tasks,
                focusSessions = focusSessions,
                streakGoal = streakGoal,
                usageStats = usageStats
            )
        }

        composable("timer") {
            FocusTimerScreen(
                timerSeconds = timerSeconds,
                isRunning = isTimerRunning,
                timerMode = timerMode,
                onStart = { focusViewModel.startTimer() },
                onPause = { focusViewModel.pauseTimer() },
                onReset = { minutes -> focusViewModel.resetTimer(minutes) },
                onTick = { focusViewModel.tickTimer() },
                onSetMode = { mode, minutes -> focusViewModel.setTimerMode(mode, minutes) },
                focusWallpaperOverride = themeSettings?.focusTimerWallpaperOverride
            )
        }

        composable("blocker") {
            AppBlockerScreen(
                blockedApps = blockedApps,
                onToggleAppBlocked = { pkg, name, cat, isBlocked ->
                    focusViewModel.toggleAppBlocked(pkg, name, cat, isBlocked)
                }
            )
        }

        composable("shorts_reels") {
            ShortsReelsBlockerScreen(
                blockedApps = blockedApps,
                onToggleReelsBlocked = { pkg, name, isBlocked ->
                    focusViewModel.toggleReelsBlocked(pkg, name, isBlocked)
                },
                onToggleShortsBlocked = { pkg, name, isBlocked ->
                    focusViewModel.toggleShortsBlocked(pkg, name, isBlocked)
                }
            )
        }

        composable("youtube_study") {
            YoutubeStudyModeScreen(
                whitelist = youtubeWhitelist,
                isStudyModeEnabled = themeSettings?.isYoutubeStudyModeEnabled ?: true,
                onToggleStudyMode = { enabled -> focusViewModel.toggleYoutubeStudyMode(enabled) },
                onAddChannel = { id, title -> focusViewModel.addYoutubeChannel(id, title) },
                onRemoveChannel = { channel -> focusViewModel.removeYoutubeChannel(channel) }
            )
        }

        composable("website_blocker") {
            WebsiteBlockerScreen(
                websiteBlocks = websiteBlocks,
                onAddDomain = { dom, cat -> focusViewModel.addWebsiteBlock(dom, cat) },
                onRemoveDomain = { item -> focusViewModel.removeWebsiteBlock(item) }
            )
        }

        composable("tasks") {
            TodoListScreen(
                tasks = tasks,
                onAddTask = { title, notes, priority, cat, est ->
                    taskViewModel.addTask(title, notes, priority, cat, est)
                },
                onToggleTask = { task -> taskViewModel.toggleTaskCompleted(task) },
                onDeleteTask = { id -> taskViewModel.deleteTask(id) },
                onStartTaskSession = {
                    focusViewModel.setTimerMode("POMODORO", 25)
                    navController.navigate("timer")
                }
            )
        }

        composable("calendar") {
            CalendarScreen(
                events = calendarEvents,
                onAddEvent = { title, type, dateStr, isExam ->
                    calendarViewModel.addEvent(title, type, dateStr, isExam)
                },
                onDeleteEvent = { id -> calendarViewModel.deleteEvent(id) }
            )
        }

        composable("stats") {
            StatsDashboardScreen(
                usageStats = usageStats,
                focusSessions = focusSessions
            )
        }

        composable("streaks") {
            StreaksGoalsScreen(
                streakGoal = streakGoal,
                onUpdateStreakGoal = { goalMins -> focusViewModel.updateStreakGoal(goalMins) }
            )
        }

        composable("settings") {
            ThemeCustomizerScreen(
                themeSettings = themeSettings,
                onUpdateWallpaperPreset = { themeViewModel.updateWallpaperPreset(it) },
                onUpdateGlassBlur = { themeViewModel.updateGlassBlur(it) },
                onUpdateGlassOpacity = { themeViewModel.updateGlassOpacity(it) }
            )
        }
    }
}
