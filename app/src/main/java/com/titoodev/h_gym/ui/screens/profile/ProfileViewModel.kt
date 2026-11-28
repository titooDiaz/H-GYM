package com.titoodev.h_gym.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.titoodev.h_gym.HGymApplication
import com.titoodev.h_gym.data.local.entities.BodyMeasurementEntity
import com.titoodev.h_gym.data.local.entities.UserEntity
import com.titoodev.h_gym.data.local.entities.UserProfileHistoryEntity
import com.titoodev.h_gym.util.DataExportManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HGymApplication).container.repository

    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val profileHistoryState: StateFlow<List<UserProfileHistoryEntity>> = repository.profileHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMeasurementState: StateFlow<BodyMeasurementEntity?> = repository.latestMeasurementFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateWeight(newWeight: Float) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            val oldWeight = user.currentWeight ?: 0f

            // Update user entity
            val updatedUser = user.copy(
                currentWeight = newWeight,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUser(updatedUser)

            // Record immutable measurement entry in Room
            val newMeasurement = BodyMeasurementEntity(
                uid = user.uid,
                weight = newWeight,
                timestamp = System.currentTimeMillis()
            )
            repository.addMeasurement(newMeasurement)

            // Record immutable history audit
            val history = UserProfileHistoryEntity(
                uid = user.uid,
                fieldName = "currentWeight",
                previousValue = "$oldWeight kg",
                newValue = "$newWeight kg",
                timestamp = System.currentTimeMillis()
            )
            repository.recordProfileHistoryChange(history)

            com.titoodev.h_gym.data.sync.CloudSyncManager.syncLocalDataToCloud(getApplication())
        }
    }

    fun exportData(onCompleted: (File) -> Unit) {
        viewModelScope.launch {
            val file = DataExportManager.exportUserDataToJson(getApplication())
            onCompleted(file)
        }
    }

    fun deleteAccount(onCompleted: () -> Unit) {
        viewModelScope.launch {
            DataExportManager.deleteUserAccountAndData(getApplication())
            onCompleted()
        }
    }
}
