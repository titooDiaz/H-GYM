package com.titoodev.h_gym.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.titoodev.h_gym.data.local.entities.UserProfileHistoryEntity
import com.titoodev.h_gym.ui.components.DuolingoButton
import com.titoodev.h_gym.ui.components.DuolingoCard
import com.titoodev.h_gym.ui.components.DuolingoRed
import com.titoodev.h_gym.ui.components.DuolingoRedDark
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val haptics = rememberHGymHaptics()
    val context = LocalContext.current

    val userState by viewModel.userState.collectAsState()
    val latestMeasurement by viewModel.latestMeasurementState.collectAsState()
    val profileHistory by viewModel.profileHistoryState.collectAsState()

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val userName = userState?.name ?: firebaseUser?.displayName ?: "Atleta H-GYM"
    val userEmail = userState?.email ?: firebaseUser?.email ?: "Modo offline / Invitado"
    val currentWeightVal = latestMeasurement?.weight ?: userState?.currentWeight

    var newWeightInput by remember { mutableStateOf("") }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = userEmail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

                    if (userState != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = "Nivel: ${userState!!.experienceLevel}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    text = "Estilo: ${userState!!.preferredTrainingType}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Current Weight & Measurement Action Card
        item {
            DuolingoCard {
                Column(modifier = Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Peso Corporal Registrado", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = {
                            haptics.performClick()
                            newWeightInput = currentWeightVal?.toString() ?: ""
                            showWeightDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Actualizar Peso", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentWeightVal != null) "$currentWeightVal kg" else "Sin peso registrado aún",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Al registrar un nuevo peso se añade una entrada histórica inmutable en Room sin sobrescribir marcas anteriores.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Export Data & Account Actions
        item {
            DuolingoCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Gestión de Datos y Privacidad", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                    Button(
                        onClick = {
                            haptics.performSuccess()
                            viewModel.exportData { file ->
                                Toast.makeText(context, "Copia local exportada en: ${file.name}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar Copia de Seguridad JSON")
                    }

                    Button(
                        onClick = {
                            haptics.performSuccess()
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cerrar Sesión")
                    }

                    OutlinedButton(
                        onClick = {
                            haptics.performClick()
                            showDeleteAccountDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DuolingoRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = DuolingoRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar Cuenta y Datos Locales", color = DuolingoRed)
                    }
                }
            }
        }

        // Profile Audit History
        if (profileHistory.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Historial de Cambios de Perfil", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            items(profileHistory) { historyItem ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        Text(text = sdf.format(Date(historyItem.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(text = "${historyItem.fieldName}: ${historyItem.previousValue ?: "N/A"} ➔ ${historyItem.newValue}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Weight Update Dialog
    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("Registrar Nuevo Peso Corporal", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newWeightInput,
                    onValueChange = { newWeightInput = it },
                    label = { Text("Peso en kg") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                DuolingoButton(
                    text = "Guardar",
                    onClick = {
                        val w = newWeightInput.toFloatOrNull()
                        if (w != null && w > 0f) {
                            haptics.performSuccess()
                            viewModel.updateWeight(w)
                            showWeightDialog = false
                        }
                    },
                    modifier = Modifier.width(120.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("¿Eliminar Cuenta y Todos tus Datos?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Esta acción purgará tu base de datos local Room, tus fotos locales y tu perfil. Esta operación no se puede deshacer.")
            },
            confirmButton = {
                DuolingoButton(
                    text = "Eliminar Todo",
                    onClick = {
                        viewModel.deleteAccount {
                            showDeleteAccountDialog = false
                            onSignOut()
                        }
                    },
                    containerColor = DuolingoRed,
                    shadowColor = DuolingoRedDark,
                    modifier = Modifier.width(150.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
