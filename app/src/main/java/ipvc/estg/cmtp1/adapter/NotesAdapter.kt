package ipvc.estg.cmtp1.adapter

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.util.isEmpty
import androidx.core.util.isNotEmpty
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.entities.Note

class NotesAdapter(notesList: MutableList<Note>, listener: NotesAdapterListener,context: Context) : RecyclerView.Adapter<NotesAdapter.MyViewHolder>(){
    var listener: NotesAdapterListener? = null
    var arrList : MutableList<Note>
    val selectedItems = SparseBooleanArray()
    private var currentSelectedPos: Int = -1
    private var notes = emptyList<Note>() // Cached copy of notes

    interface NotesAdapterListener {
        fun onNotePress(note: Note?, position: Int)
        fun onNoteLongPress(note: Note?,position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesAdapter.MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_notes, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return arrList.size
    }

    fun toggleSelection(position: Int) {
        currentSelectedPos = position
        if (selectedItems[position]) {
            selectedItems.delete(position)
            arrList[position].selected = false
        } else {
            selectedItems.put(position, true)
            arrList[position].selected = true
        }

        notifyItemChanged(position)
    }

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView? = null
        var tvDesc: TextView? = null
        var tvDateTime: TextView? = null
        var cardView: CardView? = null
        var noteSelect: ImageView? = null

        init {
            tvTitle = view.findViewById(R.id.tvTitle)
            tvDesc = view.findViewById(R.id.tvDesc)
            tvDateTime = view.findViewById(R.id.tvDateTime)
            cardView = view.findViewById(R.id.cardView)
            noteSelect = view.findViewById(R.id.noteSelect)
        }

        fun bind(note: Note) {
            with(note) {
                if (note.selected) {
                    noteSelect!!.visibility = View.VISIBLE
                } else {
                    noteSelect!!.visibility = View.INVISIBLE
                }
            }
        }

    }

    fun deleteNotes():MutableList<Note> {

        val filtered =  arrList.filter {
            it.selected
        }


        arrList.removeAll(filtered)
        notifyDataSetChanged()
        currentSelectedPos = -1

        return filtered as MutableList<Note>
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvTitle!!.text = arrList[position].title
        holder.tvDesc!!.text = arrList[position].noteText
        holder.tvDateTime!!.text = arrList[position].dateTime
        holder.bind(arrList[position])

        if (arrList[position].color != null) {
            holder.cardView!!.setCardBackgroundColor(Color.parseColor(arrList[position].color))
        } else {
            holder.cardView!!.setCardBackgroundColor(Color.parseColor(R.color.ColorLightBlack.toString()))
        }

        holder.cardView!!.setOnClickListener {
            if(selectedItems.isEmpty()){
                listener!!.onNotePress(arrList[position],position)
            }
        }

        holder.cardView!!.setOnLongClickListener {
            listener!!.onNoteLongPress(arrList[position],position)
            return@setOnLongClickListener true
        }

        if (currentSelectedPos == position) currentSelectedPos = -1


    }

    fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }


    init {
        setHasStableIds(false)
        this.arrList = notesList
        this.listener = listener
    }

}