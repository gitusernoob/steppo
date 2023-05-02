package com.example.steppo


import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList


private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.bottomNavigation.setOnNavigationItemSelectedListener{ menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    changeFragment(FolderListFragment())
                    true
                }
                R.id.navigation_firebase -> {
                    changeFragment(TutorialListFirebase())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            changeFragment(FolderListFragment())
        }


        //se non ci sono cartelle nel database aggiunge 3 tre cartelle, e 5 tutorial esempio per uso offline
        val folderDao = MyAppDatabase.getInstance(applicationContext).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(applicationContext).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(applicationContext).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)
        var list = repository.allFolders
        list.observe(this, Observer { folders ->
            if (folders.isEmpty()){
                populateDatabase()
            }
        })

    }

    private fun changeFragment(fragment : Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun populateDatabase(){
        val assetManager = assets

        Toast.makeText(applicationContext, "attendi qualche secondo... sto popolando il database", Toast.LENGTH_SHORT).show()

        val folderDao = MyAppDatabase.getInstance(applicationContext).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(applicationContext).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(applicationContext).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        var arrayListFolder  = ArrayList<Folder>()

        //aggiungo Folders
        val folderId1 = UUID.randomUUID()
        var uri = saveImageGiveMeUri("folder1.png")
        arrayListFolder.add(Folder(folderId1,"git e github", uri.toString(),Calendar.getInstance().time))

        val folderId2 = UUID.randomUUID()
        uri = saveImageGiveMeUri("folder2.png")
        arrayListFolder.add(Folder(folderId2,"brucia le tappe con Firebase", uri.toString(),Calendar.getInstance().time))

        val folderId3 = UUID.randomUUID()
        uri = saveImageGiveMeUri("folder3.png")
        arrayListFolder.add(Folder(folderId3,"android e dintorni", uri.toString(),Calendar.getInstance().time))


        //aggiungo Tutorials
        var arrayListTutorial  = ArrayList<Tutorial>()

        val tutorialId1 = UUID.randomUUID()
        uri = saveImageGiveMeUri("stepZoomTutorial.png")
        arrayListTutorial.add(Tutorial(tutorialId1, folderId3.toString(),"Zoom 10x per imageView", uri.toString(),Calendar.getInstance().time))

        val tutorialId2 = UUID.randomUUID()
        uri = saveImageGiveMeUri("stepButtonTutorial.png")
        arrayListTutorial.add(Tutorial(tutorialId2, folderId3.toString(),"Un tasto tutto da pigiare", uri.toString(),Calendar.getInstance().time))

        val tutorialId3 = UUID.randomUUID()
        uri = saveImageGiveMeUri("stepRoomTutorial.png")
        arrayListTutorial.add(Tutorial(tutorialId3, folderId3.toString(),"database che passione (con room)", uri.toString(),Calendar.getInstance().time))

        val tutorialId4 = UUID.randomUUID()
        uri = saveImageGiveMeUri("stepFirebaseTutorial.png")
        arrayListTutorial.add(Tutorial(tutorialId4, folderId2.toString(),"collega la tua app a Firebase", uri.toString(),Calendar.getInstance().time))

        val tutorialId5 = UUID.randomUUID()
        uri = saveImageGiveMeUri("stepGithubTutorial.png")
        arrayListTutorial.add(Tutorial(tutorialId5, folderId1.toString(),"carica la tua app su github", uri.toString(),Calendar.getInstance().time))

        //aggiungo Steps
        var arrayListStep  = ArrayList<Step>()

        //tutorial1 zoom imageview
        uri = saveImageGiveMeUri("stepZoom1.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId1.toString(),null, uri.toString(),"installo le librerie", 1, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepZoom2.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId1.toString(),null, uri.toString(),"uso SubsamplingScaleImageView", 2, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepZoom3.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId1.toString(),null, uri.toString(),"usa ImageSource.uri() per gestire l'uri, imposta maxScale = 10f per lo zoom 10x ", 3, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepZoom4.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId1.toString(),null, uri.toString(),"ora puoi ingrandire le immagini ", 4, Calendar.getInstance().time))

        //tutorial2 button design
        uri = saveImageGiveMeUri("stepButton1.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId2.toString(),null, uri.toString(),"crea un drawable con questo codice per creare un gradiente ", 1, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepButton2.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId2.toString(),null, uri.toString(),"aggiungi come background il drawable", 2, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepButtonTutorial.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId2.toString(),null, uri.toString(),"questo è il risultato", 3, Calendar.getInstance().time))

        //tutorial3 room db
        uri = saveImageGiveMeUri("stepRoom1.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId3.toString(),null, uri.toString(),"installa le librerie", 1, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepRoom2.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId3.toString(),null, uri.toString(),"crea gli entity", 2, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepRoom3.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId3.toString(),null, uri.toString(),"crea il dao", 3, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepRoom4.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId3.toString(),null, uri.toString(),"crea la classe RoomDatabase", 4, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepRoom5.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId3.toString(),null, uri.toString(),"crea il repository", 5, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepRoom6.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId3.toString(),null, uri.toString(),"recupera i dati in questo modo tramite repository", 6, Calendar.getInstance().time))

        //tutorial4 collega la tua app con firebase
        uri = saveImageGiveMeUri("stepFirebase1.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"vai su firebase, accedi alla console e clicca su aggiungi progetto", 1, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase2.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"inserisci il nome del progetto", 2, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase3.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"scegli account base", 3, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase4.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"clicca sull'icona di android", 4, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase6.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"inserisci il nome del package name cosi come si trova in appliccation ID in build gradle, ed aggiungi nickname", 6, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase7.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"scarica il file json", 7, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase8.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"cambia la visualizzazione da android -> project e copia il json nella cartella app", 8, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase9.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"cosi si dovrebbe visualizzare", 9, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase10.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"copia le dependencies che ti mancano", 10, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepFirebase11.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId4.toString(),null, uri.toString(),"aggiungi queste librerie per realtime database", 11, Calendar.getInstance().time))

        //tutorial5 carica app su github
        uri = saveImageGiveMeUri("stepGithub1.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"segui le istruzioni fino al punto 6", 1, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepGithub2.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"non c'è più il supporto per l'autenticazione tramite password. serve un token se no da errore", 2, Calendar.getInstance().time))

        uri = saveImageGiveMeUri("stepGithub4.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"vai su github -> settings", 4, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepGithub5.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"vai su developer settings", 5, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepGithub6.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"genera nuovo token", 6, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepGithub7.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"inserisci 'note' metti expiration quanto preferisci, seleziona tutto le caselle sotto", 7, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepGithub8.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"copia il token", 8, Calendar.getInstance().time))
        uri = saveImageGiveMeUri("stepGithub9.png")
        arrayListStep.add(Step(UUID.randomUUID(), tutorialId5.toString(),null, uri.toString(),"inserisci al posto della password", 9, Calendar.getInstance().time))



        //inserisce nel database i dati delle cartelle, tutorial e passaggi tutorial
        CoroutineScope(Dispatchers.IO).launch {
            for (i in arrayListFolder){
                repository.insert(i)
            }
            for (i in arrayListTutorial){
                repository.insert(i)
            }
            for (i in arrayListStep){
                repository.insert(i)
            }
        }

    }

    private fun saveImageGiveMeUri(imageName : String): Uri {
        val assetManager = assets
        val drawable = try {
            Drawable.createFromStream(assetManager.open(imageName), null)
        } catch (e: Exception) {
            null
        }
        if (drawable == null) {
            println("c'è un problema con drawable... è nullo")
        }
        return saveImageToInternalStorage(drawable!!)
    }

    private fun saveImageToInternalStorage(drawable: Drawable):Uri{
        // Get the bitmap from drawable object
        var bitmap = (drawable as BitmapDrawable).bitmap

        if(bitmap.byteCount > 1000000){
            println("il peso finale dell' immagine in è")
            println(bitmap.byteCount)
            //bitmap = getResizedBitmap(bitmap, 500)
        }

        println("il peso dopo riduzione dell' immagine in è")
        println(bitmap.byteCount)

        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)


        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }

    private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

}