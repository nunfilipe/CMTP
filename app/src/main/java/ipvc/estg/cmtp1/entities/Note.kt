package ipvc.estg.cmtp1.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
class Note (

    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,

    @ColumnInfo(name = "title")
    var title:String? = null,

    @ColumnInfo(name = "date_time")
    var dateTime:String? = null,

    @ColumnInfo(name = "note_text")
    var noteText:String? = null,

    @ColumnInfo(name = "color")
    var color:String? = null

)
