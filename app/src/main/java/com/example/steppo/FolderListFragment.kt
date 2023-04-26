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

import com.example.steppo.databinding.FragmentFolderListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class FolderListFragment : Fragment(), FolderListAdapter.ItemClickListener{

    private var _binding: FragmentFolderListBinding? = null
    private val binding get() = _binding!!
    private  val foldersListViewmModel: FolderListViewModel by viewModels()
    private lateinit var layoutManager : LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFolderListBinding.inflate(layoutInflater, container, false)
        layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFolderList.layoutManager  = layoutManager


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerSetUp()

        binding.btnNewFolder.setOnClickListener {
            val fragment = FolderCreateFragment()
            parentFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .addToBackStack("FolderListFragment")
                .commit()
        }

    }

    private fun recyclerSetUp(){


        val adapter = FolderListAdapter(emptyList(), requireContext(),this)
        binding.recyclerFolderList.adapter = adapter


        foldersListViewmModel.getFolders(requireContext()).observe(viewLifecycleOwner) { folders ->
            adapter.foldersList = folders
            adapter.notifyDataSetChanged()
            layoutManager.scrollToPosition(foldersListViewmModel.recyclerViewPosition)
        }

        binding.recyclerFolderList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != -1){
                    foldersListViewmModel.recyclerViewPosition  =position
                }

            }
        })

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun btnClicked(item: Folder, position: Int, buttonClicked: String) {

        if(buttonClicked == "deleteItemFolder"){
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Elimina Cartella")
            builder.setMessage("Sei sicuro di voler cancellare questo elemento?")
            builder.setPositiveButton("Elimina") { dialog, which ->
                deleteFolder(item)
            }
            builder.setNegativeButton("Annulla", null)

            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun deleteFolder(folder : Folder){
        val folderDao = MyAppDatabase.getInstance(requireContext()).folderDao()
        val tutorialDao = MyAppDatabase.getInstance(requireContext()).tutorialDao()
        val stepDao = MyAppDatabase.getInstance(requireContext()).stepDao()
        val repository = MyAppRepository(folderDao, tutorialDao, stepDao)

        //elimino tutti i tutorial della cartella
        var folderTutorials = repository.allTutorials(folder.id.toString())
        folderTutorials.observe(viewLifecycleOwner) {folders ->
            for (i in folders){

                println("eliminato tutorial: ${i.title}")
                CoroutineScope(Dispatchers.IO).launch {
                    repository.delete(i) //elimino il tutorial
                    val fileUri: Uri = i.image!!.toUri()// URI del file che vuoi eliminare
                    val fileToDelete = File(fileUri.path!!) // Creazione di un oggetto file dal path dell'URI
                    if (fileToDelete.exists()) {
                        fileToDelete.delete()
                        println("immagine tutorial eliminata") // Eliminazione del file
                    } else {
                        println("immagine tutorial non esiste")
                    }
                }

                //elimino gli step di ogni tutorial della cartella
                var stepsTutorial = repository.allSteps(i.id.toString())
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
            }
        }


        // elimino la cartella e l'immagine associata
        CoroutineScope(Dispatchers.IO).launch {
            repository.delete(folder)
            val fileUri: Uri = folder.image!!.toUri()// URI del file che vuoi eliminare
            val fileToDelete = File(fileUri.path!!) // Creazione di un oggetto file dal path dell'URI
            if (fileToDelete.exists()) {
                fileToDelete.delete()
                println("immagine cartella eliminata") // Eliminazione del file
            } else {
                println("immagine cartella non esiste")
            }
        }
        Toast.makeText(context, "Cartella eliminata", Toast.LENGTH_SHORT).show()
    }

}