package com.example.steppo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.FragmentStepBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*



private lateinit var imageUri: Uri // variable to hold the selected image URI

class StepFragment : Fragment(), StepListAdapter.ItemClickListener{
    lateinit var adapter : StepListAdapter
    private var _binding: FragmentStepBinding? = null
    private val binding get() = _binding!!
    private val PICK_IMAGE_REQUEST = 12332 // request code for picking an image from the gallery
    var step : Step? = null
    private val stepListViewModel : StepListViewModel by viewModels()
    private lateinit var layoutManager: LinearLayoutManager



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStepBinding.inflate(layoutInflater, container, false)
        layoutManager =  LinearLayoutManager(requireContext())
        binding.recyclerStepList.layoutManager  =layoutManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        println("prima del setup")
        println(stepListViewModel.adapterPosition)
        recyclerSetUp()

        binding.addCardStepFragment.setOnClickListener {
            createStepZero()
        }

        //tasto per rimuovere la visualizzazione a tutto schermo
        binding.imageButtonCloseImageFullScreen.setOnClickListener {
            binding.imageFullScreen.visibility = ImageView.GONE
            binding.imageButtonCloseImageFullScreen.visibility = ImageButton.GONE
        }



    }

    private fun createStepZero(){

        val tutorialId = requireArguments().getString("tutorialId")
        val date = Calendar.getInstance().time
        val step = Step(UUID.randomUUID(),tutorialId,null, null,null, 0, date)

        CoroutineScope(Dispatchers.IO).launch {
            val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
            val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
            val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
            val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

            repository.incrementSteps(0, tutorialId.toString())
            repository.insert(step)
        }

    }

    private fun recyclerSetUp(){
        adapter = StepListAdapter(emptyList(), requireContext(), this)
        binding.recyclerStepList.adapter = adapter

        val tutorialId = requireArguments().getString("tutorialId")
        val stepList = stepListViewModel.getSteps(requireContext(), tutorialId.toString())
        stepList.observe(viewLifecycleOwner) { steps ->
            adapter.stepList =  steps.sortedBy { it.stepNumber }
            binding.recyclerStepList.adapter = adapter
            adapter.notifyDataSetChanged()
            layoutManager.scrollToPosition(stepListViewModel.adapterPosition)
        }

        binding.recyclerStepList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != -1){
                    stepListViewModel.adapterPosition  =position
                }
            }
        })

    }

    override fun btnClicked(item: Step, position: Int, buttonClicked : String) {
        step = item
        //creo il repository
        val tutorialId = requireArguments().getString("tutorialId")
        val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        when (buttonClicked) {
            "btnChooseImageStep" -> {
                stepListViewModel.adapterPosition = position
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
            "btnNewCardDownItemStep" -> {
                stepListViewModel.adapterPosition=position+1 //la posizione nel recycler non corrispone allo stepNumbre della scheda
                val stepNumberNewStep = item.stepNumber!!+1 //la card alla posizione 0 potrebbe essere stata prima la 5a
                val date = Calendar.getInstance().time
                val newStep = Step(UUID.randomUUID(),tutorialId,null, null,null, stepNumberNewStep , date)
                CoroutineScope(Dispatchers.IO).launch {
                    //incrementa il numero scheda di tutti gli step successivi che fanno parte dello stesso tutorial
                    try {
                        repository.incrementSteps(stepListViewModel.adapterPosition, tutorialId!!)
                        repository.insert(newStep)
                    }catch (e: java.lang.Exception){ println(e)}
                }
            }
            "btnSaveStep" -> {
                stepListViewModel.adapterPosition = position
                //aggiorna titolo e descrizione, l' immagine viene aggiornata con il tasto apposito
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repository.updateStep(step!!.id, step!!.title, step!!.description, step!!.image)
                    } catch (e: Exception){println("se non ha l'immagine sono cazzimburg $e")}
                }
            }
            "imageButtonDeleteStep" -> {
                stepListViewModel.adapterPosition = if(position >0){
                    position-1
                } else {
                    position
                }
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Elimina Step")
                builder.setMessage("Sei sicuro di voler cancellare questo elemento?")

                builder.setPositiveButton("Elimina") { dialog, which ->

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.delete(step!!)
                        }catch (e: Exception){println(e)}
                    }
                    Toast.makeText(context, "scheda eliminata", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("Annulla", null)
                val dialog = builder.create()
                dialog.show()
            }
            "imageItemStep" -> {
                try {
                    binding.imageFullScreen.setImage(ImageSource.uri(step!!.image!!))
                    binding.imageFullScreen.maxScale = 10f
                }catch (e: Exception){ println(e)}
                binding.imageFullScreen.visibility = ImageView.VISIBLE
                binding.imageButtonCloseImageFullScreen.visibility = ImageButton.VISIBLE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!! // get the selected image URI

            var uri = imageUri
            val drawable = try {
                Drawable.createFromStream(requireContext().contentResolver.openInputStream(uri), uri.toString())
            } catch (e: Exception) {
                null
            }

            uri = saveImageToInternalStorage(drawable!!)
            val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
            val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
            val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
            val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.updateStep(step!!.id,step!!.title!!,step!!.description!!,uri.toString())
                } catch (e: java.lang.Exception){println(e)}
            }

        }
    }

    private fun saveImageToInternalStorage(drawable: Drawable):Uri{
        // Get the bitmap from drawable object
        var bitmap = (drawable as BitmapDrawable).bitmap

        if(bitmap.byteCount > 1000000){
            println("il peso finale dell' immagine in è")
            println(bitmap.byteCount)
            bitmap = getResizedBitmap(bitmap, 1000)
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