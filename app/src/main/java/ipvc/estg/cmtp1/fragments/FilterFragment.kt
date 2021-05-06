package ipvc.estg.cmtp1.fragments

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.api.Category
import ipvc.estg.cmtp1.api.Event
import kotlinx.android.synthetic.main.fragment_filter.view.*
import java.math.BigDecimal
import java.math.RoundingMode

class FilterFragment : BottomSheetDialogFragment() {

    var radius:String? = null
    var rgp: RadioGroup? = null
    var category: String? = null
    var noneChecked:Boolean? = true
    private val categoryList: MutableList<Category> = ArrayList<Category>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        private const val TAG = "Filter"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ;
        val arr = resources.getStringArray(R.array.category)

        categoryList.add(Category(id="1", arr[0].toString()))
        categoryList.add(Category(id="2", arr[1].toString()))
        categoryList.add(Category(id="3", arr[2].toString()))
        categoryList.add(Category(id="4", arr[3].toString()))

        val view = inflater.inflate(R.layout.fragment_filter, container, false)

        val preferences: SharedPreferences = context!!.getSharedPreferences("FILTERMAP", Context.MODE_PRIVATE)

        radius = preferences.getString("radius","0")

        rgp = view.findViewById(R.id.fancy_radio_group) as RadioGroup

        (activity as AppCompatActivity).setSupportActionBar(view.btn_close_filter)

        view.btn_close_filter.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        view.filter_btn.setOnClickListener {
            val preferences: SharedPreferences = context!!.getSharedPreferences("FILTERMAP", Context.MODE_PRIVATE)
            preferences.edit().putString("radius",radius).apply();
            activity?.onBackPressed()
        }


        val slider: Slider = view.findViewById(R.id.slider)

        slider.addOnSliderTouchListener(touchListener)
        slider.value = radius!!.toFloat()

        /**/


        categoryList.forEach{
            val preferences: SharedPreferences = context!!.getSharedPreferences("FILTERMAP", Context.MODE_PRIVATE)
            val rbn = CheckBox(context)
            rbn.background = context?.let { ContextCompat.getDrawable(it,R.drawable.radio_states_yellow) }
            rbn.setTextColor(ContextCompat.getColorStateList(context!!, R.color.black))
            rbn.buttonDrawable = null
            rbn.id = it.id.toInt()
            rbn.text = it.category
            rbn.setPadding(40,40,40,40)
            val params = RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(30, 0, 0, 30)
            rbn.layoutParams = params
            rbn.height = 140
            rgp!!.addView(rbn)
            rbn.setOnCheckedChangeListener { buttonView, isChecked ->

                preferences.edit().putBoolean(it.id,isChecked).apply();
            }
            rbn.isChecked = preferences.getBoolean(it.id,true)

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //markersDistance()
    }

   /* private fun markersDistance() {
        if (incident.latitude != null && incident.longitude != null) {
            if (radius != 0) {
                val locationA = Location("me")
                Log.e("Teste",location!!.latitude.toString())
                locationA.latitude = location!!.latitude
                locationA.longitude = location!!.longitude
                val locationB = Location("marker")

                locationB.latitude = incident.latitude.toDouble()
                locationB.longitude = incident.longitude.toDouble()

                val distance: Float = locationA.distanceTo(locationB)
                if (distance < radius) {
                    createMarker(
                        incident.latitude.toDouble(),
                        incident.longitude.toDouble(),
                        incident.title,
                        incident.description,
                        mMap,
                        icon,
                        incident
                    )
                }
            } else {
                createMarker(
                    incident.latitude.toDouble(),
                    incident.longitude.toDouble(),
                    incident.title,
                    incident.description,
                    mMap,
                    icon,
                    incident
                )
            }

        }
    }*/

    private val touchListener: Slider.OnSliderTouchListener = object :
        Slider.OnSliderTouchListener {
        override fun onStartTrackingTouch(slider: Slider) {
            //view?.price?.text = getString(R.string.price_range,BigDecimal(slider.values[0].toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','),BigDecimal(slider.values[1].toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','))
            radius = BigDecimal(slider.value.toString()).setScale(2, RoundingMode.HALF_EVEN).toString()
        }

        override fun onStopTrackingTouch(slider: Slider) {
           // view?.price?.text = getString(R.string.price_range,BigDecimal(slider.values[0].toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','),BigDecimal(slider.values[1].toString()).setScale(2, RoundingMode.HALF_EVEN).toString().replace('.', ','))
            radius = BigDecimal(slider.value.toString()).setScale(2, RoundingMode.HALF_EVEN).toString()
        }
    }


}