package ipvc.estg.cmtp1.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import ipvc.estg.cmtp1.R
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
}