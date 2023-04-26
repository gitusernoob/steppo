package com.example.steppo.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.steppo.Folder
import com.example.steppo.Step
import com.example.steppo.Tutorial
import java.util.*


@Dao
interface FolderDao {

    @Query("SELECT * FROM Folder ORDER BY date DESC")
    fun getFolders(): LiveData<List<Folder>>

    @Query("SELECT * FROM Folder ORDER BY date DESC")
    fun getFoldersList(): List<Folder>

    @Query("SELECT * FROM Folder WHERE id=(:id)")
    fun getFolder(id: String): Folder

    @Insert
    fun insert(insert: Folder)

    @Delete
    fun delete(delete: Folder)

}




@Dao
interface TutorialDao {

    @Query("SELECT * FROM Tutorial WHERE folderId like :folderId ORDER BY date DESC")
    fun getTutorials(folderId : String): LiveData<List<Tutorial>>

    @Query("SELECT * FROM Tutorial WHERE id=(:id)")
    fun getTutorial(id: String): Tutorial

    @Insert
    fun insert(insert: Tutorial)

    @Delete
    fun delete(delete: Tutorial)

}

@Dao
interface StepDao {

    @Query("SELECT * FROM Step WHERE tutorialId like :tutorialId")
    fun getSteps(tutorialId : String): LiveData<List<Step>>

    @Query("SELECT * FROM Step WHERE id=(:id)")
    fun getStep(id: String): Step

    @Query("UPDATE Step SET stepNumber = stepNumber + 1 WHERE stepNumber >= :stepNumber AND tutorialId like :tutorialId")
    fun incrementSteps(stepNumber: Int, tutorialId: String)

    @Query("UPDATE Step SET title = :title, description = :description, image = :image WHERE id = :id")
    fun updateStep(id: UUID, title: String?, description: String?, image: String?)

    @Insert
    fun insert(insert: Step)

    @Delete
    fun delete(delete: Step)

}