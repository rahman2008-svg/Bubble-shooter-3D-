package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HighScore
import com.example.game.*

// Semantic Vibrant Palette Tokens (Slate 900, Slate 800, Indigo 500, Rose 500, Emerald 500)
val SpaceDarkBg = Color(0xFF0F172A) // Slate 900
val CyberIce = Color(0xFF6366F1)    // Indigo 500
val NeonPink = Color(0xFFF43F5E)    // Rose 500
val CyberPurple = Color(0xFF10B981) // Emerald 500
val CardGray = Color(0xFF1E293B)    // Slate 800

@Composable
fun MainLayout(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentRoute by viewModel.navRoute.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
    ) {
        Crossfade(
            targetState = currentRoute,
            animationSpec = tween(350),
            label = "ScreenTransition"
        ) { route ->
            when (route) {
                "menu" -> MainMenuScreen(viewModel)
                "game" -> GamePlayScreen(viewModel)
                "scores" -> LeaderboardScreen(viewModel)
                "settings" -> SettingsScreen(viewModel)
                "credits" -> CreditsScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TitleScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BUBBLE SHOOTER",
                    fontSize = 32.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        shadow = Shadow(
                            color = NeonPink,
                            offset = Offset(0f, 4f),
                            blurRadius = 8f
                        )
                    ),
                    modifier = Modifier.animateContentSize()
                )
                Text(
                    text = "3D",
                    fontSize = 58.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = CyberIce,
                    letterSpacing = 6.sp,
                    style = MaterialTheme.typography.displayLarge.copy(
                        shadow = Shadow(
                            color = CyberIce.copy(alpha = 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = 15f * titleScale
                        )
                    )
                )
                Text(
                    text = "HYPER-PERFORMANCE 3D ENGINE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Action Buttons Matrix
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CyberGameButton(
                text = "START CAMPAIGN",
                icon = Icons.Default.PlayArrow,
                primaryColor = CyberIce,
                secondaryColor = CyberPurple,
                onClick = { viewModel.navigateTo("game") },
                modifier = Modifier.testTag("start_game_button")
            )

            CyberGameButton(
                text = "LEADERBOARDS",
                icon = Icons.Default.Star,
                primaryColor = NeonPink,
                secondaryColor = CyberPurple,
                onClick = { viewModel.navigateTo("scores") },
                modifier = Modifier.testTag("high_scores_button")
            )

            CyberGameButton(
                text = "SYSTEM SETTINGS",
                icon = Icons.Default.Settings,
                primaryColor = Color.White,
                secondaryColor = CardGray,
                onClick = { viewModel.navigateTo("settings") },
                modifier = Modifier.testTag("settings_button")
            )

            CyberGameButton(
                text = "DEVELOPER CREDITS",
                icon = Icons.Default.Info,
                primaryColor = CyberIce,
                secondaryColor = CardGray,
                onClick = { viewModel.navigateTo("credits") },
                modifier = Modifier.testTag("credits_button")
            )
        }

        // Developer attribution & copyright signature
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "DEVELOPED BY PRINCE AR ABDUR RAHMAN",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Text(
                text = "PUBLISHED BY NEXVORA LAB'S OFC",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                color = CyberIce.copy(alpha = 0.5f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "© 2026 NEXVORA LAB'S OFC. ALL RIGHTS RESERVED.",
                fontSize = 8.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun CyberGameButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = CardGray.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.7f)),
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Slight modern accent background
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(primaryColor.copy(alpha = 0.08f), secondaryColor.copy(alpha = 0.02f))
                        )
                    )
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun GamePlayScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val soundEnabled by viewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.isHapticEnabled.collectAsStateWithLifecycle()

    // Instantiate game logic and view
    val gameEngine = remember {
        GameEngine().apply {
            isSoundEnabled = soundEnabled
            isHapticEnabled = hapticEnabled
        }
    }

    var isPaused by remember { mutableStateOf(false) }
    var triggerScoreSubmit by remember { mutableStateOf<Pair<Int, Int>?>(null) } // Score, Level

    // Synchronize updates of settings
    LaunchedEffect(soundEnabled, hapticEnabled) {
        gameEngine.isSoundEnabled = soundEnabled
        gameEngine.isHapticEnabled = hapticEnabled
    }

    // Set callback to receive score on game over/victory
    gameEngine.onScoreSavedListener = { finalScore, finalLevel ->
        if (gameEngine.state == GameEngine.State.GAME_OVER || gameEngine.state == GameEngine.State.VICTORY) {
            triggerScoreSubmit = Pair(finalScore, finalLevel)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Embed high-performance Custom SurfaceView
        AndroidView(
            factory = { ctx ->
                GameView(ctx).apply {
                    this.gameEngine = gameEngine
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Game HUD Head-Up Displays
        GamePlayHUD(
            score = gameEngine.score,
            level = gameEngine.level,
            nextColor = gameEngine.nextLauncherColor,
            onPauseToggle = {
                isPaused = true
                gameEngine.state = GameEngine.State.PAUSED
            }
        )

        // Overlay states: paused
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SpaceDarkBg.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, CyberIce.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "GAME PAUSED",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Divider(color = CyberIce.copy(alpha = 0.3f), thickness = 1.dp)

                        Button(
                            onClick = {
                                isPaused = false
                                gameEngine.state = GameEngine.State.RUNNING
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberIce),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RESUME", color = SpaceDarkBg, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                isPaused = false
                                gameEngine.resetGame()
                                triggerScoreSubmit = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("RESTART", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                isPaused = false
                                viewModel.navigateTo("menu")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
                            border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("QUIT TO MENU", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Overlay states: Game Over or Victory triggers score archiving
        triggerScoreSubmit?.let { data ->
            val isVictory = gameEngine.state == GameEngine.State.VICTORY
            var nameToSave by remember { mutableStateOf(viewModel.playerName.value) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SpaceDarkBg.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, if (isVictory) CyberIce.copy(alpha = 0.8f) else NeonPink.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isVictory) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isVictory) CyberIce else NeonPink,
                            modifier = Modifier.size(54.dp)
                        )

                        Text(
                            text = if (isVictory) "MISSION COMPLETED" else "GAME OVER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = if (isVictory) "You have cleared all bubbles!" else "The bubble wall invaded your boundaries.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpaceDarkBg),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("SCORE", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                                    Text("${data.first}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CyberIce)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("LEVEL", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                                    Text("${data.second}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NeonPink)
                                }
                            }
                        }

                        // Save score parameters form
                        Text(
                            text = "ENTER PILOT CODE SIGNATURE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.Start)
                        )

                        OutlinedTextField(
                            value = nameToSave,
                            onValueChange = {
                                if (it.length <= 15) {
                                    nameToSave = it
                                    viewModel.playerName.value = it
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberIce,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedContainerColor = SpaceDarkBg,
                                unfocusedContainerColor = SpaceDarkBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    // Record record offline
                                    viewModel.saveScore(data.first, data.second)
                                    if (isVictory) {
                                        triggerScoreSubmit = null
                                        gameEngine.nextLevel()
                                    } else {
                                        triggerScoreSubmit = null
                                        gameEngine.resetGame()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberIce),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isVictory) "NEXT LEVEL" else "PLAY AGAIN",
                                    color = SpaceDarkBg,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.saveScore(data.first, data.second)
                                    triggerScoreSubmit = null
                                    viewModel.navigateTo("menu")
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("MENU")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamePlayHUD(
    score: Int,
    level: Int,
    nextColor: BubbleColor,
    onPauseToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        // Header HUD Control board (Material 3 Game Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left HUD: Level & Score stacked
            Column {
                Text(
                    text = "LEVEL $level",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF818CF8), // Indigo 400
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = String.format("%,d", score),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }

            // Right HUD: 60 FPS badge & Pause button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 60 FPS Simulator Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "FpsDotPulse")
                        val alphaPulse by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "FpsDotPulse"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399).copy(alpha = alphaPulse)) // Emerald 400
                        )
                        Text(
                            text = "60 FPS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399) // Emerald 400
                        )
                    }
                }

                // Pause button
                IconButton(
                    onClick = onPauseToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberIce)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Pause Grid System",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Upcoming ammo bullet HUD panel overlays Launcher
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 60.dp, end = 10.dp),
            colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "NEXT AMMO",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.5f)
                )
                // Graphical sphere of next color
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White, Color(nextColor.displayColor), Color.Black),
                                center = Offset(4f, 4f)
                            )
                        )
                )
            }
        }

        // Touch Instructions
        Text(
            text = "◀  DRAG TO AIM • RELEASE TO SHOOT  ▶",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.White.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}

@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val scores by viewModel.topHighScores.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo("menu") },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardGray)
                    .testTag("back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Return",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "PILOTS LEADERBOARD",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.5.sp
            )
        }

        if (scores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(74.dp)
                    )
                    Text(
                        text = "LEADERBOARD VACANT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Take down target levels to establish records!",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(scores) { index, entry ->
                    LeaderboardRow(rank = index + 1, score = entry)
                }
            }
        }

        // Footer option
        OutlinedButton(
            onClick = { viewModel.clearAllScores() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
            border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("RESET ALL DATABASE RECORDS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, score: HighScore) {
    // Styling attributes based on 1st, 2nd, 3rd rankings
    val (rankColor, borderColor) = when (rank) {
        1 -> Pair(Color(0xFFFFD600), Color(0xFFFFD600).copy(alpha = 0.6f))
        2 -> Pair(Color(0xFFC0C0C0), Color(0xFFC0C0C0).copy(alpha = 0.4f))
        3 -> Pair(Color(0xFFFF8D1A), Color(0xFFFF8D1A).copy(alpha = 0.4f))
        else -> Pair(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.1f))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        color = CardGray.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank circle tag
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (rank in 1..3) rankColor.copy(alpha = 0.15f) else Color.White.copy(
                                alpha = 0.05f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = rankColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = score.playerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Level ${score.level}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                text = "${score.score}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = rankColor
            )
        }
    }
}

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val sound by viewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val haptic by viewModel.isHapticEnabled.collectAsStateWithLifecycle()
    var nameInput by remember { mutableStateOf(viewModel.playerName.value) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("menu") },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CardGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Return",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "SYSTEM SETTINGS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            }

            // Configurations list Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Pilot Sign Block
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PLAYER SIGNATURE CODE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                if (it.length <= 15) {
                                    nameInput = it
                                    viewModel.playerName.value = it
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberIce,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = SpaceDarkBg,
                                unfocusedContainerColor = SpaceDarkBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f))

                    // Audio Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("SOUND EFFECTS", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Aim & shot explosion audios", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                        Switch(
                            checked = sound,
                            onCheckedChange = { viewModel.isSoundEnabled.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpaceDarkBg,
                                checkedTrackColor = CyberIce,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                uncheckedTrackColor = CardGray
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f))

                    // Haptic Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("HAPTIC RESPONSES", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Trigger motor vibration during shots", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                        Switch(
                            checked = haptic,
                            onCheckedChange = { viewModel.isHapticEnabled.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpaceDarkBg,
                                checkedTrackColor = CyberIce,
                                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                uncheckedTrackColor = CardGray
                            )
                        )
                    }
                }
            }
        }

        // Developer info
        Card(
            colors = CardDefaults.cardColors(containerColor = CardGray),
            border = BorderStroke(1.dp, CyberIce.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "HARDENED ENGINE v1.0.0",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberIce
                )
                Text(
                    text = "Offline optimized, 60fps target rendering fully compatible with older Android CPUs.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CreditsScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo("menu") },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardGray)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Return",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "DEVELOPER CREDITS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.5.sp
            )
        }

        // Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, CyberIce.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ABOUT DEVELOPER",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberIce,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Prince AR Abdur Rahman",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "CONTACT DETAILS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace
                        )
                        Text("• WhatsApp: 01707424006", color = Color.White, fontSize = 13.sp)
                        Text("• WhatsApp: 01796951709", color = Color.White, fontSize = 13.sp)
                        Text("• Facebook:\n  https://www.facebook.com/share/1BNn32qoJo/", color = CyberIce, fontSize = 12.sp)
                        Text("• Instagram:\n  https://www.instagram.com/ur___abdur____rahman__2008", color = CyberIce, fontSize = 12.sp)
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ABOUT COMPANY",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "NexVora Lab's Ofc",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                        Text(
                            text = "Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberIce
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "COMPANY PRODUCTS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace
                        )
                        Text("• NexPlay X  • LifeSphere OS  • Smart Day Planner X\n• Study AI  • Lensora Studio  • Offline AI\n• NexVora Love Space  • CalcVerse  • NexVoice OS", color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
