package com.titoodev.h_gym.data.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.titoodev.h_gym.data.local.database.HGymDatabase
import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.local.entities.UserEntity
import com.titoodev.h_gym.data.local.entities.WorkoutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object CloudSyncManager {

    /**
     * Uploads local user profile, exercises, workouts, and body measurements to Cloud Firestore under the user's uid.
     */
    suspend fun syncLocalDataToCloud(context: Context) = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return@withContext
        val uid = currentUser.uid

        val db = HGymDatabase.getDatabase(context)
        val quotaMetrics = db.quotaDao().getQuotaMetrics().firstOrNull()

        if (!QuotaManager.isWriteSyncAllowed(quotaMetrics)) return@withContext

        val firestore = FirebaseFirestore.getInstance()

        try {
            // 1. Sync User Profile
            val user = db.userDao().getUser().firstOrNull()
            if (user != null) {
                val userMap = mapOf(
                    "uid" to user.uid,
                    "name" to user.name,
                    "email" to user.email,
                    "photoUrl" to (user.photoUrl ?: ""),
                    "age" to (user.age ?: 0),
                    "sex" to (user.sex ?: ""),
                    "height" to (user.height ?: 0f),
                    "currentWeight" to (user.currentWeight ?: 0f),
                    "goals" to user.goals,
                    "experienceLevel" to user.experienceLevel,
                    "trainingFrequency" to user.trainingFrequency,
                    "preferredTrainingType" to user.preferredTrainingType,
                    "updatedAt" to user.updatedAt
                )
                firestore.collection("users").document(uid).set(userMap).await()
                QuotaManager.recordOperation(db.quotaDao(), writes = 1)
            }

            // 2. Sync Exercises
            val exercises = db.exerciseDao().getAllExercises().firstOrNull() ?: emptyList()
            for (e in exercises) {
                val exMap = mapOf(
                    "id" to e.id,
                    "name" to e.name,
                    "muscleGroup" to e.muscleGroup,
                    "secondaryMuscles" to (e.secondaryMuscles ?: ""),
                    "description" to (e.description ?: ""),
                    "equipment" to (e.equipment ?: ""),
                    "exerciseType" to e.exerciseType,
                    "isCustom" to e.isCustom
                )
                firestore.collection("users").document(uid)
                    .collection("exercises").document(e.id.toString()).set(exMap).await()
                QuotaManager.recordOperation(db.quotaDao(), writes = 1)
            }

            // 3. Sync Workouts
            val workouts = db.workoutDao().getAllWorkouts().firstOrNull() ?: emptyList()
            for (w in workouts) {
                val workoutMap = mapOf(
                    "id" to w.id,
                    "title" to w.title,
                    "date" to w.date,
                    "durationSeconds" to w.durationSeconds,
                    "notes" to (w.notes ?: "")
                )
                firestore.collection("users").document(uid)
                    .collection("workouts").document(w.id.toString()).set(workoutMap).await()
                QuotaManager.recordOperation(db.quotaDao(), writes = 1)
            }

            // 4. Sync Body Measurements
            val measurements = db.bodyMeasurementDao().getAllMeasurements().firstOrNull() ?: emptyList()
            for (m in measurements) {
                val mMap = mapOf(
                    "id" to m.id,
                    "weight" to m.weight,
                    "chest" to (m.chest ?: 0f),
                    "waist" to (m.waist ?: 0f),
                    "hips" to (m.hips ?: 0f),
                    "arm" to (m.arm ?: 0f),
                    "thigh" to (m.thigh ?: 0f),
                    "bodyFatPercentage" to (m.bodyFatPercentage ?: 0f),
                    "timestamp" to m.timestamp
                )
                firestore.collection("users").document(uid)
                    .collection("measurements").document(m.id.toString()).set(mMap).await()
                QuotaManager.recordOperation(db.quotaDao(), writes = 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Downloads user profile, exercises, workouts, and measurements from Cloud Firestore for the authenticated user uid
     * and inserts them into local Room database so multi-device persistence is seamlessly synchronized.
     */
    suspend fun fetchCloudDataToLocal(context: Context) = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return@withContext
        val uid = currentUser.uid

        val db = HGymDatabase.getDatabase(context)
        val quotaMetrics = db.quotaDao().getQuotaMetrics().firstOrNull()

        if (!QuotaManager.isReadSyncAllowed(quotaMetrics)) return@withContext

        val firestore = FirebaseFirestore.getInstance()

        try {
            // 1. Fetch User Profile
            val userDoc = firestore.collection("users").document(uid).get().await()
            QuotaManager.recordOperation(db.quotaDao(), reads = 1)
            if (userDoc != null && userDoc.exists()) {
                val userEntity = UserEntity(
                    uid = userDoc.getString("uid") ?: uid,
                    name = userDoc.getString("name") ?: "Atleta",
                    email = userDoc.getString("email") ?: currentUser.email ?: "",
                    photoUrl = userDoc.getString("photoUrl"),
                    age = userDoc.getLong("age")?.toInt(),
                    sex = userDoc.getString("sex"),
                    height = userDoc.getDouble("height")?.toFloat(),
                    currentWeight = userDoc.getDouble("currentWeight")?.toFloat(),
                    goals = userDoc.getString("goals") ?: "",
                    experienceLevel = userDoc.getString("experienceLevel") ?: "Intermedio",
                    trainingFrequency = userDoc.getLong("trainingFrequency")?.toInt() ?: 4,
                    preferredTrainingType = userDoc.getString("preferredTrainingType") ?: "Fuerza",
                    updatedAt = userDoc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
                db.userDao().insertUser(userEntity)
            }

            // 2. Fetch Exercises
            val exercisesSnapshot = firestore.collection("users").document(uid)
                .collection("exercises").get().await()
            if (exercisesSnapshot != null) {
                QuotaManager.recordOperation(db.quotaDao(), reads = exercisesSnapshot.size().coerceAtLeast(1))
                for (doc in exercisesSnapshot.documents) {
                    val ex = ExerciseEntity(
                        id = doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: 0L,
                        name = doc.getString("name") ?: "Ejercicio",
                        muscleGroup = doc.getString("muscleGroup") ?: "General",
                        secondaryMuscles = doc.getString("secondaryMuscles"),
                        description = doc.getString("description"),
                        equipment = doc.getString("equipment"),
                        exerciseType = doc.getString("exerciseType") ?: "Fuerza",
                        isCustom = doc.getBoolean("isCustom") ?: true
                    )
                    db.exerciseDao().insertExercise(ex)
                }
            }

            // 3. Fetch Workouts
            val workoutsSnapshot = firestore.collection("users").document(uid)
                .collection("workouts").get().await()
            if (workoutsSnapshot != null) {
                QuotaManager.recordOperation(db.quotaDao(), reads = workoutsSnapshot.size().coerceAtLeast(1))
                for (doc in workoutsSnapshot.documents) {
                    val w = WorkoutEntity(
                        id = doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: 0L,
                        title = doc.getString("title") ?: "Entrenamiento",
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        durationSeconds = doc.getLong("durationSeconds") ?: 0L,
                        notes = doc.getString("notes")
                    )
                    db.workoutDao().insertWorkout(w)
                }
            }

            // 4. Fetch Body Measurements
            val measurementsSnapshot = firestore.collection("users").document(uid)
                .collection("measurements").get().await()
            if (measurementsSnapshot != null) {
                QuotaManager.recordOperation(db.quotaDao(), reads = measurementsSnapshot.size().coerceAtLeast(1))
                for (doc in measurementsSnapshot.documents) {
                    val m = BodyMeasurementEntity(
                        id = doc.getString("id")?.toLongOrNull() ?: doc.id.toLongOrNull() ?: 0L,
                        uid = uid,
                        weight = doc.getDouble("weight")?.toFloat() ?: 70f,
                        chest = doc.getDouble("chest")?.toFloat(),
                        waist = doc.getDouble("waist")?.toFloat(),
                        hips = doc.getDouble("hips")?.toFloat(),
                        arm = doc.getDouble("arm")?.toFloat(),
                        thigh = doc.getDouble("thigh")?.toFloat(),
                        bodyFatPercentage = doc.getDouble("bodyFatPercentage")?.toFloat(),
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                    db.bodyMeasurementDao().insertMeasurement(m)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
