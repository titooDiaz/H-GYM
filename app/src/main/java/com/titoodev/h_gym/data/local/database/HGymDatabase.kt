package com.titoodev.h_gym.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.titoodev.h_gym.data.local.dao.*
import com.titoodev.h_gym.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        UserProfileHistoryEntity::class,
        BodyMeasurementEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class,
        RoutineEntity::class,
        RoutineItemEntity::class,
        FoodLogEntity::class,
        FirebaseQuotaEntity::class,
        SyncOutboxEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class HGymDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userProfileHistoryDao(): UserProfileHistoryDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun foodDao(): FoodDao
    abstract fun quotaDao(): QuotaDao
    abstract fun syncOutboxDao(): SyncOutboxDao

    companion object {
        @Volatile
        private var INSTANCE: HGymDatabase? = null

        fun getDatabase(context: Context): HGymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HGymDatabase::class.java,
                    "h_gym_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialCatalog(database.exerciseDao())
                    }
                }
            }

            private suspend fun seedInitialCatalog(exerciseDao: ExerciseDao) {
                val defaultCatalog = listOf(
                    ExerciseEntity(id = 101, name = "Press de Banca Plano", muscleGroup = "Pecho", equipment = "Barra", exerciseType = "Fuerza", isCustom = false),
                    ExerciseEntity(id = 102, name = "Dominadas Pronas", muscleGroup = "Espalda", equipment = "Peso Corporal", exerciseType = "Calistenia", isCustom = false),
                    ExerciseEntity(id = 103, name = "Sentadilla Libre", muscleGroup = "Piernas", equipment = "Barra", exerciseType = "Fuerza", isCustom = false),
                    ExerciseEntity(id = 104, name = "Press Militar", muscleGroup = "Hombros", equipment = "Barra", exerciseType = "Fuerza", isCustom = false),
                    ExerciseEntity(id = 105, name = "Curl de Bíceps", muscleGroup = "Brazos", equipment = "Mancuernas", exerciseType = "Hipertrofia", isCustom = false),
                    ExerciseEntity(id = 106, name = "Curl de Antebrazo", muscleGroup = "Antebrazos", equipment = "Barra", exerciseType = "Hipertrofia", isCustom = false),
                    ExerciseEntity(id = 107, name = "Elevación de Pantorrillas", muscleGroup = "Pantorrillas", equipment = "Máquina", exerciseType = "Aislamiento", isCustom = false),
                    ExerciseEntity(id = 108, name = "Plancha Isométrica", muscleGroup = "Abs / Core", equipment = "Peso Corporal", exerciseType = "Calistenia", isCustom = false),
                    ExerciseEntity(id = 109, name = "Hip Thrust", muscleGroup = "Glúteos", equipment = "Barra", exerciseType = "Fuerza", isCustom = false),
                    ExerciseEntity(id = 110, name = "Carrera en Cinta", muscleGroup = "Cardio", equipment = "Máquina", exerciseType = "Resistencia", isCustom = false),
                    ExerciseEntity(id = 111, name = "Estiramiento de Isquiotibiales", muscleGroup = "Movilidad / Estiramientos", equipment = "Ninguno", exerciseType = "Flexibilidad", isCustom = false),
                    ExerciseEntity(id = 112, name = "Burpees", muscleGroup = "Funcional / Calistenia", equipment = "Peso Corporal", exerciseType = "HIIT", isCustom = false)
                )
                exerciseDao.insertAll(defaultCatalog)
            }
        }
    }
}
