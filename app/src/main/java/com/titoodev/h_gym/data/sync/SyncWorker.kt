package com.titoodev.h_gym.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.titoodev.h_gym.data.local.database.HGymDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = HGymDatabase.getDatabase(applicationContext)
        val quotaMetrics = db.quotaDao().getQuotaMetrics().firstOrNull()

        // Preventive freeze if quota threshold reached
        if (!QuotaManager.isWriteSyncAllowed(quotaMetrics)) {
            return@withContext Result.success()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return@withContext Result.success()

        try {
            val firestore = FirebaseFirestore.getInstance()
            val userEntity = db.userDao().getUser().firstOrNull()

            if (userEntity != null) {
                val userDataMap = hashMapOf(
                    "uid" to currentUser.uid,
                    "name" to userEntity.name,
                    "goals" to userEntity.goals,
                    "experienceLevel" to userEntity.experienceLevel,
                    "trainingFrequency" to userEntity.trainingFrequency,
                    "updatedAt" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(userDataMap)

                // Record 1 write operation in Quota Tracker
                QuotaManager.recordOperation(db.quotaDao(), writes = 1)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
