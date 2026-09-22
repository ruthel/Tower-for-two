package com.crabscode.towerfortwo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crabscode.towerfortwo.model.AppSettings
import com.crabscode.towerfortwo.model.Challenge
import com.crabscode.towerfortwo.model.ChallengeType
import com.crabscode.towerfortwo.model.GameState
import com.crabscode.towerfortwo.model.GameUiState
import com.crabscode.towerfortwo.model.Intensity
import com.crabscode.towerfortwo.model.PlayerGender
import com.crabscode.towerfortwo.model.ResolvedSexualAction
import com.crabscode.towerfortwo.model.SexPositionCatalog
import com.crabscode.towerfortwo.model.SexStickerPose
import com.crabscode.towerfortwo.model.SexualPractice
import com.crabscode.towerfortwo.viewmodel.TowerViewModel
import kotlinx.coroutines.delay

private object Routes {
    const val HOME = "home"
    const val GAME = "game"
    const val SETTINGS = "settings"
    const val CUSTOM = "custom"
    const val FALLEN = "fallen"
    const val FREE = "free"
}

@Composable
fun TowerForTwoApp(viewModel: TowerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    if (!state.loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                state = state,
                onStart = { p1, g1, p2, g2 ->
                    viewModel.startNewGame(p1, g1, p2, g2)
                    navController.navigate(Routes.GAME) { launchSingleTop = true }
                },
                onResume = { navController.navigate(Routes.GAME) { launchSingleTop = true } },
                onSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                state = state,
                onBlockPlaced = viewModel::placeBlock,
                onNextFloor = viewModel::nextFloor,
                onJoker = viewModel::useJoker,
                onRerollSexPosition = viewModel::rerollSexPosition,
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onCustom = { navController.navigate(Routes.CUSTOM) },
                onPlayers = { navController.navigate(Routes.HOME) },
                onTowerFallen = { player ->
                    viewModel.markTowerFallen(player)
                    navController.navigate(Routes.FALLEN)
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(state, viewModel, onBack = { navController.popBackStack() }, onCustom = { navController.navigate(Routes.CUSTOM) })
        }
        composable(Routes.CUSTOM) {
            CustomChallengesScreen(state, viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.FALLEN) {
            FallenScreen(
                state = state,
                onReplay = {
                    viewModel.replay()
                    navController.navigate(Routes.GAME) { popUpTo(Routes.HOME); launchSingleTop = true }
                },
                onFreePlay = {
                    viewModel.pickFreePlay()
                    navController.navigate(Routes.FREE)
                },
            )
        }
        composable(Routes.FREE) {
            FreePlayScreen(state, onNext = viewModel::pickFreePlay, onReplay = {
                viewModel.replay()
                navController.navigate(Routes.GAME) { popUpTo(Routes.HOME); launchSingleTop = true }
            })
        }
    }
}


@Composable
private fun HomeScreen(
    state: GameUiState,
    onStart: (String, PlayerGender, String, PlayerGender) -> Unit,
    onResume: () -> Unit,
    onSettings: () -> Unit,
) {
    var p1 by remember(state.game.player1) {
        mutableStateOf(if (state.game.player1 == "Joueur 1") "" else state.game.player1)
    }
    var p2 by remember(state.game.player2) {
        mutableStateOf(if (state.game.player2 == "Joueur 2") "" else state.game.player2)
    }
    var g1 by remember(state.game.player1Gender) { mutableStateOf(state.game.player1Gender) }
    var g2 by remember(state.game.player2Gender) { mutableStateOf(state.game.player2Gender) }
    var playersValidated by remember(
        state.game.player1,
        state.game.player2,
        state.game.player1Gender,
        state.game.player2Gender,
    ) { mutableStateOf(state.game.isInProgress) }

    val displayP1 = p1.trim().ifBlank { "Joueur 1" }
    val displayP2 = p2.trim().ifBlank { "Joueur 2" }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TOWER FOR TWO", fontSize = 29.sp, fontWeight = FontWeight.Black)
                    Text(
                        "Une tour · deux joueurs · un défi à la fois",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                }
            }

            if (state.game.isInProgress) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Partie en cours", fontWeight = FontWeight.Bold)
                                Text(
                                    "Niveau ${state.game.currentLevel} · Bloc ${state.game.currentSlot}/3",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                )
                            }
                            Button(onClick = onResume) { Text("CONTINUER") }
                        }
                    }
                }
            }

            item { Text("Joueurs", fontSize = 22.sp, fontWeight = FontWeight.Bold) }

            if (!playersValidated) {
                item {
                    PlayerEditor(
                        number = "1",
                        name = p1,
                        onNameChange = { p1 = it },
                        gender = g1,
                        onGenderChange = { g1 = it },
                    )
                }
                item {
                    PlayerEditor(
                        number = "2",
                        name = p2,
                        onNameChange = { p2 = it },
                        gender = g2,
                        onGenderChange = { g2 = it },
                    )
                }
                item {
                    Button(
                        onClick = { playersValidated = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text("VALIDER LES JOUEURS", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        PlayerValidatedCard("1", displayP1, g1, Modifier.weight(1f))
                        PlayerValidatedCard("2", displayP2, g2, Modifier.weight(1f))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = { playersValidated = false },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("MODIFIER")
                        }
                        Button(
                            onClick = { onStart(displayP1, g1, displayP2, g2) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (state.game.isInProgress) "REJOUER" else "COMMENCER")
                        }
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                TextButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Paramètres")
                }
            }

            item {
                Text(
                    "16 étages · 10 niveaux · données locales",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerEditor(
    number: String,
    name: String,
    onNameChange: (String) -> Unit,
    gender: PlayerGender,
    onGenderChange: (PlayerGender) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            number,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                Column {
                    Text("Joueur $number", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        "Prénom et genre",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Prénom") },
                placeholder = { Text("Facultatif") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                "Genre",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlayerGender.entries.forEach { candidate ->
                    FilterChip(
                        selected = gender == candidate,
                        onClick = { onGenderChange(candidate) },
                        label = { Text(candidate.label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerValidatedCard(
    number: String,
    name: String,
    gender: PlayerGender,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        number,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    gender.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameScreen(
    state: GameUiState,
    onBlockPlaced: () -> Unit,
    onNextFloor: () -> Unit,
    onJoker: () -> Unit,
    onRerollSexPosition: () -> Unit,
    onSettings: () -> Unit,
    onCustom: () -> Unit,
    onPlayers: () -> Unit,
    onTowerFallen: (Int) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    var fallenDialog by remember { mutableStateOf(false) }
    val game = state.game
    val challenge = state.currentChallenge
    val level = challenge?.level ?: game.nextLevel
    val slot = challenge?.slot ?: game.nextSlot
    val actor = game.challengePlayerIndex?.let(game::playerName)

    Scaffold(
        bottomBar = {
            if (!game.isFinished) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = onJoker,
                            enabled = challenge != null,
                            modifier = Modifier.weight(1f).height(58.dp),
                            shape = RoundedCornerShape(
                                topStart = 28.dp,
                                bottomStart = 28.dp,
                                topEnd = 12.dp,
                                bottomEnd = 12.dp,
                            ),
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.size(7.dp))
                            Text("JOKER", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onBlockPlaced,
                            enabled = game.isInProgress &&
                                !game.isFinished &&
                                !game.isPlaced(game.targetLevel, game.targetSlot),
                            modifier = Modifier.weight(1f).height(58.dp),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                bottomStart = 12.dp,
                                topEnd = 28.dp,
                                bottomEnd = 28.dp,
                            ),
                        ) {
                            Text(
                                "BLOC ${game.targetSlot} POSÉ",
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 18.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "NIVEAU $level",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Box {
                        IconButton(onClick = { menu = true }) {
                            Icon(Icons.Default.MoreVert, "Menu")
                        }
                        DropdownMenu(
                            expanded = menu,
                            onDismissRequest = { menu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("La tour est tombée") },
                                onClick = {
                                    menu = false
                                    fallenDialog = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Joueurs") },
                                onClick = {
                                    menu = false
                                    onPlayers()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Défis personnalisés") },
                                onClick = {
                                    menu = false
                                    onCustom()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Paramètres") },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick = {
                                    menu = false
                                    onSettings()
                                },
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Étage ${16 + level} — Bloc $slot/3",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp,
                )
            }

            item {
                LevelProgress(
                    currentLevel = game.targetLevel,
                    completed = game.completedLevelCount,
                )
            }

            if (challenge == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        shape = RoundedCornerShape(26.dp),
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(26.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Au tour de ${game.playerName(game.currentPlayerIndex)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Pose un bloc au-dessus de la tour, puis appuie sur BLOC POSÉ.",
                                textAlign = TextAlign.Center,
                                fontSize = 23.sp,
                                lineHeight = 31.sp,
                            )
                        }
                    }
                }
            } else {
                item {
                    ChallengeCard(
                        challenge = challenge,
                        actor = actor,
                        game = game,
                        settings = state.settings,
                        resolvedSexualAction = game.resolvedSexualAction(),
                        onRerollSexPosition = onRerollSexPosition,
                    )
                }

                item {
                    Text(
                        "Au tour de ${game.playerName(game.currentPlayerIndex)} pour le prochain bloc",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (!game.isFinished) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    "Étage ${16 + game.targetLevel}",
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "${game.placedOnCurrentLevel}/3 blocs posés",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                )
                            }
                            Text(
                                "Prochain · ${game.targetSlot}/3",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                if (game.targetLevel < 10) {
                    item {
                        OutlinedButton(
                            onClick = onNextFloor,
                            enabled = game.canAdvanceFloor,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Text("PASSER À L'ÉTAGE SUIVANT")
                        }
                    }

                    if (!game.canAdvanceFloor) {
                        item {
                            Text(
                                "Pose au moins 1 bloc à cet étage avant de passer au suivant.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            "Dernier étage · pose les 3 blocs pour terminer la partie.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (fallenDialog) {
        AlertDialog(
            onDismissRequest = { fallenDialog = false },
            title = { Text("Qui a fait tomber la tour ?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            fallenDialog = false
                            onTowerFallen(0)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(game.player1) }
                    Button(
                        onClick = {
                            fallenDialog = false
                            onTowerFallen(1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(game.player2) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { fallenDialog = false }) {
                    Text("Annuler")
                }
            },
        )
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
    actor: String?,
    game: GameState,
    settings: AppSettings,
    resolvedSexualAction: ResolvedSexualAction? = null,
    onRerollSexPosition: (() -> Unit)? = null,
) {
    val action = challenge.type == ChallengeType.ACTION
    val container = if (action) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    val localResolved = if (challenge.sexual && resolvedSexualAction == null) {
        remember(challenge.id) {
            SexPositionCatalog.resolve(
                challenge = challenge,
                game = game,
                settings = settings,
                actor = game.challengePlayerIndex ?: 0,
            )
        }
    } else null
    val effectiveResolved = resolvedSexualAction ?: localResolved

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Column(Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            if (actor != null) Text("Pour $actor", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (action) "ACTION" else "VÉRITÉ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (action) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            )
            if (challenge.sexual && effectiveResolved != null) {
                Text(
                    effectiveResolved.practice.label,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "Pratique tirée automatiquement pour ce niveau.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
                SexualActionPanel(
                    resolved = effectiveResolved,
                    game = game,
                    onReroll = onRerollSexPosition,
                )
            } else {
                Text(
                    challenge.text,
                    fontSize = 27.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (action) {
                ActionCountdown(
                    challengeId = challenge.id,
                    text = challenge.text,
                    durationOverrideSec = effectiveResolved?.durationSec,
                )
            }
        }
    }
}

@Composable
private fun ActionCountdown(
    challengeId: String,
    text: String,
    durationOverrideSec: Int? = null,
) {
    val initialSeconds = remember(challengeId, text, durationOverrideSec) {
        durationOverrideSec ?: actionDurationSeconds(text)
    }
    var remaining by remember(challengeId, initialSeconds) { mutableIntStateOf(initialSeconds) }
    var running by remember(challengeId, initialSeconds) { mutableStateOf(false) }

    LaunchedEffect(running, remaining, challengeId) {
        if (running && remaining > 0) {
            delay(1_000)
            remaining -= 1
        } else if (remaining <= 0) {
            running = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (remaining > 0) "Décompte · ${formatCountdown(remaining)}" else "Décompte terminé",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    when {
                        remaining <= 0 -> {
                            remaining = initialSeconds
                            running = true
                        }
                        else -> running = !running
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    when {
                        remaining <= 0 -> "RECOMMENCER"
                        running -> "PAUSE"
                        remaining < initialSeconds -> "REPRENDRE"
                        else -> "LANCER"
                    }
                )
            }
            OutlinedButton(
                onClick = {
                    running = false
                    remaining = initialSeconds
                },
                enabled = remaining != initialSeconds || running,
            ) {
                Text("RESET")
            }
        }
    }
}

private fun actionDurationSeconds(text: String): Int {
    val minuteMatch = Regex("""(\d+)\s*(?:minute|minutes|min)""", RegexOption.IGNORE_CASE).find(text)
    if (minuteMatch != null) return minuteMatch.groupValues[1].toIntOrNull()?.times(60) ?: 30

    val secondMatch = Regex("""(\d+)\s*(?:seconde|secondes|sec|s)\b""", RegexOption.IGNORE_CASE).find(text)
    return secondMatch?.groupValues?.get(1)?.toIntOrNull() ?: 30
}

private fun formatCountdown(seconds: Int): String =
    if (seconds >= 60) "%d:%02d".format(seconds / 60, seconds % 60) else "${seconds}s"

@Composable
private fun SexualActionPanel(
    resolved: ResolvedSexualAction,
    game: GameState,
    onReroll: (() -> Unit)?,
) {
    val position = SexPositionCatalog.position(resolved.positionId) ?: return
    val instruction = SexPositionCatalog.instruction(resolved, game)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("MISE EN PRATIQUE", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text(resolved.practice.label, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            SexPositionSticker(position.sticker)

            Text(position.label, fontSize = 21.sp, fontWeight = FontWeight.Black)

            if (resolved.mutual) {
                Text(
                    "${game.player1} + ${game.player2} · ${SexPositionCatalog.durationLabel(resolved.durationSec)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                val giver = resolved.giverPlayerIndex?.let(game::playerName) ?: game.player1
                val receiver = resolved.receiverPlayerIndex?.let(game::playerName) ?: game.player2
                Text(
                    "$giver → $receiver · ${SexPositionCatalog.durationLabel(resolved.durationSec)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(instruction, lineHeight = 23.sp)

            Text(
                "Commencez uniquement si vous êtes tous les deux d'accord. Chacun peut ralentir ou arrêter à tout moment.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (onReroll != null) {
                OutlinedButton(
                    onClick = onReroll,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.size(8.dp))
                    Text("AUTRE TIRAGE")
                }
            }
        }
    }
}

@Composable
private fun SexPositionSticker(pose: SexStickerPose) {
    val first = MaterialTheme.colorScheme.primary
    val second = MaterialTheme.colorScheme.secondary
    val outline = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = Modifier.fillMaxWidth().height(150.dp),
    ) {
        val w = size.width
        val h = size.height

        fun dot(center: Offset, color: Color) {
            drawCircle(outline, radius = 17f, center = center)
            drawCircle(color, radius = 11f, center = center)
        }

        fun segment(a: Offset, b: Offset, color: Color) {
            drawLine(outline, a, b, strokeWidth = 24f, cap = StrokeCap.Round)
            drawLine(color, a, b, strokeWidth = 14f, cap = StrokeCap.Round)
        }

        fun person(
            head: Offset,
            shoulder: Offset,
            hip: Offset,
            foot1: Offset,
            foot2: Offset,
            hand1: Offset,
            hand2: Offset,
            color: Color,
        ) {
            dot(head, color)
            segment(shoulder, hip, color)
            segment(shoulder, hand1, color)
            segment(shoulder, hand2, color)
            segment(hip, foot1, color)
            segment(hip, foot2, color)
        }

        when (pose) {
            SexStickerPose.FACE_TO_FACE, SexStickerPose.SEATED_EMBRACE -> {
                person(Offset(w*.32f,h*.25f),Offset(w*.34f,h*.40f),Offset(w*.37f,h*.67f),Offset(w*.24f,h*.86f),Offset(w*.48f,h*.86f),Offset(w*.48f,h*.53f),Offset(w*.43f,h*.62f),first)
                person(Offset(w*.68f,h*.25f),Offset(w*.66f,h*.40f),Offset(w*.63f,h*.67f),Offset(w*.52f,h*.86f),Offset(w*.76f,h*.86f),Offset(w*.52f,h*.53f),Offset(w*.57f,h*.62f),second)
            }
            SexStickerPose.SIDE_BY_SIDE -> {
                person(Offset(w*.20f,h*.38f),Offset(w*.32f,h*.43f),Offset(w*.52f,h*.50f),Offset(w*.72f,h*.56f),Offset(w*.74f,h*.70f),Offset(w*.42f,h*.28f),Offset(w*.48f,h*.64f),first)
                person(Offset(w*.26f,h*.64f),Offset(w*.38f,h*.66f),Offset(w*.58f,h*.70f),Offset(w*.78f,h*.72f),Offset(w*.78f,h*.86f),Offset(w*.48f,h*.54f),Offset(w*.50f,h*.80f),second)
            }
            SexStickerPose.SPOON -> {
                person(Offset(w*.22f,h*.34f),Offset(w*.34f,h*.40f),Offset(w*.54f,h*.50f),Offset(w*.75f,h*.62f),Offset(w*.70f,h*.78f),Offset(w*.44f,h*.30f),Offset(w*.48f,h*.60f),first)
                person(Offset(w*.28f,h*.56f),Offset(w*.40f,h*.58f),Offset(w*.58f,h*.62f),Offset(w*.78f,h*.70f),Offset(w*.74f,h*.86f),Offset(w*.49f,h*.48f),Offset(w*.52f,h*.72f),second)
            }
            SexStickerPose.RECEIVER_LYING -> {
                person(Offset(w*.17f,h*.68f),Offset(w*.30f,h*.66f),Offset(w*.55f,h*.68f),Offset(w*.79f,h*.64f),Offset(w*.80f,h*.80f),Offset(w*.42f,h*.54f),Offset(w*.43f,h*.78f),second)
                person(Offset(w*.63f,h*.22f),Offset(w*.62f,h*.38f),Offset(w*.58f,h*.60f),Offset(w*.48f,h*.84f),Offset(w*.68f,h*.84f),Offset(w*.50f,h*.52f),Offset(w*.72f,h*.50f),first)
            }
            SexStickerPose.RECEIVER_SEATED -> {
                person(Offset(w*.64f,h*.24f),Offset(w*.62f,h*.40f),Offset(w*.60f,h*.64f),Offset(w*.48f,h*.86f),Offset(w*.73f,h*.86f),Offset(w*.50f,h*.52f),Offset(w*.72f,h*.50f),second)
                person(Offset(w*.34f,h*.55f),Offset(w*.38f,h*.68f),Offset(w*.45f,h*.80f),Offset(w*.35f,h*.92f),Offset(w*.55f,h*.92f),Offset(w*.49f,h*.62f),Offset(w*.52f,h*.74f),first)
            }
            SexStickerPose.KNEELING -> {
                person(Offset(w*.67f,h*.18f),Offset(w*.66f,h*.34f),Offset(w*.65f,h*.58f),Offset(w*.60f,h*.88f),Offset(w*.73f,h*.88f),Offset(w*.55f,h*.48f),Offset(w*.76f,h*.48f),second)
                person(Offset(w*.36f,h*.56f),Offset(w*.40f,h*.68f),Offset(w*.46f,h*.80f),Offset(w*.33f,h*.92f),Offset(w*.56f,h*.92f),Offset(w*.51f,h*.60f),Offset(w*.52f,h*.74f),first)
            }
            SexStickerPose.PARTNER_ON_TOP -> {
                person(Offset(w*.19f,h*.72f),Offset(w*.32f,h*.68f),Offset(w*.56f,h*.68f),Offset(w*.80f,h*.66f),Offset(w*.80f,h*.82f),Offset(w*.44f,h*.56f),Offset(w*.45f,h*.78f),first)
                person(Offset(w*.52f,h*.18f),Offset(w*.53f,h*.34f),Offset(w*.53f,h*.57f),Offset(w*.39f,h*.80f),Offset(w*.67f,h*.80f),Offset(w*.41f,h*.48f),Offset(w*.65f,h*.48f),second)
            }
            SexStickerPose.STANDING_FACE_TO_FACE -> {
                person(Offset(w*.36f,h*.18f),Offset(w*.38f,h*.34f),Offset(w*.40f,h*.60f),Offset(w*.32f,h*.90f),Offset(w*.48f,h*.90f),Offset(w*.52f,h*.46f),Offset(w*.50f,h*.58f),first)
                person(Offset(w*.64f,h*.18f),Offset(w*.62f,h*.34f),Offset(w*.60f,h*.60f),Offset(w*.52f,h*.90f),Offset(w*.68f,h*.90f),Offset(w*.48f,h*.46f),Offset(w*.50f,h*.58f),second)
            }
        }
    }
}

@Composable
private fun LevelProgress(currentLevel: Int, completed: Int) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items((1..10).toList()) { n ->
            val isComplete = n <= completed
            val isCurrent = n == currentLevel
            Surface(
                shape = CircleShape,
                color = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isComplete -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(31.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isComplete && !isCurrent) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    else Text("$n", fontSize = 12.sp, fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Normal, color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    state: GameUiState,
    viewModel: TowerViewModel,
    onBack: () -> Unit,
    onCustom: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SettingsSection(
                    title = "Intensité",
                    subtitle = "Détermine à quel moment commence la fin exclusivement sexuelle.",
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Intensity.entries.forEach { intensity ->
                            FilterChip(
                                selected = state.settings.intensity == intensity,
                                onClick = { viewModel.setIntensity(intensity) },
                                label = { Text(intensity.label) },
                            )
                        }
                    }
                    Text(
                        when (state.settings.intensity) {
                            Intensity.SENSUEL -> "Final sexuel : niveau 10"
                            Intensity.TORRIDE -> "Final sexuel : niveaux 9–10"
                            Intensity.VERY_HOT -> "Final sexuel : niveaux 8–10"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }

            item {
                SettingsSection(title = "Préférences") {
                    SettingSwitch(
                        "Retrait de vêtements",
                        state.settings.allowClothing,
                        viewModel::setAllowClothing,
                    )
                    SettingSwitch(
                        "Questions sur les fantasmes",
                        state.settings.allowFantasy,
                        viewModel::setAllowFantasy,
                    )
                    SettingSwitch(
                        "Positions debout",
                        state.settings.allowStandingSexPositions,
                        viewModel::setAllowStandingSexPositions,
                    )
                }
            }

            item {
                SettingsSection(
                    title = "Pratiques du final",
                    subtitle = "Au moins une pratique reste toujours active pour éviter de bloquer la partie.",
                ) {
                    SexualPractice.playable.forEach { practice ->
                        val checked = practice in state.settings.allowedSexualPractices
                        SettingSwitch(practice.label, checked) { enabled ->
                            viewModel.setSexualPracticeAllowed(practice, enabled)
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onCustom,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(Modifier.size(8.dp))
                    Text("Mes défis personnalisés")
                }
            }

            item {
                Text(
                    "Aucun compte ni serveur. Toutes les données restent sur cet appareil.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            subtitle?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomChallengesScreen(
    state: GameUiState,
    viewModel: TowerViewModel,
    onBack: () -> Unit,
) {
    var addDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<Challenge?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes défis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { addDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter")
                    }
                },
            )
        },
    ) { padding ->
        if (state.customChallenges.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Aucun défi personnalisé", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ajoute une action ou une vérité à n'importe quel niveau.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
                Button(onClick = { addDialog = true }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.size(6.dp))
                    Text("AJOUTER")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.customChallenges, key = { it.id }) { challenge ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "N${challenge.level} · Bloc ${challenge.slot} · ${if (challenge.type == ChallengeType.ACTION) "Action" else "Vérité"}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(challenge.text, maxLines = 3)
                            }
                            IconButton(onClick = { editTarget = challenge }) {
                                Icon(Icons.Default.Edit, "Modifier")
                            }
                            IconButton(onClick = { viewModel.deleteCustomChallenge(challenge.id) }) {
                                Icon(Icons.Default.Delete, "Supprimer")
                            }
                        }
                    }
                }
            }
        }
    }

    if (addDialog) {
        AddChallengeDialog(
            initial = null,
            onDismiss = { addDialog = false },
            onSave = { level, slot, text, intensity, clothing, fantasy, sexual ->
                viewModel.addCustomChallenge(level, slot, text, intensity, clothing, fantasy, sexual)
                addDialog = false
            },
        )
    }

    editTarget?.let { target ->
        AddChallengeDialog(
            initial = target,
            onDismiss = { editTarget = null },
            onSave = { level, slot, text, intensity, clothing, fantasy, sexual ->
                viewModel.updateCustomChallenge(target.id, level, slot, text, intensity, clothing, fantasy, sexual)
                editTarget = null
            },
        )
    }
}

@Composable
private fun AddChallengeDialog(
    initial: Challenge?,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, Intensity, Boolean, Boolean, Boolean) -> Unit,
) {
    var level by remember(initial?.id) { mutableIntStateOf(initial?.level ?: 1) }
    var slot by remember(initial?.id) { mutableIntStateOf(initial?.slot ?: 1) }
    var text by remember(initial?.id) { mutableStateOf(initial?.text ?: "") }
    var intensity by remember(initial?.id) { mutableStateOf(initial?.intensity ?: Intensity.SENSUEL) }
    var clothing by remember(initial?.id) { mutableStateOf(initial?.clothing ?: false) }
    var fantasy by remember(initial?.id) { mutableStateOf(initial?.fantasy ?: false) }
    var sexual by remember(initial?.id) { mutableStateOf(initial?.sexual ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nouveau défi" else "Modifier le défi") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Niveau", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { if (level > 1) level-- }) { Text("−") }
                        Text("$level", fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { if (level < 10) level++ }) { Text("+") }
                    }
                }
                item {
                    Text("Case")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..3).forEach { n -> FilterChip(selected = slot == n, onClick = { slot = n }, label = { Text(if (n == 1) "1 Action" else "$n Vérité") }) }
                    }
                }
                item { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Texte du défi") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
                item {
                    Text("Intensité")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Intensity.entries.forEach { i -> FilterChip(selected = intensity == i, onClick = { intensity = i }, label = { Text(i.label) }) }
                    }
                }
                item { SettingSwitch("Retrait de vêtements", clothing) { clothing = it } }
                item { SettingSwitch("Fantasme", fantasy) { fantasy = it } }
                item { SettingSwitch("Pratique sexuelle", sexual) { sexual = it } }
            }
        },
        confirmButton = { Button(onClick = { onSave(level, slot, text, intensity, clothing, fantasy, sexual) }, enabled = text.isNotBlank()) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@Composable
private fun FallenScreen(state: GameUiState, onReplay: () -> Unit, onFreePlay: () -> Unit) {
    val game = state.game
    val fallenIndex = game.fallenByIndex
    val fallen = fallenIndex?.let(game::playerName) ?: "—"
    val chooser = fallenIndex?.let { game.playerName(1 - it) } ?: "l'autre joueur"

    val consequences = listOf(
        "Retirer un vêtement",
        "Répondre à une vérité très intime",
        "Donner un massage ou une série de baisers",
        "Réaliser un défi sensuel choisi par l'autre",
    )

    var selectedConsequence by remember(fallenIndex) { mutableStateOf<String?>(null) }
    var consentConfirmed by remember(fallenIndex) { mutableStateOf(false) }
    var consequenceStarted by remember(fallenIndex) { mutableStateOf(false) }
    var consequenceFinished by remember(fallenIndex) { mutableStateOf(false) }
    var remaining by remember(fallenIndex) { mutableIntStateOf(180) }

    LaunchedEffect(consequenceStarted, consequenceFinished, remaining) {
        if (consequenceStarted && !consequenceFinished && remaining > 0) {
            delay(1_000)
            remaining -= 1
        } else if (consequenceStarted && remaining <= 0) {
            consequenceFinished = true
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("La tour est tombée", fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(
                    "$fallen est à la merci de $chooser pendant 3 minutes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Niveau", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text("${game.reachedLevel}", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Blocs", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text("${game.blocksPlaced}", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Chute", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(fallen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Text("Choisir une conséquence", fontWeight = FontWeight.Bold, fontSize = 18.sp) }

            consequences.forEach { consequence ->
                item {
                    FilterChip(
                        selected = selectedConsequence == consequence,
                        onClick = {
                            if (!consequenceStarted) {
                                selectedConsequence = consequence
                                consentConfirmed = false
                            }
                        },
                        enabled = !consequenceStarted,
                        label = { Text(consequence, modifier = Modifier.padding(vertical = 4.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                SettingSwitch("Nous sommes tous les deux d'accord", consentConfirmed) {
                    if (!consequenceStarted && selectedConsequence != null) consentConfirmed = it
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (consequenceStarted) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            if (consequenceFinished) "Terminé" else formatCountdown(remaining),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                        )
                        selectedConsequence?.let {
                            Text(it, textAlign = TextAlign.Center)
                        }

                        if (!consequenceStarted) {
                            Button(
                                onClick = {
                                    consequenceStarted = true
                                    remaining = 180
                                },
                                enabled = selectedConsequence != null && consentConfirmed,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("LANCER 3:00") }
                        } else if (!consequenceFinished) {
                            OutlinedButton(
                                onClick = { consequenceFinished = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("ARRÊTER") }
                        }
                    }
                }
            }

            item {
                Text(
                    "Un seul défi est choisi avant le chrono. Chacun peut arrêter à tout moment.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onFreePlay,
                        enabled = game.blocksPlaced > 0 && (!consequenceStarted || consequenceFinished),
                        modifier = Modifier.weight(1f),
                    ) { Text("MODE LIBRE") }
                    Button(
                        onClick = onReplay,
                        enabled = !consequenceStarted || consequenceFinished,
                        modifier = Modifier.weight(1f),
                    ) { Text("REJOUER") }
                }
            }
        }
    }
}

@Composable
private fun ResultLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FreePlayScreen(state: GameUiState, onNext: () -> Unit, onReplay: () -> Unit) {
    val challenge = state.freePlayChallenge

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("Mode libre", fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(
                    "Défis tirés uniquement dans les niveaux déjà atteints.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                if (challenge != null) {
                    ChallengeCard(
                        challenge = challenge,
                        actor = null,
                        game = state.game,
                        settings = state.settings,
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            "Tire un défi pour commencer.",
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.size(8.dp))
                    Text("AUTRE DÉFI")
                }
            }

            item {
                TextButton(onClick = onReplay, modifier = Modifier.fillMaxWidth()) {
                    Text("Rejouer depuis le début")
                }
            }
        }
    }
}

