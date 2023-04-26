package com.example.steppo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.steppo.database.MyAppDatabase
import com.example.steppo.database.MyAppRepository
import com.example.steppo.databinding.ItemFolderBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


class TutorialHolder(private val binding : ItemFolderBinding, private val listener : TutorialListAdapter.ItemClickListener) : RecyclerView.ViewHolder(binding.root){

    fun bindToItem(tutorial: Tutorial, context: Context){

        binding.txtTitleItemFolder.text = tutorial.title
        binding.btnUploadFirebase.visibility = Button.VISIBLE

        try {
            binding.imageItemFolder.setImageURI(tutorial.image!!.toUri())
        }catch (e : Exception){
            println(e)
        }
        binding.root.setOnClickListener{
            val stepListFragment = StepFragment()
            val bundle = Bundle()
            bundle.putString("tutorialId", tutorial.id.toString())
            stepListFragment.arguments = bundle
            (itemView.context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .addToBackStack("TutorialListFragment")
                .replace(R.id.fragment_container, stepListFragment)
                .commit()
        }

        binding.deleteItemFolder.setOnClickListener {
            listener.btnClicked(tutorial, adapterPosition, "deleteItemTutorial")
        }

        binding.btnUploadFirebase.setOnClickListener {
            listener.btnClicked(tutorial,adapterPosition,"btnUploadFirebase")
        }

    }
}

class TutorialListAdapter (var tutorialList : List<Tutorial>,
                           val context: Context,
                           private val listener : TutorialListAdapter.ItemClickListener ) : RecyclerView.Adapter<TutorialHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFolderBinding.inflate(inflater, parent, false)
        return TutorialHolder(binding , listener)
    }

    override fun onBindViewHolder(holder: TutorialHolder, position: Int) {
        val tutorial = tutorialList[position]
        holder.bindToItem(tutorial, context)
    }

    override fun getItemCount(): Int {
        return tutorialList.count()
    }

    interface ItemClickListener {
        fun btnClicked(item : Tutorial, position: Int, buttonClicked : String)
    }

}