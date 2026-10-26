package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ActiveOverlaySheet
import com.example.ui.CalculatorViewModel
import com.example.ui.SuiteTab
import com.example.ui.components.ScallopedCookieShape
import com.example.ui.screens.ConverterAndBaseNScreen
import com.example.ui.screens.GraphingStudioScreen
import com.example.ui.screens.MatrixAndAlgebraScreen
import com.example.ui.screens.QuantiaOverlaySheets
import com.example.ui.screens.ScientificWorkspaceScreen
import com.example.ui.screens.StatsAndFinanceScreen
import com.example.ui.theme.LocalExpressivePalette
import com.example.ui.theme.QuantiaTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuantiaApp()
        }
    }
}

@Composable
fun QuantiaApp() {
    val context = LocalContext.current
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModel.provideFactory(context)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val evaluation by viewModel.liveEvaluation.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val variables by viewModel.variables.collectAsStateWithLifecycle()
    val formulas by viewModel.customFormulas.collectAsStateWithLifecycle()

    // BackHandler returns to Scientific home suite if on a secondary suite tab
    BackHandler(enabled = uiState.activeSuite != SuiteTab.SCIENTIFIC) {
        viewModel.selectSuite(SuiteTab.SCIENTIFIC)
    }

    // Auto-dismiss status banner after 2.6 seconds
    LaunchedEffect(uiState.statusBanner) {
        if (uiState.statusBanner != null) {
            delay(2600)
            viewModel.clearBanner()
        }
    }

    QuantiaTheme(
        preset = preferences.themePreset,
        themeMode = preferences.themeMode,
        useDynamicColor = preferences.dynamicColor
    ) {
        val palette = LocalExpressivePalette.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                QuantiaExpressiveTopBar(
                    activePresetName = preferences.themePreset.displayName,
                    historyCount = history.size,
                    onOpenHistory = { viewModel.openSheet(ActiveOverlaySheet.HISTORY_AND_MEMORY) },
                    onOpenConstants = { viewModel.openSheet(ActiveOverlaySheet.PHYSICS_CONSTANTS) },
                    onOpenThemes = { viewModel.openSheet(ActiveOverlaySheet.THEME_STUDIO) }
                )
            },
            bottomBar = {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp
                ) {
                    SuiteTab.entries.forEach { tab ->
                        val icon = when (tab) {
                            SuiteTab.SCIENTIFIC -> Icons.Default.Calculate
                            SuiteTab.GRAPHING -> Icons.Default.ShowChart
                            SuiteTab.MATRIX_ALGEBRA -> Icons.Default.GridOn
                            SuiteTab.CONVERTER_BASEN -> Icons.Default.SwapHoriz
                            SuiteTab.STATS_FINANCE -> Icons.Default.BarChart
                        }
                        NavigationBarItem(
                            selected = uiState.activeSuite == tab,
                            onClick = { viewModel.selectSuite(tab) },
                            icon = { Icon(icon, contentDescription = tab.title) },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.name}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isExpandedScreen = maxWidth >= 700.dp

                if (isExpandedScreen && uiState.activeSuite != SuiteTab.SCIENTIFIC) {
                    // Canonical Two-Pane Adaptive Layout on tablets / foldables / landscape
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ScientificWorkspaceScreen(
                            uiState = uiState,
                            preferences = preferences,
                            evaluation = evaluation,
                            variables = variables,
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.weight(1.1f)) {
                            ActiveSuiteContent(
                                suite = uiState.activeSuite,
                                uiState = uiState,
                                preferences = preferences,
                                evaluation = evaluation,
                                variables = variables,
                                viewModel = viewModel
                            )
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = uiState.activeSuite,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "suiteTransition"
                    ) { suite ->
                        ActiveSuiteContent(
                            suite = suite,
                            uiState = uiState,
                            preferences = preferences,
                            evaluation = evaluation,
                            variables = variables,
                            viewModel = viewModel
                        )
                    }
                }

                // Floating Expressive Status Banner
                AnimatedVisibility(
                    visible = uiState.statusBanner != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.scallopedBadgeContainer,
                        tonalElevation = 8.dp
                    ) {
                        Text(
                            text = uiState.statusBanner.orEmpty(),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.scallopedBadgeContent,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Modal Bottom Sheets
            QuantiaOverlaySheets(
                uiState = uiState,
                preferences = preferences,
                history = history,
                variables = variables,
                formulas = formulas,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ActiveSuiteContent(
    suite: SuiteTab,
    uiState: com.example.ui.CalculatorUiState,
    preferences: com.example.data.UserPreferences,
    evaluation: com.example.domain.EvaluationOutput,
    variables: List<com.example.data.SavedVariableEntity>,
    viewModel: CalculatorViewModel
) {
    when (suite) {
        SuiteTab.SCIENTIFIC -> ScientificWorkspaceScreen(
            uiState = uiState,
            preferences = preferences,
            evaluation = evaluation,
            variables = variables,
            viewModel = viewModel
        )
        SuiteTab.GRAPHING -> GraphingStudioScreen(
            uiState = uiState,
            viewModel = viewModel
        )
        SuiteTab.MATRIX_ALGEBRA -> MatrixAndAlgebraScreen(
            viewModel = viewModel,
            hapticsEnabled = preferences.hapticsEnabled
        )
        SuiteTab.CONVERTER_BASEN -> ConverterAndBaseNScreen(
            uiState = uiState,
            viewModel = viewModel,
            hapticsEnabled = preferences.hapticsEnabled
        )
        SuiteTab.STATS_FINANCE -> StatsAndFinanceScreen(
            viewModel = viewModel
        )
    }
}

@Composable
private fun QuantiaExpressiveTopBar(
    activePresetName: String,
    historyCount: Int,
    onOpenHistory: () -> Unit,
    onOpenConstants: () -> Unit,
    onOpenThemes: () -> Unit
) {
    val palette = LocalExpressivePalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Header with Scalloped Cookie Icon Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = ScallopedCookieShape(lobes = 10, amplitudeRatio = 0.09f),
                color = palette.scallopedBadgeContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "∑",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.scallopedBadgeContent
                    )
                }
            }
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$activePresetName  •  118+ Math Tools",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Expressive Action Pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onOpenConstants,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("topbar_constants_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = stringResource(R.string.action_constants)
                    )
                }
            }
            Surface(
                onClick = onOpenHistory,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("topbar_history_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.action_history),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$historyCount",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Surface(
                onClick = onOpenThemes,
                shape = ScallopedCookieShape(lobes = 12, amplitudeRatio = 0.085f),
                color = palette.resultPillContainer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("topbar_theme_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = stringResource(R.string.action_themes),
                        tint = palette.resultPillContent
                    )
                }
            }
        }
    }
}
