package ipvc.estg.cmtp1.fragments

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ipvc.estg.cmtp1.Listener.NavigationIconClickListener
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.adapter.NotesAdapter
import ipvc.estg.cmtp1.api.Category
import ipvc.estg.cmtp1.api.EndPoints
import ipvc.estg.cmtp1.api.Event
import ipvc.estg.cmtp1.api.ServiceBuilder
import ipvc.estg.cmtp1.entities.Note
import ipvc.estg.cmtp1.interfaces.NavigationHost
import ipvc.estg.cmtp1.viewModel.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.view.app_bar
import kotlinx.android.synthetic.main.cmtp_backdrop.view.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_notes.*
import kotlinx.android.synthetic.main.fragment_notes.fabBtnCreateNote
import kotlinx.android.synthetic.main.fragment_notes.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback {

    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var nMap: GoogleMap
    private var idUser: Int? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var lastKnownLocation: Location? = null

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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }

            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location: Location? = task.result
                if (location == null) {
                    nMap.isMyLocationEnabled = true
                } else {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }

            nMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Callback for the result from requesting permissions.
    // This method is invoked for every call on requestPermissions(android.app.Activity, String[],
    // int).
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getAllMarkers()
        call.enqueue(object : Callback<List<Event>> {
            override fun onResponse(call: Call<List<Event>>, response: Response<List<Event>>) {
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

                    val marker = nMap.addMarker(
                        MarkerOptions().position(LatLng(it.latitude, it.longitude))
                            .title(it.location)
                    )
                    marker!!.tag = item
                }

            }

            override fun onFailure(call: Call<List<Event>>, t: Throwable) {
                Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
            }
        })

        nMap.setOnInfoWindowClickListener { marker ->
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
                            nMap.moveCamera(
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
                        nMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 15
/*        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5*/
    }

}