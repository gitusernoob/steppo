package com.example.steppo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class TutorialListViewModel() : ViewModel(){

    var adapterPosition = 0

    fun getTutorials(context: Context, folderId: String) : LiveData<List<Tutorial>> {
        val folderDao = MyAppDatabase.getInstance(context).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(context).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(context).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        return  repository.allTutorials(folderId)

    }


}