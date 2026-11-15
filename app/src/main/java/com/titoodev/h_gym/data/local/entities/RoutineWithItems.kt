package com.titoodev.h_gym.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RoutineWithItems(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val items: List<RoutineItemEntity>
)
