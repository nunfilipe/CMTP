package ipvc.estg.cmtp1.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ipvc.estg.cmtp1.R
import kotlinx.android.synthetic.main.fragment_edit_event.*
import java.util.*
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ipvc.estg.cmtp1.api.EndPoints
import ipvc.estg.cmtp1.api.Event
import ipvc.estg.cmtp1.api.ServiceBuilder
import kotlinx.android.synthetic.main.fragment_add_event.*
import kotlinx.android.synthetic.main.fragment_add_event.view.*
import kotlinx.android.synthetic.main.fragment_edit_event.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


class EditEventFragment : Fragment() {

    private val REQUEST_LOCATION_PERMISSION = 1

    private var photo: String? = null
    private var userId: String? = null
    private var idUser: Int? = null
    private var eventId: String? = null
    private var description: String? = null
    private var image: Bitmap? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var payload: String? = null
    private var idToken: String? = null
    private var category: Int? = null
    private var spinner: Spinner? = null


    private fun isPermissionGranted() : Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.CAMERA
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    private fun capturePhoto() {
        if (isPermissionGranted()) {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.CAMERA
                    )
                } != PackageManager.PERMISSION_GRANTED && context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.CAMERA
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_LOCATION_PERMISSION)
        }
        else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                capturePhoto()
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_event, container, false)
        idUser = getMe()
        idToken = getMeToken()
        declareItems(view)
        val bundle = this.arguments
        if (bundle != null) {
            userId = bundle.getString("user_id", null)
            latitude = bundle.getDouble("latitude", 0.0)
            longitude = bundle.getDouble("longitude", 0.0)
            photo = bundle.getString("photo",null)
            description = bundle.getString("description",null)
            val imageBytes = Base64.decode(photo, 0)
            image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            eventId = bundle.getString("event_id", null)
            category = bundle.getInt("category", 1)
            category = category!!-1
            setMiniMap()
        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(userId!! != idUser.toString()){
            btn_capture_edit.isVisible = false
            spinner_category_edit.isEnabled = false
            text_description_event_edit.isEnabled = false
            btn_saveEvent_edit.isVisible = false
        }
        if (spinner != null) ArrayAdapter.createFromResource(
            activity!!,
            R.array.category,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner!!.adapter = adapter
        }
        spinner!!.setSelection(category!!)
        image_view_edit.background = null
        image_view_edit.setImageBitmap(image)
        text_description_event_edit.setText(description)


        close_edit_event.setOnClickListener {
            activity!!.onBackPressed()
        }
        btn_capture_edit.setOnClickListener {
            capturePhoto()
        }

        btn_saveEvent_edit.setOnClickListener {
            if (text_description_event_edit.text.isNullOrEmpty()) {
                text_description_event_edit.error = getString(R.string.text_description_required)
            } else if (image_view_edit.drawable == null) {
                add_textview_foto_edit.error = getString(R.string.text_description_required)
            }else{
                btn_saveEvent_edit!!.isEnabled = false
                val obj = JSONObject()
                 obj.put("id", eventId)
               if (image != null) {
                    val bos = ByteArrayOutputStream()
                    image!!.compress(Bitmap.CompressFormat.JPEG, 60, bos)
                    val image: ByteArray = bos.toByteArray()
                    val base64Encoded = java.util.Base64.getEncoder().encodeToString(image)
                    obj.put("image", base64Encoded)
                }
                description = view.text_description_event_edit.text.toString()
                obj.put("description", description)

                payload = obj.toString()
                payload = Base64.encodeToString(
                    obj.toString().toByteArray(charset("UTF-8")), Base64.DEFAULT
                )
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.updateEvent(payload = payload!!, auth = idToken!!)

                call.enqueue(object : Callback<Event> {
                    override fun onResponse(call: Call<Event>, response: Response<Event>) {
                        if (response.isSuccessful) {
                            btn_saveEvent_edit!!.isEnabled = true
                            Toast.makeText(activity, R.string.toast_edit_event, Toast.LENGTH_LONG).show()
                            activity!!.onBackPressed()
                        } else {
                            btn_saveEvent_edit!!.isEnabled = true
                            Toast.makeText(context!!, getString(R.string.toast_fail_edit_event), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Event>, t: Throwable) {
                        Log.i("Failure", t.toString())
                        Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_LOCATION_PERMISSION && data != null){
            image = data.extras!!.get("data") as Bitmap
            image_view_edit.setImageBitmap(image)
        }
    }

    private fun setMiniMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.location_img) as SupportMapFragment?
        Objects.requireNonNull(mapFragment!!.view)!!.isClickable = false
        mapFragment.getMapAsync(OnMapReadyCallback {
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!, longitude!!), 14f))
            it.addMarker(
                MarkerOptions().position(LatLng(latitude!!, longitude!!)).draggable(false)
            )
        });
    }

    private fun getMe(): Int? {
        val sharedPref: SharedPreferences? =
            activity!!.getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
        return sharedPref?.getInt("id_user", 0)
    }

    private fun getMeToken(): String? {
        val sharedPref: SharedPreferences? =
            activity!!.getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun declareItems(view: View) {
        spinner = view.findViewById(R.id.spinner_category_edit)
    }

}