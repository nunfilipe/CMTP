package ipvc.estg.cmtp1.fragments

import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.api.EndPoints
import ipvc.estg.cmtp1.api.ServiceBuilder
import ipvc.estg.cmtp1.api.User
import ipvc.estg.cmtp1.interfaces.NavigationHost
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    var btnRegister: Button? = null
    var btnLogin: Button? = null
    var rememberMe: CheckBox? = null
    var me: String? = null
    var payload: String? = null
    var username: String? = null
    var password: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        declareItems(view)
        setOnClickListener(view)
        return view
    }

    private fun declareItems(view: View) {
        btnLogin = view.findViewById(R.id.btn_login)
        btnRegister = view.findViewById(R.id.btn_register)
        rememberMe = view.findViewById(R.id.remember_me)
        if (me != null) {
            view.username_edit_text.setText(me)
            rememberMe?.isChecked = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        close_login.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setOnClickListener(view: View?) {
        view!!.btn_login.setOnClickListener {
            btnLogin!!.isEnabled = false
            username = view.username_edit_text.text.toString()
            password = view.password_edit_text.text.toString()
            if (username!!.isEmpty()) {
                btnLogin!!.isEnabled = true
            } else if (password!!.isEmpty()) {
                btnLogin!!.isEnabled = true
            } else {
                val obj = JSONObject()
                obj.put("username", username)
                obj.put("password", password)
                payload = obj.toString()
                payload = Base64.encodeToString(
                    payload?.toByteArray(charset("UTF-8")), Base64.DEFAULT
                )
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.userLogin(payload = payload!!)
                call.enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            val token = "Bearer " + data.token
                            val user = User(
                                id = data.id,
                                name = data.name,
                                email = data.email,
                                token = token
                            )

                            if (rememberMe!!.isChecked) {
                                val sharedPref: SharedPreferences = context!!.getSharedPreferences(
                                    "REMEMBER",
                                    Context.MODE_PRIVATE
                                )
                                sharedPref.edit().putString("username", "response").apply()
                            } else {
                                val sharedPref: SharedPreferences = context!!.getSharedPreferences(
                                    "REMEMBER",
                                    Context.MODE_PRIVATE
                                )
                                sharedPref.edit().clear().apply()
                            }

                            val sharedPref: SharedPreferences = context!!.getSharedPreferences(
                                "AUTHENTICATION",
                                Context.MODE_PRIVATE
                            )
                            sharedPref.edit().putInt("id_user", user.id).apply()
                            sharedPref.edit().putString("token", user.token).apply()

                            (activity as NavigationHost).navigateTo(MapFragment(), false, false)
                            //requireActivity().supportFragmentManager.popBackStack()
                            Toast.makeText(activity, getString(R.string.welcom), Toast.LENGTH_LONG)
                                .show()
                        } else {
                            if (response.code() == 403 && response.message() == "login_fail") {
                                username_text_input.error = getString(R.string.user_info_wrong)
                                password_text_input.error = getString(R.string.user_info_wrong)
                                btnLogin!!.isEnabled = true
                            } else {
                                Toast.makeText(
                                    activity,
                                    getString(R.string.login_fail),
                                    Toast.LENGTH_LONG
                                ).show()
                                btnLogin!!.isEnabled = true
                            }
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        btnLogin!!.isEnabled = true
                        Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
                        Log.i("Failure", t.toString())
                    }
                })
            }
        }

    }
}
