package com.example.steppo
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.FragmentStepListFirebaseBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.util.*


class StepListFirebase : Fragment(), StepListFirebaseAdapter.ItemClickListener {

    private var _binding: FragmentStepListFirebaseBinding? = null
    private val binding get() = _binding!!
    private lateinit var layoutManager : LinearLayoutManager
    var arrayListStepFirebase = ArrayList<Step>()
    private lateinit var listFolder: List<Folder>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStepListFirebaseBinding.inflate(layoutInflater, container, false)
        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFirebaseStep.layoutManager  = layoutManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerSetUp()

        GlobalScope.launch {
            val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
            val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
            val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
            val repository = MyAppRepository(folderDao, tutorialDao, stepDao)
            listFolder = repository.getFolderList()
        }

        //tasto per rimuovere la visualizzazione a tutto schermo
        binding.imageButtonCloseImageFullScreen.setOnClickListener {
            binding.imageFullScreen.visibility = ImageView.GONE
            binding.imageButtonCloseImageFullScreen.visibility = ImageButton.GONE
        }

        binding.btnDownload.setOnClickListener {

                val listTitle = listFolder.map { it.title } // estraggo nomi cartella
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Seleziona una cartella")
                builder.setItems(listTitle.toTypedArray()) { dialog, which ->
                    val selectedName = listTitle[which]
                    for (i in listFolder){
                        if(i.title == selectedName){
                            saveTutorialToInternalStorage(i.id.toString())
                        }
                    }
                }
                val dialog = builder.create()
                dialog.show()

        }

    }

    private fun recyclerSetUp(){

        arrayListStepFirebase.clear()
        val adapter = StepListFirebaseAdapter(arrayListStepFirebase, requireContext(), this)
        binding.recyclerFirebaseStep.adapter = adapter
        //recupera dati
        // Get a reference to the tutorials collection
        val key = requireArguments().getString("tutorialKeyFirebase")
        val stepsRef = FirebaseDatabase.getInstance("https://steppo-4f2be-default-rtdb.europe-west1.firebasedatabase.app")
            .getReference("tutorials").child(key.toString()).child("steps")

        stepsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                arrayListStepFirebase.clear()
                for (stepSnapshot in dataSnapshot.children) {
                    val description = stepSnapshot.child("description").getValue(String::class.java)
                    val title = stepSnapshot.child("title").getValue(String::class.java)
                    val image = stepSnapshot.child("image").getValue(String::class.java)
                    val stepNumber = stepSnapshot.child("stepNumber").getValue(Int::class.java)
                    val tutorialId = stepSnapshot.child("tutorialId").getValue(String::class.java)
                    val folderId = stepSnapshot.child("folderId").getValue(String::class.java)
                    println("$id, $folderId, $title, $image")
                    val uuid = UUID.randomUUID()

                    val step = Step(uuid, tutorialId, title, image, description, stepNumber, Calendar.getInstance().time)
                    arrayListStepFirebase.add(step)
                }

                val sortedList = arrayListStepFirebase.sortedBy { it.stepNumber }.toList()
                adapter.stepList = sortedList
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    override fun btnClicked(item: Step, position: Int, buttonClicked: String) {

        when (buttonClicked) {
            "imageItemStep" -> {
                GlobalScope.launch {
                    try {
                        val url = URL(item.image)
                        val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        withContext(Dispatchers.Main) {
                            // This code runs on the main thread
                            binding.imageFullScreen.setImage(ImageSource.bitmap(image))
                            binding.imageFullScreen.maxScale = 10f
                        }
                    } catch (e : Exception) {
                        println(e)
                    }
                }
                binding.imageFullScreen.visibility = ImageView.VISIBLE
                binding.imageButtonCloseImageFullScreen.visibility = ImageButton.VISIBLE
            }

        }

    }

    fun saveTutorialToInternalStorage(folderId : String){
        val key = requireArguments().getString("tutorialKeyFirebase")
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
                println(", $folderId, $title, $image, $date")

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
                    val tutorialId = stepSnapshot.child("tutorialId").getValue(String::class.java)
                    println(", $folderId, $title, $image")
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



