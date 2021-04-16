package ipvc.estg.cmtp1.interfaces

import android.nfc.Tag
import android.os.Bundle
import androidx.fragment.app.Fragment

interface NavigationHost {

    fun navigateTo(fragment: Fragment, addToBackstack: Boolean, animate: Boolean)
    fun navigateToWithData(fragment: Fragment, addToBackstack: Boolean, animate: Boolean,tag: String, data: Bundle)

}
