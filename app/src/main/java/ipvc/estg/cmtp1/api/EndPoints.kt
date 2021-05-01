package ipvc.estg.cmtp1.api

import retrofit2.Call
import retrofit2.http.*


interface EndPoints {

    @FormUrlEncoded
    @POST("/api/user/login")
    fun userLogin(@Field("payload") payload: String): Call<User>

    @GET("/api/event/getAll")
    fun getAllMarkers(): Call<List<Event>>

/*    @GET("/users/")
    fun getUsers(): Call<List<User>>

    @GET("/users/{id}")
    fun getUserById(@Path("id") id: Int): Call<User>

    @FormUrlEncoded
    @POST("/api/user/login")
    fun postTest(@Field("username") first: String?): Call<OutputPost>*/
}