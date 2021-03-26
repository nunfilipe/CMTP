package ipvc.estg.cmtp1.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.entities.Note

class NotesAdapter() :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    var listener:OnItemClickListener? = null
    var arrList = ArrayList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv_notes,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return arrList.size
    }

    fun setData(arrNotesList: List<Note>){
        arrList = arrNotesList as ArrayList<Note>
    }

    fun setOnClickListener(listener1: OnItemClickListener){
        listener = listener1
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {

        holder.tvTitle.text = arrList[position].title
        holder.tvDesc.text = arrList[position].noteText
        holder.tvDateTime.text = arrList[position].dateTime

        if (arrList[position].color != null){
            holder.cardView.setCardBackgroundColor(Color.parseColor(arrList[position].color))
        }else{
            holder.cardView.setCardBackgroundColor(Color.parseColor(R.color.ColorLightBlack.toString()))
        }

/*        if (arrList[position].imgPath != null){
            holder.itemView.imgNote.setImageBitmap(BitmapFactory.decodeFile(arrList[position].imgPath))
            holder.itemView.imgNote.visibility = View.VISIBLE
        }else{
            holder.itemView.imgNote.visibility = View.GONE
        }*/

        holder.cardView.setOnClickListener {
            listener!!.onClicked(arrList[position].id!!)
        }

    }

    class NotesViewHolder(view:View) : RecyclerView.ViewHolder(view){
            var tvTitle: TextView
            var tvDesc: TextView
            var tvDateTime: TextView
            var cardView: CardView

            init {
                tvTitle = view.findViewById(R.id.tvTitle)
                tvDesc = view.findViewById(R.id.tvDesc)
                tvDateTime = view.findViewById(R.id.tvDateTime)
                cardView = view.findViewById(R.id.cardView)
            }
    }


    interface OnItemClickListener{
        fun onClicked(noteId:Int)
    }


}