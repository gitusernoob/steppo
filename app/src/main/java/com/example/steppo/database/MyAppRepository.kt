package com.example.steppo.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.steppo.Folder
import com.example.steppo.Step
import com.example.steppo.Tutorial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

class MyAppRepository(private val folderDao: FolderDao,
                      private val tutorialDao : TutorialDao,
                      private val stepDao : StepDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    suspend fun insert(folder: Folder) { withContext(Dispatchers.IO) { folderDao.insert(folder) } }
    suspend fun delete(folder: Folder){ withContext(Dispatchers.IO) { folderDao.delete(folder) } }
    suspend fun getFolderList() : List<Folder>{ return folderDao.getFoldersList() }
    val allFolders: LiveData<List<Folder>> = folderDao.getFolders()

    suspend fun insert(tutorial: Tutorial) { withContext(Dispatchers.IO) { tutorialDao.insert(tutorial) } }
    suspend fun delete(tutorial: Tutorial){ withContext(Dispatchers.IO) { tutorialDao.delete(tutorial) } }
    fun allTutorials(folderId : String) : LiveData<List<Tutorial>>{ return tutorialDao.getTutorials(folderId) }
    suspend fun incrementSteps(stepNumber : Int, tutorialName : String){ withContext(Dispatchers.IO) {stepDao.incrementSteps(stepNumber, tutorialName)} }

    suspend fun insert(step: Step) { withContext(Dispatchers.IO) { stepDao.insert(step) } }
    suspend fun delete(step: Step){ withContext(Dispatchers.IO) { stepDao.delete(step) } }
    fun allSteps(tutorialId: String) : LiveData<List<Step>>{ return stepDao.getSteps(tutorialId) }
    fun updateStep(id: UUID, title: String?, description: String?, image: String?) { stepDao.updateStep(id,title, description, image)}

}