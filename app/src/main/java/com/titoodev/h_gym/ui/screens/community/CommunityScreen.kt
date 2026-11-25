package com.titoodev.h_gym.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.ui.components.rememberHGymHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen() {
    val haptics = rememberHGymHaptics()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { HGymDatabase.getDatabase(context) }

    val userState by db.userDao().getUser().collectAsState(initial = null)
    val workoutsState by db.workoutDao().getAllWorkouts().collectAsState(initial = emptyList())

    var groupCodeInput by remember { mutableStateOf("") }
    var activeGroupName by remember { mutableStateOf<String?>(null) }
    var isJoined by remember { mutableStateOf(false) }

    val totalWorkoutsCount = workoutsState.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Group Join Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (isJoined) "Grupo Activo: ${activeGroupName ?: "Atletas H-GYM"}" else "Unirse a un Grupo Deportivo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!isJoined) {
                            Text(
                                text = "Ingresa el código de invitación de tu gimnasio o grupo de entrenamiento para comparar nivel de constancia.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = groupCodeInput,
                                onValueChange = { groupCodeInput = it },
                                label = { Text("Código de grupo (ej. HGYM-POWER)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (groupCodeInput.isNotBlank()) {
                                        haptics.performSuccess()
                                        activeGroupName = groupCodeInput.uppercase()
                                        isJoined = true

                                        // Connect to Firestore if authenticated
                                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                                        if (firebaseUser != null) {
                                            coroutineScope.launch {
                                                val firestore = FirebaseFirestore.getInstance()
                                                val groupMemberData = hashMapOf(
                                                    "uid" to firebaseUser.uid,
                                                    "name" to (userState?.name ?: "Atleta"),
                                                    "workoutsCount" to totalWorkoutsCount,
                                                    "joinedAt" to System.currentTimeMillis()
                                                )
                                                firestore.collection("groups")
                                                    .document(groupCodeInput.uppercase())
                                                    .collection("members")
                                                    .document(firebaseUser.uid)
                                                    .set(groupMemberData)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Unirse al Grupo", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "¡Perteneces al grupo $activeGroupName! Tus sesiones completadas son acumuladas para el ranking mensual de constancia.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    haptics.performClick()
                                    isJoined = false
                                    activeGroupName = null
                                    groupCodeInput = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Salir del Grupo")
                            }
                        }
                    }
                }
            }

            // Community Challenges Card
            item {
                Text(
                    text = "Retos Activos de la Comunidad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Card(
                    onClick = { haptics.performSuccess() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(text = "Reto Constancia 16 Sesiones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Completa al menos 16 entrenamientos durante este mes.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(12.dp))

                        val progressFactor = (totalWorkoutsCount.toFloat() / 16f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressFactor },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Tu progreso real: $totalWorkoutsCount / 16 entrenamientos (${(progressFactor * 100).toInt()}%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            // Privacy Guarantee Notice
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(text = "Garantía de Privacidad H-GYM", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tus registros de alimentación, calorías, macros, peso corporal y fotografías personales permanecen 100% privados en la memoria interna de tu dispositivo. Jamás se transmiten a los grupos ni a la comunidad.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
}
