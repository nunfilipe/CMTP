package ipvc.estg.cmtp1.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ipvc.estg.cmtp1.R
import ipvc.estg.cmtp1.fragments.LoginFragment
import ipvc.estg.cmtp1.fragments.MapFragment
import ipvc.estg.cmtp1.fragments.NoteFragment
import ipvc.estg.cmtp1.interfaces.NavigationHost

class MainActivity : AppCompatActivity(), NavigationHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, MapFragment())
                .commit()
        }
    }

    override fun navigateTo(
        fragment: androidx.fragment.app.Fragment,
        addToBackstack: Boolean,
        animate: Boolean
    ) {

        val transaction = supportFragmentManager
            .beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        if (animate) {
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        transaction.replace(R.id.container, fragment).commit()
    }

    override fun navigateToWithData(
        fragment: Fragment,
        addToBackstack: Boolean,
        animate: Boolean,
        tag: String,
        data: Bundle
    ) {
        val transaction = supportFragmentManager
            .beginTransaction()

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        fragment.arguments = data
        transaction.replace(R.id.container, fragment, tag).commit()
    }

    override fun logout() {
        MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.leave))
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                val remember = getSharedPreferences("REMEMBER", Context.MODE_PRIVATE)
                remember.edit().clear().apply()
                val authentication = getSharedPreferences("AUTHENTICATION", Context.MODE_PRIVATE)
                authentication.edit().clear().apply()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.container, MapFragment())
                    .commit()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, which -> }
            .show()
    }
}