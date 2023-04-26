package com.example.steppo
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.steppo.databinding.ItemStepBinding

class StepHolder( private val binding : ItemStepBinding) : RecyclerView.ViewHolder(binding.root){

    fun bindToItem(step: Step, listener: StepListAdapter.ItemClickListener){

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
            binding.imageItemStep.setImageURI(step.image!!.toUri())
        }catch (e : Exception){ println(e) }

        //visualizza l'immagine ingrandita se cliccata
        binding.imageItemStep.setOnClickListener{
            listener.btnClicked(step, adapterPosition, "imageItemStep" )
        }

        //mostra gli elementi per la modifica dello step
        binding.btnModifyStep.setOnClickListener {
            //binding.editTxtTitleItemStep.visibility = EditText.VISIBLE
            binding.editTxtDescriptionItemStep.visibility = EditText.VISIBLE
            binding.btnChooseImageStep.visibility = Button.VISIBLE
            binding.btnSaveStep.visibility = Button.VISIBLE

            binding.btnModifyStep.visibility = Button.GONE
            //binding.TxtTitleItemStep.visibility = TextView.GONE
            binding.TxtDescriptionItemStep.visibility = TextView.GONE
        }

        //tasto per salvare modifiche
        binding.btnSaveStep.setOnClickListener {
            //nascondi tutti gli elementi di layout per modificare
            //binding.editTxtTitleItemStep.visibility = EditText.GONE
            binding.editTxtDescriptionItemStep.visibility = EditText.GONE
            binding.btnChooseImageStep.visibility = Button.GONE
            binding.btnSaveStep.visibility = Button.GONE
            //mostra i textview al posto degli edittext
            binding.btnModifyStep.visibility = Button.VISIBLE
            //binding.TxtTitleItemStep.visibility = TextView.VISIBLE
            binding.TxtDescriptionItemStep.visibility = TextView.VISIBLE
            // salva modifiche
            step.title = binding.editTxtTitleItemStep.text.toString()
            step.description = binding.editTxtDescriptionItemStep.text.toString()
            listener.btnClicked(step, adapterPosition, "btnSaveStep" )
        }

        //aggiunge una card subito sotto la card cliccata
        binding.btnNewCardDownItemStep.setOnClickListener {
            listener.btnClicked(step, adapterPosition, "btnNewCardDownItemStep")
        }

        //cambia l'immagine scegliendo da galleria. salva eventuali modifiche negli edittext
        binding.btnChooseImageStep.setOnClickListener {
            step.title = binding.editTxtTitleItemStep.text.toString()
            step.description = binding.editTxtDescriptionItemStep.text.toString()
            listener.btnClicked(step, adapterPosition, "btnChooseImageStep")
        }

        //visualizza un alert dialog per elimina la scheda
        binding.imageButtonDeleteStep.setOnClickListener {
            listener.btnClicked(step, adapterPosition, "imageButtonDeleteStep")
        }

        //nasconde gli elementi layout per modificare
        //binding.editTxtTitleItemStep.visibility = EditText.GONE
        binding.editTxtDescriptionItemStep.visibility = EditText.GONE
        binding.btnChooseImageStep.visibility = Button.GONE
        binding.btnSaveStep.visibility = Button.GONE
        //mostra gli elementi per visualizzare
        binding.btnModifyStep.visibility = Button.VISIBLE
        //binding.TxtTitleItemStep.visibility = TextView.VISIBLE
        binding.TxtDescriptionItemStep.visibility = TextView.VISIBLE

    }

}

class StepListAdapter (var stepList : List<Step>,val context: Context, private val listener: ItemClickListener )
    : RecyclerView.Adapter<StepHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemStepBinding.inflate(inflater, parent, false)
        return StepHolder(binding)
    }

    override fun onBindViewHolder(holder: StepHolder, position: Int) {
        val step = stepList[position]
        holder.bindToItem(step, listener)
    }

    override fun getItemCount(): Int {
        return stepList.count()
    }

    interface ItemClickListener {
        fun btnClicked(item : Step, position: Int, buttonClicked : String)
    }

}