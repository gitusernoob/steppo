package com.example.steppo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.TypeConverters
import com.example.steppo.Folder
import com.example.steppo.Step
import com.example.steppo.Tutorial


@Database(entities = [Folder::class, Tutorial::class, Step::class], version = 1)
@TypeConverters(FolderTypeConverters::class)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun tutorialDao(): TutorialDao
    abstract fun stepDao(): StepDao

    companion object {
        private var instance: MyAppDatabase? = null

        fun getInstance(context: Context): MyAppDatabase {
            if (instance == null) {
                instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        MyAppDatabase::class.java,
                        "my_app_db")
                    //.allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }

    }
}


