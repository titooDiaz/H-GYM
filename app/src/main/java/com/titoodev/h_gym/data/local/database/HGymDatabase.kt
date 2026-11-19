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
    version = 7,
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
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.exerciseDao().clearSystemSeededExercises()
                    }
                }
            }
        }
    }
}
