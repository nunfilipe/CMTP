package ipvc.estg.cmtp1.fragments

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.entities.Note
import ipvc.estg.cmtp1.util.NoteBottomSheetFragment
import ipvc.estg.cmtp1.viewModel.NoteViewModel
import kotlinx.android.synthetic.main.fragment_create_note.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteFragment : BaseFragment(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    var selectedColor = "#171C26"
    var currentDate: String? = null
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456
    var destination: String? = null
    var noteId: Int? = 0
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_note, container, false)
        val bundle = this.arguments
        if (bundle != null) {
            destination = bundle.getString("destination")
            noteId = bundle.getInt("noteId", 0)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (noteId != 0) {
            // view model
            noteViewModel =
                ViewModelProvider.AndroidViewModelFactory(activity?.applicationContext as Application)
                    .create(NoteViewModel::class.java)
            noteId?.let {
                noteViewModel.getSpecificNote(noteId!!).observe(this, Observer { notes ->
                    colorView.setBackgroundColor(Color.parseColor(notes.color.toString()))
                    etNoteTitle.setText(notes.title)
                    etNoteDesc.setText(notes.noteText)
                })
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )

        val sdf = SimpleDateFormat("dd/M/yyyy")

        currentDate = sdf.format(Date())
        colorView.setBackgroundColor(Color.parseColor(selectedColor))

        tvDateTime.text = currentDate

        imgDone.setOnClickListener {
            if (noteId != 0) {
                updateNote()
            } else {
                saveNote()
            }
        }

        imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        imgMore.setOnClickListener {
            val noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId!!)
            noteBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                "Note Bottom Sheet Fragment"
            )
        }
    }

    private fun updateNote() {
        if (etNoteTitle.text.isNullOrEmpty()) {
            Toast.makeText(context, "Note Title is Required", Toast.LENGTH_SHORT).show()
        } else {
            noteViewModel =
                ViewModelProvider.AndroidViewModelFactory(activity?.applicationContext as Application)
                    .create(NoteViewModel::class.java)
            noteId?.let {
                noteViewModel.getSpecificNote(noteId!!).observe(this, Observer { notes ->
                    notes.title = etNoteTitle.text.toString()
                    notes.noteText = etNoteDesc.text.toString()
                    notes.dateTime = currentDate
                    notes.color = selectedColor
                    noteViewModel.updateNote(notes)
                    etNoteTitle.setText("")
                    etNoteDesc.setText("")
                    requireActivity().supportFragmentManager.popBackStack()
                })
            }
        }
    }

    private fun saveNote() {
        if (etNoteTitle.text.isNullOrEmpty()) {
            etNoteTitle.error = "Note Title is Required"
            Toast.makeText(context, "Note Title is Required", Toast.LENGTH_SHORT).show()
        } else {
            val note = Note()
            note.title = etNoteTitle.text.toString()
            note.noteText = etNoteDesc.text.toString()
            note.dateTime = currentDate
            note.color = selectedColor
            noteViewModel =
                ViewModelProvider.AndroidViewModelFactory(activity?.applicationContext as Application)
                    .create(NoteViewModel::class.java)
            noteViewModel.insertNotes(note)
            etNoteTitle.setText("")
            etNoteDesc.setText("")
            layoutImage.visibility = View.GONE
            imgNote.visibility = View.GONE
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    private val BroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val actionColor = p1!!.getStringExtra("action")
            when (actionColor!!) {
                "Blue" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Yellow" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Purple" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Green" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Orange" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Black" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "DeleteNote" -> {
                    //delete note
                    deleteNote()
                }
                else -> {
                    /*layoutImage.visibility = View.GONE
                    imgNote.visibility = View.GONE
                    layoutWebUrl.visibility = View.GONE*/
                    selectedColor = p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
        }
    }

    private fun deleteNote() {
        noteViewModel = ViewModelProvider.AndroidViewModelFactory(activity?.applicationContext as Application).create(NoteViewModel::class.java)
        noteViewModel.deleteSpecificNote(noteId!!)
        requireActivity().supportFragmentManager.popBackStack()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()
    }

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUrl = data.data
                if (selectedImageUrl != null) {
                    try {
                        val inputStream =
                            requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility = View.VISIBLE
                        layoutImage.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            requireActivity()
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)) {
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onRationaleAccepted(requestCode: Int) {

    }
}

