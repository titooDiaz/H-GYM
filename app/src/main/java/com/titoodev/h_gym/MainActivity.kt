package com.titoodev.h_gym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import com.titoodev.h_gym.data.local.entities.UserEntity
import com.titoodev.h_gym.data.local.preferences.ThemePreferences
import com.titoodev.h_gym.data.local.preferences.ThemeSettings
import com.titoodev.h_gym.ui.navigation.HGymMainScreen
import com.titoodev.h_gym.ui.screens.auth.LoginScreen
import com.titoodev.h_gym.ui.screens.auth.RegistrationWizardScreen
import com.titoodev.h_gym.ui.theme.HGYMTheme
import com.titoodev.h_gym.ui.theme.ThemeVariant
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var themePreferences: ThemePreferences

    private var isAuthenticatedState by mutableStateOf(false)
    private var isProfileCompletedState by mutableStateOf(false)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { taskResult ->
                        if (taskResult.isSuccessful) {
                            isAuthenticatedState = true
                        }
                    }
            }
        } catch (_: ApiException) {
            isAuthenticatedState = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        themePreferences = ThemePreferences(applicationContext)
        isAuthenticatedState = auth.currentUser != null

        val db = HGymDatabase.getDatabase(applicationContext)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            val themeSettings by themePreferences.themeSettingsFlow.collectAsState(
                initial = ThemeSettings(ThemeVariant.PINK, true)
            )
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(isAuthenticatedState) {
                if (isAuthenticatedState) {
                    db.userDao().getUser().collect { user ->
                        isProfileCompletedState = user != null
                    }
                }
            }

            HGYMTheme(
                darkTheme = themeSettings.isDarkMode,
                themeVariant = themeSettings.variant
            ) {
                when {
                    !isAuthenticatedState -> {
                        LoginScreen(
                            onGoogleSignInClicked = {
                                val signInIntent = googleSignInClient.signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            },
                            onGuestLoginClicked = {
                                isAuthenticatedState = true
                            }
                        )
                    }
                    !isProfileCompletedState -> {
                        val currentUser = auth.currentUser
                        RegistrationWizardScreen(
                            initialName = currentUser?.displayName ?: "Atleta",
                            initialEmail = currentUser?.email ?: "",
                            onRegistrationCompleted = { name, age, sex, height, weight, goals, expLevel, freq, trainType, chest, waist, hips, arm, thigh, bodyFat ->
                                coroutineScope.launch {
                                    val now = System.currentTimeMillis()
                                    val userEntity = UserEntity(
                                        uid = currentUser?.uid ?: "local_guest",
                                        name = name,
                                        email = currentUser?.email ?: "guest@hgym.app",
                                        photoUrl = currentUser?.photoUrl?.toString(),
                                        age = age,
                                        sex = sex,
                                        height = height,
                                        currentWeight = weight,
                                        goals = goals.joinToString(","),
                                        experienceLevel = expLevel,
                                        trainingFrequency = freq,
                                        preferredTrainingType = trainType,
                                        updatedAt = now
                                    )
                                    db.userDao().insertUser(userEntity)

                                    val initialMeasurement = BodyMeasurementEntity(
                                        uid = currentUser?.uid ?: "local_guest",
                                        weight = weight,
                                        chest = chest,
                                        waist = waist,
                                        hips = hips,
                                        arm = arm,
                                        thigh = thigh,
                                        bodyFatPercentage = bodyFat,
                                        timestamp = now
                                    )
                                    db.bodyMeasurementDao().insertMeasurement(initialMeasurement)
                                    isProfileCompletedState = true
                                }
                            }
                        )
                    }
                    else -> {
                        HGymMainScreen(
                            onSignOut = {
                                auth.signOut()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    isAuthenticatedState = false
                                    isProfileCompletedState = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
