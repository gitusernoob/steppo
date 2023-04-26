package com.example.steppo
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.steppo.databinding.ItemFolderBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.lang.Exception


class TutorialFirebaseHolder(private val binding : ItemFolderBinding, private val listener : TutorialListFirebaseAdapter.ItemClickListener) : RecyclerView.ViewHolder(binding.root){

    fun bindToItem(tutorial: Tutorial, context: Context){

        binding.txtTitleItemFolder.text = tutorial.title
        binding.btnDownloadFirebase.visibility = Button.VISIBLE

        try {
            Glide.with(context)
                .load(tutorial.image)
                .into(binding.imageItemFolder)

        }catch (e : Exception){
            println(e)
        }
        binding.root.setOnClickListener{
            val stepListFirebase = StepListFirebase()
            val bundle = Bundle()
            bundle.putString("tutorialKeyFirebase", tutorial.folderId.toString()) // folderId usato temporaneamente
            stepListFirebase.arguments = bundle
            (itemView.context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .addToBackStack("TutorialListFragment")
                .replace(R.id.fragment_container, stepListFirebase)
                .commit()
        }

        binding.deleteItemFolder.setOnClickListener {
            listener.btnClicked(tutorial, adapterPosition, "deleteItemTutorial")
        }

        binding.btnDownloadFirebase.setOnClickListener {
            listener.btnClicked(tutorial,adapterPosition,"btnDownloadFirebase")
        }

    }
}

class TutorialListFirebaseAdapter (var tutorialList : ArrayList<Tutorial>,
                                   val context: Context,
                                   private val listener : TutorialListFirebaseAdapter.ItemClickListener
                                   ) : RecyclerView.Adapter<TutorialFirebaseHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialFirebaseHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFolderBinding.inflate(inflater, parent, false)
        return TutorialFirebaseHolder(binding , listener)
    }

    override fun onBindViewHolder(holder: TutorialFirebaseHolder, position: Int) {
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