package ipvc.estg.cmtp1.api

data class Event(
    val id: String,
    val user_id: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val photo: String,
    val description: String,
    val date: String,
    val time: String,
    val category: Category
)

data class Category(
    val id: String,
    val category: String
)

