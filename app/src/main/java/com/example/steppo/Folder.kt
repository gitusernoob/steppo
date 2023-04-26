package com.example.steppo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Folder (
    @PrimaryKey  @ColumnInfo(name = "id") val id: UUID = UUID.randomUUID(),
    val title : String?,
    val image : String?,
    val date : Date?
    )

@Entity
data class Tutorial (
    @PrimaryKey  @ColumnInfo(name = "id") val id: UUID = UUID.randomUUID(),
    val folderId : String?,
    val title : String?,
    var image : String?,
    val date : Date?
)

@Entity
data class Step (
    @PrimaryKey  @ColumnInfo(name = "id") val id: UUID = UUID.randomUUID(),
    val tutorialId : String?,
    var title : String?,
    var image : String?,
    var description : String?,
    val stepNumber : Int?,
    val date : Date?
)
