package ipvc.estg.cmtp1.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ipvc.estg.cmtp1.entities.Note
import ipvc.estg.cmtp1.dao.NoteDao
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the City class

// Note: When you modify the database schema, you'll need to update the version number and define a migration strategy
//For a sample, a destroy and re-create strategy can be sufficient. But, for a real app, you must implement a migration strategy.

@Database(entities = arrayOf(Note::class), version = 1, exportSchema = false)
public abstract class NoteDB : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    private class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val noteDao = database.noteDao()

                    // Delete all content here.
                    //noteDao.deleteAll()

                    // Add sample cities.
                    var note = Note(1, "title1", "12/3/2021 03:31:28", "note1", "#FFFFFF")
                    noteDao.insertNotes(note)
                    note = Note(2, "title2", "12/3/2021 03:31:28", "note2", "#000000")
                    noteDao.insertNotes(note)
                }
            }
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NoteDB? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NoteDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDB::class.java,
                    "note_database",
                )
                    //estratégia de destruição
                    .fallbackToDestructiveMigration()
                    .addCallback(WordDatabaseCallback(scope))
                    .build()

                INSTANCE = instance
                return instance
            }
        }

        fun getDatabase(context: Context): NoteDB {
            var notesDatabase: NoteDB? = null
            if (notesDatabase == null) {
                notesDatabase = Room.databaseBuilder(
                    context
                    , NoteDB::class.java
                    , "note_database"
                ).build()
            }
            return notesDatabase
        }
    }
/*companion object {
    var notesDatabase: NoteDB? = null

    @Synchronized
    fun getDatabase(context: Context): NoteDB {
        if (notesDatabase == null) {
            notesDatabase = Room.databaseBuilder(
                context
                , NoteDB::class.java
                , "note_database"
            ).build()
        }
        return notesDatabase!!
    }
}*/
}
