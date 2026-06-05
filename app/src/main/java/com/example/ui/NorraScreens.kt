package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.ApiException
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -------------------------------------------------------------
// AI CHAT SCREEN (FULL PAGE)
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: NorraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userMessageText by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoadingChat by viewModel.chatLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assistant Norra AI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Effacer l'historique")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Messages area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    val sortedMessages = chatMessages.reversed()
                    items(sortedMessages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                ),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 15.sp,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                if (isLoadingChat) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // Input Row
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = { userMessageText = it },
                    placeholder = { Text("Écris un message à Norra...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (userMessageText.isNotBlank()) {
                            viewModel.sendMessage(userMessageText)
                            userMessageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("chat_send_button"),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Envoyer",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MAIN TAB 1: HOME & SOS
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeSosScreen(
    viewModel: NorraViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSosDialog by remember { mutableStateOf(false) }
    var userMessageText by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isLoadingChat by viewModel.chatLoading.collectAsState()
    val trackingHistory by viewModel.moodHistory.collectAsState()
    val customContacts by viewModel.customContacts.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var selectedEmotion by remember { mutableStateOf<NorraEmotion?>(null) }
    var emotionIntensity by remember { mutableFloatStateOf(3f) }
    var emotionNote by remember { mutableStateOf("") }
    var emotionLoggedSuccess by remember { mutableStateOf(false) }

    var showQuickValidateDialog by remember { mutableStateOf(false) }
    var showAllEmotionsDialog by remember { mutableStateOf(false) }

    // Determine latest logged emotion to dynamically adapt Norra's appearance!
    val latestEmotion = remember(trackingHistory) {
        val typeStr = trackingHistory.firstOrNull()?.emotionType ?: "default"
        NorraEmotion.fromId(typeStr)
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 0. Compact & Elegant Combined Header and Greeting (Saves massive screen space)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bonjour $userName,",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "On respire ?",
                        style = TextStyle(
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Mon Profil",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 1. Norra Character Presentation (Prominently Centered, Sited Above)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NorraCharacter(
                        emotion = latestEmotion,
                        modifier = Modifier.fillMaxSize(),
                        animate = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive conversation bubble styled as a minimalist elegant badge
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = when (latestEmotion) {
                            NorraEmotion.DEFAULT -> "Je me sens bien aujourd'hui. Et toi ?"
                            NorraEmotion.JOIE -> "Quelle joyeuse énergie ! Profitons-en ensemble !"
                            NorraEmotion.SEREIN -> "Un moment de calme parfait... Savoure-le."
                            NorraEmotion.MOTIVE -> "Une belle motivation ! Qu'allons-nous accomplir ?"
                            NorraEmotion.FIER -> "Tu as de quoi être fier(e) ! Je célèbre ça avec toi."
                            NorraEmotion.COLERE -> "Je ressens ta colère... Respire, je suis là."
                            NorraEmotion.TRISTE -> "Je porte un bout de ta tristesse... Tu as le droit de pleurer."
                            NorraEmotion.VIDE -> "Je ressens le vide. C'est passager, reste avec moi."
                            NorraEmotion.SEUL -> "Tu n'es pas seul(e). Je suis juste ici à tes côtés."
                            NorraEmotion.PEUR -> "La panique est là... Ancrons-nous ensemble."
                            NorraEmotion.HONTE -> "Pas de culpabilité ici. Tu es parfait(e) tel(le) quel(le)."
                            NorraEmotion.IDK -> "C'est normal de ne pas savoir. Accueillons ce flou."
                            NorraEmotion.AUTRE -> "Curieuse de te lire. Raconte-moi !"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    )
                }
            }
        }

        // 2. Interactive "État d'esprit" (Mood) Section displaying Top emotions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "COMMENT TE SENS-TU MAINTENANT ?",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val popularEmotions = listOf(NorraEmotion.JOIE, NorraEmotion.SEREIN, NorraEmotion.MOTIVE, NorraEmotion.DEFAULT, NorraEmotion.COLERE)
                        popularEmotions.forEach { emo ->
                            EmotionBubble(
                                emotion = emo,
                                isSelected = selectedEmotion == emo,
                                onClick = {
                                    selectedEmotion = emo
                                    emotionLoggedSuccess = false
                                    showQuickValidateDialog = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = { showAllEmotionsDialog = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Toutes les Émotions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 3. Central SOS Section (Moved below Mood)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulse effect on SOS Button
                val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Button(
                    onClick = {
                        showSosDialog = true
                        viewModel.addCrisisLog("SOS", false)
                    },
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(110.dp)
                        .shadow(12.dp, CircleShape)
                        .testTag("sos_button"),
                    shape = CircleShape,
                    border = BorderStroke(3.dp, Color.White),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "SOS Alert",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "S.O.S",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Appuie ici pour ouvrir le parcours de secours",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2. Norra Assistant Chat Bubble/Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable { onNavigateToChat() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Chat avec Norra",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Discuter avec Norra",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Une pensée à partager ? Écris-moi.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // QUICK MOOD VALIDATION DIALOG
    // -------------------------------------------------------------
    if (showQuickValidateDialog && selectedEmotion != null) {
        val emo = selectedEmotion!!
        val label = when (emo) {
            NorraEmotion.JOIE -> "Joie"
            NorraEmotion.SEREIN -> "Serein"
            NorraEmotion.MOTIVE -> "Motivé"
            NorraEmotion.FIER -> "Fier"
            NorraEmotion.COLERE -> "Colère"
            NorraEmotion.TRISTE -> "Tristesse"
            NorraEmotion.VIDE -> "Vide"
            NorraEmotion.SEUL -> "Seul(e)"
            NorraEmotion.PEUR -> "Peur"
            NorraEmotion.HONTE -> "Honte"
            NorraEmotion.IDK -> "Flou"
            NorraEmotion.DEFAULT -> "Neutre"
            NorraEmotion.AUTRE -> "Autre"
        }
        AlertDialog(
            onDismissRequest = { showQuickValidateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp)) {
                        NorraCharacter(emotion = emo, animate = true, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comment te sens-tu ?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text("Tu as choisi : $label", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Intensité de l'émotion : ${emotionIntensity.toInt()}/5", fontWeight = FontWeight.Medium)
                    Slider(
                        value = emotionIntensity,
                        onValueChange = { emotionIntensity = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Un petit mot ? (Optionnel)", fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = emotionNote,
                        onValueChange = { emotionNote = it },
                        placeholder = { Text("Qu'est ce qui déclenche cela ?") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addMoodEntry(
                        emotion = emo.id,
                        intensity = emotionIntensity.toInt(),
                        note = emotionNote.ifBlank { "Saisie rapide accueil" }
                    )
                    emotionNote = ""
                    showQuickValidateDialog = false
                    Toast.makeText(context, "Humeur enregistrée !", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickValidateDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // ALL EMOTIONS GRID DIALOG
    // -------------------------------------------------------------
    if (showAllEmotionsDialog) {
        val allEmoGroups = listOf(
            listOf(NorraEmotion.JOIE, NorraEmotion.SEREIN, NorraEmotion.MOTIVE, NorraEmotion.FIER),
            listOf(NorraEmotion.DEFAULT, NorraEmotion.COLERE, NorraEmotion.TRISTE, NorraEmotion.VIDE),
            listOf(NorraEmotion.SEUL, NorraEmotion.PEUR, NorraEmotion.HONTE, NorraEmotion.IDK)
        )
        AlertDialog(
            onDismissRequest = { showAllEmotionsDialog = false },
            title = { Text("Choisir une émotion", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    allEmoGroups.forEach { rowEmos ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceAround) {
                            rowEmos.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = false,
                                    onClick = {
                                        selectedEmotion = emo
                                        emotionLoggedSuccess = false
                                        showAllEmotionsDialog = false
                                        showQuickValidateDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllEmotionsDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // -------------------------------------------------------------
    // INTERACTIVE SOS DIALOG PANEL
    // -------------------------------------------------------------
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Urgence",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Aide & Secours d'Urgence",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Reassuring Calm Message from Norra
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(60.dp)) {
                                NorraCharacter(
                                    emotion = NorraEmotion.DEFAULT,
                                    modifier = Modifier.fillMaxSize(),
                                    animate = false
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Respire profondément...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tu traverses une tempête émotionnelle extrême, mais tu n'es pas seul(e). Prends une grande inspiration. Cette crise va passer.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // 1.5 Quick Intervention Exercises (Moved Up + More)
                    Text(
                        text = "EXERCICES D'APAISEMENT RAPIDE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    val favoriteExercises by viewModel.favoriteExercises.collectAsState()
                    val sosExercisesToShow = remember(favoriteExercises) {
                        val allPossible = listOf(
                            Triple("bubble_burst", "Éclate Pensée", Icons.Default.Delete),
                            Triple("tactile_breathe", "Resp. Tactile", Icons.Default.Refresh),
                            Triple("box_breathing", "Resp. Carrée", Icons.Default.CheckCircle),
                            Triple("butterfly", "EMDR Papillon", Icons.Default.Favorite),
                            Triple("grounding", "Ancrage (5-4)", Icons.Default.Star),
                            Triple("tipp", "Méthode TIPP", Icons.Default.Notifications),
                            Triple("stop", "Méthode STOP", Icons.Default.Warning),
                            Triple("panic_sos", "SOS Attaque", Icons.Default.Warning)
                        )
                        val favs = allPossible.filter { favoriteExercises.contains(it.first) }
                        if (favs.isNotEmpty()) {
                            val remaining = allPossible.filter { !favoriteExercises.contains(it.first) }
                            (favs + remaining).take(6)
                        } else {
                            val interactiveIds = listOf("bubble_burst", "tactile_breathe", "box_breathing", "butterfly")
                            val interactive = allPossible.filter { interactiveIds.contains(it.first) }
                            val rest = allPossible.filter { !interactiveIds.contains(it.first) }
                            (interactive + rest).take(6)
                        }
                    }

                    val chunkedSos = sosExercisesToShow.chunked(3)
                    chunkedSos.forEach { rowExercises ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowExercises.forEach { (id, label, icon) ->
                                val isFav = favoriteExercises.contains(id)
                                Button(
                                    onClick = { 
                                        showSosDialog = false
                                        viewModel.setActiveExercise(id, directLaunch = true)
                                        viewModel.selectTab(2) 
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFav) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = if (isFav) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = icon, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(20.dp),
                                            tint = if (isFav) Color(0xFFFF5252) else LocalContentColor.current
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label, 
                                            fontSize = 9.sp, 
                                            maxLines = 1,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Emergency Helplines Section
                    Text(
                        text = "LIGNES D'ÉCOUTE ET SECOURS DIRECTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 3114 Call
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "3114 - Prévention Suicide",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Gratuit, confidentiel, accessible 24h/24 par des psychologues.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:3114"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("3114", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // SAMU 15 Call
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "15 / 112 - SAMU & Urgences",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Détresse vitale, crise physique critique ou urgence médicale.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:15"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Appel 15", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 114 SMS Discret
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "114 - Urgences par SMS",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Si tu ne peux pas parler ou si tu es confiné(e). Gratuit par SMS.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:114")
                                        putExtra("sms_body", "URGENT SOS - Je traverse une grosse crise et j'ai besoin d'une assistance immédiate.")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SMS 114", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. User Relais Contacts Section
                    Text(
                        text = "MES PROCHES DE CONFIANCE ENREGISTRÉS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (customContacts.isEmpty()) {
                        Text(
                            text = "Aucun contact enregistré pour l'instant. Configure tes relais de soutien dans l'onglet 'Contacts' pour pouvoir les alerter en 1 clic d'urgence.",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        customContacts.forEach { person ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = person.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Relation : ${person.relation}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quick Call Relais
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = "Appeler",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        // Quick SMS Relais
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("smsto:${person.phone}")
                                                    putExtra("sms_body", "SOS - Je traverse une grosse crise d'émotions et j'ai besoin de soutien immédiat. Peux-tu m'accompagner s'il te plaît ?")
                                                }
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = "Message",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSosDialog = false },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("FERMER LE SECOURS", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}


// -------------------------------------------------------------
// MAIN TAB 2: MOOD TRACKER & HISTORY
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodColorChart(
    moodHistory: List<com.example.data.MoodEntry>,
    modifier: Modifier = Modifier
) {
    if (moodHistory.isEmpty()) return

    var selectedPeriod by remember { mutableStateOf("all") } // "all", "7", "30"
    var selectedGraphType by remember { mutableStateOf("bar") } // "bar", "line", "heatmap"

    var heatMapMonthOffset by remember { mutableIntStateOf(0) }

    // Calculate filtered history based on selected period
    val now = System.currentTimeMillis()
    val filteredHistory = remember(moodHistory, selectedPeriod) {
        when (selectedPeriod) {
            "7" -> moodHistory.filter { it.timestamp >= now - 7 * 24 * 60 * 60 * 1000L }
            "30" -> moodHistory.filter { it.timestamp >= now - 30 * 24 * 60 * 60 * 1000L }
            else -> moodHistory
        }
    }

    val totalCount = filteredHistory.size

    val emotionThemeColors = mapOf(
        "default" to Color(0xFF95A4FA),
        "joie" to Color(0xFFFFAB40),
        "serein" to Color(0xFF75CBAD),
        "motive" to Color(0xFFFF4949),
        "fier" to Color(0xFF8B51F5),
        "colere" to Color(0xFFFF3E55),
        "triste" to Color(0xFF8E91B0),
        "vide" to Color(0xFFB5B7CE),
        "seul" to Color(0xFF555977),
        "peur" to Color(0xFF0F7C2D),
        "honte" to Color(0xFF605F70),
        "idk" to Color(0xFFFFF59D),
        "autre" to Color(0xFF95A4FA),
        "calme" to Color(0xFF75CBAD),
        "anxiété" to Color(0xFF0F7C2D),
        "anxiete" to Color(0xFF0F7C2D),
        "tristesse" to Color(0xFF8E91B0),
        "colère" to Color(0xFFFF3E55)
    )

    val emotionLabels = mapOf(
        "default" to "Neutre / OK",
        "joie" to "Joie",
        "serein" to "Serein",
        "motive" to "Motivé",
        "fier" to "Fier",
        "colere" to "Colère",
        "triste" to "Tristesse",
        "vide" to "Vide",
        "seul" to "Isolé / Seul",
        "peur" to "Panique / Peur",
        "honte" to "Honte",
        "idk" to "Flou / IDK",
        "autre" to "Autre",
        "calme" to "Serein",
        "anxiété" to "Panique / Peur",
        "anxiete" to "Panique / Peur",
        "tristesse" to "Tristesse",
        "colère" to "Colère"
    )

    // Calculate emotion distribution sorted by total intensity
    val distribution = remember(filteredHistory) {
        filteredHistory.groupBy { it.emotionType }
            .mapValues { entry -> 
                val count = entry.value.size
                val sumIntensity = entry.value.sumOf { it.intensity.toDouble() }.toFloat()
                Pair(count, sumIntensity)
            }
            .toList()
            .sortedByDescending { it.second.second }
    }
    
    val totalIntensity = distribution.sumOf { it.second.second.toDouble() }.toFloat()

    // Calculate overall average intensity
    val avgIntensity = remember(filteredHistory) {
        if (filteredHistory.isEmpty()) 0.0 else filteredHistory.map { it.intensity }.average()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Visualisation des émotions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (totalCount > 0) "Répartition sur un total de $totalCount saisie(s)" else "Aucune saisie sur cette période",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (totalCount > 0) {
                    // Average Intensity Badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Intensité Moy.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f/5", avgIntensity),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Period & Graph Type selector tabs inside card
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Période :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf(
                    "all" to "Tout",
                    "7" to "7 jours",
                    "30" to "30 jours"
                ).forEach { (periodId, periodLabel) ->
                    val isSelected = selectedPeriod == periodId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPeriod = periodId },
                        label = { Text(periodLabel, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vue :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf(
                    "bar" to "Barres",
                    "line" to "Courbe",
                    "heatmap" to "Calendrier"
                ).forEach { (typeId, typeLabel) ->
                    val isSelected = selectedGraphType == typeId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGraphType = typeId },
                        label = { Text(typeLabel, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun état enregistré sur cette période. 📝 Écris tes ressentis pour commencer !",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                when (selectedGraphType) {
                    "bar" -> {
                        // 1. Beautiful Segmented Color Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            distribution.forEach { (emotionId, stats) ->
                                val (count, sumIntensity) = stats
                                val weight = if (totalIntensity > 0) sumIntensity / totalIntensity else 0f
                                val color = emotionThemeColors[emotionId] ?: Color.Gray

                                if (weight > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(weight)
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                    "line" -> {
                        val sortedHistory = filteredHistory.sortedBy { it.timestamp }
                        val maxIntens = maxOf(5f, sortedHistory.maxOfOrNull { it.intensity.toFloat() } ?: 5f)
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp)) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val padding = 16.dp.toPx()
                                
                                for (i in 0..5) {
                                    val y = h - padding - (i / 5f) * (h - 2 * padding)
                                    drawLine(
                                        color = Color.Gray.copy(alpha = 0.3f),
                                        start = androidx.compose.ui.geometry.Offset(0f, y),
                                        end = androidx.compose.ui.geometry.Offset(w, y),
                                        strokeWidth = 1f
                                    )
                                }
                                
                                if (sortedHistory.size > 1) {
                                    val path = androidx.compose.ui.graphics.Path()
                                    sortedHistory.forEachIndexed { index, entry ->
                                        val x = padding + (index.toFloat() / (sortedHistory.size - 1)) * (w - 2 * padding)
                                        val y = h - padding - (entry.intensity / maxIntens) * (h - 2 * padding)
                                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color.Gray.copy(alpha = 0.5f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                    
                                    sortedHistory.forEachIndexed { index, entry ->
                                        val x = padding + (index.toFloat() / (sortedHistory.size - 1)) * (w - 2 * padding)
                                        val y = h - padding - (entry.intensity / maxIntens) * (h - 2 * padding)
                                        val color = emotionThemeColors[entry.emotionType] ?: Color.Gray
                                        drawCircle(color = color, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                                    }
                                } else if (sortedHistory.size == 1) {
                                    val entry = sortedHistory.first()
                                    val y = h - padding - (entry.intensity / maxIntens) * (h - 2 * padding)
                                    val color = emotionThemeColors[entry.emotionType] ?: Color.Gray
                                    drawCircle(color = color, radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w/2f, y))
                                }
                            }
                        }
                    }
                    "heatmap" -> {
                        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        
                        val cal = java.util.Calendar.getInstance()
                        cal.add(java.util.Calendar.MONTH, heatMapMonthOffset)
                        cal.set(java.util.Calendar.DAY_OF_MONTH, 1) // Start of month

                        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                        val currentMonthName = monthFormat.format(cal.time).replaceFirstChar { it.uppercase() }

                        val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { heatMapMonthOffset-- }) {
                                    Icon(Icons.Default.KeyboardArrowLeft, "Mois précédent")
                                }
                                Text(currentMonthName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                IconButton(onClick = { heatMapMonthOffset++ }, enabled = heatMapMonthOffset < 0) {
                                    Icon(Icons.Default.KeyboardArrowRight, "Mois suivant")
                                }
                            }
                            
                            val startDayOfWeek = (cal.get(java.util.Calendar.DAY_OF_WEEK) - java.util.Calendar.MONDAY + 7) % 7 // Monday = 0
                            
                            val daysList = mutableListOf<String?>()
                            for (i in 0 until startDayOfWeek) daysList.add(null)
                            for (i in 1..daysInMonth) {
                                cal.set(java.util.Calendar.DAY_OF_MONTH, i)
                                daysList.add(format.format(cal.time))
                            }

                            val weeks = daysList.chunked(7)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                weeks.forEach { weekDays ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        weekDays.forEach { dateString ->
                                            if (dateString == null) {
                                                Spacer(modifier = Modifier.size(24.dp))
                                            } else {
                                                val dayEntries = filteredHistory.filter { 
                                                    format.format(java.util.Date(it.timestamp)) == dateString 
                                                }
                                                val todayString = format.format(java.util.Date())
                                                val isToday = dateString == todayString
                                                val boxColor = if (dayEntries.isEmpty()) {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                } else {
                                                    val mainEmo = dayEntries.groupBy { it.emotionType }
                                                        .maxByOrNull { group -> group.value.sumOf { (it.intensity).toInt() } }?.key
                                                    emotionThemeColors[mainEmo] ?: Color.Gray
                                                }
                                                
                                                val borderModifier = if (isToday) {
                                                    Modifier.border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                                } else Modifier
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(boxColor)
                                                        .then(borderModifier)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Grid breakdown table showing legends, item ratios & counts
                Text(
                    text = "Détails de tes ressentis :",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Dynamic grid layout for breakdown details (2 items per row)
                distribution.chunked(2).forEach { pairs ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pairs.forEach { (emotionId, stats) ->
                            val (count, sumIntensity) = stats
                            val pct = if (totalIntensity > 0) (sumIntensity / totalIntensity * 100).toInt() else 0
                            val color = emotionThemeColors[emotionId] ?: Color.Gray
                            val label = emotionLabels[emotionId] ?: emotionId

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color orb
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )

                                Text(
                                    text = "$pct% ($count)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Empty spacer filler if odd count in row
                        if (pairs.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuiviScreen(
    viewModel: NorraViewModel,
    modifier: Modifier = Modifier
) {
    var selectedEmotion by remember { mutableStateOf(NorraEmotion.DEFAULT) }
    var selectedIntensity by remember { mutableFloatStateOf(3f) }
    var moodTriggerText by remember { mutableStateOf("") }
    var moodNoteText by remember { mutableStateOf("") }
    val moodHistory by viewModel.moodHistory.collectAsState()

    var showJournalDialog by remember { mutableStateOf(false) }
    var dialogEmotion by remember { mutableStateOf(NorraEmotion.DEFAULT) }
    var dialogIntensity by remember { mutableFloatStateOf(3f) }
    var dialogTriggerText by remember { mutableStateOf("") }
    var dialogJournalText by remember { mutableStateOf("") }

    var selectedSuiviTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSuiviTab,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            divider = {}
        ) {
            Tab(selected = selectedSuiviTab == 0, onClick = { selectedSuiviTab = 0 }) {
                Text("Humeur", modifier = Modifier.padding(vertical = 12.dp), fontSize = 13.sp)
            }
            Tab(selected = selectedSuiviTab == 1, onClick = { selectedSuiviTab = 1 }) {
                Text("Traitements", modifier = Modifier.padding(vertical = 12.dp), fontSize = 13.sp)
            }
            Tab(selected = selectedSuiviTab == 2, onClick = { selectedSuiviTab = 2 }) {
                Text("Crises", modifier = Modifier.padding(vertical = 12.dp), fontSize = 13.sp)
            }
        }

        if (selectedSuiviTab == 0) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tracker Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Comment te sens-tu ?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Évalue ton humeur pour adapter Norra",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Adorable Interactive Grid of Norra Characters based on user's DA!
                        Text(
                            text = "Choisis ton reflet émotionnel :",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val row1 = listOf(NorraEmotion.JOIE, NorraEmotion.SEREIN, NorraEmotion.MOTIVE, NorraEmotion.FIER)
                            row1.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = selectedEmotion == emo,
                                    onClick = { selectedEmotion = emo }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val row2 = listOf(NorraEmotion.COLERE, NorraEmotion.TRISTE, NorraEmotion.VIDE, NorraEmotion.SEUL)
                            row2.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = selectedEmotion == emo,
                                    onClick = { selectedEmotion = emo }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val row3 = listOf(NorraEmotion.PEUR, NorraEmotion.HONTE, NorraEmotion.IDK, NorraEmotion.AUTRE)
                            row3.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = selectedEmotion == emo,
                                    onClick = { selectedEmotion = emo }
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val row4 = listOf(NorraEmotion.DEFAULT)
                            row4.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = selectedEmotion == emo,
                                    onClick = { selectedEmotion = emo }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Intensity slider
                        Text(
                            text = "Intensité de l'émotion : ${selectedIntensity.toInt()}/5",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                          value = selectedIntensity,
                          onValueChange = { selectedIntensity = it },
                          valueRange = 1f..5f,
                          steps = 3,
                          modifier = Modifier.testTag("intensity_slider")
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Qu'est ce qui a déclenché cette émotion ?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        OutlinedTextField(
                            value = moodTriggerText,
                            onValueChange = { moodTriggerText = it },
                            placeholder = { Text("Qu'est ce qui déclenche cela ? (Optionnel)", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trigger_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Mon Journal de ressentis ✍",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Text Note Input
                        OutlinedTextField(
                            value = moodNoteText,
                            onValueChange = { moodNoteText = it },
                            placeholder = { Text("Écris librement comment tu te sens, tes pensées ou ce qui s'est passé...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp)
                                .testTag("note_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 6
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val finalNote = buildString {
                                    if (moodTriggerText.isNotBlank()) append("Déclencheur : $moodTriggerText\n\n")
                                    append(moodNoteText)
                                }.trim()
                            
                                viewModel.addMoodEntry(
                                    emotion = selectedEmotion.id,
                                    intensity = selectedIntensity.toInt(),
                                    note = finalNote
                                )
                                moodNoteText = ""
                                moodTriggerText = ""
                                selectedEmotion = NorraEmotion.DEFAULT
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("log_mood_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Valider mon humeur")
                        }
                    }
                }
            }

            item {
                PsychApptCard(viewModel = viewModel)
            }

            if (moodHistory.isNotEmpty()) {
                item {
                    MoodColorChart(moodHistory = moodHistory)
                }
            }

        // History Log Section
        item {
            Text(
                text = "Journal Numérique",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (moodHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aucun état enregistré pour le moment. Enregistre ton humeur ci-dessus !",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(moodHistory) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(64.dp)) {
                            NorraCharacter(
                                emotion = NorraEmotion.fromId(entry.emotionType),
                                modifier = Modifier.fillMaxSize(),
                                animate = false
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = NorraEmotion.fromId(entry.emotionType).label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Intensité : ${entry.intensity}/5",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (entry.note.isNotBlank()) {
                                Text(
                                    text = entry.note,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            // Format basic time
                            val timeText = remember(entry.timestamp) {
                                val date = java.util.Date(entry.timestamp)
                                val format = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                                format.format(date)
                            }
                            Text(
                                text = timeText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.deleteMoodEntry(entry.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
        }

        // Persistent floating bubble to directly write a journal entry
        ExtendedFloatingActionButton(
            onClick = {
                // Pre-fill dialog with screen settings
                dialogEmotion = selectedEmotion
                dialogIntensity = selectedIntensity
                dialogJournalText = ""
                dialogTriggerText = ""
                showJournalDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 18.dp)
                .testTag("floating_journal_bubble"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Ressentis",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Écrire Journal ✍", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showJournalDialog) {
        AlertDialog(
            onDismissRequest = { showJournalDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mon Journal intime",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Comment tu te sens ?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Compact Emotion Select row
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val r1 = listOf(NorraEmotion.JOIE, NorraEmotion.SEREIN, NorraEmotion.MOTIVE, NorraEmotion.FIER)
                            r1.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = dialogEmotion == emo,
                                    onClick = { dialogEmotion = emo }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val r2 = listOf(NorraEmotion.COLERE, NorraEmotion.TRISTE, NorraEmotion.VIDE, NorraEmotion.SEUL)
                            r2.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = dialogEmotion == emo,
                                    onClick = { dialogEmotion = emo }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val r3 = listOf(NorraEmotion.PEUR, NorraEmotion.HONTE, NorraEmotion.IDK, NorraEmotion.AUTRE)
                            r3.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = dialogEmotion == emo,
                                    onClick = { dialogEmotion = emo }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val r4 = listOf(NorraEmotion.DEFAULT)
                            r4.forEach { emo ->
                                EmotionBubble(
                                    emotion = emo,
                                    isSelected = dialogEmotion == emo,
                                    onClick = { dialogEmotion = emo }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Intensité : ${dialogIntensity.toInt()}/5",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = dialogIntensity,
                        onValueChange = { dialogIntensity = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Qu'est ce qui a déclenché cette émotion ?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = dialogTriggerText,
                        onValueChange = { dialogTriggerText = it },
                        placeholder = { Text("Qu'est ce qui déclenche cela ? (Optionnel)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tes ressentis / Pensées libres :",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = dialogJournalText,
                        onValueChange = { dialogJournalText = it },
                        placeholder = { Text("Écris ici ce que tu as sur le cœur...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalNote = buildString {
                            if (dialogTriggerText.isNotBlank()) append("Déclencheur : $dialogTriggerText\n\n")
                            append(dialogJournalText)
                        }.trim()

                        viewModel.addMoodEntry(
                            emotion = dialogEmotion.id,
                            intensity = dialogIntensity.toInt(),
                            note = finalNote
                        )
                        showJournalDialog = false
                        dialogJournalText = ""
                        dialogTriggerText = ""
                    }
                ) {
                    Text("Sauvegarder")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showJournalDialog = false }
                ) {
                    Text("Fermer")
                }
            }
        )
    }
        } else if (selectedSuiviTab == 1) {
            MedicationTrackerScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
        } else {
            CrisisHistoryScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationTrackerScreen(viewModel: NorraViewModel, modifier: Modifier = Modifier) {
    val medications by viewModel.medications.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<com.example.data.Medication?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    val todayFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val selectedDateString by remember { mutableStateOf(todayFormatter.format(java.util.Date())) }
    val logs by viewModel.getMedicationLogsByDate(selectedDateString).collectAsState(initial = emptyList())
    val allLogs by viewModel.allMedicationLogs.collectAsState()
    val globalReminderEnabled by viewModel.globalReminderEnabled.collectAsState()
    val globalReminderTime by viewModel.globalReminderTime.collectAsState()
    
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("10") }
    var medCount by remember { mutableStateOf("1") }
    var medTime by remember { mutableStateOf("08:00") }
    var medReminderEnabled by remember { mutableStateOf(true) }
    var medIntakes by remember { mutableStateOf("") }
    
    val totalExpectedIntakes = remember(medications) {
        medications.sumOf { med ->
            if (med.intakes.isNotBlank()) med.intakes.split(";").filter { it.isNotBlank() }.size else 1
        }
    }
    val takenCount = logs.count { it.isTaken }
    val isAllMedsTakenToday = totalExpectedIntakes > 0 && takenCount >= totalExpectedIntakes
    
    var showConfetti by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val globalVibrationEnabled by viewModel.globalVibrationEnabled.collectAsState()
    
    var hasTriggeredConfetti by remember { mutableStateOf(isAllMedsTakenToday) }
    
    androidx.compose.runtime.LaunchedEffect(isAllMedsTakenToday) {
        if (isAllMedsTakenToday && !hasTriggeredConfetti) {
            hasTriggeredConfetti = true
            showConfetti = true
            if (globalVibrationEnabled) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            }
            kotlinx.coroutines.delay(2000)
            showConfetti = false
        } else if (!isAllMedsTakenToday) {
            hasTriggeredConfetti = false
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mes Traitements",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ajoute tes médicaments. Les rappels t'aideront à ne pas les oublier.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Rappel global", fontWeight = FontWeight.SemiBold)
                            Text("Un seul rappel pour tout (ou désactiver)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        androidx.compose.material3.Switch(
                            checked = globalReminderEnabled,
                            onCheckedChange = { checked ->
                                viewModel.setGlobalReminderEnabled(checked)
                                com.example.util.NotificationHelper.scheduleGlobalAlarm(context, globalReminderTime, checked)
                                medications.forEach { med ->
                                    if (checked && med.reminderEnabled) {
                                        com.example.util.NotificationHelper.scheduleMedicationAlarm(
                                            context, med.id, med.name, med.dosageMg, med.pillCount, med.timeToTake, true, med.intakes
                                        )
                                    } else {
                                        com.example.util.NotificationHelper.cancelMedicationAlarm(context, med.id)
                                    }
                                }
                            }
                        )
                    }
                    
                    if (globalReminderEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Heure du rappel global :", fontSize = 14.sp)
                            OutlinedButton(onClick = {
                                val tp = globalReminderTime.split(":")
                                val h = tp.getOrNull(0)?.toIntOrNull() ?: 8
                                val m = tp.getOrNull(1)?.toIntOrNull() ?: 0
                                android.app.TimePickerDialog(context, { _, ch, cm ->
                                    val newT = String.format(java.util.Locale.getDefault(), "%02d:%02d", ch, cm)
                                    viewModel.setGlobalReminderTime(newT)
                                    com.example.util.NotificationHelper.scheduleGlobalAlarm(context, newT, true)
                                    android.widget.Toast.makeText(context, "Rappel global réglé sur $newT", android.widget.Toast.LENGTH_SHORT).show()
                                }, h, m, true).show()
                            }) {
                                Text(globalReminderTime, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { 
                            editingMedication = null
                            medName = ""
                            medDosage = "10"
                            medCount = "1"
                            medTime = "08:00"
                            medIntakes = ""
                            medReminderEnabled = true
                            showAddDialog = true 
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ajouter un traitement")
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                MedicationHeatmap(
                    medications = medications,
                    logs = allLogs,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                )
            }
        }

        item {
            Text(
                text = "Suivi d'aujourd'hui",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(medications) { med ->
            val numIntakes = if (med.intakes.isNotBlank()) {
                med.intakes.split(";").filter { it.isNotBlank() }.size
            } else {
                1
            }
            val allIntakesLogs = logs.filter { it.medicationId == med.id }
            val takenCount = allIntakesLogs.count { it.isTaken }
            val isAllTaken = takenCount > 0 && takenCount >= numIntakes
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAllTaken) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = med.name, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 16.sp,
                                color = if (isAllTaken) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (!med.reminderEnabled || !globalReminderEnabled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Rappel désactivé",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(" off", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                        
                        val displayIntakes = if (med.intakes.isNotBlank()) {
                            med.intakes.split(";").mapNotNull {
                                val p = it.split(",")
                                if (p.size == 2) "${p[0]} (${p[1]}x)" else null
                            }.joinToString(", ")
                        } else {
                            "${med.timeToTake} (${med.pillCount}x)"
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${med.dosageMg}mg par cachet",
                            fontSize = 13.sp,
                            color = if (isAllTaken) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Prises : $displayIntakes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 0 until numIntakes) {
                            val isTakenForIndex = allIntakesLogs.any { it.intakeIndex == i && it.isTaken }
                            Checkbox(
                                checked = isTakenForIndex,
                                onCheckedChange = { checked ->
                                    viewModel.toggleMedicationLog(med.id, selectedDateString, checked, i)
                                }
                            )
                        }
                    }
                    
                    Column {
                        IconButton(onClick = {
                            val newValue = !med.reminderEnabled
                            viewModel.updateMedication(
                                id = med.id, 
                                name = med.name, 
                                dosageMg = med.dosageMg, 
                                pillCount = med.pillCount, 
                                timeToTake = med.timeToTake, 
                                reminderEnabled = newValue, 
                                intakes = med.intakes,
                                onUpdated = {}
                            )
                            if (newValue && globalReminderEnabled) {
                                com.example.util.NotificationHelper.scheduleMedicationAlarm(
                                    context, med.id, med.name, med.dosageMg, med.pillCount, med.timeToTake, true, med.intakes
                                )
                            } else {
                                com.example.util.NotificationHelper.cancelMedicationAlarm(context, med.id)
                            }
                        }) {
                            val iconAlpha = if (med.reminderEnabled) 1.0f else 0.4f
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Toggle Rappel", tint = MaterialTheme.colorScheme.primary.copy(alpha=iconAlpha))
                        }
                        IconButton(onClick = {
                            editingMedication = med
                            medName = med.name
                            medDosage = med.dosageMg.toString()
                            medCount = med.pillCount.toString()
                            medTime = med.timeToTake
                            medReminderEnabled = med.reminderEnabled
                            medIntakes = med.intakes
                            showAddDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Éditer", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { 
                            viewModel.deleteMedication(med.id)
                            com.example.util.NotificationHelper.cancelMedicationAlarm(context, med.id)
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        val titleText = if (editingMedication != null) "Modifier le traitement" else "Ajouter un traitement"
        val submitText = if (editingMedication != null) "Mettre à jour" else "Ajouter"
        
        var intakesList by remember { mutableStateOf(
            if (medIntakes.isNotBlank()) {
                medIntakes.split(";").mapNotNull {
                    val p = it.split(",")
                    if (p.size == 2) Pair(p[0], p[1].toIntOrNull() ?: 1) else null
                }
            } else {
                listOf(Pair(medTime, medCount.toIntOrNull() ?: 1))
            }
        ) }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(titleText, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Activer le rappel pour ce médoc", fontSize = 14.sp)
                        androidx.compose.material3.Switch(
                            checked = medReminderEnabled,
                            onCheckedChange = { medReminderEnabled = it }
                        )
                    }

                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Nom du médicament") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Dosage standard :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("10", "20", "30", "50", "100").forEach { quickDose ->
                            FilterChip(
                                selected = medDosage == quickDose,
                                onClick = { medDosage = quickDose },
                                label = { Text("${quickDose}mg") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("Ou dosage libre (mg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Prises journalières :", fontWeight = FontWeight.SemiBold)
                    
                    intakesList.forEachIndexed { index, intake ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val timeParts = intake.first.split(":")
                                    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                                    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                                    android.app.TimePickerDialog(
                                        context,
                                        { _, h, m ->
                                            val newTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m)
                                            val newList = intakesList.toMutableList()
                                            newList[index] = intake.copy(first = newTime)
                                            intakesList = newList
                                        },
                                        hour, minute, true
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(intake.first, fontWeight = FontWeight.Bold)
                            }

                            Text("x", fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = intake.second.toString(),
                                onValueChange = { newCount ->
                                    val countInt = newCount.toIntOrNull() ?: 0
                                    val newList = intakesList.toMutableList()
                                    newList[index] = intake.copy(second = countInt)
                                    intakesList = newList
                                },
                                modifier = Modifier.width(60.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            
                            if (intakesList.size > 1) {
                                IconButton(onClick = {
                                    val newList = intakesList.toMutableList()
                                    newList.removeAt(index)
                                    intakesList = newList
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                        }
                    }
                    
                    TextButton(onClick = {
                        intakesList = intakesList + Pair("08:00", 1)
                    }, modifier = Modifier.align(Alignment.End)) {
                        Text("+ Ajouter une prise")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (medName.isNotBlank()) {
                        val dosageInt = medDosage.toIntOrNull() ?: 10
                        val currentName = medName
                        
                        val newIntakesStr = intakesList.joinToString(";") { "${it.first},${it.second}" }
                        val firstIntakeCount = intakesList.firstOrNull()?.second ?: 1
                        val firstIntakeTime = intakesList.firstOrNull()?.first ?: "08:00"
                        
                        if (editingMedication != null) {
                            val medId = editingMedication!!.id
                            com.example.util.NotificationHelper.cancelMedicationAlarm(context, medId)
                            viewModel.updateMedication(
                                id = medId,
                                name = currentName,
                                dosageMg = dosageInt,
                                pillCount = firstIntakeCount,
                                timeToTake = firstIntakeTime,
                                reminderEnabled = medReminderEnabled,
                                intakes = newIntakesStr
                            ) {
                                com.example.util.NotificationHelper.scheduleMedicationAlarm(
                                    context,
                                    medId,
                                    currentName,
                                    dosageInt,
                                    firstIntakeCount,
                                    firstIntakeTime,
                                    medReminderEnabled && globalReminderEnabled,
                                    newIntakesStr
                                )
                            }
                            Toast.makeText(context, "Traitement mis à jour.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addMedication(
                                name = currentName,
                                dosageMg = dosageInt,
                                pillCount = firstIntakeCount,
                                timeToTake = firstIntakeTime,
                                reminderEnabled = medReminderEnabled,
                                intakes = newIntakesStr
                            ) { medId ->
                                com.example.util.NotificationHelper.scheduleMedicationAlarm(
                                    context,
                                    medId,
                                    currentName,
                                    dosageInt,
                                    firstIntakeCount,
                                    firstIntakeTime,
                                    medReminderEnabled && globalReminderEnabled,
                                    newIntakesStr
                                )
                            }
                            Toast.makeText(context, "Rappel programmé.", Toast.LENGTH_SHORT).show()
                        }
                        showAddDialog = false
                    }
                }) {
                    Text(submitText)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false 
                    editingMedication = null 
                }) { Text("Annuler") }
            }
        )
    }
    
    if (showConfetti) {
        ConfettiOverlay()
    }
    } // closes Box
}

@Composable
fun ConfettiOverlay() {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "confetti"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        for (i in 0..50) {
            val randomX = remember { (Math.random() * 1000).toFloat() }
            val randomOffset = remember { (Math.random() * 1000).toFloat() }
            val colorChoices = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)
            val randomColor = remember { colorChoices.random() }
            
            Box(
                modifier = Modifier
                    .offset(x = randomX.dp, y = (offsetY - randomOffset).dp)
                    .size(8.dp)
                    .background(randomColor)
            )
        }
    }
}

@Composable
fun EmotionBubble(
    emotion: NorraEmotion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else Color.Transparent
                )
                .border(
                    width = if (isSelected) 2.5.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            NorraCharacter(
                emotion = emotion,
                modifier = Modifier.fillMaxSize(),
                animate = false
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = emotion.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// -------------------------------------------------------------
// MAIN TAB 3: PSYCHOLOGICAL CALMING EXERCISES
// -------------------------------------------------------------

@Composable
fun ExercisesScreen(
    viewModel: NorraViewModel,
    modifier: Modifier = Modifier
) {
    val activeExercise by viewModel.activeExercise.collectAsState()
    val directLaunch by viewModel.directLaunchExercise.collectAsState()
    var showFeedbackFor by remember { mutableStateOf<String?>(null) }
    var feedbackTone by remember { mutableStateOf<String?>(null) } // "oui", "un_peu", "non"
    val globalVibrationEnabled by viewModel.globalVibrationEnabled.collectAsState()

    // Tracks whether the user has clicked "Start" on the intro screen
    var isExerciseStarted by remember(activeExercise) { mutableStateOf(directLaunch) }

    Box(modifier = modifier.fillMaxSize()) {
        if (showFeedbackFor != null) {
            FeedbackScreen(
                exerciseName = when (showFeedbackFor) {
                    "sensory_detox" -> "Apaisement Sensoriel - Autisme"
                    "bpd_anger" -> "Régulation de Colère - Trouble Borderline"
                    "panic_sos" -> "SOS Attaque de Panique"
                    "breath" -> "La Cohérence Cardiaque Norra"
                    "grounding" -> "L'Ancrage Sensoriel 5-4-3-2-1"
                    "affirmation" -> "Les Phrases d'Ancrage Scientifiques"
                    "tipp" -> "La Méthode TIPP en Urgence"
                    "stop" -> "La Technique STOP de de-escalade"
                    "jacobson" -> "Relaxation Musculaire de Jacobson"
                    "butterfly" -> "Le Câlin du Papillon (EMDR)"
                    "safe_place" -> "Visualisation du Lieu Sûr"
                    "bubble_burst" -> "Exutoire : Éclate ta Pensée"
                    "tactile_breathe" -> "Respiration avec Ancrage Tactile"
                    else -> "Exercice d'Apaisement"
                },
                feedbackTone = feedbackTone,
                onFeedbackSelected = { tone ->
                    feedbackTone = tone
                    viewModel.addCrisisLog(
                        exerciseId = showFeedbackFor ?: "unknown",
                        worked = tone == "oui" || tone == "un_peu"
                    )
                },
                onFinish = {
                    showFeedbackFor = null
                    feedbackTone = null
                    viewModel.setActiveExercise(null, false)
                }
            )
        } else if (activeExercise == null) {
            val favoriteExercises by viewModel.favoriteExercises.collectAsState()
            
            val exercises = listOf(
                Triple("bubble_burst", "Éclate ta Pensée", "Extériorisation active") to Icons.Default.Delete,
                Triple("tactile_breathe", "Respiration Tactile", "Ancrage physique sensoriel") to Icons.Default.Refresh,
                Triple("box_breathing", "Respiration Carrée", "Calme le mental") to Icons.Default.CheckCircle,
                Triple("breath", "Cohérence Cardiaque N.", "Idéal stress et panique") to Icons.Default.Refresh,
                Triple("panic_sos", "SOS Attaque", "Urgence angoisse") to Icons.Default.Warning,
                Triple("grounding", "Ancrage (5-4)", "Idéal dissociation") to Icons.Default.Star,
                Triple("tipp", "Méthode TIPP", "Dérégulation extrême") to Icons.Default.Notifications,
                Triple("stop", "Technique STOP", "Inhibition impulsive") to Icons.Default.Warning,
                Triple("bpd_anger", "Action Opposée", "Idéal trouble borderline") to Icons.Default.Build,
                Triple("sensory_detox", "Apaisement Sensoriel", "Idéal surcharge autistique") to Icons.Default.Face,
                Triple("affirmation", "Phrases d'Ancrage", "Idéal dépression") to Icons.Default.Favorite,
                Triple("jacobson", "Relaxation Jacobson", "Tension musculaire") to Icons.Default.Build,
                Triple("butterfly", "Hug du Papillon", "Stimulation bilatérale (EMDR)") to Icons.Default.Favorite,
                Triple("safe_place", "Lieu Sûr", "Régulation trauma/stress") to Icons.Default.Home
            )

            val favExercises = exercises.filter { favoriteExercises.contains(it.first.first) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Espace d'Apaisement",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Exercices cliniquement validés pour dissoudre le stress ou affronter une crise.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // FAVORITES SECTION (dynamically shown if not empty)
                if (favExercises.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mes Favoris",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val chunkedFavs = favExercises.chunked(2)
                    chunkedFavs.forEach { rowExercises ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowExercises.forEach { (contentInfo, icon) ->
                                    val (id, title, tag) = contentInfo
                                    val isFav = favoriteExercises.contains(id)
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(140.dp)
                                            .clickable { 
                                                viewModel.setActiveExercise(id, directLaunch = false) 
                                            }
                                            .shadow(4.dp, RoundedCornerShape(20.dp)),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Column(
                                                modifier = Modifier.fillMaxSize().padding(14.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = title,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = tag,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.toggleFavoriteExercise(id) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Favori",
                                                    tint = if (isFav) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowExercises.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // ALL EXERCISES SECTION
                item {
                    Text(
                        text = if (favExercises.isNotEmpty()) "Tous les Exercices" else "Parcourir les Exercices",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                val chunkedExercises = exercises.chunked(2)

                chunkedExercises.forEach { rowExercises ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowExercises.forEach { (contentInfo, icon) ->
                                val (id, title, tag) = contentInfo
                                val isFav = favoriteExercises.contains(id)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(140.dp)
                                        .clickable { 
                                            viewModel.setActiveExercise(id, directLaunch = false) 
                                        }
                                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                maxLines = 2
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = tag,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleFavoriteExercise(id) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favori",
                                                tint = if (isFav) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowExercises.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else if (!isExerciseStarted) {
            // Introductory screen before launching
            val exerciseTitle = when(activeExercise) {
                "sensory_detox" -> "Apaisement Sensoriel"
                "bpd_anger" -> "Action Opposée"
                "panic_sos" -> "SOS Attaque"
                "breath" -> "Cohérence Cardiaque"
                "box_breathing" -> "Respiration Carrée"
                "grounding" -> "Ancrage (5-4-3-2-1)"
                "affirmation" -> "Phrases d'Ancrage"
                "tipp" -> "Méthode TIPP"
                "stop" -> "Technique STOP"
                "jacobson" -> "Relaxation Jacobson"
                "butterfly" -> "Câlin du Papillon"
                "safe_place" -> "Lieu Sûr"
                "bubble_burst" -> "Éclate ta Pensée"
                "tactile_breathe" -> "Respiration Tactile"
                else -> "Exercice"
            }
            
            val exerciseDesc = when(activeExercise) {
                "sensory_detox" -> "Un parcours guidé pour réduire la surcharge sensorielle en se concentrant sur un élément apaisant. Super en cas de sur-stimulation."
                "bpd_anger" -> "Agir à l'inverse de ce que te dicte une émotion intense (comme la colère) pour en diminuer la force."
                "panic_sos" -> "Un guide rapide pour t'aider à retrouver ton calme pendant une attaque de panique."
                "breath" -> "Synchronise ta respiration pour rééquilibrer ton rythme cardiaque et apaiser rapidement ton système nerveux."
                "box_breathing" -> "Une respiration rythmée en 4 temps qui aide à ralentir le rythme cardiaque et clarifier l'esprit."
                "grounding" -> "Ramène ton attention sur le moment présent en utilisant tes 5 sens. Parfait quand tu te sens dissocié.e."
                "affirmation" -> "Répète ces phrases positives et validantes pour t'ancrer et contrer les pensées négatives."
                "tipp" -> "Température, Intensité, Pacing, Paired Muscle Relaxation. Une méthode radicale pour redescendre d'une crise émotionnelle."
                "stop" -> "Stop, Take a breath, Observe, Proceed. Une pause mentale pour éviter de réagir de façon impulsive."
                "jacobson" -> "Relâche les tensions musculaires accumulées par le stress en contractant puis relâchant chaque groupe musculaire."
                "butterfly" -> "Une technique EMDR autogérée de stimulation bilatérale pour apaiser rapidement le système nerveux et l'anxiété."
                "safe_place" -> "Crée un refuge mental sécurisant pour échapper instantanément à l'angoisse par la visualisation positive."
                "bubble_burst" -> "Matérialise une pensée intrusive dans une bulle et fais-la éclater symboliquement pour te libérer l'esprit."
                "tactile_breathe" -> "Synchronise ta respiration avec la pression de ton doigt pour un ancrage psychocorporel extrêmement puissant."
                else -> "Cet exercice va t'aider à te relaxer et retrouver ton calme."
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header (Icon & Title)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face, // just a placeholder icon 
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = exerciseTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = exerciseDesc,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = { isExerciseStarted = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Commencer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { viewModel.setActiveExercise(null, false) }
                ) {
                    Text("Retour", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            // Render active exercise panel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { viewModel.setActiveExercise(null, false) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (activeExercise) {
                        "sensory_detox" -> SensoryDetoxPanel(onFinished = { showFeedbackFor = "sensory_detox" })
                        "bpd_anger" -> BpdAngerPanel(onFinished = { showFeedbackFor = "bpd_anger" })
                        "panic_sos" -> PanicSosPanel(onFinished = { showFeedbackFor = "panic_sos" })
                        "breath" -> BreathingExercisePanel(onFinished = { showFeedbackFor = "breath" })
                        "box_breathing" -> BoxBreathingPanel(onFinished = { showFeedbackFor = "box_breathing" })
                        "grounding" -> GroundingExercisePanel(onFinished = { showFeedbackFor = "grounding" })
                        "affirmation" -> AffirmationsPanel(onFinished = { showFeedbackFor = "affirmation" })
                        "tipp" -> TippExercisePanel(onFinished = { showFeedbackFor = "tipp" })
                        "stop" -> StopExercisePanel(onFinished = { showFeedbackFor = "stop" })
                        "jacobson" -> JacobsonExercisePanel(onFinished = { showFeedbackFor = "jacobson" })
                        "butterfly" -> ButterflyExercisePanel(onFinished = { showFeedbackFor = "butterfly" })
                        "safe_place" -> SafePlaceExercisePanel(onFinished = { showFeedbackFor = "safe_place" })
                        "bubble_burst" -> BubbleBurstPanel(vibrationEnabled = globalVibrationEnabled, onFinished = { showFeedbackFor = "bubble_burst" })
                        "tactile_breathe" -> TactileBreathePanel(globalVibrationEnabled = globalVibrationEnabled, onFinished = { showFeedbackFor = "tactile_breathe" })
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: BOX BREATHING PANEL (RESPIRATION CARRÉE)
// -------------------------------------------------------------
@Composable
fun BoxBreathingPanel(onFinished: () -> Unit) {
    var phaseIndex by remember { mutableIntStateOf(0) }
    // 0: Inspire, 1: Bloque, 2: Expire, 3: Bloque
    val phases = listOf("Inspire...", "Bloque...", "Expire...", "Bloque...")
    var phaseText by remember { mutableStateOf(phases[0]) }
    var timeRemaining by remember { mutableIntStateOf(4) }
    
    // Animation progress for Norra position
    val infiniteTransition = rememberInfiniteTransition()
    val rawProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "box_progress"
    )

    // Sync phase and time remaining with progress (0..4)
    LaunchedEffect(rawProgress) {
        val currentPhase = (rawProgress % 4f).toInt().coerceIn(0, 3)
        if (currentPhase != phaseIndex) {
            phaseIndex = currentPhase
            phaseText = phases[currentPhase]
        }
        val currentFraction = rawProgress - currentPhase
        val remaining = 4 - (currentFraction * 4).toInt()
        timeRemaining = remaining.coerceIn(1, 4)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Respiration Carrée",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Suit Norra le long des bords du carré.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
        )

        // Draw the square and Norra
        val squareSize = 240.dp
        val norraSize = 60.dp
        val pathSize = squareSize - norraSize
        
        // Calculate offsets based on phase
        val progressPercent = rawProgress % 1f
        
        val xOffset = when(phaseIndex) {
            0 -> pathSize * progressPercent
            1 -> pathSize
            2 -> pathSize * (1f - progressPercent)
            else -> 0.dp
        }
        val yOffset = when(phaseIndex) {
            0 -> 0.dp
            1 -> pathSize * progressPercent
            2 -> pathSize
            else -> pathSize * (1f - progressPercent)
        }

        Box(
            modifier = Modifier
                .size(squareSize)
        ) {
            // Draw the base square track
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(norraSize / 2) // center track with Norra
            ) {
                val strokeWidth = 8.dp.toPx()
                val cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                
                // Track Background
                drawRoundRect(
                    color = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                    cornerRadius = cornerRadius
                )
                
                // Highlight track
                val activeColor = androidx.compose.ui.graphics.Color(0xFF2979FF)
                // Draw a simple path based on phase
                val path = androidx.compose.ui.graphics.Path()
                val w = size.width
                val h = size.height
                
                val pX = w * when(phaseIndex) {
                    0 -> progressPercent; 1 -> 1f; 2 -> 1f - progressPercent; else -> 0f
                }
                val pY = h * when(phaseIndex) {
                    0 -> 0f; 1 -> progressPercent; 2 -> 1f; else -> 1f - progressPercent
                }
                
                path.moveTo(0f, 0f)
                if (phaseIndex > 0) path.lineTo(w, 0f)
                if (phaseIndex > 1) path.lineTo(w, h)
                if (phaseIndex > 2) path.lineTo(0f, h)
                
                if (phaseIndex == 0) path.lineTo(pX, 0f)
                else if (phaseIndex == 1) path.lineTo(w, pY)
                else if (phaseIndex == 2) path.lineTo(pX, h)
                else if (phaseIndex == 3) path.lineTo(0f, pY)

                drawPath(
                    path = path,
                    color = activeColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
            }
            
            // Dynamic Norra following the path
            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .size(norraSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                NorraCharacter(
                    emotion = NorraEmotion.DEFAULT,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    animate = false
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = phaseText,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "$timeRemaining s",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        Button(
            onClick = onFinished,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
        ) {
            Text("Terminer l'exercice")
        }
    }
}

@Composable
fun BreathingExercisePanel(onFinished: () -> Unit) {
    var phaseText by remember { mutableStateOf("Inspire lentement...") }
    var scaleTarget by remember { mutableFloatStateOf(1f) }
    var progressSeconds by remember { mutableIntStateOf(5) }

    // Pulsing inhalation / exhalation cycle
    LaunchedEffect(Unit) {
        while (true) {
            // Inhale Phase (5 secs)
            phaseText = "Inspire lentement..."
            scaleTarget = 2.4f
            for (i in 5 downTo 1) {
                progressSeconds = i
                delay(1000)
            }
            // Exhale Phase (5 secs)
            phaseText = "Expire lentement..."
            scaleTarget = 1.0f
            for (i in 5 downTo 1) {
                progressSeconds = i
                delay(1000)
            }
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(durationMillis = 5000, easing = LinearEasing),
        label = "breath_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Cohérence Cardiaque Norra",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Laisse tes épaules se relâcher. Calque ta respiration sur Norra :",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Pulsing Norra breathing sphere
        Box(
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Dynamic glowing background air sphere
            Box(
                modifier = Modifier
                    .scale(animatedScale)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF80D8FF).copy(alpha = 0.6f), Color(0xFF2979FF).copy(alpha = 0.1f))
                        )
                    )
            )

            // Dynamic core Norra sphere
            Box(
                modifier = Modifier
                    .scale(animatedScale)
                    .size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                NorraCharacter(
                    emotion = NorraEmotion.DEFAULT,
                    modifier = Modifier.fillMaxSize(),
                    animate = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = phaseText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "$progressSeconds s",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onFinished,
            modifier = Modifier.padding(horizontal = 24.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Terminer l'exercice")
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: GROUNDING EXERCISE PANEL
// -------------------------------------------------------------
@Composable
fun GroundingExercisePanel(onFinished: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(5) } // 5 to 1

    // Large pools of alternative grounding items to allow "Reload" functionality smoothly without repeating
    val pools = remember {
        mapOf(
            5 to listOf("Une source de lumière", "Un objet en bois", "Ta propre chaussure", "Une touche de couleur bleue", "Une ombre sur le sol", "Un miroir ou reflet", "Un livre", "Une plante ou feuille verte", "Un stylo ou crayon", "Une fenêtre vers l'extérieur", "Un vêtement rouge", "Une chaise"),
            4 to listOf("Le tissu de tes vêtements", "La froideur d'une table", "Tes propres cheveux", "Le sol dur sous tes pieds", "Un objet lisse", "Une surface rugueuse", "Ta peau sur tes mains", "Le métal froid d'une clé ou bague"),
            3 to listOf("Le ronronnement d'un appareil", "Une voiture ou le vent", "Un oiseau ou une voix", "Le bruit de ta propre respiration", "Le tic-tac d'une horloge", "Le silence de la pièce"),
            2 to listOf("L'odeur du café ou du thé", "Un parfum ou savon naturel", "L'odeur du linge propre", "L'odeur de ta peau", "L'odeur d'une rose ou de menthe"),
            1 to listOf("Le goût frais de l'eau sur ta langue", "Le goût résiduel d'un dentifrice", "Un goût imaginaire (menthe, sucre)", "Une gorgée de boisson")
        )
    }

    fun getShuffledItems(step: Int): List<String> {
        return (pools[step] ?: emptyList()).shuffled().take(step)
    }

    var currentItems by remember(currentStep) {
        mutableStateOf(getShuffledItems(currentStep))
    }

    // Checking items
    val checkedItems = remember(currentItems) { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ancrage 5 - 4 - 3 - 2 - 1 Senses",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Touche ou regarde pour cocher. Recharge si certains objets manquent ici !",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentStep) {
                            5 -> "👀 5 objets à regarder :"
                            4 -> "✋ 4 textures à toucher :"
                            3 -> "👂 3 sons à écouter : "
                            2 -> "👃 2 odeurs à respirer :"
                            1 -> "👅 1 chose à goûter :"
                            else -> "Félicitations !"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { currentItems = getShuffledItems(currentStep) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Changer les propositions",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                currentItems.forEach { item ->
                    val isChecked = checkedItems[item] ?: false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { checkedItems[item] = !isChecked }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checkedItems[item] = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation through grounding steps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep < 5) {
                Button(
                    onClick = { currentStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reculer")
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (currentStep > 1) {
                Button(
                    onClick = { currentStep-- },
                    enabled = checkedItems.values.count { it } >= currentItems.size,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Étape Suivante")
                }
            } else {
                Button(
                    onClick = onFinished,
                    enabled = checkedItems.values.count { it } >= currentItems.size,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Terminer l'exercice")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: POSITIVE AFFIRMATIONS COPE CARD
// -------------------------------------------------------------
@Composable
fun AffirmationsPanel(onFinished: () -> Unit) {
    val quotes = listOf(
        "Ce que je ressens en ce moment est extrêmement intense, mais temporaire. Ça va s'atténuer.",
        "Mon émotion est comme une vague : elle culmine, puis s'en va d'elle-même si je la laisse couler.",
        "Je suis en sécurité physique ici et maintenant dans cette pièce.",
        "Je ne suis pas mes pensées. Les pensées ne sont que des mots flottants, pas des faits absolus.",
        "J'ai déjà survécu à 100% de mes pires tempêtes émotionnelles.",
        "Je fais de mon mieux en ce jour précis, et cela est amplement suffisant.",
        "Je n'ai pas besoin d'une solution immédiate pour surmonter cette minute. Juste respirer.",
        "Je m'autorise à ressentir cette émotion sans me juger, puis à la laisser passer calmement."
    )

    var currentQuoteIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Phrases de Tolérance à la Détresse",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Répète ces phrases doucement dans ta tête en respirant lentement.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = quotes[currentQuoteIndex],
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
                },
                modifier = Modifier.padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Phrase Suivante")
            }

            OutlinedButton(
                onClick = onFinished,
                modifier = Modifier.padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("J'ai fini")
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: DBT TIPP EMERGENCY PANEL
// -------------------------------------------------------------
@Composable
fun TippExercisePanel(onFinished: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Temperature, 1: Intense, 2: Pace, 3: Paired
    val stepTitle = listOf(
        "❄️ T - Température",
        "🏃 I - Effort Intense",
        "🌬️ P - Respiration",
        "🧘 P - Relaxation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "La Méthode TIPP d'Urgence",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Recommandée en thérapie comportementale dialectique pour stopper net une charge émotionnelle insoutenable.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large progress tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 8.dp,
            containerColor = Color.Transparent
        ) {
            stepTitle.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                when (selectedTab) {
                    0 -> {
                        Text(
                            text = "Le Choc Thermique Apaisant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Le froid déclenche instantanément le réflexe d'immersion des mammifères, ce qui entraîne une baisse immédiate de la fréquence cardiaque et calme le système d'alerte cérébral.\n\n" +
                                    "👉 Action concrète : Passe-toi de l'eau très froide sur le visage, tiens un glaçon fermement dans ta main, ou pose une poche glacée sur tes yeux pendant 30 secondes.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                    1 -> {
                        Text(
                            text = "Détourner l'Adrénaline par l'Effort",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quand le corps accumule trop d'énergie de combat (colère noire, attaque de panique), il faut expulser physiquement cette énergie pour éviter l'impulsion destructrice.\n\n" +
                                    "👉 Action concrète : Fais 20 squats rapides, sautille énergiquement sur place, ou fais des pompes jusqu'à épuiser l'adrénaline en trop.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                    2 -> {
                        Text(
                            text = "Respiration Profonde Rythmée",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "En forçant une expiration plus longue que l'inspiration, tu indiques chimiquement à ton cerveau émotionnel qu'aucun danger n'est présent.\n\n" +
                                    "👉 Action concrète : Prends une inspiration ventrale lente durant 4 secondes, puis souffle à fond doucement durant 7 secondes. Répète cela 5 fois.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                    3 -> {
                        Text(
                            text = "Relaxation Musculaire Appariée",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "La tension du corps entretient la panique mentale. Contracter puis relâcher brise mécaniquement ce cercle vicieux.\n\n" +
                                    "👉 Action concrète : Serre très fort tes poings et hausse tes épaules durant 5 secondes, puis relâche d'un coup sec en expirant. Sens la chaleur s'installer.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTab > 0) {
                OutlinedButton(onClick = { selectedTab-- }) {
                    Text("Précédent")
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (selectedTab < 3) {
                Button(onClick = { selectedTab++ }) {
                    Text("Étape Suivante")
                }
            } else {
                Button(
                    onClick = onFinished,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("J'ai fini - Terminer ! 💜")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: DBT STOP METHOD PANEL
// -------------------------------------------------------------
@Composable
fun StopExercisePanel(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Pair("🛑 S - S'arrêter", "Arrête-toi immédiatement ! Ne réagis pas sous le coup de l'émotion brute. Tes sentiments actuels essaient de te forcer à agir impulsivement. Reste immobile une seconde."),
        Pair("💨 T - Traiter / Respirer", "Prends une grande inspiration ventrale. Expire deux fois plus lentement. Ressens le contact de l'air frais qui vient apaiser la tempête intérieure."),
        Pair("👀 O - Observer", "Observe ce qui se passe en toi et autour de toi. Quelles sensations physiques ressens-tu ? Quelles pensées défilent ? Ne les juge pas, observe-les comme de simples nuages."),
        Pair("⚖️ P - Prendre du recul", "Prends du recul d'un pas. Regarde la situation dans son ensemble. Est-ce une vraie urgence ? Que te conseillerait Norra avec bienveillance en cet instant ?"),
        Pair("✨ S - Agir avec Sagesse", "Agis d'une manière sage et constructive. Quelle est la chose la plus douce, équilibrée et utile que tu puisses accomplir pour ton bien et celui des autres ?")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "La Technique de Clarté S.T.O.P.",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Un outil fondamental pour insérer un espace de conscience libre entre le stimulus émotionnel agressif et la réaction.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Indicators
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (index <= stepIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = steps[stepIndex].first,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = steps[stepIndex].second,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (stepIndex > 0) {
                OutlinedButton(onClick = { stepIndex-- }) {
                    Text("Retour")
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (stepIndex < steps.size - 1) {
                Button(onClick = { stepIndex++ }) {
                    Text("Suivant")
                }
            } else {
                Button(
                    onClick = onFinished,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Terminer l'exercice")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: SENSORY DETOX PANEL (AUTISM/TSA)
// -------------------------------------------------------------
@Composable
fun SensoryDetoxPanel(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Pair("🎧 1. Réduire les stimuli brusques", "Ferme les yeux, ou mets un casque anti-bruit. Isole-toi dans un environnement avec moins de lumière et de bruit immédiat. Baisse la luminosité de ton téléphone."),
        Pair("👐 2. Pression profonde apaisante", "Applique une pression profonde (deep pressure sur les épaules, serre un coussin lourd contre ton coeur ou utilise une couverture lestée)."),
        Pair("🔄 3. Autorisation au Stimming", "Il est totalement normal et sain de faire des mouvements répétitifs (stimming) pour t'auto-réguler. Autorise-toi à te balancer, frotter tes mains ou utiliser un fidget cube."),
        Pair("🌱 4. Reconnexion douce", "Laisse le système nerveux redescendre. Inutile de brusquer la relance. Reste statique aussi longtemps que nécessaire dans ta bulle protectrice avant de repartir.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Apaisement Sensoriel",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Une pause structurée pour réduire la surcharge sensorielle (overstimulation) sans jugement.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (index <= stepIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = steps[stepIndex].first,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = steps[stepIndex].second,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (stepIndex > 0) {
                OutlinedButton(onClick = { stepIndex-- }) { Text("Retour") }
            } else { Spacer(modifier = Modifier.width(4.dp)) }

            if (stepIndex < steps.size - 1) {
                Button(onClick = { stepIndex++ }) { Text("Suivant") }
            } else {
                Button(onClick = onFinished, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    Text("Terminer")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: BPD ANGER ACTION OPPOSE PANEL
// -------------------------------------------------------------
@Composable
fun BpdAngerPanel(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Pair("🔥 1. Validation de l'intensité", "La douleur émotionnelle ou la colère que tu ressens en cet instant est réelle, intense et légitime en interne. Accueille le pic inflammatoire sans t'y confondre."),
        Pair("🧊 2. Action Opposée", "L'impulsion te dicte d'attaquer ou de te retirer ? Applique consciemment l'action radicalement opposée : Adopte une posture détendue, entrouvre les mains, détends la mâchoire."),
        Pair("💬 3. Refroidir le discours interne", "Change délibérément ton ton de voix interne. Au lieu de jugements froids, parle-toi chaudement, comme on parlerait à un enfant effrayé en pleine de tempête."),
        Pair("🧭 4. Reporter l'action radicale", "Demande-toi : 'Cette décision a-t-elle besoin d'être prise là, à la seconde ?' Assouplis l'urgence. L'action décisive est reportée dans 24 heures.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Action Opposée (Colère)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Réguler une intense émotion de rejet sans agressivité ni comportement auto-destructeur.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (index <= stepIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = steps[stepIndex].first,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = steps[stepIndex].second,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (stepIndex > 0) {
                OutlinedButton(onClick = { stepIndex-- }) { Text("Retour") }
            } else { Spacer(modifier = Modifier.width(4.dp)) }

            if (stepIndex < steps.size - 1) {
                Button(onClick = { stepIndex++ }) { Text("Suivant") }
            } else {
                Button(onClick = onFinished, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    Text("Terminer")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SUB-EXERCISE: PANIC SOS PANEL
// -------------------------------------------------------------
@Composable
fun PanicSosPanel(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Pair("🌬️ 1. Contre l'étouffement", "Tu n'as pas de malaise cardiaque. C'est le cerveau limbique qui pompe de l'adrénaline. Concentre-toi sur EXPIRER à fond, pas sur inspirer."),
        Pair("📦 2. Respiration Carrée 4s", "Inspire par le nez pendant 4 secondes. Parfait. \nMaintenant bloque 4 secondes.\nExpire lentement par la bouche 4 secondes.\nBloque à vide 4 secondes. \n(Fais-le 3 fois)."),
        Pair("🧊 3. Le Choc Thermique", "La technique du plongeur : Place tes poignets/mains sous l'eau très froide ou passe-toi un glaçon sur les joues. Cela active le nerf vague et casse la panique."),
        Pair("🧠 4. Distraction Cognitive", "Engage ton cortex rationnel : Compte de 100 à 0 en soustrayant 7. (100... 93... 86... 79...). Continue jusqu'à ce que la peur redescende.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SOS Crise d'Angoisse",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Protocole pour casser l'hyperventilation aigu via le système parasympathique.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (index <= stepIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(6.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = steps[stepIndex].first,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = steps[stepIndex].second,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (stepIndex > 0) {
                OutlinedButton(onClick = { stepIndex-- }) { Text("Retour") }
            } else { Spacer(modifier = Modifier.width(4.dp)) }

            if (stepIndex < steps.size - 1) {
                Button(onClick = { stepIndex++ }) { Text("Suivant") }
            } else {
                Button(onClick = onFinished, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    Text("Terminer l'exercice")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NEW EXERCISE: JACOBSON RELAXATION
// -------------------------------------------------------------
@Composable
fun JacobsonExercisePanel(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Pair("✊ 1. Les Poings & Bras", "Serre les poings très fort et crispe les bras en inspirant profondément. Maintiens 5 secondes... \n\nMaintenant relâche tout d'un coup en expirant ! Sens la lourdeur."),
        Pair("😬 2. Visage & Épaules", "Fronce le nez, sers les dents, ferme les yeux fort et monte tes épaules vers tes oreilles en inspirant. 5 secondes... \n\nRelâche ! Laisse tes épaules retomber."),
        Pair("🫁 3. Ventre & Dos", "Contracte tes abdominaux et cambre légèrement ton dos en inspirant. Serre bien fort. 5 secondes... \n\nExpire et relâche. Laisse ton ventre gonfler doucement."),
        Pair("🦵 4. Jambes & Pieds", "Tends tes jambes et contracte tes cuisses, tes mollets, et pointe tes orteils vers le haut. 5 secondes... \n\nRelâche tout ! Observe tes muscles s'enfoncer dans le sol.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Relaxation de Jacobson",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Mieux que d'essayer de se détendre : force la tension pour vider l'adrénaline.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (index <= stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp).shadow(6.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = steps[stepIndex].first, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = steps[stepIndex].second, fontSize = 15.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (stepIndex > 0) { OutlinedButton(onClick = { stepIndex-- }) { Text("Retour") } } else { Spacer(modifier = Modifier.width(4.dp)) }
            if (stepIndex < steps.size - 1) { Button(onClick = { stepIndex++ }) { Text("Suivant") } } else {
                Button(onClick = onFinished, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("Terminer l'exercice") }
            }
        }
    }
}

// -------------------------------------------------------------
// NEW EXERCISE: BUTTERFLY HUG (EMDR)
// -------------------------------------------------------------
@Composable
fun ButterflyExercisePanel(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Pair("🦋 1. La Posture", "Croise tes bras sur ta poitrine, les mains posées sous tes clavicules. Tes pouces peuvent s'accrocher ensemble pour former un papillon."),
        Pair("👐 2. Le Mouvement", "Tapote doucement et alternativement ta poitrine : d'abord la main gauche, puis la main droite, puis la gauche... Fais-le au rythme d'un battement de cœur lent."),
        Pair("🌬️ 3. La Respiration", "Pendant que tu tapotes, inspire par le nez et expire par la bouche. Remarque les sensations autour et à l'intérieur de toi. Ne juge pas."),
        Pair("🛡️ 4. L'Ancrage", "Continue ces tapotements alternés pendant une minute. Laisse l'anxiété traverser ton corps sans la bloquer, la situation va s'apaiser d'elle-même.")
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Le Câlin du Papillon (EMDR)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Active tes deux hémisphères cérébraux pour digérer l'émotion.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEachIndexed { index, _ -> Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(if (index <= stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp).shadow(6.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = steps[stepIndex].first, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = steps[stepIndex].second, fontSize = 15.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (stepIndex > 0) { OutlinedButton(onClick = { stepIndex-- }) { Text("Retour") } } else { Spacer(modifier = Modifier.width(4.dp)) }
            if (stepIndex < steps.size - 1) { Button(onClick = { stepIndex++ }) { Text("Suivant") } } else {
                Button(onClick = onFinished) { Text("Terminer l'exercice") }
            }
        }
    }
}

// -------------------------------------------------------------
// NEW EXERCISE: SAFE PLACE VISUALIZATION
// -------------------------------------------------------------
@Composable
fun SafePlaceExercisePanel(onFinished: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Pair("🏞️ 1. Le Choix", "Ferme les yeux. Choisis un endroit réel ou imaginaire (plage, forêt, cabane) où tu te sens en absolue sécurité et apaisé(e)."),
        Pair("👁️ 2. Décris ce que tu vois", "Quelles sont les couleurs ? La lumière ? Observe les moindres détails visuels de ce lieu ressource."),
        Pair("👂 3. Installe les sons & sensations", "Entends le vent ou l'eau ? Sens-tu une brise, une chaleur douce ou la texture sous tes pieds ? Imprègne-t'en."),
        Pair("🔒 4. Point d'ancrage", "Pendant que tu observes ce monde parfait, prononce mentalement 'Je suis en sécurité' ou trouve un mot-clé qui te permettra de revenir ici plus tard.")
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Le Lieu Sûr", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Détache-toi de la détresse en rejoignant un abri émotionnel.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEachIndexed { index, _ -> Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(if (index <= stepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp).shadow(6.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = steps[stepIndex].first, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = steps[stepIndex].second, fontSize = 15.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp)
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (stepIndex > 0) { OutlinedButton(onClick = { stepIndex-- }) { Text("Retour") } } else { Spacer(modifier = Modifier.width(4.dp)) }
            if (stepIndex < steps.size - 1) { Button(onClick = { stepIndex++ }) { Text("Suivant") } } else {
                Button(onClick = onFinished) { Text("Terminer l'exercice") }
            }
        }
    }
}

// -------------------------------------------------------------
// REUSABLE FEEDBACK PANEL ASKED BY THE USER
// -------------------------------------------------------------
@Composable
fun FeedbackScreen(
    exerciseName: String,
    feedbackTone: String?,
    onFeedbackSelected: (String) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NorraCharacter(
                        emotion = if (feedbackTone == "non") NorraEmotion.TRISTE else if (feedbackTone == "un_peu") NorraEmotion.IDK else NorraEmotion.DEFAULT,
                        modifier = Modifier.fillMaxSize(),
                        animate = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Avis de Fin d'Exercice",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = exerciseName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (feedbackTone == null) {
                    Text(
                        text = "Est-ce que cet exercice t'a été utile pour t'apaiser ou prendre du recul ?",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Button(
                        onClick = { onFeedbackSelected("oui") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("👍 Oui, beaucoup !")
                    }

                    Button(
                        onClick = { onFeedbackSelected("un_peu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("😐 Un petit peu")
                    }

                    OutlinedButton(
                        onClick = { onFeedbackSelected("non") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("👎 Pas vraiment")
                    }
                } else {
                    val msg = when (feedbackTone) {
                        "oui" -> "Merveilleux ! Norra est ravie d'apprendre que cela t'a soulagé(e). Prends soin de toi, tu fais de grands progrès. 💜"
                        "un_peu" -> "Chaque petite fraction de calme et d'effort compte énormément. Sois fier(e) d'avoir fait ce pas pour toi ! Pas à pas. 🌸"
                        "non" -> "C'est tout à fait normal. L'impact varie selon les moments. N'hésite pas à tester une autre méthode, ou écris directement à Norra !"
                        else -> ""
                    }

                    Text(
                        text = msg,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 16.dp),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Fermer l'exercice")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MAIN TAB 5: PREVENTATIVE & DAILY RESOURCES ASKED BY THE USER
// -------------------------------------------------------------
@Composable
fun ResourcesScreen(
    viewModel: NorraViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("all") } // "all", "coping", "daily", "compassion"
    var activeArticle by remember { mutableStateOf<ResourceArticle?>(null) }

    if (activeArticle != null) {
        ArticleReaderPanel(article = activeArticle!!, onBack = { activeArticle = null })
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page header & Intro
        item {
            Text(
                text = "Guide & Ressources",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Pour aller plus loin : des conseils cliniques pour mieux vivre avec le trouble au quotidien et dépasser le stade exclusif des crises.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Horizontal filtering chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    Triple("all", "Tout", Icons.Default.Done),
                    Triple("coping", "Palier au Trouble 🌋", Icons.Default.Warning),
                    Triple("daily", "Vivre Avec 🌿", Icons.Default.Home),
                    Triple("compassion", "Auto-Compassion ✨", Icons.Default.Favorite)
                )

                // Render chips horizontally with scroll
                Box(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { (id, label, _) ->
                            FilterChip(
                                selected = selectedCategory == id,
                                onClick = { selectedCategory = id },
                                label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        }

        // Filtered resource content items
        val filteredArticles = if (selectedCategory == "all") {
            libraryArticles
        } else {
            libraryArticles.filter { it.categoryId == selectedCategory }
        }

        items(filteredArticles) { article ->
            ResourceEntryCard(
                title = article.title,
                categoryBadge = article.categoryBadge,
                tagColor = article.tagColor,
                description = article.description,
                onClick = { activeArticle = article }
            )
        }
    }
}

@Composable
fun ArticleReaderPanel(article: ResourceArticle, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Spacer(modifier = Modifier.width(8.dp))
            SuggestionChip(
                onClick = { },
                label = { Text(article.categoryBadge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = article.tagColor) },
                border = BorderStroke(1.dp, article.tagColor.copy(alpha = 0.5f))
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Text(
                    text = article.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                Text(
                    text = article.longContent,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                Text(
                    text = "Conseils pratiques :",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                article.bulletPoints.forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = article.tagColor,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = point,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceEntryCard(
    title: String,
    categoryBadge: String,
    tagColor: Color,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = { },
                    label = { Text(categoryBadge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = tagColor) },
                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lire l'article →",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// -------------------------------------------------------------
// MAIN TAB 4: CRISIS RELAY CONTACTS
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactsScreen(
    viewModel: NorraViewModel,
    modifier: Modifier = Modifier
) {
    val customContacts by viewModel.customContacts.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }
    var newContactRelation by remember { mutableStateOf("Ami") }
    var customRelationMsg by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val contentResolver = context.contentResolver
                    var name = ""
                    var phone = ""
                    
                    val cursor = contentResolver.query(uri, null, null, null, null)
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            if (nameIndex >= 0) {
                                name = c.getString(nameIndex)
                            }
                            
                            val phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (phoneIndex >= 0) {
                                phone = c.getString(phoneIndex)
                            }
                        }
                    }
                    
                    if (name.isNotBlank()) {
                        newContactName = name
                    }
                    if (phone.isNotBlank()) {
                        newContactPhone = phone
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Intro Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Parrains / Relais de soutien",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Aussi appelé 'Cope Network'. Enregistre ici tes proches de confiance ou ton thérapeute pour pouvoir leur envoyer un message ou les appeler en 1 clic.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Action button to add contact
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_contact_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter un contact relais")
            }
        }

        // Custom Contacts list
        if (customContacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Aucun contact personnalisé enregistré. Ajoute un ami de confiance !", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        } else {
            items(customContacts) { person ->
                ContactItemRow(
                    person = person,
                    onDelete = { viewModel.deleteContact(person.id) },
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                        context.startActivity(intent)
                    },
                    onSms = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:${person.phone}")
                            putExtra("sms_body", "Coucou, je traverse une crise émo et j'utilise Norra. J'aurais besoin de soutien d'un parrain/relais. S'il te plaît, contacte moi !")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        // Default Institutional Helplines in French region
        item {
            Text(
                text = "Services de santé officiels (France)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            InstitutionalContactRow(
                name = "3114 (Suicide Prévention)",
                description = "Gratuit, confidentiel et disponible 24h/24.",
                phone = "3114",
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:3114"))
                    context.startActivity(intent)
                }
            )
        }

        item {
            InstitutionalContactRow(
                name = "15 (Samu)",
                description = "Urgences vitales médicales ou détresse psychiatrique majeure.",
                phone = "15",
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:15"))
                    context.startActivity(intent)
                }
            )
        }
    }

    // Modal to add a custom contact
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Ajouter un contact relais") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Contact Importer trigger
                    Button(
                        onClick = { 
                            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                            contactPickerLauncher.launch(intent) 
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Importer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importer depuis mes contacts", fontSize = 13.sp)
                    }

                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Nom du contact") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_contact_name")
                    )

                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Téléphone") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_contact_phone")
                    )

                    // Relation text label
                    Text(
                        text = "Relation :",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Multi-row FlowRow relationship selector (AMI, FAMILLE, CONJOINT, THERAPEUTE, AUTRE)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Ami", "Famille", "Conjoint", "Thérapeute", "Autre").forEach { rel ->
                            FilterChip(
                                selected = newContactRelation == rel,
                                onClick = { newContactRelation = rel },
                                label = { Text(rel) }
                            )
                        }
                    }

                    if (newContactRelation == "Autre") {
                        OutlinedTextField(
                            value = customRelationMsg,
                            onValueChange = { customRelationMsg = it },
                            label = { Text("Spécifier la relation (ex: Parrain, Voisin...)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                            val finalRelation = if (newContactRelation == "Autre") {
                                customRelationMsg.ifBlank { "Autre" }
                            } else {
                                newContactRelation
                            }
                            viewModel.addContact(newContactName, newContactPhone, finalRelation)
                            newContactName = ""
                            newContactPhone = ""
                            customRelationMsg = ""
                            newContactRelation = "Ami"
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_contact_button")
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newContactName = ""
                    newContactPhone = ""
                    customRelationMsg = ""
                    newContactRelation = "Ami"
                }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ContactItemRow(
    person: com.example.data.ContactPerson,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onSms: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = person.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "${person.relation} • ${person.phone}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCall) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Appeler", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onSms) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Sms", tint = Color(0xFF007AFF))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun InstitutionalContactRow(
    name: String,
    description: String,
    phone: String,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3B30))
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onCall) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = "Appeler", tint = Color(0xFFFF3B30))
            }
        }
    }
}

// -------------------------------------------------------------
// PROFILE & CLOUD BACKUP SCREEN
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: NorraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userEmailFlow by viewModel.userEmail.collectAsState()
    val isConnectedFlow by viewModel.isUserLoggedIn.collectAsState()
    val userNameFlow by viewModel.userName.collectAsState()

    val whyUseNorra by viewModel.whyUseNorra.collectAsState()
    val diagnosedDisorder by viewModel.diagnosedDisorder.collectAsState()
    val referredBy by viewModel.referredBy.collectAsState()

    var isConnected by remember { mutableStateOf(isConnectedFlow) }
    var userEmail by remember { mutableStateOf(userEmailFlow) }
    var userName by remember { mutableStateOf(userNameFlow) }
    var isBackingUp by remember { mutableStateOf(false) }
    var lastBackupTime by remember { mutableStateOf("Jamais") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isBackingUp = true
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    val backupData = viewModel.getExportJsonString()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(backupData.toByteArray())
                    }
                    val nowTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        lastBackupTime = "Aujourd'hui à $nowTime"
                        Toast.makeText(context, "Sauvegarde réussie !", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(context, "Erreur de sauvegarde", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                } finally {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isBackingUp = false
                    }
                }
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                var jsonString = ""
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    jsonString = inputStream.bufferedReader().use { it.readText() }
                }
                if (jsonString.isNotBlank()) {
                    viewModel.importDataFromJson(jsonString)
                }
                val nowTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                lastBackupTime = "Aujourd'hui à $nowTime (Importé)"
                Toast.makeText(context, "Données importées avec succès !", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur lors de l'importation", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Dialog state for editing QCM inside Profile
    var showEditQcmDialog by remember { mutableStateOf(false) }
    var editQcmIndex by remember { mutableIntStateOf(1) } // 1: Why, 2: Disorder, 3: Referred

    // Temp selection for editing
    var tempSelection by remember { mutableStateOf("") }
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    
    var showEditEmailDialog by remember { mutableStateOf(false) }
    var tempEmail by remember { mutableStateOf("") }

    var showEditPasswordDialog by remember { mutableStateOf(false) }
    var tempPassword by remember { mutableStateOf("") }
    
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }

    // Sync state
    LaunchedEffect(isConnectedFlow, userEmailFlow, userNameFlow) {
        isConnected = isConnectedFlow
        userEmail = if (userEmailFlow.isBlank()) "perrine@gmail.com" else userEmailFlow
        userName = userNameFlow.ifBlank { "Perrine" }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            isConnected = true
            userEmail = account?.email ?: "perrine@gmail.com"
            val fetchedName = account?.givenName ?: "Perrine"
            viewModel.setLoginState(true, userEmail, fetchedName)
            Toast.makeText(context, "Connecté avec succès !", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            if (e.statusCode == 10) {
                Toast.makeText(context, "Mode simulation (SHA-1 local) : Connecté virtuellement", Toast.LENGTH_LONG).show()
                isConnected = true
                userEmail = "perrine@gmail.com"
                viewModel.setLoginState(true, "perrine@gmail.com", "Perrine")
            } else {
                Toast.makeText(context, "Erreur de connexion : ${e.statusCode}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                text = if (isConnected) userName else "Profil hors-ligne",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            if (isConnected) {
                Text(text = userEmail, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // -------------------------------------------------------------
            // SECTION : CONSULT AND EDIT SURVEY/QCM ANSWERS
            // -------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📋 Configuration d'Accompagnement",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Why use Norra
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editQcmIndex = 1
                                tempSelection = whyUseNorra
                                showEditQcmDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pourquoi utiliser Norra :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Éditer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(whyUseNorra, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Diagnosed disorder
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editQcmIndex = 2
                                tempSelection = diagnosedDisorder
                                showEditQcmDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Défis de santé déclarés :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Éditer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(diagnosedDisorder, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Referred by
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editQcmIndex = 3
                                tempSelection = referredBy
                                showEditQcmDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Conseillé à l'utilisation par :", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Éditer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(referredBy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Paramètres de Compte
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "⚙️ Paramètres du Compte",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Modifier le prénom
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempName = userName
                                showEditNameDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modifier le prénom", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Éditer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(userName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Divider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Modifier l'adresse e-mail
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempEmail = userEmail
                                showEditEmailDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modifier l'adresse E-mail", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Éditer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Modifier le mot de passe
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempPassword = ""
                                showEditPasswordDialog = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modifier le mot de passe", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Éditer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Global Vibration
                    val vibrationEnabled by viewModel.globalVibrationEnabled.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibrations dans l'application", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { viewModel.setGlobalVibrationEnabled(it) },
                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Test Notification
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { com.example.util.NotificationHelper.testNotification(context) }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tester une notification", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Tester", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Signaler un bug
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("helpmenorra@gmail.com"))
                                    putExtra(Intent.EXTRA_SUBJECT, "Norra App - Rapport de Bug")
                                    putExtra(Intent.EXTRA_TEXT, "Bonjour l'équipe Norra,\n\nVoici le bug que je souhaite vous signaler :\n\n...")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Signaler un bug via..."))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Aucune application email n'est installée.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Signaler un bug ou problème", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            Icon(imageVector = Icons.Default.Email, contentDescription = "Rapport de Bug", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Se déconnecter
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso).signOut()
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                viewModel.setLoginState(false, "", "")
                                android.widget.Toast.makeText(context, "Vous êtes déconnecté.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Se déconnecter", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Se déconnecter", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Supprimer toutes mes données
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteDataDialog = true }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Supprimer toutes mes données", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer les données", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Supprimer le compte
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAccountDialog = true }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Supprimer mon compte", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Google Drive Backup Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Sauvegarde des Données",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Protège tes données en les exportant vers ton appareil ou Google Drive.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Dernière sauvegarde : $lastBackupTime",
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Button(
                        onClick = {
                            val now = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            exportLauncher.launch("Norra_Backup_$now.json")
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isBackingUp
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sauvegarde en cours...")
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sauvegarder mes données")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importer mes données")
                    }
                }
            }
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Modifier le prénom", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nouveau prénom") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(tempName)
                            .build()
                        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                viewModel.updateUserName(tempName)
                                Toast.makeText(context, "Prénom mis à jour avec succès", Toast.LENGTH_SHORT).show()
                                showEditNameDialog = false
                            } else {
                                Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        viewModel.updateUserName(tempName)
                        Toast.makeText(context, "Prénom mis à jour avec succès (Local)", Toast.LENGTH_SHORT).show()
                        showEditNameDialog = false
                    }
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showEditEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEditEmailDialog = false },
            title = { Text("Modifier l'adresse E-mail", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = tempEmail,
                    onValueChange = { tempEmail = it },
                    label = { Text("Nouvelle adresse") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        user.updateEmail(tempEmail).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                viewModel.setUserEmail(tempEmail)
                                Toast.makeText(context, "E-mail mis à jour avec succès", Toast.LENGTH_SHORT).show()
                                showEditEmailDialog = false
                            } else {
                                Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        viewModel.setUserEmail(tempEmail)
                        Toast.makeText(context, "E-mail mis à jour avec succès (Local)", Toast.LENGTH_SHORT).show()
                        showEditEmailDialog = false
                    }
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEmailDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showEditPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showEditPasswordDialog = false },
            title = { Text("Modifier le mot de passe", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Nouveau mot de passe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        user.updatePassword(tempPassword).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Mot de passe mis à jour avec succès", Toast.LENGTH_SHORT).show()
                                showEditPasswordDialog = false
                            } else {
                                Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Erreur: Veuillez vous reconnecter", Toast.LENGTH_SHORT).show()
                        showEditPasswordDialog = false
                    }
                }) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPasswordDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Supprimer le compte", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.error) },
            text = { Text("Es-tu sûr(e) de vouloir supprimer définitivement ton compte et toutes tes données associées ? Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            user.delete().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    viewModel.resetAllUserData(context)
                                    Toast.makeText(context, "Compte et données associés supprimés avec succès.", Toast.LENGTH_SHORT).show()
                                    showDeleteAccountDialog = false
                                } else {
                                    if (task.exception is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                                        Toast.makeText(context, "Par sécurité, veuillez vous déconnecter puis vous reconnecter avant de supprimer votre compte.", Toast.LENGTH_LONG).show()
                                    } else {
                                        // Delete local data anyway
                                        viewModel.resetAllUserData(context)
                                        Toast.makeText(context, "Données locales supprimées. Compte cloud indisponible: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                    showDeleteAccountDialog = false
                                }
                            }
                        } else {
                            viewModel.resetAllUserData(context)
                            Toast.makeText(context, "Compte local et toutes les données effacés avec succès.", Toast.LENGTH_SHORT).show()
                            showDeleteAccountDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer définitivement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text("Supprimer mes données", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.error) },
            text = { Text("Es-tu sûr(e) de vouloir supprimer toutes tes données (historique des émotions, notes, contacts, médicaments, etc.) de cet appareil et redémarrer à zéro ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllUserData(context)
                        Toast.makeText(context, "Toutes vos données ont été définitivement supprimées.", Toast.LENGTH_SHORT).show()
                        showDeleteDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer toutes mes données")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Modal to Edit QCM selections
    if (showEditQcmDialog) {
        // State for multiple disorders if editQcmIndex is 2
        var tempDisorders by remember { 
            mutableStateOf(
                if (editQcmIndex == 2) {
                    tempSelection.split(", ").filter { it.isNotBlank() }.toSet()
                } else {
                    emptySet()
                }
            )
        }

        val options = when (editQcmIndex) {
            1 -> listOf(
                "Gérer mes émotions au quotidien",
                "Désamorcer mes crises aiguës",
                "Suivre et comprendre mon humeur",
                "Discuter avec l'IA Norra"
            )
            2 -> listOf(
                "Autisme (TSA / Asperger)",
                "Trouble Borderline / TPL",
                "Trouble Anxieux / Panique",
                "Dépression / Cyclothymie",
                "Aucun diagnostic particulier"
            )
            else -> listOf(
                "Un médecin ou psychologue",
                "Un proche ou un ami",
                "Réseaux sociaux ou Internet",
                "Recherche personnelle"
            )
        }

        AlertDialog(
            onDismissRequest = { showEditQcmDialog = false },
            title = {
                Text(
                    text = when (editQcmIndex) {
                        1 -> "Objectif de soutien"
                        2 -> "Défis de santé déclarés"
                        else -> "Découverte de l'application"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { opt ->
                        val isSelected = if (editQcmIndex == 2) {
                            tempDisorders.contains(opt)
                        } else {
                            tempSelection == opt
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (editQcmIndex == 2) {
                                        if (opt == "Aucun diagnostic particulier") {
                                            tempDisorders = setOf("Aucun diagnostic particulier")
                                        } else {
                                            var next = tempDisorders.filter { it != "Aucun diagnostic particulier" }.toSet()
                                            next = if (next.contains(opt)) {
                                                next - opt
                                            } else {
                                                next + opt
                                            }
                                            tempDisorders = if (next.isEmpty()) setOf("Aucun diagnostic particulier") else next
                                        }
                                        tempSelection = tempDisorders.joinToString(", ")
                                    } else {
                                        tempSelection = opt 
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (editQcmIndex) {
                            1 -> viewModel.updateQcm(tempSelection, diagnosedDisorder, referredBy)
                            2 -> viewModel.updateQcm(whyUseNorra, tempSelection, referredBy)
                            3 -> viewModel.updateQcm(whyUseNorra, diagnosedDisorder, tempSelection)
                        }
                        showEditQcmDialog = false
                    }
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditQcmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// VECTOR LOGOS
// -------------------------------------------------------------

@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val width = size.width
        val height = size.height
        val scaleX = width / 24f
        val scaleY = height / 24f
        
        withTransform({
            scale(scaleX, scaleY, Offset.Zero)
        }) {
            // Red Arc
            val redPath = Path().apply {
                moveTo(12f, 5f)
                cubicTo(13.6f, 5f, 15f, 5.6f, 16.1f, 6.7f)
                lineTo(19.2f, 3.6f)
                cubicTo(17.3f, 1.8f, 14.8f, 1f, 12f, 1f)
                cubicTo(7.3f, 1f, 3.4f, 3.7f, 1.5f, 7.7f)
                lineTo(5.2f, 10.6f)
                cubicTo(6.1f, 7.3f, 8.8f, 5f, 12f, 5f)
                close()
            }
            drawPath(redPath, Color(0xFFEA4335))

            // Green Arc
            val greenPath = Path().apply {
                moveTo(12f, 23f)
                cubicTo(15.2f, 23f, 18f, 21.9f, 20f, 20f)
                lineTo(16.3f, 17.1f)
                cubicTo(15.2f, 17.8f, 13.7f, 18.2f, 12f, 18.2f)
                cubicTo(8.8f, 18.2f, 6.1f, 15.9f, 5.2f, 12.6f)
                lineTo(1.4f, 15.5f)
                cubicTo(3.4f, 19.3f, 7.3f, 23f, 12f, 23f)
                close()
            }
            drawPath(greenPath, Color(0xFF34A853))

            // Yellow Arc
            val yellowPath = Path().apply {
                moveTo(5.2f, 12.7f)
                cubicTo(5f, 12.1f, 4.9f, 11.4f, 4.9f, 10.7f)
                cubicTo(4.9f, 10f, 5f, 9.3f, 5.2f, 8.7f)
                lineTo(1.5f, 5.8f)
                cubicTo(0.5f, 7.6f, 0f, 9.7f, 0f, 12f)
                cubicTo(0f, 14.3f, 0.5f, 16.4f, 1.5f, 18.2f)
                lineTo(5.2f, 15.3f)
                close()
            }
            drawPath(yellowPath, Color(0xFFFBBC05))

            // Blue Arc
            val bluePath = Path().apply {
                moveTo(23f, 12f)
                cubicTo(23f, 11.2f, 22.9f, 10.3f, 22.7f, 9.5f)
                lineTo(12f, 9.5f)
                lineTo(12f, 14.3f)
                lineTo(18.2f, 14.3f)
                cubicTo(17.9f, 15.8f, 17.1f, 17.1f, 15.8f, 18f)
                lineTo(19.5f, 20.9f)
                cubicTo(21.7f, 18.9f, 23f, 15.9f, 23f, 12f)
                close()
            }
            drawPath(bluePath, Color(0xFF4285F4))
        }
    }
}

@Composable
fun AppleLogo(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier.size(18.dp)) {
        val width = size.width
        val height = size.height
        val scaleX = width / 24f
        val scaleY = height / 24f
        
        withTransform({
            scale(scaleX, scaleY, Offset.Zero)
        }) {
            // Leaf
            val leafPath = Path().apply {
                moveTo(15.4f, 2.4f)
                cubicTo(14.4f, 3.6f, 13f, 4.3f, 11.7f, 4.2f)
                cubicTo(11.5f, 2.9f, 12.3f, 1.5f, 13.4f, 0.4f)
                cubicTo(14.4f, -0.7f, 16f, -1.2f, 17.1f, -1f)
                cubicTo(17.2f, 0.3f, 16.5f, 1.6f, 15.4f, 2.4f)
                close()
            }
            drawPath(leafPath, color)

            // Body
            val bodyPath = Path().apply {
                moveTo(17f, 5.5f)
                cubicTo(15.3f, 5.5f, 14.6f, 4.5f, 12.6f, 4.5f)
                cubicTo(10.6f, 4.5f, 9.8f, 5.5f, 8.2f, 5.5f)
                cubicTo(5.2f, 5.5f, 2.4f, 8.2f, 2.4f, 12.7f)
                cubicTo(2.4f, 17.2f, 5.1f, 21.3f, 8f, 21.3f)
                cubicTo(9.6f, 21.3f, 10.1f, 20.3f, 12.3f, 20.3f)
                cubicTo(14.5f, 20.3f, 15f, 21.3f, 16.6f, 21.3f)
                cubicTo(19.5f, 21.3f, 22.2f, 17.2f, 22.2f, 12.7f)
                cubicTo(22.2f, 10.4f, 21.1f, 7.8f, 19.5f, 6.5f)
                cubicTo(18.7f, 5.9f, 17.8f, 5.5f, 17f, 5.5f)
                close()
            }
            drawPath(bodyPath, color)
        }
    }
}

// -------------------------------------------------------------
// LOGIN & FIRST ONBOARDING SURVEY WIZARD SCREEN
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginOnboardingScreen(
    viewModel: NorraViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isSurveyMode by remember { mutableStateOf(false) }

    // Auth fields
    var firstName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                com.google.firebase.auth.FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val user = authTask.result?.user
                            val name = user?.displayName ?: "Perrine"
                            val currentEmail = user?.email ?: "perrine@gmail.com"
                            email = currentEmail
                            firstName = name
                            
                            val isNewUser = authTask.result?.additionalUserInfo?.isNewUser == true
                            if (isNewUser) {
                                isSurveyMode = true
                            } else {
                                viewModel.setLoginState(true, currentEmail, name)
                            }
                        } else {
                            Toast.makeText(context, "Erreur Firebase: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Erreur: ID Token est null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            if (e.statusCode == 10) {
                Toast.makeText(context, "Mode simulation (SHA-1 local) : Connexion virtuelle", Toast.LENGTH_LONG).show()
                email = "perrine@gmail.com"
                firstName = "Perrine"
                isSurveyMode = true
            } else {
                Toast.makeText(context, "Erreur Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // QCM steps
    var currentStep by remember { mutableIntStateOf(1) } // 1, 2, 3
    var whyAnswer by remember { mutableStateOf("Gérer mes émotions au quotidien") }
    var disorderAnswer by remember { mutableStateOf("Aucun diagnostic particulier") }
    var multipleDisorders by remember { mutableStateOf(setOf("Aucun diagnostic particulier")) }
    var referredAnswer by remember { mutableStateOf("Recherche personnelle") }

    if (!isSurveyMode) {
        // Landing and Standard Sign-In Screen
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NorraCharacter(
                        emotion = NorraEmotion.DEFAULT,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Bienvenue sur Norra",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Ton assistant IA bienveillant d'accompagnement émotionnel.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Form container
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (isSignUp) "Créer ton compte" else "Te connecter",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Adresse E-mail") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Mot de passe") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                            )

                            if (isSignUp) {
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it },
                                    label = { Text("Prénom") },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            
                            if (!isSignUp) {
                                TextButton(
                                    onClick = {
                                        if (email.isNotBlank()) {
                                            com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        Toast.makeText(context, "Lien de réinitialisation envoyé à $email", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        } else {
                                            Toast.makeText(context, "Veuillez renseigner votre email", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Mot de passe oublié ?", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        if (isSignUp) {
                                            if (firstName.isNotBlank()) {
                                                com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                                .setDisplayName(firstName)
                                                                .build()
                                                            task.result?.user?.updateProfile(profileUpdates)
                                                            isSurveyMode = true
                                                        } else {
                                                            Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            } else {
                                                Toast.makeText(context, "Veuillez entrer votre prénom", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val user = task.result?.user
                                                        val name = user?.displayName ?: "Perrine"
                                                        viewModel.setLoginState(true, email, name)
                                                    } else {
                                                        Toast.makeText(context, "Erreur: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        }
                                    } else {
                                        Toast.makeText(context, "Veuillez remplir email et mot de passe.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isSignUp) "Suivant (Enregistrement)" else "Se connecter")
                            }

                            TextButton(
                                onClick = { isSignUp = !isSignUp },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    if (isSignUp) "Déjà inscrit ? Connecte-toi" 
                                    else "Nouveau ici ? Crée un compte gratuit"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "— OU CONTINUER AVEC —",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google & Apple Login Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(com.example.R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()
                                val client = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(client.signInIntent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            GoogleLogo()
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continuer avec Google")
                        }

                        // Apple Login Removed For Now
                        
                        TextButton(
                            onClick = {
                                email = "invite@demo.com"
                                isSurveyMode = true
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Accéder en mode anonyme et gratuit")
                        }
                    }
                }
            }
        }
    } else {
        // Multi-step QCM onboarding Survey Screen
        val stepsHeader = when (currentStep) {
            1 -> "Objectifs de soutien"
            2 -> "Troubles de santé"
            else -> "Trouver Norra"
        }
        val currentQuestion = when (currentStep) {
            1 -> "Pourquoi veux-tu utiliser l'application Norra ?"
            2 -> "Quel trouble ou sentiment d'urgence as-tu déjà déclaré/diagnostiqué ?"
            else -> "Comment as-tu entendu parler de Norra ?"
        }
        val options = when (currentStep) {
            1 -> listOf(
                "Gérer mes émotions au quotidien",
                "Désamorcer mes crises aiguës",
                "Suivre et comprendre mon humeur",
                "Discuter avec l'IA Norra"
            )
            2 -> listOf(
                "Autisme (TSA / Asperger)",
                "Trouble Borderline / TPL",
                "Trouble Anxieux / Panique",
                "Dépression / Cyclothymie",
                "Aucun diagnostic particulier"
            )
            else -> listOf(
                "Un médecin ou psychologue",
                "Un proche ou un ami",
                "Réseaux sociaux ou Internet",
                "Recherche personnelle"
            )
        }
        val selectedOption = when (currentStep) {
            1 -> whyAnswer
            2 -> disorderAnswer
            else -> referredAnswer
        }

        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Stepper bar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stepsHeader, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("$currentStep / 3", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 1..3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (i <= currentStep) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = currentQuestion,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Survey options list
                    options.forEach { opt ->
                        val isSel = if (currentStep == 2) {
                            multipleDisorders.contains(opt)
                        } else {
                            selectedOption == opt
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (currentStep == 2) {
                                        if (opt == "Aucun diagnostic particulier") {
                                            multipleDisorders = setOf("Aucun diagnostic particulier")
                                        } else {
                                            var next = multipleDisorders.filter { it != "Aucun diagnostic particulier" }.toSet()
                                            next = if (next.contains(opt)) {
                                                next - opt
                                            } else {
                                                next + opt
                                            }
                                            multipleDisorders = if (next.isEmpty()) setOf("Aucun diagnostic particulier") else next
                                        }
                                        disorderAnswer = multipleDisorders.joinToString(", ")
                                    } else {
                                        when (currentStep) {
                                            1 -> whyAnswer = opt
                                            3 -> referredAnswer = opt
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp, 
                                if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSel) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Control panel buttons (Back/Continuer)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("Retour")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                // Finalize Onboarding Survey
                                viewModel.updateQcm(whyAnswer, disorderAnswer, referredAnswer)
                                val finalName = firstName.ifBlank { "Perrine" }
                                viewModel.setLoginState(true, email.ifBlank { "perrine@gmail.com" }, finalName)
                                Toast.makeText(context, "Inscription finalisée !", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(if (currentStep == 3) "Terminer" else "Continuer")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsychApptCard(viewModel: NorraViewModel) {
    val context = LocalContext.current
    val psyAppointmentDate by viewModel.psyAppointmentDate.collectAsState()
    val psyNotesListJson by viewModel.psyNotesListJson.collectAsState()
    val psyNotesExpanded by viewModel.psyNotesExpanded.collectAsState()

    val notesList = remember(psyNotesListJson) {
        try {
            val array = org.json.JSONArray(psyNotesListJson)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    var newNoteText by remember { mutableStateOf("") }

    val displayDate = remember(psyAppointmentDate) {
        try {
            if (psyAppointmentDate.isBlank()) ""
            else {
                val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                val date = sdfInput.parse(psyAppointmentDate)
                if (date != null) {
                    val sdfOutput = java.text.SimpleDateFormat("EEEE d MMMM 'à' HH:mm", java.util.Locale.FRENCH)
                    val formatted = sdfOutput.format(date)
                    formatted.substring(0, 1).uppercase(java.util.Locale.FRENCH) + formatted.substring(1)
                } else {
                    psyAppointmentDate
                }
            }
        } catch (e: Exception) {
            psyAppointmentDate
        }
    }

    // Picker Dialog Setup
    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            android.app.TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedCal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.YEAR, year)
                        set(java.util.Calendar.MONTH, month)
                        set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                        set(java.util.Calendar.MINUTE, minute)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    val formatted = sdf.format(selectedCal.time)
                    viewModel.savePsyAppointment(context, formatted)
                    Toast.makeText(context, "Rendez-vous programmé et rappels planifiés ! 🌸", Toast.LENGTH_LONG).show()
                },
                calendar.get(java.util.Calendar.HOUR_OF_DAY),
                calendar.get(java.util.Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .testTag("psy_appt_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setPsyNotesExpanded(!psyNotesExpanded) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Rdv psy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Suivi & RDV chez mon Psy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (psyAppointmentDate.isBlank()) "Aucun rendez-vous noté" else "Prochain RDV : $displayDate",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Icon(
                    imageVector = if (psyNotesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (psyNotesExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // Schedule Section
                Text(
                    text = "Mon Prochain RDV",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (psyAppointmentDate.isBlank()) {
                    Button(
                        onClick = { datePickerDialog.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Noter mon RDV Psy", fontSize = 13.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayDate,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { datePickerDialog.show() }) {
                                Text("Modifier", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { 
                                    viewModel.savePsyAppointment(context, "") 
                                    Toast.makeText(context, "Rappel annulé", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Supprimer", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Text(
                        text = "🔔 Norra te rappellera ton RDV 15j, 7j, 1j et 2h avant avec tes notes.",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // Notes Section
                Text(
                    text = "Ce que je souhaite lui dire 📝",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Note tes humeurs, des crises, ou des pensées récurrentes à aborder lors de ta prochaine séance.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Add note row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        placeholder = { Text("Une idée, un ressenti à aborder...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        maxLines = 2
                    )
                    IconButton(
                        onClick = {
                            if (newNoteText.isNotBlank()) {
                                val newList = notesList.toMutableList().apply { add(newNoteText.trim()) }
                                viewModel.savePsyNotesListJson(org.json.JSONArray(newList).toString())
                                newNoteText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes List
                if (notesList.isEmpty()) {
                    Text(
                        text = "Aucune note pour le moment. Ajoute des choses ci-dessus !",
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    notesList.forEachIndexed { index, note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = note,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    val newList = notesList.toMutableList().apply { removeAt(index) }
                                    viewModel.savePsyNotesListJson(org.json.JSONArray(newList).toString())
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
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
fun CrisisHistoryScreen(viewModel: NorraViewModel, modifier: Modifier = Modifier) {
    val crisisHistory by viewModel.crisisHistory.collectAsState()
    
    // Stats computation
    val sosCount = crisisHistory.count { it.exerciseId == "SOS" }
    
    val allPossible = listOf(
        Triple("bubble_burst", "Éclate ta Pensée", Icons.Default.Delete),
        Triple("tactile_breathe", "Respiration Tactile", Icons.Default.Refresh),
        Triple("box_breathing", "Respiration Carrée", Icons.Default.CheckCircle),
        Triple("breath", "Cohérence Cardiaque N.", Icons.Default.Refresh),
        Triple("panic_sos", "SOS Attaque", Icons.Default.Warning),
        Triple("grounding", "Ancrage (5-4)", Icons.Default.Star),
        Triple("tipp", "Méthode TIPP", Icons.Default.Notifications),
        Triple("stop", "Technique STOP", Icons.Default.Warning),
        Triple("bpd_anger", "Action Opposée", Icons.Default.Build),
        Triple("sensory_detox", "Apaisement Sensoriel", Icons.Default.Face),
        Triple("affirmation", "Phrases d'Ancrage", Icons.Default.Favorite),
        Triple("jacobson", "Relaxation Jacobson", Icons.Default.Build),
        Triple("butterfly", "Hug du Papillon", Icons.Default.Favorite),
        Triple("safe_place", "Lieu Sûr", Icons.Default.Home)
    )

    val getExerciseName = { id: String ->
        if (id == "SOS") "Bouton SOS" else allPossible.find { it.first == id }?.second ?: id
    }
    
    val getExerciseCount = { id: String ->
        crisisHistory.count { it.exerciseId == id }
    }
    
    val getExerciseSuccessRate = { id: String ->
        val exercised = crisisHistory.filter { it.exerciseId == id }
        if (exercised.isEmpty()) 0 else (exercised.count { it.worked } * 100) / exercised.size
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Vue d'ensemble", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Total des alertes SOS : $sosCount", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cet historique vous permet de suivre l'efficacité des exercices Norra sur vos crises.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        // Success Stats Section
        if (crisisHistory.any { it.exerciseId != "SOS" }) {
            item {
                Text("Efficacité de vos exercices", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            
            val uniqueExercises = crisisHistory.filter { it.exerciseId != "SOS" }.map { it.exerciseId }.distinct()
            items(uniqueExercises) { exId ->
                val rate = getExerciseSuccessRate(exId)
                val count = getExerciseCount(exId)
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(getExerciseName(exId), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Testé $count fois", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Text(
                            text = "$rate% d'efficacité",
                            fontWeight = FontWeight.Bold,
                            color = if (rate >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Historique détaillé", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
        
        if (crisisHistory.isEmpty()) {
            item {
                Text("Aucun historique pour l'instant.", modifier = Modifier.padding(top = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(crisisHistory) { log ->
                val timeText = remember(log.timestamp) {
                    val date = java.util.Date(log.timestamp)
                    val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    format.format(date)
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(getExerciseName(log.exerciseId), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(timeText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (log.exerciseId != "SOS") {
                            if (log.worked) {
                                Icon(Icons.Default.CheckCircle, "A fonctionné", tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Icon(Icons.Default.Close, "N'a pas fonctionné", tint = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Icon(Icons.Default.Warning, "SOS", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
