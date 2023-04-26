package com.example.steppo
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.steppo.databinding.ItemFolderBinding
import com.example.steppo.databinding.ItemStepBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.lang.Exception


class StepListFirebaseHolder(private val binding : ItemStepBinding, private val listener : StepListFirebaseAdapter.ItemClickListener) : RecyclerView.ViewHolder(binding.root){

    fun bindToItem(step: Step, context: Context, listener: StepListFirebaseAdapter.ItemClickListener){

        //inserisce il titolo
        if(step.title != null){
            binding.editTxtTitleItemStep.setText(step.title.toString())
            binding.TxtTitleItemStep.text = (step.title.toString())
        }

        //inserisce la descrizione
        if(step.description != null){
            binding.editTxtDescriptionItemStep.setText(step.description.toString())
            binding.TxtDescriptionItemStep.text = (step.description.toString())
        }

        //inserisce l'immagine
        try {
            Glide.with(context)
                .load(step.image)
                .into(binding.imageItemStep)
        }catch (e : Exception){ println(e) }

        //visualizza l'immagine ingrandita se cliccata
        binding.imageItemStep.setOnClickListener{
            listener.btnClicked(step, adapterPosition, "imageItemStep" )
        }

        //nasconde tutti gli elementi per la modifica tutorial offline
        binding.editTxtDescriptionItemStep.visibility = EditText.GONE
        binding.btnChooseImageStep.visibility = Button.GONE
        binding.btnSaveStep.visibility = Button.GONE
        binding.btnModifyStep.visibility = Button.GONE
        binding.btnNewCardDownItemStep.visibility = Button.GONE
        binding.imageButtonDeleteStep.visibility = ImageButton.GONE

    }
}

class StepListFirebaseAdapter (var stepList : List<Step>,
                                   val context: Context,
                                   private val listener : StepListFirebaseAdapter.ItemClickListener
) : RecyclerView.Adapter<StepListFirebaseHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepListFirebaseHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemStepBinding.inflate(inflater, parent, false)
        return StepListFirebaseHolder(binding , listener)
    }

    override fun onBindViewHolder(holder: StepListFirebaseHolder, position: Int) {
        val step = stepList[position]
        holder.bindToItem(step, context,  listener)
    }

    override fun getItemCount(): Int {
        return stepList.count()
    }

    interface ItemClickListener {
        fun btnClicked(item : Step, position: Int, buttonClicked : String)
    }

}