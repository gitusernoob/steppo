package com.example.steppo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class StepListViewModel() : ViewModel(){

    var adapterPosition = 0

    fun getSteps(context: Context, tutorialId : String) : LiveData<List<Step>>{
        val folderDao = MyAppDatabase.getInstance(context).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(context).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(context).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        return repository.allSteps(tutorialId)
    }

}