package com.titoodev.h_gym.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.titoodev.h_gym.data.local.dao.BodyMeasurementDao
import com.titoodev.h_gym.data.local.dao.ExerciseDao
import com.titoodev.h_gym.data.local.dao.FoodDao
import com.titoodev.h_gym.data.local.dao.QuotaDao
import com.titoodev.h_gym.data.local.dao.UserDao
import com.titoodev.h_gym.data.local.dao.WorkoutDao
import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import com.titoodev.h_gym.data.local.entities.ExerciseEntity
import com.titoodev.h_gym.data.local.entities.ExercisePerformanceEntity
import com.titoodev.h_gym.data.local.entities.FirebaseQuotaEntity
import com.titoodev.h_gym.data.local.entities.FoodLogEntity
import com.titoodev.h_gym.data.local.entities.RoutineEntity
import com.titoodev.h_gym.data.local.entities.RoutineItemEntity
import com.titoodev.h_gym.data.local.entities.UserEntity
import com.titoodev.h_gym.data.local.entities.WorkoutEntity
import com.titoodev.h_gym.data.local.entities.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        BodyMeasurementEntity::class,
        ExerciseEntity::class,
        ExercisePerformanceEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class,
        RoutineEntity::class,
        RoutineItemEntity::class,
        FoodLogEntity::class,
        FirebaseQuotaEntity::class,
    ],
    version = 6,
    exportSchema = false
)
abstract class HGymDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun foodDao(): FoodDao
    abstract fun quotaDao(): QuotaDao

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
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
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
                        seedDefaultExercises(database.exerciseDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultExercises(database.exerciseDao())
                    }
                }
            }

            private suspend fun seedDefaultExercises(exerciseDao: ExerciseDao) {
                val defaults = listOf(
                    // Pecho
                    ExerciseEntity(name = "Press de Banca Plano", muscleGroup = "Pecho", equipment = "Barra", exerciseType = "Fuerza"),
                    ExerciseEntity(name = "Press Inclinado con Mancuernas", muscleGroup = "Pecho", equipment = "Mancuernas", exerciseType = "Hipertrofia"),
                    ExerciseEntity(name = "Aperturas con Polea", muscleGroup = "Pecho", equipment = "Polea", exerciseType = "Aislamiento"),
                    ExerciseEntity(name = "Fondos en Paralelas", muscleGroup = "Pecho", equipment = "Peso Corporal", exerciseType = "Calistenia"),

                    // Espalda
                    ExerciseEntity(name = "Dominadas Pronas", muscleGroup = "Espalda", equipment = "Peso Corporal", exerciseType = "Calistenia"),
                    ExerciseEntity(name = "Remo con Barra", muscleGroup = "Espalda", equipment = "Barra", exerciseType = "Fuerza"),
                    ExerciseEntity(name = "Jalón al Pecho", muscleGroup = "Espalda", equipment = "Polea", exerciseType = "Hipertrofia"),
                    ExerciseEntity(name = "Remo con Mancuerna Unilateral", muscleGroup = "Espalda", equipment = "Mancuernas", exerciseType = "Hipertrofia"),

                    // Piernas
                    ExerciseEntity(name = "Sentadilla Libre con Barra", muscleGroup = "Piernas", equipment = "Barra", exerciseType = "Fuerza"),
                    ExerciseEntity(name = "Prensa de Piernas 45°", muscleGroup = "Piernas", equipment = "Máquina", exerciseType = "Hipertrofia"),
                    ExerciseEntity(name = "Peso Muerto Rumano", muscleGroup = "Piernas", equipment = "Barra", exerciseType = "Fuerza"),
                    ExerciseEntity(name = "Extensión de Cuádriceps", muscleGroup = "Piernas", equipment = "Máquina", exerciseType = "Aislamiento"),
                    ExerciseEntity(name = "Curl Femoral Tumbado", muscleGroup = "Piernas", equipment = "Máquina", exerciseType = "Aislamiento"),
                    ExerciseEntity(name = "Elevación de Gemelos de Pie", muscleGroup = "Piernas", equipment = "Máquina", exerciseType = "Aislamiento"),

                    // Hombros
                    ExerciseEntity(name = "Press Militar de Pie", muscleGroup = "Hombros", equipment = "Barra", exerciseType = "Fuerza"),
                    ExerciseEntity(name = "Elevaciones Laterales", muscleGroup = "Hombros", equipment = "Mancuernas", exerciseType = "Aislamiento"),
                    ExerciseEntity(name = "Pájaro con Mancuernas", muscleGroup = "Hombros", equipment = "Mancuernas", exerciseType = "Aislamiento"),

                    // Brazos
                    ExerciseEntity(name = "Curl de Bíceps con Barra Z", muscleGroup = "Brazos", equipment = "Barra Z", exerciseType = "Hipertrofia"),
                    ExerciseEntity(name = "Curl Martillo con Mancuernas", muscleGroup = "Brazos", equipment = "Mancuernas", exerciseType = "Hipertrofia"),
                    ExerciseEntity(name = "Extensión de Tríceps en Polea", muscleGroup = "Brazos", equipment = "Polea", exerciseType = "Aislamiento"),
                    ExerciseEntity(name = "Press Francés en Banco Plano", muscleGroup = "Brazos", equipment = "Barra Z", exerciseType = "Hipertrofia"),

                    // Core
                    ExerciseEntity(name = "Crunch Abdominal", muscleGroup = "Core", equipment = "Peso Corporal", exerciseType = "Calistenia"),
                    ExerciseEntity(name = "Plancha Isométrica", muscleGroup = "Core", equipment = "Peso Corporal", exerciseType = "Calistenia"),
                    ExerciseEntity(name = "Rueda Abdominal", muscleGroup = "Core", equipment = "Rueda", exerciseType = "Calistenia")
                )
                exerciseDao.insertAll(defaults)
            }
        }
    }
}
