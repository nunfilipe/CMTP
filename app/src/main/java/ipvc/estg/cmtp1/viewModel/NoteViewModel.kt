package ipvc.estg.cmtp1.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ipvc.estg.cmtp1.db.NoteDB
import ipvc.estg.cmtp1.db.NoteRepository
import ipvc.estg.cmtp1.entities.Note
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    var allNotes: LiveData<List<Note>>


    init {
        val notesDao = NoteDB.getDatabase(application, viewModelScope).noteDao()
        repository = NoteRepository(notesDao)
        allNotes = repository.allNotes
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insertNotes(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNotes(note)
    }

    /*
        // delete all
        fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
*/
        // delete by city
    fun deleteSpecificNote(id:Int) = viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSpecificNote(id)
        }

    /* fun getNoteByID(id: Int): LiveData<Note> {
         return repository.getNoteByID(id)
     }*/

    fun getSpecificNote(id: Int): LiveData<Note> {
        return repository.getSpecificNote(id)
    }

/*
    fun getCountryFromCity(city: String): LiveData<City> {
        return repository.getCountryFromCity(city)
    }
*/
    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }
/*
    fun updateCountryFromCity(city: String, country: String) = viewModelScope.launch {
        repository.updateCountryFromCity(city, country)
    }*/
}