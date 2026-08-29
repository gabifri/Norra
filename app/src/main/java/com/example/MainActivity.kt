package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import com.example.data.DatabaseProvider
import com.example.data.NorraRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private lateinit var viewModel: NorraViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channelId = "medication_channel"
        val channel = android.app.NotificationChannel(
            channelId,
            "Rappels Médicaments",
            android.app.NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Rappels pour prendre vos médicaments"
        }
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    // Initialize Room database & repository
    val database = DatabaseProvider.getDatabase(this)
    val repository = NorraRepository(database.norraDao())
    val sharedPreferences = getSharedPreferences("norra_prefs", android.content.Context.MODE_PRIVATE)
    val factory = NorraViewModelFactory(repository, sharedPreferences)
    viewModel = ViewModelProvider(this, factory)[NorraViewModel::class.java]

    val gotoScreen = intent.getStringExtra("GOTO_SCREEN")
    if (gotoScreen == "psy_notes") {
        viewModel.selectTab(1)
        viewModel.setPsyNotesExpanded(true)
    }

    setContent {
      MyApplicationTheme {
        NorraApp(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    val gotoScreen = intent.getStringExtra("GOTO_SCREEN")
    if (gotoScreen == "psy_notes") {
        viewModel.selectTab(1)
        viewModel.setPsyNotesExpanded(true)
    }
  }
}

@Composable
fun NorraApp(viewModel: NorraViewModel) {
  val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()
  val currentTab by viewModel.currentTab.collectAsState()
  val context = androidx.compose.ui.platform.LocalContext.current
  val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
  var backPressedOnce by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  val activity = (context as? androidx.activity.ComponentActivity)
  
  // Request notifications permission globally
  val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
      contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
  ) { }
  androidx.compose.runtime.LaunchedEffect(Unit) {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
          permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
      }
  }

  // Handle back button presses
  androidx.activity.compose.BackHandler(enabled = isLoggedIn) {
      if (currentTab != 0) {
          viewModel.selectTab(0)
      } else {
          if (backPressedOnce) {
              activity?.finish()
          } else {
              backPressedOnce = true
              android.widget.Toast.makeText(context, "Appuyez une deuxième fois pour quitter", android.widget.Toast.LENGTH_SHORT).show()
              coroutineScope.launch {
                  kotlinx.coroutines.delay(2000L)
                  backPressedOnce = false
              }
          }
      }
  }

  if (!isLoggedIn) {
    com.example.ui.LoginOnboardingScreen(viewModel = viewModel)
  } else {
    val isBetaWelcomeSeen by viewModel.isBetaWelcomeSeen.collectAsState()
    
    if (!isBetaWelcomeSeen) {
        AlertDialog(
            onDismissRequest = { viewModel.setBetaWelcomeSeen() },
            title = { Text("Bienvenue sur Norra (Bêta)") },
            text = { 
                Text("Norra est actuellement en version de test (bêta).\n\nVotre aide est précieuse ! Si vous rencontrez un bug ou un comportement inattendu, veuillez envoyer un rapport de bug via la page Ressources de l'application.\n\nN'oubliez pas également de faire les mises à jour régulièrement pour profiter des dernières corrections.", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setBetaWelcomeSeen() }) {
                    Text("J'ai compris")
                }
            }
        )
    }

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        if (currentTab != 5 && currentTab != 6) {
          NavigationBar(modifier = Modifier.testTag("bottom_nav")) {
            NavigationBarItem(
              selected = currentTab == 0,
              onClick = { viewModel.selectTab(0) },
              icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Accueil") },
              label = { 
                Text(
                  text = "Accueil", 
                  style = MaterialTheme.typography.labelSmall,
                  maxLines = 1,
                  softWrap = false,
                  overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                ) 
              },
              modifier = Modifier.testTag("tab_home")
            )
            NavigationBarItem(
              selected = currentTab == 1,
              onClick = { viewModel.selectTab(1) },
              icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Suivi") },
              label = { 
                Text(
                  text = "Suivi", 
                  style = MaterialTheme.typography.labelSmall,
                  maxLines = 1,
                  softWrap = false,
                  overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                ) 
              },
              modifier = Modifier.testTag("tab_tracker")
            )
            NavigationBarItem(
              selected = currentTab == 2,
              onClick = { viewModel.selectTab(2) },
              icon = { Icon(imageVector = Icons.Default.Face, contentDescription = "Exercices") },
              label = { 
                Text(
                  text = "Exercices", 
                  style = MaterialTheme.typography.labelSmall,
                  maxLines = 1,
                  softWrap = false,
                  overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                ) 
              },
              modifier = Modifier.testTag("tab_exercises")
            )
            NavigationBarItem(
              selected = currentTab == 3,
              onClick = { viewModel.selectTab(3) },
              icon = { Icon(imageVector = Icons.Default.Call, contentDescription = "Contacts") },
              label = { 
                Text(
                  text = "Contacts", 
                  style = MaterialTheme.typography.labelSmall,
                  maxLines = 1,
                  softWrap = false,
                  overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                ) 
              },
              modifier = Modifier.testTag("tab_contacts")
            )
            NavigationBarItem(
              selected = currentTab == 4,
              onClick = { viewModel.selectTab(4) },
              icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Ressources") },
              label = { 
                Text(
                  text = "Ressources", 
                  style = MaterialTheme.typography.labelSmall,
                  maxLines = 1,
                  softWrap = false,
                  overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                ) 
              },
              modifier = Modifier.testTag("tab_resources")
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentTab) {
          0 -> HomeSosScreen(
            viewModel = viewModel, 
            onNavigateToChat = { viewModel.selectTab(5) },
            onNavigateToProfile = { viewModel.selectTab(6) }
          )
          1 -> SuiviScreen(viewModel = viewModel)
          2 -> ExercisesScreen(viewModel = viewModel)
          3 -> ContactsScreen(viewModel = viewModel)
          4 -> ResourcesScreen(viewModel = viewModel)
          5 -> com.example.ui.AiChatScreen(viewModel = viewModel, onBack = { viewModel.selectTab(0) })
          6 -> com.example.ui.ProfileScreen(viewModel = viewModel, onBack = { viewModel.selectTab(0) })
        }
      }
    }
  }
}
