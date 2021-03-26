package ipvc.estg.cmtp1.dao

import ipvc.estg.cmtp1.entities.Note
import androidx.room.*

@Dao
interface NoteDao {

    @Query("SELECT * FROM note_table ORDER BY id DESC")
    suspend fun getAllNotes() : List<Note>

    @Query("SELECT * FROM note_table WHERE id =:id")
    suspend fun getSpecificNote(id:Int) : Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(note:Note)

    @Delete
    suspend fun deleteNote(note:Note)

    @Query("DELETE FROM note_table WHERE id =:id")
    suspend fun deleteSpecificNote(id:Int)

    @Update
    suspend fun updateNote(note:Note)

//    @Query("DELETE FROM city_table")
//    suspend fun deleteAll()

//    @Query("UPDATE city_table SET country=:country WHERE city == :city")
//    suspend fun updateCountryFromCity(city: String, country: String)
}