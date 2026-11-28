package com.titoodev.h_gym

import android.content.Context
import android.os.Bundle
import android.widget.Toast
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
import com.titoodev.h_gym.util.ImageUtils
import kotlinx.coroutines.launch
import java.util.UUID

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
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isAnonymous) {
                    currentUser.linkWithCredential(credential)
                        .addOnCompleteListener(this) { linkTask ->
                            if (linkTask.isSuccessful) {
                                isAuthenticatedState = true
                            } else {
                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener(this) { signInTask ->
                                        if (signInTask.isSuccessful) {
                                            isAuthenticatedState = true
                                        }
                                    }
                            }
                        }
                } else {
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { taskResult ->
                            if (taskResult.isSuccessful) {
                                isAuthenticatedState = true
                            } else {
                                Toast.makeText(this, "Error de autenticación con Google", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Error en inicio de sesión (${e.statusCode})", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOrCreateGuestUid(): String {
        val prefs = getSharedPreferences("hgym_guest_prefs", Context.MODE_PRIVATE)
        var guestUid = prefs.getString("guest_uid", null)
        if (guestUid == null) {
            guestUid = "guest_" + UUID.randomUUID().toString()
            prefs.edit().putString("guest_uid", guestUid).apply()
        }
        return guestUid
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
                    val appContext = applicationContext
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        com.titoodev.h_gym.data.sync.CloudSyncManager.fetchCloudDataToLocal(appContext)
                        com.titoodev.h_gym.data.sync.CloudSyncManager.syncLocalDataToCloud(appContext)
                    }

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
                                auth.signInAnonymously().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        isAuthenticatedState = true
                                    } else {
                                        isAuthenticatedState = true
                                    }
                                }
                            }
                        )
                    }
                    !isProfileCompletedState -> {
                        val currentUser = auth.currentUser
                        val effectiveUid = currentUser?.uid ?: getOrCreateGuestUid()
                        val effectiveEmail = currentUser?.email ?: "guest@hgym.app"

                        RegistrationWizardScreen(
                            initialName = currentUser?.displayName ?: "Atleta Invitado",
                            initialEmail = effectiveEmail,
                            onRegistrationCompleted = { name, birthTimestamp, sex, height, weight, goals, expLevel, trainingDays, trainType, photoUri, chest, waist, hips, arm, forearm, thigh, calf, bodyFat ->
                                coroutineScope.launch {
                                    val now = System.currentTimeMillis()
                                    var photoPath: String? = null
                                    if (photoUri != null) {
                                        val compressedFile = ImageUtils.compressImageTo100Kb(applicationContext, photoUri)
                                        photoPath = compressedFile?.absolutePath
                                    }

                                    val userEntity = UserEntity(
                                        uid = effectiveUid,
                                        name = name,
                                        email = effectiveEmail,
                                        photoUrl = photoPath ?: currentUser?.photoUrl?.toString(),
                                        birthDateTimestamp = birthTimestamp,
                                        sex = sex,
                                        height = height,
                                        currentWeight = weight,
                                        goals = goals.joinToString(","),
                                        experienceLevel = expLevel,
                                        trainingFrequency = trainingDays.split(",").size.coerceAtLeast(1),
                                        preferredTrainingType = trainType,
                                        updatedAt = now
                                    )
                                    db.userDao().insertUser(userEntity)

                                    if (weight != null && weight > 0f) {
                                        val initialMeasurement = BodyMeasurementEntity(
                                            uid = effectiveUid,
                                            weight = weight,
                                            chest = chest,
                                            waist = waist,
                                            hips = hips,
                                            arm = arm,
                                            forearm = forearm,
                                            thigh = thigh,
                                            calf = calf,
                                            bodyFatPercentage = bodyFat,
                                            timestamp = now
                                        )
                                        db.bodyMeasurementDao().insertMeasurement(initialMeasurement)
                                    }
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
