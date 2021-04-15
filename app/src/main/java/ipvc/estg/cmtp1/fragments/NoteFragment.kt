package ipvc.estg.cmtp1.fragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ipvc.estg.cmtp1.Listener.NavigationIconClickListener
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.adapter.NotesAdapter
import ipvc.estg.cmtp1.entities.Note
import ipvc.estg.cmtp1.interfaces.NavigationHost
import ipvc.estg.cmtp1.viewModel.NoteViewModel
import kotlinx.android.synthetic.main.activity_note_fragment.view.app_bar
import kotlinx.android.synthetic.main.cmtp_backdrop.view.*
import kotlinx.android.synthetic.main.fragment_notes.*
import kotlinx.android.synthetic.main.fragment_notes.view.*
import java.util.*
import kotlin.collections.ArrayList

class NoteFragment : Fragment(), NotesAdapter.NotesAdapterListener {

    private var actionMode: ActionMode? = null
    var arrNotes = ArrayList<Note>()
    private lateinit var noteViewModel: NoteViewModel
    private var notesAdapter: NotesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment with the ProductGrid theme
        val view = inflater.inflate(R.layout.fragment_notes, container, false)
        // Set up the tool bar
        (activity as AppCompatActivity).setSupportActionBar(view.app_bar)
        view.app_bar.setNavigationOnClickListener(
            NavigationIconClickListener(
                activity!!, view.note_grid,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(context!!, R.drawable.ic_menu), // Menu open icon
                ContextCompat.getDrawable(context!!, R.drawable.ic_close_menu)
            )
        ) // Menu close icon

        view.app_bar_nota.setOnClickListener {
            (activity as NavigationHost).navigateTo(NoteFragment(), true, false)
        }

        view.app_bar_mapa.setOnClickListener {
            (activity as NavigationHost).navigateTo(MapFragment(), true, false)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        // view model
        noteViewModel =
            ViewModelProvider.AndroidViewModelFactory(activity?.applicationContext as Application)
                .create(NoteViewModel::class.java)
        noteViewModel.allNotes.observe(this, Observer { notes ->
            // Update the cached copy of the words in the adapter.
            notes?.let {
                notesAdapter?.setNotes(it)
                notesAdapter = context?.let {
                    NotesAdapter(
                        notes as MutableList<Note>,
                        listener = this@NoteFragment,
                        context = it
                    )
                }
                notesAdapter!!.setHasStableIds(false)
                notesAdapter!!.notifyItemRangeInserted(0, notes.size - 1)
                recycler_view!!.adapter = notesAdapter
            }
        })

        fabBtnCreateNote.setOnClickListener {
            (activity as NavigationHost).navigateTo(CreateNoteFragment(), true, false)
        }

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {

                val tempArr = ArrayList<Note>()

                for (arr in arrNotes) {
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(p0.toString())) {
                        tempArr.add(arr)
                    }
                }
                notesAdapter?.notifyDataSetChanged()
                return true
            }
        })
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            actionMode = (activity as AppCompatActivity?)?.startSupportActionMode(object :
                ActionMode.Callback {
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    if (item?.itemId == R.id.action_delete) {
                        val toDelete = notesAdapter!!.deleteNotes()
                        Log.e("todelete", toDelete.toString())
                        //noteViewModel = ViewModelProvider.AndroidViewModelFactory(activity?.applicationContext as Application).create(NoteViewModel::class.java)
                        toDelete.forEach {
                            it.id?.let { it1 -> noteViewModel.deleteSpecificNote(it1) }
                            //notesAdapter?.notifyDataSetChanged()
                            Log.e("delete", it.id.toString())
                        }
                        notesAdapter?.notifyDataSetChanged()
                        mode?.finish()
                        return true
                    }
                    return false
                }

                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    mode?.menuInflater?.inflate(R.menu.menu_delete_note, menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    notesAdapter?.selectedItems?.clear()
                    notesAdapter?.notifyDataSetChanged()
                    notesAdapter?.arrList
                        ?.filter { it.selected }
                        ?.forEach { it.selected = false }
                    actionMode = null
                }
            })
        }

        notesAdapter?.toggleSelection(position)
        val size = notesAdapter?.selectedItems?.size()
        if (size == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = "$size Selected"
            actionMode?.invalidate()
        }
    }

    override fun onNotePress(note: Note?, position: Int) {
        val bundle = Bundle()
        bundle.putString("destination", "view_note")
        bundle.putInt("noteId", note!!.id!!)
        (activity as NavigationHost).navigateToWithData(
            CreateNoteFragment(),
            addToBackstack = true,
            animate = true,
            tag = "view_note",
            data = bundle
        )
    }

    override fun onNoteLongPress(note: Note?, position: Int) {
        enableActionMode(position)
    }
}