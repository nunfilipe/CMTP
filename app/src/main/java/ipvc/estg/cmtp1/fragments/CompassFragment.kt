package ipvc.estg.cmtp1.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ipvc.estg.cmtp1.R
import kotlinx.android.synthetic.main.fragment_compass.*

class CompassFragment : Fragment() {

    private var compass: Compass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_compass, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_compass.setOnClickListener {
            activity!!.onBackPressed()
        }

        try {
            compass = Compass(context!!)
        } catch (e:IllegalStateException) {
            e.printStackTrace()
            Toast.makeText(context, "Either accelerometer or magnetic sensor not found" , Toast.LENGTH_LONG).show()
        }
        compass?.arrowView = view.findViewById(R.id.main_image_hands)

    }

    override fun onResume() {
        super.onResume()
        compass?.start()
    }

    override fun onPause() {
        super.onPause()
        compass?.stop()
    }

    companion object {
        private val TAG = "CompassActivity"
    }

}