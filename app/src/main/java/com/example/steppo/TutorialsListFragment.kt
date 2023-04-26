package com.example.steppo

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.FragmentTutorialsListBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class TutorialsListFragment : Fragment(), TutorialListAdapter.ItemClickListener {

    private var _binding: FragmentTutorialsListBinding? = null
    private val binding get() = _binding!!
    private  val tutorialListViewModel: TutorialListViewModel by viewModels()
    private lateinit var layoutManager : LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTutorialsListBinding.inflate(layoutInflater, container, false)
        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTutorialList.layoutManager  = layoutManager
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //aggiorno il recycler con i dati del db
        recyclerSetUp()

        //recupero id folder per associarci tutorial
        val folderId = requireArguments().getString("folderId")

        //invio l'id della cartella al fragment per creare nuovi tutorial
        binding.btnNewTutorial.setOnClickListener {

            val fragment = TutorialCreateFragment()
            val bundle = Bundle()
            bundle.putString("folderId", folderId)
            fragment.arguments = bundle
            parentFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .addToBackStack("FolderListFragment")
                .commit()

        }


    }

    private fun recyclerSetUp(){

              val adapter = TutorialListAdapter(emptyList(), requireContext(), this)
              binding.recyclerTutorialList.adapter = adapter

              //recupero id folder per associarci tutorial
              val folderId = requireArguments().getString("folderId")

              val tutorialList = tutorialListViewModel.getTutorials(requireContext(), folderId.toString())
              println("il numero di tutorial è tutorialist : $tutorialList")
              tutorialList.observe(viewLifecycleOwner) { tutorials ->
                  adapter.tutorialList = tutorials
                  adapter.notifyDataSetChanged()
                  layoutManager.scrollToPosition(tutorialListViewModel.adapterPosition)
              }

            binding.recyclerTutorialList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != -1){
                        tutorialListViewModel.adapterPosition  =position
                    }
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun btnClicked(item: Tutorial, position: Int, buttonClicked: String) {

        when (buttonClicked) {
            "deleteItemTutorial" -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Elimina Tutorial")
                builder.setMessage("Sei sicuro di voler cancellare questo elemento?")
                builder.setPositiveButton("Delete") { dialog, which ->
                    deleteTutorial(item)
                    Toast.makeText(context, "Tutorial eliminato", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("Annulla", null)

                val dialog = builder.create()
                dialog.show()
            }
            "btnUploadFirebase" -> {
                
                uploadTutorial(item)
            }
            "boh" -> {
                println("non so ")
            }
        }

    }

    private fun uploadTutorial(tutorial: Tutorial){

        //caricare un immagine su firestore e l'elemento su firebase

        val storage = FirebaseStorage.getInstance().reference
        val uploadUri = Uri.fromFile(File(tutorial.image.toString()))
        val imageRef = storage.child("images/${UUID.randomUUID()}.jpg")

        val uploadTask = imageRef.putFile(uploadUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                tutorial.image = uri.toString()

                println("url immagine tutorial: $uri")

                // Create a map of tutorial properties
                val tutorialMap = mapOf(
                    "id" to tutorial.id.toString(),
                    "folderId" to tutorial.folderId,
                    "title" to tutorial.title,
                    "image" to tutorial.image,
                    "date" to tutorial.date?.time
                )

                // Write the tutorial to the database
                val tutorialsRef = FirebaseDatabase.getInstance("https://steppo-4f2be-default-rtdb.europe-west1.firebasedatabase.app").getReference("tutorials")
                val tutorialId = tutorialsRef.push().key
                tutorialsRef.child(tutorialId!!).setValue(tutorialMap)
                    .addOnSuccessListener { println("Tutorial added to the database") }

                // Upload steps for the tutorial
                val stepsRef = tutorialsRef.child(tutorialId).child("steps")
                uploadSteps(stepsRef, tutorial.id.toString())
            }
        }.addOnFailureListener { exception ->
            println("Upload failed: ${exception.message}")
        }
        /*
        val storage = FirebaseStorage.getInstance().reference
        println("storageref: $storage")
        val uploadUri = Uri.fromFile(File(tutorial.image.toString()))
        val imageRef = storage.child("images/${UUID.randomUUID()}.jpg")
        println("uri image: ${tutorial.image}")
        val uploadTask = imageRef.putFile(uploadUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            tutorial.image = taskSnapshot.storage.downloadUrl.toString()
            println(tutorial.image)
            val tutorialMap = mapOf(
                "id" to tutorial.id.toString(),
                "folderId" to tutorial.folderId,
                "title" to tutorial.title,
                "image" to tutorial.image,
                "date" to tutorial.date?.time
            )
               // Write the tutorial to the database
            val tutorialsRef =  FirebaseDatabase.getInstance("https://steppo-4f2be-default-rtdb.europe-west1.firebasedatabase.app").getReference("tutorials")
            val tutorialId = tutorialsRef.push().key
            tutorialsRef.child(tutorialId!!).setValue(tutorialMap).addOnSuccessListener { println("qua ho aggiunto il tutorial sul realtima ") }
            val stepsRef = tutorialsRef.child(tutorialId).child("steps")

            uploadSteps(stepsRef, tutorial.id.toString())

        }
        */

    }

    /*
    //scaricare l'immagine
    val imageUrl = tutorial.image // Replace with the image URL from your Tutorial object
    val storageRef = storage.getReferenceFromUrl(imageUrl)
    val localFile = File.createTempFile("images", "jpg")
    storageRef.getFile(localFile).addOnSuccessListener {
        // Do something with the downloaded file (e.g. display it in an ImageView)
    }.addOnFailureListener {
        // Handle any errors that occur during the download
    }

    //eliminare l'immagine
    val imageUrl = tutorial.image // Replace with the image URL from your Tutorial object
    val storageRef = storage.getReferenceFromUrl(imageUrl)
    storageRef.delete().addOnSuccessListener {
        // The image was deleted successfully
    }.addOnFailureListener {
        // Handle any errors that occur during the deletion
    }
    //.... devo prendere

         */
    /*
    struttura Firebase

    - tutorials
      - {tutorialId}
        - folderId: String
        - title: String
        - image: String
        - date: Number
        - steps
          - {stepId}
            - tutorialId: String
            - title: String
            - image: String
            - description: String
            - stepNumber: Number
            - date: Number
     */

    private fun uploadSteps(stepRef : DatabaseReference, tutorialId: String){

        //val refSteps = FirebaseDatabase.getInstance().getReference("steps").get


        val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)
        val stepsTutorial = repository.allSteps(tutorialId)
        val storage = FirebaseStorage.getInstance()
        stepsTutorial.observe(viewLifecycleOwner) {steps ->
            for (j in steps){

                val uploadUri = Uri.fromFile(File(j.image.toString()))
                val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
                val uploadTask = imageRef.putFile(uploadUri)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    imageRef.getDownloadUrl().addOnSuccessListener { downloadUri ->
                        println("url immagine step: $downloadUri")
                        val stepMap = mapOf(
                            "tutorialId" to j.tutorialId.toString(),
                            "title" to j.tutorialId,
                            "image" to downloadUri.toString(),
                            "description" to j.description,
                            "stepNumber" to j.stepNumber,
                            "date" to j.date
                        )
                        stepRef.push().setValue(stepMap).addOnSuccessListener {
                            println("caricato tutorial su realtimedb")
                        }
                    }.addOnFailureListener {
                        println("problema download Url")
                    }
                }.addOnFailureListener {
                    println("problema con upload immagine")
                }
                /* codice con problema Url
                val uploadUri = Uri.fromFile(File(j.image.toString()))
                val imageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
                val uploadTask = imageRef.putFile(uploadUri)
                uploadTask.addOnSuccessListener { taskSnapshot ->

                    println("stepref: $stepRef")
                    val stepMap = mapOf(
                        "tutorialId" to j.tutorialId.toString(),
                        "title" to j.tutorialId,
                        "image" to taskSnapshot.storage.downloadUrl.toString(),
                        "description" to j.description,
                        "stepNumber" to j.stepNumber,
                        "date" to j.date
                    )
                    stepRef.push().setValue(stepMap).addOnSuccessListener { println("sono riuscito a caricare qualcosa") }
                }*/
            }
        }


        /*
        // Get a reference to the tutorials collection
        val tutorialsRef = FirebaseDatabase.getInstance().getReference("tutorials")

        // Create a new tutorial object
        val tutorial = Tutorial(/* fill in tutorial properties */)

        // Write the tutorial to the database
        val tutorialId = tutorialsRef.push().key
        tutorialsRef.child(item.id.toString()).setValue(tutorial)

        // Get a reference to the steps subcollection for the tutorial
        val stepsRef = tutorialsRef.child(item.id.toString()).child("steps")

        // Create new step objects
        val step1 = Step(/* fill in step properties */)
        val step2 = Step(/* fill in step properties */)

        val imageStepRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
        val uploadTask = imageRef.putFile(item.image!!.toUri())

        // Write the steps to the database
        stepsRef.push().setValue(step1)
        stepsRef.push().setValue(step2)
        //caricare i dati su firebase a partire da un oggetto
        FirebaseApp.initializeApp(requireContext())

        val tutorialId : String?,
        var title : String?,
        var image : String?,
        var description : String?,
        val stepNumber : Int?,
        val date : Date?


        val tutorialMaps = mapOf(
            "id" to item.id.toString(),
            "folderId" to item.folderId,
            "title" to item.title,
            "image" to item.image,
            "date" to item.date?.time
        )
        ref.child(item.id.toString()).setValue(tutorialMap)
        */
    }


    private fun deleteTutorial(tutorial: Tutorial){
        val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        //elimino gli step del tutorial
        var stepsTutorial = repository.allSteps(tutorial.id.toString())
        stepsTutorial.observe(viewLifecycleOwner) {steps ->
            for (j in steps){
                println("eliminato step: ${j.description}")
                CoroutineScope(Dispatchers.IO).launch {
                    repository.delete(j) //elimino lo step
                    val fileUri: Uri = j.image!!.toUri()// URI del file che vuoi eliminare
                    val fileToDelete = File(fileUri.path!!) // Creazione di un oggetto file dal path dell'URI
                    if (fileToDelete.exists()) {
                        fileToDelete.delete()
                        println("immagine step eliminata") // Eliminazione del file
                    } else {
                        println("immagine step non esiste")
                    }
                }
            }
        }

        // elimino il tutorial e l'immagine associata
        CoroutineScope(Dispatchers.IO).launch {
            repository.delete(tutorial)
            val fileUri: Uri = tutorial.image!!.toUri()// URI del file che vuoi eliminare
            val fileToDelete = File(fileUri.path!!) // Creazione di un oggetto file dal path dell'URI
            if (fileToDelete.exists()) {
                fileToDelete.delete()
                println("immagine tutorial eliminata") // Eliminazione del file
            } else {
                println("immagine tutorial non esiste")
            }
        }
        Toast.makeText(context, "Tutorial eliminato", Toast.LENGTH_SHORT).show()
    }

}