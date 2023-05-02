package com.example.steppo

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.FragmentTutorialCreateBinding
import kotlinx.coroutines.*
import java.io.*
import java.util.*

class TutorialCreateFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 12332 // request code for picking an image from the gallery
    private lateinit var imageUri: Uri


    private var _binding: FragmentTutorialCreateBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTutorialCreateBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnChooseImageTutorial.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.btnSaveNewTutorial.setOnClickListener {
            saveFolder()
        }


    }

    //gestisci l'immagine selezionata
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!

            //mostra immagine nell'image view
            Glide.with(requireContext()).load(imageUri).into(binding.imageTutorialPreview)
        }
    }

    private fun saveFolder(){

        val folderId = requireArguments().getString("folderId")
        val uri: Uri = saveImageToInternalStorage(binding.imageTutorialPreview.id)
        val title = binding.edittxtTitleNewTutorial.text.toString()
        val date = Calendar.getInstance().time
        val tutorial = Tutorial(UUID.randomUUID(),folderId,title,uri.toString(),date)

        CoroutineScope(Dispatchers.IO).launch {
            val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
            val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
            val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
            val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

            repository.insert(tutorial)
        }
        fragmentManager?.popBackStack()

    }

    private fun saveImageToInternalStorage(drawableId:Int):Uri{
        // Get the image from drawable resource as drawable object

        val immagine = requireView().findViewById<ImageView>(drawableId)
        val drawable = immagine.drawable

        // Get the bitmap from drawable object
        var bitmap = (drawable as BitmapDrawable).bitmap

        if(bitmap.byteCount > 1000000){
            println("il peso finale dell' immagine in è")
            println(bitmap.byteCount)
            bitmap = getResizedBitmap(bitmap, 500)
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