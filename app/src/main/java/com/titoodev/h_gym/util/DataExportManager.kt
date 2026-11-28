package com.titoodev.h_gym.util

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.titoodev.h_gym.data.local.database.HGymDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object DataExportManager {

    /**
     * Exports all local Room database contents (User profile, Measurements, Workouts, Exercises, Food Logs)
     * into a JSON file in the app's files directory for user backup/export.
     */
    suspend fun exportUserDataToJson(context: Context): File = withContext(Dispatchers.IO) {
        val db = HGymDatabase.getDatabase(context)

        val user = db.userDao().getUser().firstOrNull()
        val measurements = db.bodyMeasurementDao().getAllMeasurements().firstOrNull() ?: emptyList()
        val workouts = db.workoutDao().getAllWorkoutsWithSets().firstOrNull() ?: emptyList()
        val exercises = db.exerciseDao().getAllExercises().firstOrNull() ?: emptyList()
        val foodLogs = db.foodDao().getAllFoodLogs().firstOrNull() ?: emptyList()

        val rootJson = JSONObject()

        // User Object
        if (user != null) {
            val userJson = JSONObject().apply {
                put("uid", user.uid)
                put("name", user.name)
                put("email", user.email)
                put("sex", user.sex ?: "")
                put("height", user.height ?: 0f)
                put("currentWeight", user.currentWeight ?: 0f)
                put("goals", user.goals)
                put("experienceLevel", user.experienceLevel)
                put("trainingFrequency", user.trainingFrequency)
                put("preferredTrainingType", user.preferredTrainingType)
                put("preferredUnit", user.preferredUnit)
            }
            rootJson.put("userProfile", userJson)
        }

        // Measurements Array
        val mArray = JSONArray()
        for (m in measurements) {
            mArray.put(JSONObject().apply {
                put("weight", m.weight)
                put("chest", m.chest ?: 0f)
                put("waist", m.waist ?: 0f)
                put("hips", m.hips ?: 0f)
                put("arm", m.arm ?: 0f)
                put("forearm", m.forearm ?: 0f)
                put("thigh", m.thigh ?: 0f)
                put("calf", m.calf ?: 0f)
                put("bodyFatPercentage", m.bodyFatPercentage ?: 0f)
                put("timestamp", m.timestamp)
            })
        }
        rootJson.put("bodyMeasurements", mArray)

        // Exercises Array
        val exArray = JSONArray()
        for (e in exercises) {
            exArray.put(JSONObject().apply {
                put("id", e.id)
                put("name", e.name)
                put("muscleGroup", e.muscleGroup)
                put("equipment", e.equipment ?: "")
                put("description", e.description ?: "")
                put("isCustom", e.isCustom)
            })
        }
        rootJson.put("exercises", exArray)

        // Workouts Array
        val wArray = JSONArray()
        for (wWithSets in workouts) {
            val w = wWithSets.workout
            val setsArray = JSONArray()
            for (s in wWithSets.sets) {
                setsArray.put(JSONObject().apply {
                    put("exerciseId", s.exerciseId)
                    put("setNumber", s.setNumber)
                    put("weight", s.weight)
                    put("reps", s.reps)
                    put("durationSeconds", s.durationSeconds)
                    put("unit", s.unit)
                    put("notes", s.notes ?: "")
                })
            }
            wArray.put(JSONObject().apply {
                put("title", w.title)
                put("date", w.date)
                put("durationSeconds", w.durationSeconds)
                put("notes", w.notes ?: "")
                put("sets", setsArray)
            })
        }
        rootJson.put("workouts", wArray)

        // Food Logs Array
        val fArray = JSONArray()
        for (f in foodLogs) {
            fArray.put(JSONObject().apply {
                put("mealType", f.mealType)
                put("foodName", f.foodName)
                put("calories", f.calories)
                put("description", f.description ?: "")
                put("timestamp", f.timestamp)
            })
        }
        rootJson.put("foodLogs", fArray)

        val exportFile = File(context.filesDir, "hgym_export_${System.currentTimeMillis()}.json")
        exportFile.writeText(rootJson.toString(2))
        exportFile
    }

    /**
     * Purges local Room database tables and deletes Firestore documents if user is logged in.
     */
    suspend fun deleteUserAccountAndData(context: Context) = withContext(Dispatchers.IO) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            try {
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(currentUser.uid).delete().await()
                currentUser.delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val db = HGymDatabase.getDatabase(context)
        db.userDao().clearUsers()
    }
}
