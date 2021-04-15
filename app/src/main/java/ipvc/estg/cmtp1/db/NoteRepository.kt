package ipvc.estg.cmtp1.db

import androidx.lifecycle.LiveData
import ipvc.estg.cmtp1.dao.NoteDao
import ipvc.estg.cmtp1.entities.Note

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class NoteRepository(private val noteDao: NoteDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    fun getSpecificNote(id:Int) : LiveData<Note> {
        return noteDao.getSpecificNote(id)
    }

    suspend fun insertNotes(note: Note) {
        noteDao.insertNotes(note)
    }
/*
    fun getCountryFromCity(city: String): LiveData<City> {
        return cityDao.getCountryFromCity(city)
    }

    suspend fun deleteAllCheck(){
        noteDao.deleteAllCheck()
    }
    */
    suspend fun deleteSpecificNote(id:Int){
        noteDao.deleteSpecificNote(id)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
/*
    suspend fun updateCountryFromCity(city: String, country: String){
        cityDao.updateCountryFromCity(city, country)
    }*/
}