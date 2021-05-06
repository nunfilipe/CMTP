package ipvc.estg.cmtp1.fragments

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import ipvc.estg.cmtp1.Listener.NavigationIconClickListener
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.api.Category
import ipvc.estg.cmtp1.api.EndPoints
import ipvc.estg.cmtp1.api.Event
import ipvc.estg.cmtp1.api.ServiceBuilder
import ipvc.estg.cmtp1.interfaces.NavigationHost
import kotlinx.android.synthetic.main.activity_main.view.app_bar
import kotlinx.android.synthetic.main.cmtp_backdrop.view.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_notes.*
import kotlinx.android.synthetic.main.fragment_notes.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback, SensorEventListener {

    private val REQUEST_LOCATION_PERMISSION = 1
    private var nMap: GoogleMap? = null
    private var idUser: Int? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var lastKnownLocation: Location? = null
    private val categoryList: MutableList<Category> = ArrayList<Category>()
    private var mSensorManager: SensorManager? = null
    private var mLight: Sensor? = null




    private fun isPermissionGranted(): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        ACCESS_FINE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED && context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location: Location? = task.result
                if (location == null) {
                    nMap!!.isMyLocationEnabled = true
                } else {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }

            nMap!!.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(ACCESS_FINE_LOCATION),
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
                enableMyLocation()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setUpSensorLight()

        val arr = resources.getStringArray(R.array.category)

        categoryList.add(Category(id="1", arr[0].toString()))
        categoryList.add(Category(id="2", arr[1].toString()))
        categoryList.add(Category(id="3", arr[2].toString()))
        categoryList.add(Category(id="4", arr[3].toString()))

        val view = inflater.inflate(R.layout.fragment_map, container, false)
        //view.app_bar.title = getString(R.string.app_name)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        idUser = getMe()

        // Set up the tool bar
        (activity as AppCompatActivity).setSupportActionBar(view.app_bar)
        view.app_bar.setNavigationOnClickListener(
            NavigationIconClickListener(
                activity!!, view.map_frame,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(context!!, R.drawable.ic_menu), // Menu open icon
                ContextCompat.getDrawable(context!!, R.drawable.ic_close_menu)
            )
        ) // Menu close icon

        view.app_bar_nota.setOnClickListener {
            (activity as NavigationHost).navigateTo(NoteFragment(), true, false)
        }

        view.app_bar_mapa.setOnClickListener {
            (activity as NavigationHost).navigateTo(MapFragment(), true, false)
        }

        if (idUser != 0) {
            view.cmtp_leave.isVisible = true
            view.app_bar_profile.isVisible = false
            view.cmtp_leave.setOnClickListener {
                (activity as NavigationHost).logout()
            }
        } else {
            view.app_bar_profile.isVisible = true
            view.cmtp_leave.isVisible = false
            view.app_bar_profile.setOnClickListener {
                (activity as NavigationHost).navigateTo(LoginFragment(), true, false)
            }
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)


        fabBtnCreateEvent.setOnClickListener {
            //(activity as NavigationHost).navigateTo(AddEventFragment(), true, false)
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                    ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@setOnClickListener
            }

            fusedLocationProviderClient.lastLocation.addOnSuccessListener(activity!!) { location ->
                if (idUser != 0) {
                    val bundle = Bundle()
                    bundle.putString("lt", "location")
                    bundle.putDouble("latitude", location.latitude)
                    bundle.putDouble("longitude", location.longitude)
                    (activity as NavigationHost).navigateToWithData(
                        AddEventFragment(),
                        addToBackstack = true,
                        animate = true,
                        tag = "location",
                        data = bundle
                    )
                } else {
                    (activity as NavigationHost).navigateTo(LoginFragment(), true, false)
                }
            }
        }

    }


    private fun getMe(): Int? {
        val sharedPref: SharedPreferences? =
            activity?.getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
        return sharedPref?.getInt("id_user", 0)
    }


    override fun onMapReady(googleMap: GoogleMap) {

        nMap = googleMap
        googleMap.clear()
        enableMyLocation()
        getDeviceLocation()

        googleMap.uiSettings.isZoomControlsEnabled = false //button zoom
        googleMap.uiSettings.isMapToolbarEnabled = false;

        getMarkers()

        nMap!!.setOnInfoWindowClickListener { marker ->
            val it: Event = (marker.tag as Event);
            val bundle = Bundle()
            bundle.putString("event_id", it.id)
            bundle.putString("user_id", it.user_id)
            bundle.putString("location", it.location)
            bundle.putDouble("latitude", it.latitude)
            bundle.putDouble("longitude", it.longitude)
            bundle.putString("photo", it.photo)
            bundle.putString("description", it.description)
            bundle.putString("date", it.date)
            bundle.putString("time", it.time)
            bundle.putInt("category", it.category.id.toInt())
            (activity as NavigationHost).navigateToWithData(
                EditEventFragment(),
                addToBackstack = true,
                animate = true,
                tag = "event",
                data = bundle
            )

        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (isPermissionGranted()) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            nMap!!.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        /*  nMap.moveCamera(CameraUpdateFactory
                              .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))*/
                        nMap!!.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter -> {
                (activity as NavigationHost).navigateTo(FilterFragment(), addToBackstack = true, animate = false)
                true
            }
            else -> false
        }
    }

    fun getMarkers(){
        if (ActivityCompat.checkSelfPermission(context!!, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        nMap!!.clear()
        val preferences: SharedPreferences = context!!.getSharedPreferences("FILTERMAP", Context.MODE_PRIVATE)
        val filter : HashMap<Int, Boolean> = HashMap<Int, Boolean> ()
        categoryList.forEach {
            filter[it.id.toInt()] = preferences.getBoolean(it.id,true)
        }

        val gson = Gson()


        val payload = Base64.encodeToString(
            gson.toJson(filter).toByteArray(charset("UTF-8")),
            Base64.DEFAULT
        )

        Log.e("payload",payload)

        val radius = preferences.getString("radius","0")!!.toFloat()

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(activity!!) { location ->

            if(radius != 0f){
                nMap!!.addCircle(
                    CircleOptions()
                        .center(LatLng(location.latitude, location.longitude))
                        .radius(radius.toDouble())
                        .strokeColor(R.color.cpb_blue_dark)
                        .fillColor(Color.TRANSPARENT)
                )
            }

            val call = request.getAllMarkers(payload)
            call.enqueue(object : Callback<List<Event>> {
                override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
                    Log.e("response",response.body().toString())
                    val data = response.body()
                    data.forEach {

                        val item = Event(
                            id = it.id,
                            user_id = it.user_id,
                            location = it.location,
                            latitude = it.latitude,
                            longitude = it.longitude,
                            photo = it.photo,
                            description = it.description,
                            date = it.date,
                            time = it.time,
                            category = it.category
                        )

                        if(radius!!.toFloat() == 0f){
                            val marker = nMap!!.addMarker(
                                MarkerOptions().position(LatLng(it.latitude, it.longitude))
                                    .title(it.location)
                            )
                            marker!!.tag = item
                        }else{
                            val locationA = Location("me")

                            locationA.latitude = location.latitude
                            locationA.longitude = location.longitude

                            val locationB = Location("marker")

                            locationB.latitude = item.latitude
                            locationB.longitude = item.longitude
                            val distance: Float = locationA.distanceTo(locationB)
                            Log.e("distance", distance.toString())
                            Log.e("locationA", locationA.toString())
                            Log.e("locationB", locationB.toString())
                            if (distance < radius) {
                                val marker = nMap!!.addMarker(
                                    MarkerOptions().position(LatLng(it.latitude, it.longitude))
                                        .title(it.location)
                                )
                                marker!!.tag = item
                            }
                        }


                    }

                }

                override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                    Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setUpSensorLight() {
        mSensorManager = context!!.getSystemService(SENSOR_SERVICE) as SensorManager
        mLight = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        if(nMap != null){
            getMarkers()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.values[0] < 20000.0) {
            nMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_in_night
                )
            );
        } else {
            nMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_in_day
                )
            );
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

}

