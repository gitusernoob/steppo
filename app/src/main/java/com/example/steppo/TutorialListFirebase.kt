package com.example.steppo

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.PrimaryKey
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.FragmentTutorialListFirebaseBinding
import com.example.steppo.databinding.FragmentTutorialsListBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class TutorialListFirebase : Fragment(), TutorialListFirebaseAdapter.ItemClickListener {

    private var _binding: FragmentTutorialListFirebaseBinding? = null
    private val binding get() = _binding!!
    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var listFolder : List<Folder>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTutorialListFirebaseBinding.inflate(layoutInflater, container, false)
        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFirebaseTutorial.layoutManager  = layoutManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerSetUp()


        val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        GlobalScope.launch {
             listFolder = repository.getFolderList()
        }

    }

    private fun recyclerSetUp(){


        var arrayListTutorialFirebase = ArrayList<Tutorial>()
        val adapter = TutorialListFirebaseAdapter(arrayListTutorialFirebase, requireContext(), this)
        binding.recyclerFirebaseTutorial.adapter = adapter
        //recupera dati
        // Get a reference to the tutorials collection
        val tutorialsRef = FirebaseDatabase.getInstance("https://steppo-4f2be-default-rtdb.europe-west1.firebasedatabase.app").getReference("tutorials")


        // Attach a listener to get the tutorials and their steps
        tutorialsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // recupero tutorial per adapter
                for (tutorialSnapshot in dataSnapshot.children) {
                    // Get the tutorial object
                    tutorialSnapshot.key
                    val id = tutorialSnapshot.child("id").getValue(String::class.java)
                    val folderId = tutorialSnapshot.child("folderId").getValue(String::class.java)
                    val title = tutorialSnapshot.child("title").getValue(String::class.java)
                    val image = tutorialSnapshot.child("image").getValue(String::class.java)
                    val date = tutorialSnapshot.child("date").value
                    println("$id, $folderId, $title, $image, $date")

                    //salvo la key nella posizione di folderId che non viene usato. una scorciatoia da mettere a posto.
                    //quando clicco sul tutorial manda a StepListFirebase la chiave per poi recuperare gli steps
                    val tutorial = Tutorial(UUID.fromString(id), tutorialSnapshot.key, title, image, Calendar.getInstance().time)

                    arrayListTutorialFirebase.add(tutorial)

                    /*al momento no...
                    // Get a reference to the steps subcollection for the tutorial
                    val stepsRef = tutorialSnapshot.child("steps")

                    // Loop through each step in the subcollection
                    for (stepSnapshot in stepsRef.children) {
                        // Get the step object
                        val step = stepSnapshot.getValue(Step::class.java)

                        // Do something with the tutorial and step objects
                    }
                    */
                }

                adapter.tutorialList = arrayListTutorialFirebase
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })

        /////////////////////////////////

        //val adapter = TutorialListFirebaseAdapter(emptyList(), requireContext(), this)
        //binding.recyclerFirebaseTutorial.adapter = adapter
/*
        //recupero id folder per associarci tutorial
        val folderId = requireArguments().getString("folderId")



        retriveTutorials()

        val tutorialList = tutorialListViewModel.getTutorials(requireContext(), folderId.toString())
        println("il numero di tutorial è tutorialist : $tutorialList")
        tutorialList.observe(viewLifecycleOwner) { tutorials ->
            adapter.tutorialList = tutorials
            adapter.notifyDataSetChanged()
            layoutManager.scrollToPosition(tutorialListViewModel.adapterPosition)
        }

        binding.recyclerFirebaseTutorial.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != -1){
                    tutorialListViewModel.adapterPosition  =position
                }
            }
        })
        */
    }



    override fun btnClicked(item: Tutorial, position: Int, buttonClicked: String) {

        when(buttonClicked){
            "btnDownloadFirebase" -> {
                val listTitle = listFolder.map { it.title } // estraggo nomi cartella
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Seleziona una cartella")
                builder.setItems(listTitle.toTypedArray()) { dialog, which ->
                    val selectedName = listTitle[which]
                    for (i in listFolder){
                        if(i.title == selectedName){
                            //nel folderid c'è il key del tutorial. salvato nel recyclersetUp. il folderId non era utile.
                            saveTutorialToInternalStorage(i.id.toString(), item.folderId.toString())
                        }
                    }
                }
                val dialog = builder.create()
                dialog.show()
            }
            "deleteItemTutorial" -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Elimina Tutorial da Firebase")
                builder.setMessage("Sei sicuro di voler cancellare questo elemento?")

                builder.setPositiveButton("Elimina") { dialog, which ->
                    deleteFirebaseTutorial(item)
                }
                builder.setNegativeButton("Annulla", null)
                val dialog = builder.create()
                dialog.show()

            }
        }
    }


    fun deleteFirebaseTutorial(tutorial: Tutorial){
        //nel folderid c'è il key del tutorial. salvato nel recyclersetUp. il folderId non era utile.
        val tutorialRef = FirebaseDatabase.getInstance("https://steppo-4f2be-default-rtdb.europe-west1.firebasedatabase.app").getReference("tutorials").child(tutorial.folderId.toString())

        tutorialRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //elimina l'immagine del tutorial in storage
                val image = dataSnapshot.child("image").getValue(String::class.java)
                println("url immagine cartella")
                println(image)
                if (image != null){
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(image.toString())
                    storageRef.delete()
                }

                // elimina tutte le immagini degli step in storage
                val stepsRef = dataSnapshot.child("steps")
                for (stepSnapshot in stepsRef.children) {
                    val imageStep = stepSnapshot.child("image").getValue(String::class.java)
                    println("url immagine step")
                    println(imageStep)
                    if (imageStep != null){
                        val storageStepRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageStep.toString())
                        storageStepRef.delete()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
        //elimina i dati da realtime database
        tutorialRef.removeValue()
        recyclerSetUp()
    }


    fun saveTutorialToInternalStorage(folderId: String, key : String){

        val tutorialRef = FirebaseDatabase.getInstance("https://steppo-4f2be-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("tutorials").child(key.toString())
        //val stepsRef = tutorialRef.child("steps")

        val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        tutorialRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Loop through each tutorial in the snapshot

                val id = dataSnapshot.child("id").getValue(String::class.java)
                //val folderId = dataSnapshot.child("folderId").getValue(String::class.java)
                val title = dataSnapshot.child("title").getValue(String::class.java)
                val image = dataSnapshot.child("image").getValue(String::class.java)
                val date = dataSnapshot.child("date").value
                println("$id, $folderId, $title, $image, $date")
                val uuidTutorial = UUID.randomUUID()

                GlobalScope.launch {
                    try {
                        val url = URL(image)
                        val imageTutorialBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        val uri = saveImageToInternalStorage(imageTutorialBitmap)
                        val tutorial = Tutorial(uuidTutorial, folderId, title, uri.toString(), Calendar.getInstance().time)
                        repository.insert(tutorial)
                    } catch (e : Exception) {
                        println(e)
                    }
                }

                // Get a reference to the steps subcollection for the tutorial
                val stepsRef = dataSnapshot.child("steps")

                // Loop through each step in the subcollection
                for (stepSnapshot in stepsRef.children) {
                    // Get the step object
                    val description = stepSnapshot.child("description").getValue(String::class.java)
                    val titleStep = stepSnapshot.child("title").getValue(String::class.java)
                    val imageStep = stepSnapshot.child("image").getValue(String::class.java)
                    val stepNumber = stepSnapshot.child("stepNumber").getValue(Int::class.java)
                    //val tutorialId = stepSnapshot.child("tutorialId").getValue(String::class.java)
                    println("$id, $folderId, $title, $image")
                    val uuid = UUID.randomUUID()

                    GlobalScope.launch {
                        try {
                            val url = URL(imageStep)
                            val imageStepBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                            val uri = saveImageToInternalStorage(imageStepBitmap)
                            val step = Step(uuid, uuidTutorial.toString(), titleStep, uri.toString(), description, stepNumber, Calendar.getInstance().time)
                            repository.insert(step)
                        } catch (e : Exception) {
                            println(e)
                        }
                    }
                }
                
                val tutorialsListFragment = TutorialsListFragment()
                val bundle = Bundle()
                bundle.putString("folderId", folderId)
                tutorialsListFragment.arguments = bundle
                parentFragmentManager
                    .beginTransaction()
                    .addToBackStack("stepListFirebase")
                    .replace(R.id.fragment_container, tutorialsListFragment)
                    .commit()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("c'è stato un errore nel salvataggio offline del tutorial")
                println(databaseError)
            }
        })
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {

        if(bitmap.byteCount > 1000000){
            println("il peso finale dell' immagine in è")
            println(bitmap.byteCount)
            //bitmap = getResizedBitmap(bitmap, 500)
        }

        println("il peso dopo riduzione dell' immagine in è")
        println(bitmap.byteCount)

        // Get the context wrapper instance
        val wrapper = ContextWrapper(requireContext())

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

}