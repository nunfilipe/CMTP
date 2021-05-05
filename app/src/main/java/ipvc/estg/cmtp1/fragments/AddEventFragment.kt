package ipvc.estg.cmtp1.fragments

import android.Manifest
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.api.EndPoints
import ipvc.estg.cmtp1.api.Event
import ipvc.estg.cmtp1.api.ServiceBuilder
import kotlinx.android.synthetic.main.fragment_add_event.*
import kotlinx.android.synthetic.main.fragment_add_event.view.*
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*


class AddEventFragment : Fragment() {

    private var idUser: Int? = null
    private var idToken: String? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private var location: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var payload: String? = null
    private var spinner: Spinner? = null
    private var image: Bitmap? = null
    var description: String? = null

    private fun isPermissionGranted() : Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                CAMERA
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    private fun capturePhoto() {
        if (isPermissionGranted()) {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        CAMERA
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
                arrayOf(CAMERA),
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_event, container, false)
        //view.app_bar.title = getString(R.string.app_name)
        declareItems(view)
        idUser = getMe()
        idToken = getMeToken()

        val bundle = this.arguments
        if (bundle != null) {
            location = bundle.getString("location")
            latitude = bundle.getDouble("latitude", 0.0)
            longitude = bundle.getDouble("longitude", 0.0)
            setMiniMap()
        }
        return view
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        close_add_event.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        btn_capture.setOnClickListener {
            capturePhoto()
        }

        btn_saveEvent.setOnClickListener {
            if (text_description_event.text.isNullOrEmpty()) {
                text_description_event.error = getString(R.string.text_description_required)
            } else if (image_view.drawable == null) {
                add_textview_foto.error = getString(R.string.text_description_required)
            }else{
                    description = view.text_description_event.text.toString()
                    val obj = JSONObject()
                    btn_saveEvent!!.isEnabled = false
                    obj.put("user_id", idUser)
                    location = getCompleteAddressString(latitude!!, longitude!!)
                    obj.put("location", location)
                    obj.put("latitude", latitude)
                    obj.put("longitude", longitude)
                    if (image != null) {
                        val bos = ByteArrayOutputStream()
                        image!!.compress(Bitmap.CompressFormat.JPEG, 60, bos)
                        val image: ByteArray = bos.toByteArray()
                        val base64Encoded = java.util.Base64.getEncoder().encodeToString(image)
                        obj.put("image", base64Encoded)
                    }
                    obj.put("description", description)
                    payload = obj.toString()
                    payload = Base64.encodeToString(
                        payload?.toByteArray(charset("UTF-8")), Base64.DEFAULT
                    )
                    val request = ServiceBuilder.buildService(EndPoints::class.java)
                    val call = request.insertPoint(payload = payload!!, auth = idToken!!)

                    call.enqueue(object : Callback<Event> {
                        override fun onResponse(call: Call<Event>, response: Response<Event>) {
                            if (response.isSuccessful) {
                                btn_saveEvent!!.isEnabled = true
                                activity!!.onBackPressed()
                                Toast.makeText(
                                    context,
                                    getString(R.string.toast_add_event),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                btn_saveEvent!!.isEnabled = true
                                Toast.makeText(context, getString(R.string.welcom), Toast.LENGTH_LONG)
                                    .show()
                            }
                        }

                        override fun onFailure(call: Call<Event>, t: Throwable) {
                            Log.i("Failure", t.toString())
                            Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
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
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_LOCATION_PERMISSION && data != null){
            image_view.background = null
            image = data.extras!!.get("data") as Bitmap
            image_view.setImageBitmap(image)
        }
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

    private fun setMiniMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.location_img) as SupportMapFragment?
        Objects.requireNonNull(mapFragment!!.view)!!.isClickable = false
        mapFragment.getMapAsync(OnMapReadyCallback {
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!, longitude!!), 10f))
            it.addMarker(
                MarkerOptions().position(LatLng(latitude!!, longitude!!)).draggable(false)
            )
        });
    }

    private fun declareItems(view: View) {
        spinner = view.findViewById(R.id.spinner_category)
    }

    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(activity, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                //Log.w("Current_address", strReturnedAddress.toString())
            } else {
                //Log.w("No_Current_address", "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //Log.w("Canont_Current_ address", "Canont get Address!")
        }
        return strAdd
    }

}

