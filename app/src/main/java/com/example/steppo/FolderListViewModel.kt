package com.example.steppo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class FolderListViewModel() : ViewModel() {

    var recyclerViewPosition = 0


    fun getFolders(context: Context) : LiveData<List<Folder>>{

        val folderDao = MyAppDatabase.getInstance(context).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(context).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(context).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        return repository.allFolders
    }


}