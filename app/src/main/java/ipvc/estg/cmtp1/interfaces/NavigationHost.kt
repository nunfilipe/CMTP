package ipvc.estg.cmtp1.interfaces

import androidx.fragment.app.Fragment

interface NavigationHost {

    fun navigateTo(fragment: Fragment, addToBackstack: Boolean, animate: Boolean)

}
