package com.example.steppo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.manager.Lifecycle
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.ItemFolderBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception


class FolderHolder(private val binding : ItemFolderBinding) : RecyclerView.ViewHolder(binding.root){

    fun bindToItem(folder : Folder, context: Context,  listener: FolderListAdapter.ItemClickListener){

        binding.txtTitleItemFolder.text = folder.title

        try {
          binding.imageItemFolder.setImageURI(folder.image!!.toUri())
        }catch (e : Exception){
            println(e)
        }

        //visualizza i tutorials della cartella cliccata inviando l'id della cartella
        binding.root.setOnClickListener{
            //onFolderClicked()
            val tutorialsListFragment = TutorialsListFragment()
            val bundle = Bundle()
            bundle.putString("folderId", folder.id.toString())
            tutorialsListFragment.arguments = bundle
            (itemView.context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .addToBackStack("FolderListFragment")
                .replace(R.id.fragment_container, tutorialsListFragment)
                .commit()
        }

        binding.deleteItemFolder.setOnClickListener {
            listener.btnClicked(folder,adapterPosition,"deleteItemFolder")
        }

    }
}

class FolderListAdapter (var foldersList : List<Folder>,val context: Context,
                         private val listener: FolderListAdapter.ItemClickListener)
    : RecyclerView.Adapter<FolderHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFolderBinding.inflate(inflater, parent, false)
        return FolderHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderHolder, position: Int) {
        val folder = foldersList[position]
        holder.bindToItem(folder, context, listener)
    }

    override fun getItemCount(): Int {
        return foldersList.count()
    }

    interface ItemClickListener {
        fun btnClicked(item : Folder, position: Int, buttonClicked : String)
    }


}